package ai;

import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.Mystic;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import instances.Goldberg;
/**
 * @author nexvill
 */
public class GoldbergAI extends Mystic
{
	private static final int GOLDBERG_MOVE = 100000;
	private static final int NEXT_TEXT = 100001;
	private static final int NEXT_TEXT_2 = 100002;
	private static final int ATTACK = 100003;
	
	private boolean _says50 = false;

	public GoldbergAI(NpcInstance actor)
	{
		super(actor);		
	}
	
	@Override
    protected void onEvtSpawn()
    {
        super.onEvtSpawn();
        
        addTimer(GOLDBERG_MOVE, 5000L);
    }
	
	@Override
	protected void onEvtTimer(int timerId, Object arg1, Object arg2)
	{
		super.onEvtTimer(timerId, arg1, arg2);
		
		switch (timerId)
		{
			case GOLDBERG_MOVE:
			{
				Reflection reflection = getActor().getReflection();
				if (reflection instanceof Goldberg)
				{
					for (Player player : reflection.getPlayers())
					{
						player.sendPacket(new ExShowScreenMessage("Rats have become kings while I've been dormant.", 5000, ScreenMessageAlign.TOP_CENTER, false));
						addTimer(NEXT_TEXT, 7000);
					}
					getActor().getMovement().moveToLocation(11711, -86508, -10928, 0, true);
				}
				break;
			}
			case NEXT_TEXT:
			{
				Reflection reflection = getActor().getReflection();
				if (reflection instanceof Goldberg)
				{
					for (Player player : reflection.getPlayers())
					{
						player.sendPacket(new ExShowScreenMessage("Zaken or whatever is going wild all over the southern sea.", 5000, ScreenMessageAlign.TOP_CENTER, false));
						addTimer(NEXT_TEXT_2, 7000);
					}
				}
				break;
			}
			case NEXT_TEXT_2:
			{
				Reflection reflection = getActor().getReflection();
				if (reflection instanceof Goldberg)
				{
					for (Player player : reflection.getPlayers())
					{
						player.sendPacket(new ExShowScreenMessage("Who dare enter my place? Zaken sent you?", 5000, ScreenMessageAlign.TOP_CENTER, false));
					}
					addTimer(ATTACK, 1000);
				}
				break;
			}
			case ATTACK:
			{
				NpcInstance npc = getActor();
				if (npc.getReflection() instanceof Goldberg)
				{
					SkillEntry fatalWave = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 16147, 7);
					SkillEntry powerStrike = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4032, 12);
					ThreadPoolManager.getInstance().scheduleAtFixedRate(() ->
					{
						if (npc.getAggroList().getHateList(1000).size() > 0)
						{
							if (Rnd.get(100) < 80)
							{
								npc.doCast(fatalWave, npc.getAggroList().getMostHated(1000), true);
							}
							else
							{
								npc.doCast(powerStrike, npc.getAggroList().getMostHated(1000), true);
							}
						}
					}
					, 500, 3100);
				}
			}
		}
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, Skill skill, int damage)
	{
		NpcInstance npc = getActor();
		Reflection reflection = npc.getReflection();
		
		if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.5)) && !_says50)
		{
			_says50 = true;
			for (Player player : reflection.getPlayers())
			{
				player.sendPacket(new ExShowScreenMessage("Anyone who stands against me shall be dead!", 5000, ScreenMessageAlign.TOP_CENTER, false));
			}
		}
		
		super.onEvtAttacked(attacker, skill, damage);
	}
	
	@Override
	protected void onEvtDead(Creature killer)
	{
		NpcInstance actor = getActor();
		if(killer.getPlayer().getParty()!=null && killer.getPlayer().getParty().getMemberCount()==2){
			spawnMinions(actor,1);
		}
		else if(killer.getPlayer().getParty()!=null && killer.getPlayer().getParty().getMemberCount()==3){
			spawnMinions(actor,2);
		}
		else if(killer.getPlayer().getParty()!=null && killer.getPlayer().getParty().getMemberCount()==4){
			spawnMinions(actor,4);
		}
		else if(killer.getPlayer().getParty()!=null && killer.getPlayer().getParty().getMemberCount()==5){
			spawnMinions(actor,7);
		}
		else if(killer.getPlayer().getParty()!=null && killer.getPlayer().getParty().getMemberCount()==6){
			spawnMinions(actor,10);
		}
		else if(killer.getPlayer().getParty()!=null && killer.getPlayer().getParty().getMemberCount()==7){
			spawnMinions(actor,13);
		}
		else if(killer.getPlayer().getParty()!=null && killer.getPlayer().getParty().getMemberCount()==8){
			spawnMinions(actor,16);
		}
		else if(killer.getPlayer().getParty()!=null && killer.getPlayer().getParty().getMemberCount()==9){
			spawnMinions(actor,27);
		}

        Reflection reflection = actor.getReflection();
        reflection.setReenterTime(System.currentTimeMillis(), false);
        stopAllTaskAndTimers();
        
		super.onEvtDead(killer);
	}

	private void spawnMinions(NpcInstance actor, int count)
	{
		for (int i = 0; i <count ; i++) {

			Location loc = new Location(11708 + Rnd.get(-1000, 1000), -86505 + Rnd.get(-1000, 1000), -10928);
			NpcInstance mob = actor.getReflection().addSpawnWithoutRespawn(18357, loc, 0);
		}
	}

}