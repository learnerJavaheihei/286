package l2s.gameserver.skills.effects;

import java.util.List;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObjectTasks.EndBreakFakeDeathTask;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ChangeWaitTypePacket;
import l2s.gameserver.network.l2.s2c.RevivePacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class EffectFakeDeath extends EffectHandler
{
	public EffectFakeDeath(EffectTemplate template)
	{
		super(template);
	}

	@Override
	protected boolean checkCondition(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(effected.isPlayer())
		{
			if(effected.isInvisible(null)) //same as invis cannot be invis & fake death
				return false;

			Player player = effected.getPlayer();
			if(player.getActiveWeaponFlagAttachment() != null) //same as invis, if doing fake death during siege, cannot target? nonsense.
				return false;

			return true;
		}
		return false;
	}
	
	@Override
	public void onStart(Abnormal abnormal, Creature effector, Creature effected)
	{
		Player player = effected.getPlayer();
		player.setFakeDeath(true);
		player.getAI().notifyEvent(CtrlEvent.EVT_FAKE_DEATH, null, null);
		player.broadcastPacket(new ChangeWaitTypePacket(player, ChangeWaitTypePacket.WT_START_FAKEDEATH));
		player.broadcastCharInfo();
	}

	@Override
	public void onExit(Abnormal abnormal, Creature effector, Creature effected)
	{
		// 5 секунд после FakeDeath на персонажа не агрятся мобы
		Player player = effected.getPlayer();
		player.setNonAggroTime(System.currentTimeMillis() + 5000L);
		//player.setFakeDeath(false); //after
		player.broadcastPacket(new ChangeWaitTypePacket(player, ChangeWaitTypePacket.WT_STOP_FAKEDEATH));
		if(getSkill().getId() == 10528) //stupid but fine for now
			player.setTargetable(true);		
		player.broadcastPacket(new RevivePacket(player));
		player.broadcastCharInfo();
		ThreadPoolManager.getInstance().schedule(new EndBreakFakeDeathTask(player), 2500);
	}

	@Override
	public boolean onActionTime(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(effected.isDead())
			return false;

		double manaDam = getValue();

		if(manaDam > effected.getCurrentMp())
		{
			if(getSkill().isToggle())
			{
				effected.sendPacket(SystemMsg.NOT_ENOUGH_MP);
				effected.sendPacket(new SystemMessagePacket(SystemMsg.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(getSkill().getId(), getSkill().getDisplayLevel()));
				return false;
			}
		}

		effected.reduceCurrentMp(manaDam, null);
		return true;
	}
}