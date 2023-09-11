package l2s.gameserver.core;

import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.skillclasses.Drain;
import l2s.gameserver.skills.skillclasses.MDam;
import l2s.gameserver.skills.skillclasses.PDam;

public class BotSkillStrategy
{
	private int skillId;
	private boolean checkOnTargetHp;
	private boolean oneTime;
	private int hpPercentCheck;
	private transient int lastTargetObjectId;
	private boolean baseSelfMpCheck;
	public boolean isIs_autoDetection() {
		return is_autoDetection;
	}

	public void setIs_autoDetection(boolean is_autoDetection) {
		this.is_autoDetection = is_autoDetection;
	}

	private boolean is_autoDetection = false;

	public BotSkillStrategy()
	{
	}

	public BotSkillStrategy(int skillId, boolean checkOnTargetHp, boolean oneTime, int hpPercentCheck)
	{
		this.skillId = skillId;
		this.checkOnTargetHp = checkOnTargetHp;
		this.oneTime = oneTime;
		this.hpPercentCheck = hpPercentCheck;
	}

	public boolean useMe(Player actor, MonsterInstance target)
	{
		SkillEntry skillEntry = actor.getKnownSkill(getSkillId());
		if(skillEntry == null)
			return false;
		if(actor.isSkillDisabled(skillEntry.getTemplate()))
			return false;
		boolean HpCheck = isBaseSelfMpCheck() ? actor.getCurrentMpPercents() >= getHpPercentCheck() : isCheckOnTargetHp() ? target.getCurrentHpPercents() <= getHpPercentCheck() : actor.getCurrentHpPercents() <= getHpPercentCheck();
		if(!HpCheck)
			return false;
		boolean oneTimeCheck = isOneTime() ? getLastTargetObjectId() != target.getObjectId() : true;
		if(!oneTimeCheck)
			return false;
		if(!BotThinkTask.checkSkillMpCost(actor, skillEntry))
			return false;
		boolean skillCastConditionCheck = skillEntry.checkCondition(actor, target, false, false, true);
		if(!skillCastConditionCheck)
		{
			return false;
		}
		else
		{
			if (isIs_autoDetection()) {
				// 判断 技能策略 中 红色技能的使用
				Skill skill = skillEntry.getTemplate();
				if(skill instanceof PDam || skill instanceof MDam)
				{
					return actor.getAI().Cast(skillEntry, target, false, false);
				}
				else if(skill.getAbnormalTime() > 0)
				{
					// 目标状态存在 玩家给予了的技能状态 就不使用该技能
					if (target.getAbnormalList()!=null && target.getAbnormalList().contains(skillEntry.getId())) {
						return false;
					}
					return actor.getAI().Cast(skillEntry, target, false, false);
				}
				else if(skill instanceof Drain)
				{
					actor.getAI().Cast(skillEntry, target, false, false);
					return true;
				}else
					actor.getAI().Cast(skillEntry, target, false, false);
			}else
				actor.getAI().Cast(skillEntry, target, false, false);

			return false;
		}

	}

	public int getSkillId()
	{
		return this.skillId;
	}

	public void setSkillId(int skillId)
	{
		this.skillId = skillId;
	}

	public boolean isCheckOnTargetHp()
	{
		return this.checkOnTargetHp;
	}

	public void setCheckOnTargetHp(boolean checkOnTargetHp)
	{
		this.checkOnTargetHp = checkOnTargetHp;
	}

	public boolean isOneTime()
	{
		return this.oneTime;
	}

	public void setOneTime(boolean oneTime)
	{
		this.oneTime = oneTime;
	}

	public int getLastTargetObjectId()
	{
		return this.lastTargetObjectId;
	}

	public void setLastTargetObjectId(int lastTargetObjectId)
	{
		this.lastTargetObjectId = lastTargetObjectId;
	}

	public int getHpPercentCheck()
	{
		return this.hpPercentCheck;
	}

	public void setHpPercentCheck(int hpPercentCheck)
	{
		this.hpPercentCheck = hpPercentCheck;
	}

	public String toString(Player activeChar)
	{
		if(this.isBaseSelfMpCheck())
		{
			return "[自身MP] >=" + this.getHpPercentCheck() + "%]" + (this.isOneTime() ? "[一次]" : "[重复]");
		}
		return String.valueOf(SkillHolder.getInstance().getSkill(this.getSkillId(), 1).getName(activeChar)) + (this.isCheckOnTargetHp() ? "[目标HP" : "[自身HP") + this.getHpPercentCheck() + "%]" + (this.isOneTime() ? "[一次]" : "[重复]");
	}

	public String toTableTd(Player activeChar)
	{
		Skill skill = SkillHolder.getInstance().getSkill(this.getSkillId(), 1);
		StringBuilder builder = new StringBuilder();
		String name = skill.getName(activeChar);
		String skillColor = "C2C2C2";
		if(skill instanceof PDam || skill instanceof MDam)
		{
			skillColor = "9AFF9A";
		}
		else if(skill.getAbnormalTime() > 0)
		{
			skillColor = "BF3EFF";
		}
		else if(skill instanceof Drain)
		{
			skillColor = "CD5B45";
		}
		builder.append("<td width=85>").append("<font color=").append(skillColor).append(">").append(name).append("</font>").append("</td>");
		builder.append("<td width=55 align=CENTER><font color=8DB6CD>").append(this.isCheckOnTargetHp() ? "\u76ee\u6807" : "\u81ea\u8eab").append(this.getHpPercentCheck()).append("%").append("</font></td>");
		/*\u76ee\u6807 目标	\u81ea\u8eab 自身*/
		builder.append("<td width=20 align=CENTER>").append(this.isOneTime() ? "<font color=00FF00>1</font>" : "<font color=LEVEL>\u221e</font>").append("</td>");
		/*\u221e ∞*/
		return builder.toString();
	}

	public int hashCode()
	{
		int prime = 31;
		int result = 1;
		result = 31 * result + (this.checkOnTargetHp ? 1231 : 1237);
		result = 31 * result + this.hpPercentCheck;
		result = 31 * result + this.lastTargetObjectId;
		result = 31 * result + (this.oneTime ? 1231 : 1237);
		result = 31 * result + this.skillId;
		return result;
	}

	public boolean equals(Object obj)
	{
		if(this == obj)
		{
			return true;
		}
		if(obj == null)
		{
			return false;
		}
		if(this.getClass() != obj.getClass())
		{
			return false;
		}
		BotSkillStrategy other = (BotSkillStrategy) obj;
		if(this.checkOnTargetHp != other.checkOnTargetHp)
		{
			return false;
		}
		if(this.hpPercentCheck != other.hpPercentCheck)
		{
			return false;
		}
		if(this.lastTargetObjectId != other.lastTargetObjectId)
		{
			return false;
		}
		if(this.oneTime != other.oneTime)
		{
			return false;
		}
		return this.skillId == other.skillId;
	}

	public boolean isBaseSelfMpCheck()
	{
		return this.baseSelfMpCheck;
	}

	public void setBaseSelfMpCheck(boolean baseSelfMpCheck)
	{
		this.baseSelfMpCheck = baseSelfMpCheck;
	}
}