package ai;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.ai.Fighter;
/**
 * @author nexvill
 */
public class WaterSlimeAI extends Fighter
{
	private static final int AQUA_RAGE = 50036;
    
    public WaterSlimeAI(NpcInstance actor)
    {
        super(actor);
    }

    @Override
    protected void onEvtAttacked(Creature attacker, Skill skill, int damage)
    {
    	NpcInstance npc = getActor();
    	npc.doDie(attacker);
    	for (Abnormal ab : attacker.getAbnormalList().toArray())
    	{
    		if ((ab.getSkill().getId() == AQUA_RAGE) && (ab.getSkill().getLevel() == 1))
			{
				if (Rnd.get(100) < 50)
				{
					attacker.getAbnormalList().stop(AQUA_RAGE);
				}
			}
    		if ((ab.getSkill().getId() == AQUA_RAGE) && (ab.getSkill().getLevel() == 2))
			{
				if (Rnd.get(100) < 50)
				{
					attacker.getAbnormalList().stop(AQUA_RAGE);
					SkillEntry entry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, AQUA_RAGE, 1);
					entry.getEffects(attacker, attacker);
				}
			}
    		if ((ab.getSkill().getId() == AQUA_RAGE) && (ab.getSkill().getLevel() == 3))
			{
				if (Rnd.get(100) < 50)
				{
					attacker.getAbnormalList().stop(AQUA_RAGE);
					SkillEntry entry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, AQUA_RAGE, 2);
					entry.getEffects(attacker, attacker);
				}
			}
    		if ((ab.getSkill().getId() == AQUA_RAGE) && (ab.getSkill().getLevel() == 4))
			{
				if (Rnd.get(100) < 50)
				{
					attacker.getAbnormalList().stop(AQUA_RAGE);
					SkillEntry entry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, AQUA_RAGE, 3);
					entry.getEffects(attacker, attacker);
				}
			}
    	}
    }
}