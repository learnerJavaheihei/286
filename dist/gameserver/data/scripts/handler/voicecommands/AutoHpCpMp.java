package handler.voicecommands;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledFuture;

import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.listener.actor.OnChangeCurrentCpListener;
import l2s.gameserver.listener.actor.OnChangeCurrentHpListener;
import l2s.gameserver.listener.actor.OnChangeCurrentMpListener;
import l2s.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.AbnormalList;
import l2s.gameserver.model.actor.listener.CharListenerList;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.skills.EffectUseType;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.templates.skill.EffectTemplate;
import l2s.gameserver.utils.ItemFunctions;

/**
 * @author Bonux
**/
public class AutoHpCpMp extends ScriptVoiceCommandHandler
{
	private static class AutoHpTask implements Runnable {
		@Override
		public void run() {
			for(Player player : HP_PLAYERS) {
				tryUseHpPotion(player);
			}
		}
	}

	private static class AutoMpTask implements Runnable {
		@Override
		public void run() {
			for(Player player : MP_PLAYERS) {
				tryUseMpPotion(player);
			}
		}
	}

	private static class AutoCpTask implements Runnable {
		@Override
		public void run() {
			for(Player player : CP_PLAYERS) {
				tryUseCpPotion(player);
			}
		}
	}

	private static class ChangeCurrentCpListener implements OnChangeCurrentCpListener
	{
		@Override
		public void onChangeCurrentCp(Creature actor, double oldCp, double newCp)
		{
			if(!actor.isPlayer() || actor.isDead())
				return;

			Player player = actor.getPlayer();
			if(canUseCpPotion(player))
				CP_PLAYERS.add(player);
			else
				CP_PLAYERS.remove(player);
		}
	}

	private static boolean canUseCpPotion(Player player) {
		if(!player.getVarBoolean("acp_enabled", true))
			return false;

		int percent = player.getVarInt("autocp", 0);
		double newCp = player.getCurrentCp();
		double currentPercent = newCp / (player.getMaxCp() / 100.);
		if(percent <= 0 || currentPercent < 0 || currentPercent > percent)
			return false;
		return true;
	}

	private static void tryUseCpPotion(Player player) {
		if(!canUseCpPotion(player))
			return;

		ItemInstance effectedItem = null;
		int effectedItemPower = 0;

		ItemInstance instantItem = null;
		int instantItemPower = 0;

		final Collection<Abnormal> abnormals = player.getAbnormalList().values();
		loop: for(ItemInstance item : player.getInventory().getItems())
		{
			SkillEntry skillEntry = item.getTemplate().getFirstSkill();
			if(skillEntry == null)
				continue;

			Skill skill = skillEntry.getTemplate();
			for(EffectTemplate et : skill.getEffectTemplates(EffectUseType.NORMAL))
			{
				if(et.getHandler().getName().equalsIgnoreCase("RestoreCP"))
				{
					for(Abnormal abnormal : abnormals)
					{
						if(AbnormalList.checkAbnormalType(skill, abnormal.getSkill()) && skill.getAbnormalLvl() <= abnormal.getAbnormalLvl())
						{
							// Не хиляем, если уже наложена какая-либо хилка.
							effectedItem = null;
							effectedItemPower = 0;
							continue loop;
						}
					}

					if(!ItemFunctions.checkForceUseItem(player, item, false) || !ItemFunctions.checkUseItem(player, item, false))
						continue loop;

					int power = (int) et.getValue();
					if(power > effectedItemPower)
					{
						if(skillEntry.checkCondition(player, player, false, false, true, false, false))
						{
							effectedItem = item;
							effectedItemPower = power;
							continue loop;
						}
					}
				}
			}
		}

		loop: for(ItemInstance item : player.getInventory().getItems())
		{
			SkillEntry skillEntry = item.getTemplate().getFirstSkill();
			if(skillEntry == null)
				continue;

			if(!ItemFunctions.checkForceUseItem(player, item, false) || !ItemFunctions.checkUseItem(player, item, false))
				continue;

			Skill skill = skillEntry.getTemplate();
			for(EffectTemplate et : skill.getEffectTemplates(EffectUseType.NORMAL_INSTANT))
			{
				if(et.getHandler().getName().equalsIgnoreCase("RestoreCP"))
				{
					int power = (int) et.getValue();
					if(et.getParams().getBool("percent", false))
						power = power * (int) (player.getMaxCp() / 100.);
					if(power > instantItemPower)
					{
						if(skillEntry.checkCondition(player, player, false, false, true, false, false))
						{
							instantItem = item;
							instantItemPower = power;
							continue loop;
						}
					}
				}
			}
		}

		if(instantItem != null)
			player.useItem(instantItem, false, false);

		if(effectedItem != null)
		{
			int percent = player.getVarInt("autocp", 0);
			if(instantItemPower == 0 || percent >= (player.getCurrentCp() + instantItemPower) / (player.getMaxCp() / 100.))
				player.useItem(effectedItem, false, false);
		}
	}

