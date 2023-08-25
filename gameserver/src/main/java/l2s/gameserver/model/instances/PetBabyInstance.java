package l2s.gameserver.model.instances;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

import l2s.commons.string.StringArrayUtils;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.PetDataHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.actor.instances.creature.AbnormalList;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.skills.EffectUseType;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.templates.skill.EffectTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PetBabyInstance extends PetInstance
{
	private static final Logger _log = LoggerFactory.getLogger(PetBabyInstance.class);

	private Future<?> _actionTask;
	private boolean _buffEnabled = true;

	private final TIntObjectMap<List<Skill>> _activeSkills = new TIntObjectHashMap<List<Skill>>();
	private final TIntObjectMap<List<Skill>> _buffSkills = new TIntObjectHashMap<List<Skill>>();
	//private final TIntObjectMap<List<Skill>> _mergedBuffSkills = new TIntObjectHashMap<List<Skill>>();

	public PetBabyInstance(int objectId, NpcTemplate template, Player owner, ItemInstance control, long exp)
	{
		super(objectId, template, owner, control, exp);
		parseSkills();
	}

	public PetBabyInstance(int objectId, NpcTemplate template, Player owner, ItemInstance control)
	{
		super(objectId, template, owner, control);
		parseSkills();
	}

	private void parseSkills()
	{
		for(int step = 0; step < 10; step++)
		{
			List<Skill> skills = _activeSkills.get(step);
			for(int buff = 1; buff < 10; buff++)
			{
				String data = getTemplate().getAIParams().getString("step" + step + "_skill0" + buff, null);
				if(data == null)
					break;

				if(skills == null)
				{
					skills = new ArrayList<Skill>();
					_activeSkills.put(step, skills);
				}

				int[][] skillsData = StringArrayUtils.stringToIntArray2X(data, ";", "-");
				for(int[] skillData : skillsData)
				{
					int skillLevel = skillData.length > 1 ? skillData[1] : 1;
					skills.add(SkillHolder.getInstance().getSkill(skillData[0], skillLevel));
				}
			}

			skills = _buffSkills.get(step);
			for(int buff = 1; buff < 10; buff++)
			{
				String data = getTemplate().getAIParams().getString("step" + step + "_buff0" + buff, null);
				if(data == null)
					break;

				if(skills == null)
				{
					skills = new ArrayList<Skill>();
					_buffSkills.put(step, skills);
				}

				int[][] skillsData = StringArrayUtils.stringToIntArray2X(data, ";", "-");
				for(int[] skillData : skillsData)
				{
					int skillLevel = skillData.length > 1 ? skillData[1] : 1;
					skills.add(SkillHolder.getInstance().getSkill(skillData[0], skillLevel));
				}
			}

			/*skills = _mergedBuffSkills.get(step);
			for(int buff = 1; buff < 10; buff++)
			{
				String data = getTemplate().getAIParams().getString("step" + step + "_merged_buff0" + buff, null);
				if(data == null)
					break;

				if(skills == null)
				{
					skills = new ArrayList<Skill>();
					_mergedBuffSkills.put(step, skills);
				}

				int[][] skillsData = StringArrayUtils.stringToIntArray2X(data, ";", "-");
				for(int[] skillData : skillsData)
				{
					int skillLevel = skillData.length > 1 ? skillData[1] : 1;
					skills.add(SkillHolder.getInstance().getSkill(skillData[0], skillLevel));
				}
			}*/
		}
	}

	// heal
	private static final int HealTrick = 4717;
	private static final int GreaterHealTrick = 4718;
	private static final int GreaterHeal = 5195;
	private static final int BattleHeal = 5590;
	private static final int Recharge = 5200;

	class ActionTask implements Runnable
	{
		@Override
		public void run()
		{
			Skill skill = onActionTask();
			_actionTask = ThreadPoolManager.getInstance().schedule(new ActionTask(), skill == null ? 1000 : Formulas.calcSkillCastSpd(PetBabyInstance.this, skill, skill.getHitTime()));
		}
	}

	@Override
	public List<Skill> getActiveSkills()
	{
		for(int step = getSteep(); step >= 0; step--)
		{
			List<Skill> skills = _activeSkills.get(step);
			if(skills != null)
				return skills;
		}
		return Collections.emptyList();
	}

	@Override
	public int getActiveSkillLevel(int skillId)
	{
		for(Skill skill : getActiveSkills())
		{
			if(skill.getId() == skillId)
				return skill.getLevel();
		}
		return super.getActiveSkillLevel(skillId);
	}

	public List<Skill> getBuffs()
	{
		for(int step = getSteep(); step >= 0; step--)
		{
			List<Skill> skills = _buffSkills.get(step);
			if(skills != null)
				return skills;
		}
		return Collections.emptyList();
	}

	private Skill getHealSkill(int hpPercent)
	{
		if(PetDataHolder.isImprovedBabyPet(getNpcId()))
		{
			if(hpPercent < 90)
			{
				if(hpPercent < 33)
				{
					Skill skill = SkillHolder.getInstance().getSkill(BattleHeal, 1);
					return SkillHolder.getInstance().getSkill(BattleHeal, Math.min(getSteep(), skill.getMaxLevel()));
				}
				else if(getNpcId() != PetDataHolder.IMPROVED_BABY_KOOKABURRA_ID)
				{
					Skill skill = SkillHolder.getInstance().getSkill(GreaterHeal, 1);
					return SkillHolder.getInstance().getSkill(GreaterHeal, Math.min(getSteep(), skill.getMaxLevel()));
				}
			}
		}
		else if(PetDataHolder.isBabyPet(getNpcId()))
		{
			if(hpPercent < 90)
			{
				if(hpPercent < 33)
				{
					Skill skill = SkillHolder.getInstance().getSkill(GreaterHealTrick, 1);
					return SkillHolder.getInstance().getSkill(GreaterHealTrick, Math.min(getSteep(), skill.getMaxLevel()));
				}
				else
				{
					Skill skill = SkillHolder.getInstance().getSkill(HealTrick, 1);
					return SkillHolder.getInstance().getSkill(HealTrick, Math.min(getSteep(), skill.getMaxLevel()));
				}
			}
		}
		else
		{
			switch(getNpcId())
			{
				case PetDataHolder.WHITE_WEASEL_ID:
				case PetDataHolder.TOY_KNIGHT_ID:
					if(hpPercent < 70)
					{
						if(hpPercent < 30)
						{
							Skill skill = SkillHolder.getInstance().getSkill(BattleHeal, 1);
							return SkillHolder.getInstance().getSkill(BattleHeal, Math.min(getSteep(), skill.getMaxLevel()));
						}
						else
						{
							Skill skill = SkillHolder.getInstance().getSkill(GreaterHeal, 1);
							return SkillHolder.getInstance().getSkill(GreaterHeal, Math.min(getSteep(), skill.getMaxLevel()));
						}
					}
					break;
				case PetDataHolder.FAIRY_PRINCESS_ID:
				case PetDataHolder.SPIRIT_SHAMAN_ID:
					if(hpPercent < 30)
					{
						Skill skill = SkillHolder.getInstance().getSkill(BattleHeal, 1);
						return SkillHolder.getInstance().getSkill(BattleHeal, Math.min(getSteep(), skill.getMaxLevel()));
					}
					break;
			}
		}
		return null;
	}

	private Skill getManaHealSkill(int mpPercent)
	{
		switch(getNpcId())
		{
			case PetDataHolder.IMPROVED_BABY_KOOKABURRA_ID:
				if(mpPercent < 66)
				{
					Skill skill = SkillHolder.getInstance().getSkill(Recharge, 1);
					return SkillHolder.getInstance().getSkill(Recharge, Math.min(getSteep(), skill.getMaxLevel()));
				}
				break;
			case PetDataHolder.FAIRY_PRINCESS_ID:
			case PetDataHolder.SPIRIT_SHAMAN_ID:
				if(mpPercent < 50)
				{
					Skill skill = SkillHolder.getInstance().getSkill(Recharge, 1);
					return SkillHolder.getInstance().getSkill(Recharge, Math.min(getSteep(), skill.getMaxLevel()));
				}
				break;
		}
		return null;
	}

	public Skill onActionTask()
	{
		try
		{
			Player owner = getPlayer();
			if(!owner.isDead() && !owner.isInvulnerable() && !isCastingNow())
			{
				if(getAbnormalList().contains(5753)) // Awakening
					return null;

				if(getAbnormalList().contains(5771)) // Buff Control
					return null;

				Skill skill = null;

				if(!Config.ALT_PET_HEAL_BATTLE_ONLY || owner.isInCombat())
				{
					// проверка лечения
					double curHp = owner.getCurrentHpPercents();
					if(Rnd.chance((100 - curHp) / 3))
						skill = getHealSkill((int) curHp);

					// проверка речарджа
					if(skill == null)
					{
						double curMp = owner.getCurrentMpPercents();
						if(Rnd.chance((100 - curMp) / 2))
							skill = getManaHealSkill((int) curMp);
					}

					SkillEntry skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.SERVITOR, skill);
					if(skillEntry != null && skillEntry.checkCondition(PetBabyInstance.this, owner, false, !isFollowMode(), true))
					{
						setTarget(owner);
						getAI().Cast(skillEntry, owner, false, !isFollowMode());
						return skill;
					}
				}

				if(owner.isInOfflineMode() || owner.getAbnormalList().contains(5771))
					return null;

				outer: for(Skill buff : getBuffs())
				{
					if(getCurrentMp() < buff.getMpConsume2())
						continue;

					for(Abnormal ef : owner.getAbnormalList()) {
						if (!ef.canReplaceAbnormal(skill, 10))
							continue outer;
					}

					SkillEntry skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.SERVITOR, buff);
					if(skillEntry.checkCondition(PetBabyInstance.this, owner, false, !isFollowMode(), true))
					{
						setTarget(owner);
						getAI().Cast(skillEntry, owner, false, !isFollowMode());
						return buff;
					}
					return null;
				}
			}
		}
		catch(Throwable e)
		{
			_log.warn("Pet [#" + getNpcId() + "] a buff task error has occurred: " + e);
			_log.error("", e);
		}
		return null;
	}

	public synchronized void stopBuffTask()
	{
		if(_actionTask != null)
		{
			_actionTask.cancel(false);
			_actionTask = null;
		}
	}

	public synchronized void startBuffTask()
	{
		if(_actionTask != null)
			stopBuffTask();

		if(_actionTask == null && !isDead())
			_actionTask = ThreadPoolManager.getInstance().schedule(new ActionTask(), 5000);
	}

	public boolean isBuffEnabled()
	{
		return _buffEnabled;
	}

	public void triggerBuff()
	{
		_buffEnabled = !_buffEnabled;
	}

	@Override
	protected void onDeath(Creature killer)
	{
		stopBuffTask();
		super.onDeath(killer);
	}

	@Override
	public void doRevive()
	{
		super.doRevive();
		startBuffTask();
	}

	@Override
	public void unSummon(boolean logout)
	{
		stopBuffTask();
		super.unSummon(logout);
	}

	public int getSteep()
	{
		if(PetDataHolder.isSpecialPet(getNpcId()))
		{
			if(getLevel() < 10)
				return 0;
			if(getLevel() < 20)
				return 1;
			if(getLevel() < 30)
				return 2;
			if(getLevel() < 40)
				return 3;
			if(getLevel() < 50)
				return 4;
			if(getLevel() < 60)
				return 5;
			if(getLevel() < 70)
				return 6;
			if(getLevel() >= 70)
				return 7;
		}
		else
		{
			if(getLevel() < 60)
				return 0;
			if(getLevel() < 65)
				return 1;
			if(getLevel() < 70)
				return 2;
			if(getLevel() < 75)
				return 3;
			if(getLevel() < 80)
				return 4;
			if(getLevel() >= 80)
				return 5;
		}
		return 0;
	}

	@Override
	public int getSoulshotConsumeCount()
	{
		return 1;
	}

	@Override
	public int getSpiritshotConsumeCount()
	{
		return 1;
	}
}