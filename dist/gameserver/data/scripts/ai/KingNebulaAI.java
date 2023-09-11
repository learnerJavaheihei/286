package ai;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.ai.Mystic;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;

/**
 * @author nexvill
 */
public class KingNebulaAI extends Mystic
{
    private static final int WATER_SLIME = 29111;
    
    private static final int AQUA_RAGE = 50036;
    private static final SkillEntry AQUA_RAGE_1 = SkillEntry.makeSkillEntry(SkillEntryType.NONE, AQUA_RAGE, 1);
    private static final SkillEntry AQUA_RAGE_2 = SkillEntry.makeSkillEntry(SkillEntryType.NONE, AQUA_RAGE, 2);
    private static final SkillEntry AQUA_RAGE_3 = SkillEntry.makeSkillEntry(SkillEntryType.NONE, AQUA_RAGE, 3);
    private static final SkillEntry AQUA_RAGE_4 = SkillEntry.makeSkillEntry(SkillEntryType.NONE, AQUA_RAGE, 4);
    private static final SkillEntry AQUA_RAGE_5 = SkillEntry.makeSkillEntry(SkillEntryType.NONE, AQUA_RAGE, 5);
    private static final SkillEntry AQUA_SUMMON = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 50037, 1);
    
    private static final int SPAWN_WATER_SLIME = 100025;
    private static final int PLAYER_PARA = 100026;
    private static final int PLAYER_UNPARA = 100027;
    private static final int CAST_AQUA_RAGE = 100028;
    
    
    public KingNebulaAI(NpcInstance actor)
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
	protected void onEvtTimer(int timerId, Object arg1, Object arg2)
	{
		super.onEvtTimer(timerId, arg1, arg2);
		NpcInstance npc = getActor();
		Reflection reflection = npc.getReflection();
		
		switch (timerId)
		{
			case SPAWN_WATER_SLIME:
			{
				addTimer(CAST_AQUA_RAGE, 60000 + Rnd.get(-15000, 15000));
				npc.doCast(AQUA_SUMMON, npc, true);
				for (int i = 0; i < Rnd.get(4, 6); i++)
				{
					reflection.addSpawnWithoutRespawn(WATER_SLIME, npc.getLoc(), 0);
				}
				addTimer(SPAWN_WATER_SLIME, 300000);
				break;
			}
			case PLAYER_PARA:
			{
				for (Player plr : reflection.getPlayers())
				{
					for (Abnormal ab : plr.getAbnormalList().toArray())
					{
						if ((ab.getSkill().getId() == AQUA_RAGE) && (ab.getSkill().getLevel() == 5))
						{
							plr.startAbnormalEffect(AbnormalEffect.FROZEN_PILLAR);
							plr.getFlags().getImmobilized().start();
							addTimer(PLAYER_UNPARA, 5000);
						}
					}
				}
				break;
			}
			case PLAYER_UNPARA:
			{
				for (Player plr : reflection.getPlayers())
				{
					for (Abnormal ab : plr.getAbnormalList().toArray())
					{
						if ((ab.getSkill().getId() == AQUA_RAGE) && (ab.getSkill().getLevel() == 5))
						{
							plr.stopAbnormalEffect(AbnormalEffect.FROZEN_PILLAR);
							plr.getFlags().getImmobilized().stop();
						}
					}
				}
				break;
			}
			case CAST_AQUA_RAGE:
			{
				addTimer(CAST_AQUA_RAGE, 5000);
				Player plr = reflection.getPlayers().stream().findAny().orElse(null);
				if ((plr != null) && plr.getDistance3D(npc.getLoc()) < 500)
				{
					for (Abnormal ab : plr.getAbnormalList().toArray())
					{
						if ((ab.getSkill().getId() == AQUA_RAGE) && (ab.getSkill().getLevel() == 1))
						{
							npc.doCast(AQUA_RAGE_2, plr, true);
							AQUA_RAGE_2.getEffects(npc, plr);
						}
						else if ((ab.getSkill().getId() == AQUA_RAGE) && (ab.getSkill().getLevel() == 2))
						{
							npc.doCast(AQUA_RAGE_3, plr, true);
							AQUA_RAGE_3.getEffects(npc, plr);
						}
						else if ((ab.getSkill().getId() == AQUA_RAGE) && (ab.getSkill().getLevel() == 3))
						{
							npc.doCast(AQUA_RAGE_4, plr, true);
							AQUA_RAGE_4.getEffects(npc, plr);
						}
						else if ((ab.getSkill().getId() == AQUA_RAGE) && (ab.getSkill().getLevel() == 4))
						{
							npc.doCast(AQUA_RAGE_5, plr, true);
							AQUA_RAGE_5.getEffects(npc, plr);
							addTimer(PLAYER_PARA, 100);
						}
						else if ((ab.getSkill().getId() == AQUA_RAGE) && (ab.getSkill().getLevel() == 5))
						{
							npc.abortCast(true, false);
						}
						else
						{
							npc.doCast(AQUA_RAGE_1, plr, true);
							AQUA_RAGE_1.getEffects(npc, plr);
						}
					}
				}
				break;
			}
		}
	}
   
   @Override
	protected void onEvtAttacked(Creature attacker, Skill skill, int damage)
	{
	   addTimer(SPAWN_WATER_SLIME, 300000);
	}

    @Override
    protected void onEvtSpawn()
    {
        super.onEvtSpawn();
    }
}