	private static class ChangeCurrentHpListener implements OnChangeCurrentHpListener
	{
		@Override
		public void onChangeCurrentHp(Creature actor, double oldHp, double newHp)
		{
			if(!actor.isPlayer() || actor.isDead())
				return;

			Player player = actor.getPlayer();
			if(canUseHpPotion(player))
				HP_PLAYERS.add(player);
			else
				HP_PLAYERS.remove(player);
		}
	}

	private static boolean canUseHpPotion(Player player) {
		if(!player.getVarBoolean("acp_enabled", true))
			return false;

		int percent = player.getVarInt("autohp", 0);
		double newHp = player.getCurrentHp();
		double currentPercent = newHp / (player.getMaxHp() / 100.);
		if(percent <= 0 || currentPercent <= 0 || currentPercent > percent)
			return false;
		return true;
	}

	private static void tryUseHpPotion(Player player) {
		if(!canUseHpPotion(player))
			return;

		ItemInstance effectedItem = null;
		int effectedItemPower = 0;

		ItemInstance instantItem = null;
		int instantItemPower = 0;

		final Collection<Abnormal> abnormals = player.getAbnormalList().values();
		loop: for(ItemInstance item : player.getInventory().getItems())
		{
			SkillEntry skillEntry = item.getTemplate().getFirstSkill();
			if(skillEntry == null)
				continue;

			Skill skill = skillEntry.getTemplate();
			for(EffectTemplate et : skill.getEffectTemplates(EffectUseType.NORMAL))
			{
				if(et.getHandler().getName().equalsIgnoreCase("RestoreHP"))
				{
					for(Abnormal abnormal : abnormals)
					{
						if(AbnormalList.checkAbnormalType(skill, abnormal.getSkill()) && skill.getAbnormalLvl() <= abnormal.getAbnormalLvl())
						{
							// Не хиляем, если уже наложена какая-либо хилка.
							effectedItem = null;
							effectedItemPower = 0;
							continue loop;
						}
					}

					if(!ItemFunctions.checkForceUseItem(player, item, false) || !ItemFunctions.checkUseItem(player, item, false))
						continue loop;

					int power = (int) et.getValue();
					if(power > effectedItemPower)
					{
						if(skillEntry.checkCondition(player, player, false, false, true, false, false))
						{
							effectedItem = item;
							effectedItemPower = power;
							continue loop;
						}
					}
				}
			}
		}

		loop: for(ItemInstance item : player.getInventory().getItems())
		{
			SkillEntry skillEntry = item.getTemplate().getFirstSkill();
			if(skillEntry == null)
				continue;

			if(!ItemFunctions.checkForceUseItem(player, item, false) || !ItemFunctions.checkUseItem(player, item, false))
				continue;

			Skill skill = skillEntry.getTemplate();
			for(EffectTemplate et : skill.getEffectTemplates(EffectUseType.NORMAL_INSTANT))
			{
				if(et.getHandler().getName().equalsIgnoreCase("RestoreHP"))
				{
					int power = (int) et.getValue();
					if(et.getParams().getBool("percent", false))
						power = power * (int) (player.getMaxHp() / 100.);
					if(power > instantItemPower)
					{
						if(skillEntry.checkCondition(player, player, false, false, true, false, false))
						{
							instantItem = item;
							instantItemPower = power;
							continue loop;
						}
					}
				}
			}
		}

		if(instantItem != null)
			player.useItem(instantItem, false, false);

		if(effectedItem != null)
		{
			int percent = player.getVarInt("autohp", 0);
			if(instantItemPower == 0 || percent >= (player.getCurrentHp() + instantItemPower) / (player.getMaxHp() / 100.))
				player.useItem(effectedItem, false, false);
		}
	}

