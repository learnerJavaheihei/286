package ai;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.ai.Mystic;
/**
 * @author nexvill
 */
public class KingIgnisAI extends Mystic
{
    private static final int CAST_FIRE_RAGE_1 = 100005;
    private static final int CAST_FIRE_RAGE_2 = 100006;
    private static final int CAST_FIRE_RAGE_3 = 100007;
    private static final int CAST_FIRE_RAGE_4 = 100008;
    private static final int CAST_FIRE_RAGE_5 = 100009;
    private static final int CAST_FIRE_RAGE_6 = 100010;
    private static final int CAST_FIRE_RAGE_7 = 100011;
    private static final int CAST_FIRE_RAGE_8 = 100012;
    private static final int CAST_FIRE_RAGE_9 = 100013;
    private static final int CAST_FIRE_RAGE_10 = 100014;
 // Skills
 	private static final SkillEntry FIRE_RAGE_1 = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 50050, 1);
 	private static final SkillEntry FIRE_RAGE_2 = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 50050, 2);
 	private static final SkillEntry FIRE_RAGE_3 = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 50050, 3);
 	private static final SkillEntry FIRE_RAGE_4 = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 50050, 4);
 	private static final SkillEntry FIRE_RAGE_5 = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 50050, 5);
 	private static final SkillEntry FIRE_RAGE_6 = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 50050, 6);
 	private static final SkillEntry FIRE_RAGE_7 = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 50050, 7);
 	private static final SkillEntry FIRE_RAGE_8 = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 50050, 8);
 	private static final SkillEntry FIRE_RAGE_9 = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 50050, 9);
 	private static final SkillEntry FIRE_RAGE_10 = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 50050, 10);
    
    public KingIgnisAI(NpcInstance actor)
    {
        super(actor);
    }

    @Override
    protected void onEvtDead(Creature killer)
    {
        NpcInstance actor = getActor();
        Reflection reflection = actor.getReflection();
        reflection.setReenterTime(System.currentTimeMillis(), false);
        stopAllTaskAndTimers();
        super.onEvtDead(killer);
    }

    @Override
    protected void onEvtAttacked(Creature attacker, Skill skill, int damage)
    {
    	NpcInstance npc = getActor();
    	if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.99)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.70)))
		{
			addTimer(CAST_FIRE_RAGE_1, 1000);
		}
		else if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.70)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.50)))
		{
			addTimer(CAST_FIRE_RAGE_2, 1000);
		}
		else if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.50)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.40)))
		{
			addTimer(CAST_FIRE_RAGE_3, 1000);
		}
		else if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.40)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.25)))
		{
			addTimer(CAST_FIRE_RAGE_4, 1000);
		}
		else if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.25)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.15)))
		{
			addTimer(CAST_FIRE_RAGE_5, 1000);
		}
		else if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.15)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.10)))
		{
			addTimer(CAST_FIRE_RAGE_6, 1000);
		}
		else if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.10)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.7)))
		{
			addTimer(CAST_FIRE_RAGE_7, 1000);
		}
		else if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.7)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.5)))
		{
			addTimer(CAST_FIRE_RAGE_8, 1000);
		}
		else if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.5)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.3)))
		{
			addTimer(CAST_FIRE_RAGE_9, 1000);
		}
		else if (npc.getCurrentHp() < (npc.getMaxHp() * 0.3))
		{
			addTimer(CAST_FIRE_RAGE_10, 1000);
		}
        super.onEvtAttacked(attacker, skill, damage);
    }
    
    @Override
	protected void onEvtTimer(int timerId, Object arg1, Object arg2)
	{
		super.onEvtTimer(timerId, arg1, arg2);
		
		NpcInstance npc = getActor();
		int size = npc.getAroundCharacters(1000, 1000).size();
		Creature target = npc.getAroundCharacters(1000, 1000).get(Rnd.get(size));
		
		switch (timerId)
		{
			case CAST_FIRE_RAGE_1:
			{
				npc.doCast(FIRE_RAGE_1, target, false);
				FIRE_RAGE_1.getEffects(npc, target);
				break;
			}
			case CAST_FIRE_RAGE_2:
			{
				npc.doCast(FIRE_RAGE_2, target, false);
				FIRE_RAGE_2.getEffects(npc, target);
				break;
			}
			case CAST_FIRE_RAGE_3:
			{
				npc.doCast(FIRE_RAGE_3, target, false);
				FIRE_RAGE_3.getEffects(npc, target);
				break;
			}
			case CAST_FIRE_RAGE_4:
			{
				npc.doCast(FIRE_RAGE_4, target, false);
				FIRE_RAGE_4.getEffects(npc, target);
				break;
			}
			case CAST_FIRE_RAGE_5:
			{
				npc.doCast(FIRE_RAGE_5, target, false);
				FIRE_RAGE_5.getEffects(npc, target);
				break;
			}
			case CAST_FIRE_RAGE_6:
			{
				npc.doCast(FIRE_RAGE_6, target, false);
				FIRE_RAGE_6.getEffects(npc, target);
				break;
			}
			case CAST_FIRE_RAGE_7:
			{
				npc.doCast(FIRE_RAGE_7, target, false);
				FIRE_RAGE_7.getEffects(npc, target);
				break;
			}
			case CAST_FIRE_RAGE_8:
			{
				npc.doCast(FIRE_RAGE_8, target, false);
				FIRE_RAGE_8.getEffects(npc, target);
				break;
			}
			case CAST_FIRE_RAGE_9:
			{
				npc.doCast(FIRE_RAGE_9, target, false);
				FIRE_RAGE_9.getEffects(npc, target);
				break;
			}
			case CAST_FIRE_RAGE_10:
			{
				npc.doCast(FIRE_RAGE_10, target, false);
				FIRE_RAGE_10.getEffects(npc, target);
				break;
			}
		}
	}

    @Override
    protected void onEvtSpawn()
    {
        super.onEvtSpawn();
    }
}
