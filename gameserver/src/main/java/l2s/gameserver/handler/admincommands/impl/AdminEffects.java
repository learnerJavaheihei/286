package l2s.gameserver.handler.admincommands.impl;

import java.util.List;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.World;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.EarthQuakePacket;
import l2s.gameserver.network.l2.s2c.ExShowUsmPacket;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.network.l2.s2c.SocialActionPacket;
import l2s.gameserver.network.l2.s2c.TutorialShowHtmlPacket;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.utils.ChatUtils;
import l2s.gameserver.utils.Util;

public class AdminEffects implements IAdminCommandHandler
{
	private static enum Commands
	{
		admin_invis,
		admin_vis,
		admin_offline_vis,
		admin_offline_invis,
		admin_earthquake,
		admin_block,
		admin_unblock,
		admin_changename,
		admin_gmspeed,
		admin_invul,
		admin_setinvul,
		admin_getinvul,
		admin_social,
		admin_abnormal,
		admin_showmovie,
		admin_showusm,
		admin_showchtml,
		admin_effect
	}

	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().GodMode)
			return false;

		int val;
		AbnormalEffect ae = AbnormalEffect.NONE;
		GameObject target = activeChar.getTarget();

		switch(command)
		{
			case admin_invis:
			case admin_vis:
				if(activeChar.isGMInvisible())
				{
					activeChar.setGMInvisible(false);
					activeChar.stopAbnormalEffect(AbnormalEffect.STEALTH);
					activeChar.sendUserInfo(true);

					List<Player> players = World.getAroundObservers(activeChar);
					for(Player p : players)
					{
						if(activeChar.isVisible() && !activeChar.isInvisible(p))
							p.sendPacket(p.addVisibleObject(activeChar, null));
					}

					for(Servitor servitor : activeChar.getServitors())
					{
						servitor.stopAbnormalEffect(AbnormalEffect.STEALTH);
						for(Player p : players)
						{
							if(servitor.isVisible() && !servitor.isInvisible(p))
								p.sendPacket(p.addVisibleObject(servitor, null));
						}
					}
				}
				else
				{
					activeChar.setGMInvisible(true);
					activeChar.startAbnormalEffect(AbnormalEffect.STEALTH);
					activeChar.sendUserInfo(true);

					List<Player> players = World.getAroundObservers(activeChar);
					for(Player p : players)
					{
						if(activeChar.isInvisible(p))
							p.sendPacket(p.removeVisibleObject(activeChar, null));
					}

					for(Servitor servitor : activeChar.getServitors())
					{
						servitor.startAbnormalEffect(AbnormalEffect.STEALTH);
						for(Player p : players)
						{
							if(servitor.isInvisible(p))
								p.sendPacket(p.removeVisibleObject(servitor, null));
						}
					}
				}
				break;
			case admin_gmspeed:
				if(wordList.length < 2)
					val = 0;
				else
				{
					try
					{
						val = Integer.parseInt(wordList[1]);
					}
					catch(Exception e)
					{
						ChatUtils.sys(activeChar, "//gmspeed [0...10]");
						return false;
					}
				}

				if(val < 0 || val > 10)
				{
					// Custom limit according to SDW's request - real retail limit is unknown.
					ChatUtils.sys(activeChar, "//gmspeed [0...10]");
					return false;
				}

				final Creature targetCreature;
				if(target != null && target.isCreature())
					targetCreature = (Creature) target;
				else
				{
					// If there is no target, let's use the command executer.
					targetCreature = activeChar;
				}

				if(val == 0)
				{
					if(targetCreature.isPlayer())
						targetCreature.getPlayer().unsetVar("gm_gmspeed");
				}
				else
				{
					if(targetCreature.isPlayer() && targetCreature.getPlayer().isGM() && Config.SAVE_GM_EFFECTS)
						targetCreature.getPlayer().setVar("gm_gmspeed", String.valueOf(val), -1);
				}
				targetCreature.setGmSpeed(val);
				targetCreature.updateStats();

				ChatUtils.sys(activeChar, "[" + targetCreature.getName() + "] speed is [" + (val * 100) + "0]% fast.");
				break;
			case admin_invul:
				handleInvul(activeChar, activeChar);
				if(activeChar.isGM() && activeChar.isInvulnerable())
				{
					if(Config.SAVE_GM_EFFECTS)
						activeChar.setVar("gm_invul", "true", -1);
				}
				else
					activeChar.unsetVar("gm_invul");
				break;
		}

		if(!activeChar.isGM())
			return false;

		switch(command)
		{
			case admin_offline_vis:
				for(Player player : GameObjectsStorage.getOfflinePlayers())
				{
					player.getFlags().getInvisible().stop();
					player.decayMe();
					player.spawnMe();
				}
				break;
			case admin_offline_invis:
				for(Player player : GameObjectsStorage.getOfflinePlayers())
				{
					player.getFlags().getInvisible().start();
					player.decayMe();
				}
				break;
			case admin_earthquake:
				try
				{
					int intensity = Integer.parseInt(wordList[1]);
					int duration = Integer.parseInt(wordList[2]);
					activeChar.broadcastPacket(new EarthQuakePacket(activeChar.getLoc(), intensity, duration));
				}
				catch(Exception e)
				{
					activeChar.sendMessage("USAGE: //earthquake intensity duration");
					return false;
				}
				break;
			case admin_block:
				if(target == null || !target.isCreature())
				{
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
					return false;
				}
				if(((Creature) target).isBlocked())
					return false;
				((Creature) target).abortAttack(true, false);
				((Creature) target).abortCast(true, false);
				((Creature) target).block();
				activeChar.sendMessage("Target blocked.");
				break;
			case admin_unblock:
				if(target == null || !target.isCreature())
				{
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
					return false;
				}
				if(!((Creature) target).isBlocked())
					return false;
				((Creature) target).unblock();
				activeChar.sendMessage("Target unblocked.");
				break;
			case admin_changename:
				if(wordList.length < 2)
				{
					activeChar.sendMessage("USAGE: //changename newName");
					return false;
				}
				if(target == null)
					target = activeChar;
				if(!target.isCreature())
				{
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
					return false;
				}
				String oldName = ((Creature) target).getName();
				String newName = Util.joinStrings(" ", wordList, 1);

				((Creature) target).setName(newName);
				((Creature) target).broadcastCharInfo();

				activeChar.sendMessage("Changed name from " + oldName + " to " + newName + ".");
				break;
			case admin_setinvul:
				if(target == null || !target.isPlayer())
				{
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
					return false;
				}
				handleInvul(activeChar, (Player) target);
				break;
			case admin_getinvul:
				if(target != null && target.isCreature())
					activeChar.sendMessage("Target " + target.getName() + "(object ID: " + target.getObjectId() + ") is " + (!((Creature) target).isInvulnerable() ? "NOT " : "") + "invul");
				break;
			case admin_social:
				if(wordList.length < 2)
					val = Rnd.get(1, 7);
				else
					try
					{
						val = Integer.parseInt(wordList[1]);
					}
					catch(NumberFormatException nfe)
					{
						activeChar.sendMessage("USAGE: //social value");
						return false;
					}
				if(target == null || target == activeChar)
					activeChar.broadcastPacket(new SocialActionPacket(activeChar.getObjectId(), val));
				else if(target.isCreature())
					((Creature) target).broadcastPacket(new SocialActionPacket(target.getObjectId(), val));
				break;
			case admin_abnormal:
				try
				{
					if(wordList.length > 1)
						ae = AbnormalEffect.valueOf(Integer.parseInt(wordList[1]));
				}
				catch(Exception e)
				{
					activeChar.sendMessage("USAGE: //abnormal id");
					activeChar.sendMessage("//abnormal - Clears all abnormal effects");
					return false;
				}

				Creature effectTarget = target == null ? activeChar : (Creature) target;

				if(ae == null || ae == AbnormalEffect.NONE)
				{
					effectTarget.stopAllAbnormalEffects();
					effectTarget.sendMessage("Abnormal effects clearned by admin.");
					if(effectTarget != activeChar)
						effectTarget.sendMessage("Abnormal effects clearned.");
				}
				else
				{
					effectTarget.startAbnormalEffect(ae);
					effectTarget.sendMessage("Admin added abnormal effect: " + ae.getName());
					if(effectTarget != activeChar)
						effectTarget.sendMessage("Added abnormal effect: " + ae.getName());
				}
				break;
			case admin_showmovie:
				if(wordList.length < 2)
				{
					activeChar.sendMessage("USAGE: //showmovie id");
					return false;
				}
				int id;
				try
				{
					id = Integer.parseInt(wordList[1]);
				}
				catch(NumberFormatException e)
				{
					activeChar.sendMessage("You must specify id");
					return false;
				}
				activeChar.startScenePlayer(id);
				break;
			case admin_showusm:
				if(wordList.length < 2)
				{
					activeChar.sendMessage("USAGE: //showusm id");
					return false;
				}
				int usmId;
				try
				{
					usmId = Integer.parseInt(wordList[1]);
				}
				catch(NumberFormatException e)
				{
					activeChar.sendMessage("You must specify id");
					return false;
				}
				activeChar.sendPacket(new ExShowUsmPacket(usmId));
				break;
			case admin_showchtml:
				if(wordList.length < 2)
				{
					activeChar.sendMessage("USAGE: //showhtml file_name");
					return false;
				}
				String fileName = fullString.replace(wordList[0] + " ", "");
				activeChar.sendPacket(new TutorialShowHtmlPacket(TutorialShowHtmlPacket.LARGE_WINDOW, "..\\L2text\\" + fileName + ".htm"));
				break;
			case admin_effect:
				int level = 1;
				int hitTime = 1000;
				if (wordList.length < 2)
				{
					activeChar.sendMessage("USAGE: //effect skill_id [level|hitTime]");
					return false;
				}
				final int skillId = Integer.parseInt(wordList[1]);
				if (wordList.length > 2)
				{
					level = Integer.parseInt(wordList[2]);
				}
				if (wordList.length > 3)
				{
					hitTime = Integer.parseInt(wordList[3]);
				}
				Creature effected = activeChar.getTarget() == null ? activeChar : (Creature) target;
				effected.broadcastPacket(new MagicSkillUse(effected, activeChar, skillId, level, hitTime, 0));
				break;
		}

		return true;
	}

	private void handleInvul(Player activeChar, Player target)
	{
		if(target.isInvulnerable())
		{
			target.getFlags().getInvulnerable().stop();
			target.getFlags().getDebuffImmunity().stop();
			target.stopAbnormalEffect(AbnormalEffect.INVINCIBILITY);
			for(Servitor servitor : target.getServitors())
			{
				servitor.getFlags().getInvulnerable().stop();
				servitor.getFlags().getDebuffImmunity().stop();
				servitor.stopAbnormalEffect(AbnormalEffect.INVINCIBILITY);
			}
			activeChar.sendMessage(target.getName() + " is now mortal.");
		}
		else
		{
			target.getFlags().getInvulnerable().start();
			target.getFlags().getDebuffImmunity().start();
			target.startAbnormalEffect(AbnormalEffect.INVINCIBILITY);
			for(Servitor servitor : target.getServitors())
			{
				servitor.getFlags().getInvulnerable().start();
				servitor.getFlags().getDebuffImmunity().start();
				servitor.startAbnormalEffect(AbnormalEffect.INVINCIBILITY);
			}
			activeChar.sendMessage(target.getName() + " is now immortal.");
		}
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}