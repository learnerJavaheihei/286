package ai;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;

import java.util.*;

/**
 * @author Bonux
**/
public abstract class AbstractAntharasAI extends DefaultAI
{
	protected static final int MOVIE_MAX_DISTANCE = 2750;

	// Debuff skills
	protected final SkillEntry s_fear = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4108, 1);
	protected final SkillEntry s_fear2 = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 5092, 1);
	protected final SkillEntry s_curse = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4109, 1);
	protected final SkillEntry s_paralyze = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4111, 1);

	// Damage skills
	protected final SkillEntry s_shock = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4106, 1);
	protected final SkillEntry s_shock2 = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4107, 1);
	protected final SkillEntry s_antharas_ordinary_attack = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4112, 1);
	protected final SkillEntry s_antharas_ordinary_attack2 = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4113, 1);
	protected final SkillEntry s_meteor = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 5093, 1);
	protected final SkillEntry s_breath = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4110, 1);

	// Regen skills
	protected final SkillEntry s_regen1 = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4239, 1);
	protected final SkillEntry s_regen2 = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4240, 1);
	protected final SkillEntry s_regen3 = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4241, 1);

	protected final List<NpcInstance> _minions = new ArrayList<NpcInstance>();

	protected int _hpStage = 0;
	protected long _minionsSpawnTime = 0;
	protected int _damageCounter = 0;

	public AbstractAntharasAI(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	public final boolean isGlobalAI()
	{
		return true;
	}

	@Override
	public void onEvtDeSpawn()
	{
		for(NpcInstance minion : _minions)
			minion.deleteMe();

		super.onEvtDeSpawn();
	}

	@Override
	protected void onEvtAttacked(Creature attacker, Skill skill, int damage)
	{
		NpcInstance actor = getActor();

		if(_damageCounter == 0)
			actor.getAI().startAITask();

		for(Player player : getPlayersInsideLair())
		{
			notifyEvent(CtrlEvent.EVT_AGGRESSION, player, 1);

			for(Servitor servitor : player.getServitors())
				notifyEvent(CtrlEvent.EVT_AGGRESSION, servitor, 1);
		}

		_damageCounter++;	

		super.onEvtAttacked(attacker, skill, damage);
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

		double distance = actor.getDistance(target);

		// Buffs and stats
		double chp = actor.getCurrentHpPercents();
		if(_hpStage == 0)
		{
			actor.altOnMagicUse(actor, s_regen1);
			_hpStage = 1;
		}
		else if(chp < 75 && _hpStage == 1)
		{
			actor.altOnMagicUse(actor, s_regen2);
			_hpStage = 2;
		}
		else if(chp < 50 && _hpStage == 2)
		{
			actor.altOnMagicUse(actor, s_regen3);
			_hpStage = 3;
		}
		else if(chp < 30 && _hpStage == 3)
		{
			actor.altOnMagicUse(actor, s_regen3);
			_hpStage = 4;
		}

		// Minions spawn
		if(_minionsSpawnTime < System.currentTimeMillis() && getAliveMinionsCount() < 30)
		{
			double chance = (100. - actor.getCurrentHpPercents()) / 10. + 1.;
			if(Rnd.chance(chance))
			{
				NpcInstance minion = spawnMinion();
				if(minion != null)
					_minions.add(minion);  // Antharas Minions
			}
		}

		// Basic Attack
		if(Rnd.chance(50))
			return chooseTaskAndTargets(Rnd.chance(50) ? s_antharas_ordinary_attack : s_antharas_ordinary_attack2, target, distance);

		// Stage based skill attacks
		Map<SkillEntry, Integer> d_skill = new HashMap<SkillEntry, Integer>();
		switch(_hpStage)
		{
			case 1:
				addDesiredSkill(d_skill, target, distance, s_curse);
				addDesiredSkill(d_skill, target, distance, s_paralyze);
				addDesiredSkill(d_skill, target, distance, s_meteor);
				break;
			case 2:
				addDesiredSkill(d_skill, target, distance, s_curse);
				addDesiredSkill(d_skill, target, distance, s_paralyze);
				addDesiredSkill(d_skill, target, distance, s_meteor);
				addDesiredSkill(d_skill, target, distance, s_fear2);
				break;
			case 3:
				addDesiredSkill(d_skill, target, distance, s_curse);
				addDesiredSkill(d_skill, target, distance, s_paralyze);
				addDesiredSkill(d_skill, target, distance, s_meteor);
				addDesiredSkill(d_skill, target, distance, s_fear2);
				addDesiredSkill(d_skill, target, distance, s_shock2);
				addDesiredSkill(d_skill, target, distance, s_breath);
				break;
			case 4:
				addDesiredSkill(d_skill, target, distance, s_curse);
				addDesiredSkill(d_skill, target, distance, s_paralyze);
				addDesiredSkill(d_skill, target, distance, s_meteor);
				addDesiredSkill(d_skill, target, distance, s_fear2);
				addDesiredSkill(d_skill, target, distance, s_shock2);
				addDesiredSkill(d_skill, target, distance, s_fear);
				addDesiredSkill(d_skill, target, distance, s_shock);
				addDesiredSkill(d_skill, target, distance, s_breath);
				break;
			default:
				break;
		}

		SkillEntry r_skill = selectTopSkill(d_skill);
		if(r_skill != null && !r_skill.getTemplate().isDebuff())
			target = actor;

		return chooseTaskAndTargets(r_skill, target, distance);
	}

	private int getAliveMinionsCount()
	{
		int count = 0;
		for(NpcInstance n : _minions)
		{
			if(!n.isDead())
				count++;
		}
		return count;
	}

	protected abstract Collection<Player> getPlayersInsideLair();

	protected abstract NpcInstance spawnMinion();
}