	private static class ChangeCurrentMpListener implements OnChangeCurrentMpListener {
		@Override
		public void onChangeCurrentMp(Creature actor, double oldMp, double newMp) {
			if(!actor.isPlayer() || actor.isDead())
				return;

			Player player = actor.getPlayer();
			if(canUseMpPotion(player))
				MP_PLAYERS.add(player);
			else
				MP_PLAYERS.remove(player);
		}
	}

	private static boolean canUseMpPotion(Player player) {
		if(!player.getVarBoolean("acp_enabled", true))
			return false;

		int percent = player.getVarInt("automp", 0);
		double newMp = player.getCurrentMp();
		double currentPercent = newMp / (player.getMaxMp() / 100.);
		if(percent <= 0 || currentPercent < 0 || currentPercent > percent)
			return false;
		return true;
	}

	private static void tryUseMpPotion(Player player) {
		if(!canUseMpPotion(player))
			return;

		ItemInstance effectedItem = null;
		int effectedItemPower = 0;

		ItemInstance instantItem = null;
		int instantItemPower = 0;

		final Collection<Abnormal> abnormals = player.getAbnormalList().values();
		loop: for(ItemInstance item : player.getInventory().getItems())
		{
			SkillEntry skillEntry = item.getTemplate().getFirstSkill();
			if(skillEntry == null)
				continue;

			Skill skill = skillEntry.getTemplate();
			for(EffectTemplate et : skill.getEffectTemplates(EffectUseType.NORMAL))
			{
				if(et.getHandler().getName().equalsIgnoreCase("RestoreMP"))
				{
					for(Abnormal abnormal : abnormals)
					{
						if(AbnormalList.checkAbnormalType(skill, abnormal.getSkill()) && skill.getAbnormalLvl() <= abnormal.getAbnormalLvl())
						{
							// Не хиляем, если уже наложена какая-либо хилка.
							effectedItem = null;
							effectedItemPower = 0;
							continue loop;
						}
					}

					if(!ItemFunctions.checkForceUseItem(player, item, false) || !ItemFunctions.checkUseItem(player, item, false))
						continue loop;

					int power = (int) et.getValue();
					if(power > effectedItemPower)
					{
						if(skillEntry.checkCondition(player, player, false, false, true, false, false))
						{
							effectedItem = item;
							effectedItemPower = power;
							continue loop;
						}
					}
				}
			}
		}

		loop: for(ItemInstance item : player.getInventory().getItems())
		{
			SkillEntry skillEntry = item.getTemplate().getFirstSkill();
			if(skillEntry == null)
				continue;

			if(!ItemFunctions.checkForceUseItem(player, item, false) || !ItemFunctions.checkUseItem(player, item, false))
				continue;

			Skill skill = skillEntry.getTemplate();
			for(EffectTemplate et : skill.getEffectTemplates(EffectUseType.NORMAL_INSTANT))
			{
				if(et.getHandler().getName().equalsIgnoreCase("RestoreMP"))
				{
					int power = (int) et.getValue();
					if(et.getParams().getBool("percent", false))
						power = power * (int) (player.getMaxMp() / 100.);
					if(power > instantItemPower)
					{
						if(skillEntry.checkCondition(player, player, false, false, true, false, false))
						{
							instantItem = item;
							instantItemPower = power;
							continue loop;
						}
					}
				}
			}
		}

		if(instantItem != null)
			player.useItem(instantItem, false, false);

		if(effectedItem != null)
		{
			int percent = player.getVarInt("automp", 0);
			if(instantItemPower == 0 || percent >= (player.getCurrentMp() + instantItemPower) / (player.getMaxMp() / 100.))
				player.useItem(effectedItem, false, false);
		}
	}

