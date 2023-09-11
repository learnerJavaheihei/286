package ai;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;

public class Orfen_RibaIren extends Fighter
{
	private static final int Orfen_id = 29014;

	public Orfen_RibaIren(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean createNewTask()
	{
		return defaultNewTask();
	}

	@Override
	protected void onEvtClanAttacked(NpcInstance member, Creature attacker, int damage)
	{
		super.onEvtClanAttacked(member, attacker, damage);
		NpcInstance actor = getActor();
		if(_healSkills.length == 0)
			return;
		if(member.isDead() || actor.isDead() || member.getCurrentHpPercents() > 50)
			return;

		int heal_chance = 0;
		if(member.getNpcId() == actor.getNpcId())
			heal_chance = member.getObjectId() == actor.getObjectId() ? 100 : 0;
		else
			heal_chance = member.getNpcId() == Orfen_id ? 90 : 10;

		if(Rnd.chance(heal_chance) && canUseSkill(_healSkills[0], member, -1))
			addTaskAttack(member, SkillEntry.makeSkillEntry(SkillEntryType.NONE, _healSkills[0]), 1000000);
	}
}