package l2s.gameserver.skills.effects.instant;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;

import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
 */
public class i_spoil extends i_abstract_effect
{
	public i_spoil(EffectTemplate template)
	{
		super(template);
	}

	@Override
	protected boolean checkCondition(Creature effector, Creature effected)
	{
		if(!effector.isPlayer())
			return false;

		if(effected.isDead())
			return false;

		if(!effected.isMonster())
			return false;

		return true;
	}

	@Override
	public void instantUse(Creature effector, Creature effected, boolean reflected)
	{
		final MonsterInstance monster = (MonsterInstance) effected;
		if(monster.isSpoiled())
		{
			effector.sendPacket(SystemMsg.IT_HAS_ALREADY_BEEN_SPOILED);
			return;
		}

		final Player player = effector.getPlayer();
		final int monsterLevel = monster.getLevel();
		final int modifier = Math.abs(monsterLevel - player.getLevel());
		double rateOfSpoil = Config.BASE_SPOIL_RATE;

		if(modifier > 8)
			rateOfSpoil = rateOfSpoil - rateOfSpoil * (modifier - 8) * 9 / 100;

		rateOfSpoil = rateOfSpoil * getSkill().getMagicLevel() / monsterLevel;

		if(rateOfSpoil < Config.MINIMUM_SPOIL_RATE)
			rateOfSpoil = Config.MINIMUM_SPOIL_RATE;
		else if(rateOfSpoil > 99.)
			rateOfSpoil = 99.;

		if(player.isGM())
			player.sendMessage(new CustomMessage("l2s.gameserver.skills.skillclasses.Spoil.Chance").addNumber((long) rateOfSpoil));

		doSpoil(effector, effected, Rnd.chance(rateOfSpoil));
	}

	protected void doSpoil(Creature effector, Creature effected, boolean success)
	{
		if(success)
		{
			((MonsterInstance) effected).setSpoiled(effector.getPlayer());
			effector.sendPacket(SystemMsg.THE_SPOIL_CONDITION_HAS_BEEN_ACTIVATED);
		}
		else
			effector.sendPacket(new SystemMessagePacket(SystemMsg.S1_HAS_FAILED).addSkillName(getSkill()));
	}
}