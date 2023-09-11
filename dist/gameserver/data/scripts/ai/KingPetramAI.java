package ai;

import java.util.concurrent.ScheduledFuture;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.s2c.NSPacket;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.utils.MapUtils;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.geometry.Location;
/**
 * @author nexvill
 */
public class KingPetramAI extends Fighter
{
    private static final SkillEntry EARTH_ENERGY = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 50066, 1);
    private static final SkillEntry EARTH_FURY_SKILL = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 50059, 1);
    private static final SkillEntry TEST = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 5712, 1); // only for visual, need correct id
    
    private static final int SPAWN_MINION = 100015;
    private static final int UNSPAWN_MINION = 100016;
    private static final int SUPPORT_PETRAM = 100017;
    private static final int EARTH_FURY = 100018;
    
    private boolean spawnedMinions_1, spawnedMinions_2, spawnedMinions_3, spawnedMinions_4 = false;
    private NpcInstance mob1, mob2, mob3, mob4;
    private ScheduledFuture<?> checkDeadTask = null;
    public KingPetramAI(NpcInstance actor)
    {
        super(actor);
    }

    @Override
    protected void onEvtDead(Creature killer)
    {
        NpcInstance actor = getActor();
        Reflection reflection = actor.getReflection();
        reflection.setReenterTime(System.currentTimeMillis(), false);
        addTimer(UNSPAWN_MINION, 3000);
        super.onEvtDead(killer);
    }

    @Override
    protected void onEvtAttacked(Creature attacker, Skill skill, int damage)
    {
    	NpcInstance npc = getActor();
    	
    	if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.70)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.68)))
		{
			addTimer(EARTH_FURY, 1000);
			
			if (!spawnedMinions_1)
			{
				addTimer(SPAWN_MINION, 1000);
				spawnedMinions_1 = true;
			}
		}
		else if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.40)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.38)))
		{
			addTimer(EARTH_FURY, 1000);
			
			if (!spawnedMinions_2)
			{
				addTimer(SPAWN_MINION, 1000);
				spawnedMinions_2 = true;
			}
		}
		else if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.20)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.18)))
		{
			addTimer(EARTH_FURY, 1000);
			
			if (!spawnedMinions_3)
			{
				addTimer(SPAWN_MINION, 1000);
				spawnedMinions_3 = true;
			}
		}
		else if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.10)) && (npc.getCurrentHp() > (npc.getMaxHp() * 0.08)))
		{
			addTimer(EARTH_FURY, 1000);
			
			if (!spawnedMinions_4)
			{
				addTimer(SPAWN_MINION, 1000);
				spawnedMinions_4 = true;
			}
		}
    	
        super.onEvtAttacked(attacker, skill, damage);
    }
    
    @Override
	protected void onEvtTimer(int timerId, Object arg1, Object arg2)
	{
		super.onEvtTimer(timerId, arg1, arg2);
		NpcInstance npc = getActor();
		Reflection reflection = npc.getReflection();
		int size = npc.getAroundCharacters(1000, 1000).size();
		Creature target = npc.getAroundCharacters(1000, 1000).get(Rnd.get(size));
		
		switch (timerId)
		{
			case SPAWN_MINION:
			{
				npc.doCast(EARTH_ENERGY, target, false);
				Location loc = new Location(221543, 191530, -15486);
				mob1 = reflection.addSpawnWithoutRespawn(29116, loc, 0);
				loc = new Location(222069, 192019, -15486);
				mob2 = reflection.addSpawnWithoutRespawn(29117, loc, 0);
				loc = new Location(222595, 191479, -15486);
				mob3 = reflection.addSpawnWithoutRespawn(29116, loc, 0);
				loc = new Location(222077, 191017, -15486);
				mob4 = reflection.addSpawnWithoutRespawn(29117, loc, 0);
				npc.getFlags().getInvulnerable().start();
				npc.getFlags().getDebuffImmunity().start();
				npc.startAbnormalEffect(AbnormalEffect.INVINCIBILITY);
				sendMessage(npc, reflection, "HaHa, fighters lets kill them. Now Im invul!!!");
				checkDeadTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() ->
				{
					if (mob1.isDead() && mob2.isDead() && mob3.isDead() && mob4.isDead())
					{
						addTimer(UNSPAWN_MINION, 1000);
					}
				}, 3000L, 3000L);
				addTimer(SUPPORT_PETRAM, 3000);
				break;
			}
			case UNSPAWN_MINION:
			{
				if (checkDeadTask != null)
				{
					checkDeadTask.cancel(true);
					checkDeadTask = null;
				}
				npc.getFlags().getInvulnerable().stop();
				npc.getFlags().getDebuffImmunity().stop();
				npc.stopAbnormalEffect(AbnormalEffect.INVINCIBILITY);
				sendMessage(npc, reflection, "Nooooo... Nooooo...");
				break;
			}
			case SUPPORT_PETRAM:
			{
				if (!mob1.isDead())
				{
					mob1.doCast(TEST, npc, false);
				}
				if (!mob2.isDead())
				{
					mob2.doCast(TEST, npc, false);
				}
				if (!mob3.isDead())
				{
					mob3.doCast(TEST, npc, false);
				}
				if (!mob4.isDead())
				{
					mob4.doCast(TEST, npc, false);
				}
				addTimer(SUPPORT_PETRAM, 10100);
				break;
			}
			case EARTH_FURY:
			{
				npc.doCast(EARTH_FURY_SKILL, target, false);
				break;
			}
		}
	}
    
    private void sendMessage(NpcInstance npc, Reflection reflection, String text)
	{
		int rx = MapUtils.regionX(npc);
		int ry = MapUtils.regionY(npc);
		
		for (Player ppl : reflection.getPlayers())
		{
			int tx = MapUtils.regionX(ppl) - rx;
			int ty = MapUtils.regionY(ppl) - ry;

			if(tx*tx + ty*ty <= Config.SHOUT_SQUARE_OFFSET || ppl.isInRangeZ(npc, Config.CHAT_RANGE))
				ppl.sendPacket(new NSPacket(npc, ChatType.NPC_SHOUT, text));
		}
	}

    @Override
    protected void onEvtSpawn()
    {
        super.onEvtSpawn();
    }
}