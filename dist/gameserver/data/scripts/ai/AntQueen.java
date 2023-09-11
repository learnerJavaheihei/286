package ai;

import java.util.HashMap;
import java.util.Map;
import l2s.commons.util.Rnd;
import l2s.gameserver.ai.Mystic;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;


/*
 * @author L2-scripts.com - (SanyaDC)
 */
public class AntQueen extends Mystic
{
	
	final SkillEntry poison = getSkill(4019, 1), slow = getSkill(16150, 1);
	private static long attackDelay = 0;

	public AntQueen(NpcInstance actor)
	{
		super(actor);
	}
	private SkillEntry getSkill(int id, int level)
	{
		return SkillEntry.makeSkillEntry(SkillEntryType.NONE, id, level);
	}
	
	@Override
	protected boolean createNewTask()
	{
		clearTasks();
		Creature target;
		if((target = prepareTarget()) == null)
			return false;

		NpcInstance actor = getActor();
		if(actor.isDead())
			return false;

	

		
		
			
			if(Rnd.chance(100))
			{
				if(Rnd.chance(60))
					addTaskCast(target, poison);
				else
					addTaskCast(target, poison);
			}
			
		
		double distance = actor.getDistance(target);
		final int _hpStage = 0;
		Map<SkillEntry, Integer> d_skill = new HashMap<SkillEntry, Integer>();
		switch(_hpStage)
		{
			case 1:
				addDesiredSkill(d_skill, target, distance, poison);
				break;
			case 2:
				addDesiredSkill(d_skill, target, distance, poison);
				break;
			default:
				break;
		}

	

		SkillEntry r_skill = selectTopSkill(d_skill);
		if(r_skill != null && !r_skill.getTemplate().isDebuff())
			target = actor;

		return chooseTaskAndTargets(r_skill, target, distance);
	}
	
/*@Override
	protected void onEvtAttacked(Creature attacker, Skill skill, int damage)
	{
		super.onEvtAttacked(attacker, skill, damage);
		NpcInstance actor = getActor();		

		if(Rnd.chance(50))									
					addTaskAttack(attacker, slow, 1000000);
				
		else 				
			addTaskAttack(attacker, poison, 1000000);
		
		
	}*/
	
}