	private static class PlayerEnterListener implements OnPlayerEnterListener
	{
		public void onPlayerEnter(Player player)
		{
			if(!Config.ALLOW_AUTOHEAL_COMMANDS)
				return;

			int percent = player.getVarInt("autocp", 0);
			if(percent > 0)
			{
				player.addListener(CHANGE_CURRENT_CP_LISTENER);
				if(player.isLangRus())
					player.sendMessage("Вы используете систему автоматического восстановления CP. Ваше CP будет автоматически восстанавливаться при значении " + percent + "% и меньше.");
				else
					player.sendMessage("You are using an automatic CP recovery. Your CP will automatically recover at a value of " + percent + "% or less.");
			}
			percent = player.getVarInt("autohp", 0);
			if(percent > 0)
			{
				player.addListener(CHANGE_CURRENT_HP_LISTENER);
				if(player.isLangRus())
					player.sendMessage("Вы используете систему автоматического восстановления HP. Ваше HP будет автоматически восстанавливаться при значении " + percent + "% и меньше.");
				else
					player.sendMessage("You are using an automatic HP recovery. Your HP will automatically recover at a value of " + percent + "% or less.");
			}
			percent = player.getVarInt("automp", 0);
			if(percent > 0)
			{
				player.addListener(CHANGE_CURRENT_MP_LISTENER);
				if(player.isLangRus())
					player.sendMessage("Вы используете систему автоматического восстановления MP. Ваше MP будет автоматически восстанавливаться при значении " + percent + "% и меньше.");
				else
					player.sendMessage("You are using an automatic MP recovery. Your MP will automatically recover at a value of " + percent + "% or less.");
			}
		}
	}

	private static final OnChangeCurrentCpListener CHANGE_CURRENT_CP_LISTENER = new ChangeCurrentCpListener();
	private static final OnChangeCurrentHpListener CHANGE_CURRENT_HP_LISTENER = new ChangeCurrentHpListener();
	private static final OnChangeCurrentMpListener CHANGE_CURRENT_MP_LISTENER = new ChangeCurrentMpListener();
	private static final OnPlayerEnterListener PLAYER_ENTER_LISTENER = new PlayerEnterListener();

	private static final String[] COMMANDS = new String[] { "acp", "autocp", "autohp", "automp" };

	private static final Set<Player> HP_PLAYERS = new CopyOnWriteArraySet<>();
	private static final Set<Player> MP_PLAYERS = new CopyOnWriteArraySet<>();
	private static final Set<Player> CP_PLAYERS = new CopyOnWriteArraySet<>();

	private static ScheduledFuture<?> AUTO_HP_TASK = null;
	private static ScheduledFuture<?> AUTO_MP_TASK = null;
	private static ScheduledFuture<?> AUTO_CP_TASK = null;

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		if(!Config.ALLOW_AUTOHEAL_COMMANDS)
			return false;

