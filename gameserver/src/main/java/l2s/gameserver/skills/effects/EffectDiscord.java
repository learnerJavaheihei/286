package l2s.gameserver.skills.effects;

import java.util.ArrayList;
import java.util.List;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.instances.SummonInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.skill.EffectTemplate;

public class EffectDiscord extends EffectHandler
{
	public EffectDiscord(EffectTemplate template)
	{
		super(template);
	}

	@Override
	protected boolean checkCondition(Abnormal abnormal, Creature effector, Creature effected)
	{
		boolean multitargets = getSkill().isAoE();

		if(!effected.isMonster())
		{
			if(!multitargets)
				effector.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return false;
		}

		if(effected.isFearImmune())
		{
			if(!multitargets)
				effector.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return false;
		}

		// Discord нельзя наложить на осадных саммонов
		Player player = effected.getPlayer();
		if(player != null)
		{
			if(effected.isSummon())
			{
				for(SiegeEvent<?, ?> siegeEvent : player.getEvents(SiegeEvent.class))
				{
					if(siegeEvent.containsSiegeSummon((SummonInstance) effected))
					{
						if(!multitargets)
							effector.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
						return false;
					}
				}
			}
		}

		if(effected.isInPeaceZone())
		{
			if(!multitargets)
				effector.sendPacket(SystemMsg.YOU_MAY_NOT_ATTACK_IN_A_PEACEFUL_ZONE);
			return false;
		}

		int skilldiff = effected.getLevel() - getSkill().getMagicLevel();
		int lvldiff = effected.getLevel() - effector.getLevel();
		if(skilldiff > 10 || skilldiff > 5 && Rnd.chance(30) || Rnd.chance(Math.abs(lvldiff) * 2))
		{
			if(!multitargets)
				effector.sendPacket(new SystemMessagePacket(SystemMsg.S1_HAS_FAILED).addSkillName(getSkill()));
			return false;
		}

		return true;
	}

	@Override
	public boolean isHidden()
	{
		return true;
	}

	@Override
	public void onStart(Abnormal abnormal, Creature effector, Creature effected)
	{
		effected.getFlags().getConfused().start(this);
		onActionTime(abnormal, effector, effected);
	}

	@Override
	public void onExit(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(effected.getFlags().getConfused().stop(this))
		{
			effected.abortAttack(true, true);
			effected.abortCast(true, true);
			effected.getMovement().stopMove();
			effected.getAI().setAttackTarget(null);
			effected.setWalking();
			effected.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
	}

	@Override
	public boolean onActionTime(Abnormal abnormal, Creature effector, Creature effected)
	{
		List<Creature> targetList = new ArrayList<Creature>();

		for(Creature character : effected.getAroundCharacters(900, 200))
			if(character.isNpc() && character != effected)
				targetList.add(character);

		// if there is no target, exit function
		if(targetList.isEmpty())
			return true;

		// Choosing randomly a new target
		Creature target = targetList.get(Rnd.get(targetList.size()));

		// Attacking the target
		effected.setRunning();
		effected.getAI().Attack(target, true, false);

		return false;
	}
}