		if(command.equalsIgnoreCase("acp"))
		{
			boolean enabled = activeChar.getVarBoolean("acp_enabled", true);
			int autoHp = activeChar.getVarInt("autohp", 0);
			int autoMp = activeChar.getVarInt("automp", 0);
			int autoCp = activeChar.getVarInt("autocp", 0);
			try
			{
				String[] params = args.split("\\s+");
				if(params[0].equalsIgnoreCase("enable"))
				{
					if(!enabled)
					{
						enabled = true;
						activeChar.unsetVar("acp_enabled");
						if(activeChar.isLangRus())
							activeChar.sendMessage("Система автоматического восстановления активирована.");
						else
							activeChar.sendMessage("The automatic recovery system is activated.");
					}
				}
				else if(params[0].equalsIgnoreCase("disable"))
				{
					if(enabled)
					{
						enabled = false;
						activeChar.setVar("acp_enabled", false);
						if(activeChar.isLangRus())
							activeChar.sendMessage("Система автоматического восстановления деактивирована.");
						else
							activeChar.sendMessage("The automatic recovery system is deactivated.");
					}
				}
				else if(params[0].equalsIgnoreCase("hp"))
				{
					int newAutoHp = Math.min(99, Integer.parseInt(params[1]));
					if(newAutoHp != autoHp)
					{
						if(newAutoHp > 0)
						{
							activeChar.setVar("autohp", newAutoHp, -1);
							if(autoHp > 0)
							{
								if(activeChar.isLangRus())
									activeChar.sendMessage("Ваше HP будет автоматически восстанавливаться при значении " + newAutoHp + "% и меньше.");
								else
									activeChar.sendMessage("Your HP will automatically recover at a value of " + newAutoHp + "% or less.");
							}
							else
							{
								activeChar.addListener(CHANGE_CURRENT_HP_LISTENER);
								if(activeChar.isLangRus())
									activeChar.sendMessage("Вы включили систему автоматического восстановления HP. Ваше HP будет автоматически восстанавливаться при значении " + newAutoHp + "% и меньше.");
								else
									activeChar.sendMessage("You have enabled an automatic HP recovery. Your HP will automatically recover at a value of " + newAutoHp + "% or less.");
							}
						}
						else
						{
							activeChar.unsetVar("autohp");
							activeChar.removeListener(CHANGE_CURRENT_HP_LISTENER);
							if(activeChar.isLangRus())
								activeChar.sendMessage("Система автоматического восстановления HP отключена.");
							else
								activeChar.sendMessage("HP automatic recovery system disabled.");
						}
						autoHp = newAutoHp;
					}
				}
				else if(params[0].equalsIgnoreCase("mp"))
				{
					int newAutoMp = Math.min(99, Integer.parseInt(params[1]));
					if(newAutoMp != autoMp)
					{
						if(newAutoMp > 0)
						{
							activeChar.setVar("automp", newAutoMp, -1);
							if(autoMp > 0)
							{
								if(activeChar.isLangRus())
									activeChar.sendMessage("Ваше MP будет автоматически восстанавливаться при значении " + newAutoMp + "% и меньше.");
								else
									activeChar.sendMessage("Your MP will automatically recover at a value of " + newAutoMp + "% or less.");
							}
							else
							{
								activeChar.addListener(CHANGE_CURRENT_MP_LISTENER);
								if(activeChar.isLangRus())
									activeChar.sendMessage("Вы включили систему автоматического восстановления MP. Ваше MP будет автоматически восстанавливаться при значении " + newAutoMp + "% и меньше.");
								else
									activeChar.sendMessage("You have enabled an automatic MP recovery. Your MP will automatically recover at a value of " + newAutoMp + "% or less.");
							}
						}
						else
						{
							activeChar.unsetVar("automp");
							activeChar.removeListener(CHANGE_CURRENT_MP_LISTENER);
							if(activeChar.isLangRus())
								activeChar.sendMessage("Система автоматического восстановления MP отключена.");
							else
								activeChar.sendMessage("MP automatic recovery system disabled.");
						}
						autoMp = newAutoMp;
					}
				}
				else if(params[0].equalsIgnoreCase("cp"))
				{
					int newAutoCp = Math.min(99, Integer.parseInt(params[1]));
					if(newAutoCp != autoCp)
					{
						if(newAutoCp > 0)
						{
							activeChar.setVar("autocp", newAutoCp, -1);
							if(autoCp > 0)
							{
								if(activeChar.isLangRus())
									activeChar.sendMessage("Ваше CP будет автоматически восстанавливаться при значении " + newAutoCp + "% и меньше.");
								else
									activeChar.sendMessage("Your CP will automatically recover at a value of " + newAutoCp + "% or less.");
							}
							else
							{
								activeChar.addListener(CHANGE_CURRENT_CP_LISTENER);
								if(activeChar.isLangRus())
									activeChar.sendMessage("Вы включили систему автоматического восстановления CP. Ваше CP будет автоматически восстанавливаться при значении " + newAutoCp + "% и меньше.");
								else
									activeChar.sendMessage("You have enabled an automatic CP recovery. Your CP will automatically recover at a value of " + newAutoCp + "% or less.");
							}
						}
						else
						{
							activeChar.unsetVar("autocp");
							activeChar.removeListener(CHANGE_CURRENT_CP_LISTENER);
							if(activeChar.isLangRus())
								activeChar.sendMessage("Система автоматического восстановления CP отключена.");
							else
								activeChar.sendMessage("CP automatic recovery system disabled.");
						}
						autoCp = newAutoCp;
					}
				}
			}
			catch(Exception e)
			{
				//
			}

			HtmlMessage htmlMsg = new HtmlMessage(0);
			htmlMsg.setFile("command/acp.htm");
			htmlMsg.addVar("acp_enabled", enabled);
			htmlMsg.addVar("auto_hp_percent", autoHp);
			htmlMsg.addVar("auto_mp_percent", autoMp);
			htmlMsg.addVar("auto_cp_percent", autoCp);
			activeChar.sendPacket(htmlMsg);
		}
		else if(command.equalsIgnoreCase("autocp"))
		{
			int percent;
			try
			{
				percent = Math.min(99, Integer.parseInt(args));
			}
			catch(NumberFormatException e)
			{
				if(activeChar.isLangRus())
					activeChar.sendMessage("Неверное использование комманды! Используйте: .autocp [ПРОЦЕНТ_CP_ДЛЯ_НАЧАЛА_ВОССТАНОВЛЕНИЯ]");
				else
					activeChar.sendMessage("Incorrect use commands! Use: .autocp [CP_PERCENT_FOR EARLY_RECOVERY]");
				return false;
			}
			if(percent <= 0)
			{
				if(activeChar.getVarInt("autocp", 0) > 0)
				{
					activeChar.removeListener(CHANGE_CURRENT_CP_LISTENER);
					activeChar.unsetVar("autocp");
					if(activeChar.isLangRus())
						activeChar.sendMessage("Система автоматического восстановления CP отключена.");
					else
						activeChar.sendMessage("CP automatic recovery system disabled.");
				}
				else
				{
					if(activeChar.isLangRus())
						activeChar.sendMessage("Нельзя указать нулевое или отрицательное значение!");
					else
						activeChar.sendMessage("You can not specify zero or negative value!");
				}
				return false;
			}
			activeChar.addListener(CHANGE_CURRENT_CP_LISTENER);
			activeChar.setVar("autocp", percent, -1);
			if(activeChar.isLangRus())
				activeChar.sendMessage("Вы включили систему автоматического восстановления CP. Ваше CP будет автоматически восстанавливаться при значении " + percent + "% и меньше.");
			else
				activeChar.sendMessage("You have enabled an automatic CP recovery. Your CP will automatically recover at a value of " + percent + "% or less.");
			return true;
		}
		else if(command.equalsIgnoreCase("autohp"))
		{
			int percent;
			try
			{
				percent = Math.min(99, Integer.parseInt(args));
			}
			catch(NumberFormatException e)
			{
				if(activeChar.isLangRus())
					activeChar.sendMessage("Неверное использование комманды! Используйте: .autohp [ПРОЦЕНТ_HP_ДЛЯ_НАЧАЛА_ВОССТАНОВЛЕНИЯ]");
				else
					activeChar.sendMessage("Incorrect use commands! Use: .autohp [HP_PERCENT_FOR EARLY_RECOVERY]");
				return false;
			}
			if(percent <= 0)
			{
				if(activeChar.getVarInt("autohp", 0) > 0)
				{
					activeChar.removeListener(CHANGE_CURRENT_HP_LISTENER);
					activeChar.unsetVar("autohp");
					if(activeChar.isLangRus())
						activeChar.sendMessage("Система автоматического восстановления HP отключена.");
					else
						activeChar.sendMessage("HP automatic recovery system disabled.");
				}
				else
				{
					if(activeChar.isLangRus())
						activeChar.sendMessage("Нельзя указать нулевое или отрицательное значение!");
					else
						activeChar.sendMessage("You can not specify zero or negative value!");
				}
				return false;
			}
			activeChar.addListener(CHANGE_CURRENT_HP_LISTENER);
			activeChar.setVar("autohp", percent, -1);
			if(activeChar.isLangRus())
				activeChar.sendMessage("Вы включили систему автоматического восстановления HP. Ваше HP будет автоматически восстанавливаться при значении " + percent + "% и меньше.");
			else
				activeChar.sendMessage("You have enabled an automatic HP recovery. Your HP will automatically recover at a value of " + percent + "% or less.");
			return true;
		}
		else if(command.equalsIgnoreCase("automp"))
		{
			int percent;
			try
			{
				percent = Math.min(99, Integer.parseInt(args));
			}
			catch(NumberFormatException e)
			{
				if(activeChar.isLangRus())
					activeChar.sendMessage("Неверное использование комманды! Используйте: .automp [ПРОЦЕНТ_MP_ДЛЯ_НАЧАЛА_ВОССТАНОВЛЕНИЯ]");
				else
					activeChar.sendMessage("Incorrect use commands! Use: .automp [MP_PERCENT_FOR EARLY_RECOVERY]");
				return false;
			}
			if(percent <= 0)
			{
				if(activeChar.getVarInt("automp", 0) > 0)
				{
					activeChar.removeListener(CHANGE_CURRENT_MP_LISTENER);
					activeChar.unsetVar("automp");
					if(activeChar.isLangRus())
						activeChar.sendMessage("Система автоматического восстановления MP отключена.");
					else
						activeChar.sendMessage("MP automatic recovery system disabled.");
				}
				else
				{
					if(activeChar.isLangRus())
						activeChar.sendMessage("Нельзя указать нулевое или отрицательное значение!");
					else
						activeChar.sendMessage("You can not specify zero or negative value!");
					return false;
				}
			}
			activeChar.addListener(CHANGE_CURRENT_MP_LISTENER);
			activeChar.setVar("automp", percent, -1);
			if(activeChar.isLangRus())
				activeChar.sendMessage("Вы включили систему автоматического восстановления MP. Ваше MP будет автоматически восстанавливаться при значении " + percent + "% и меньше.");
			else
				activeChar.sendMessage("You have enabled an automatic MP recovery. Your MP will automatically recover at a value of " + percent + "% or less.");
			return true;
		}
		return false;
	}

	@Override
	public void onInit()
	{
		super.onInit();
		CharListenerList.addGlobal(PLAYER_ENTER_LISTENER);
		AUTO_HP_TASK = ThreadPoolManager.getInstance().scheduleAtFixedRate(new AutoHpTask(), 300L, 300L);
		AUTO_MP_TASK = ThreadPoolManager.getInstance().scheduleAtFixedRate(new AutoMpTask(), 300L, 300L);
		AUTO_CP_TASK = ThreadPoolManager.getInstance().scheduleAtFixedRate(new AutoCpTask(), 300L, 300L);
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return COMMANDS;
	}

	private static void useItem(Player player, ItemInstance item)
	{
		// Запускаем в новом потоке, потому что итем может юзнуться несколько раз проигнорировав откат итема
		ThreadPoolManager.getInstance().execute(() -> player.useItem(item, false, false));
	}
}