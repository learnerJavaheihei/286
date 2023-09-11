package l2s.gameserver.botscript.actionhandler;

import l2s.gameserver.core.BotConfig;
import l2s.gameserver.core.BotThinkTask;
import l2s.gameserver.core.IBotActionHandler;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.skills.SkillEntry;

public class BotAbsorbBody implements IBotActionHandler
{
	@Override
	public boolean doAction(Player actor, BotConfig config, boolean isSitting, boolean movable, boolean simpleActionDisable)
	{
		if(!config.isAbsorbBody())
		{
			return false;
		}
		if(isSitting || simpleActionDisable)
		{
			return false;
		}
		GameObject target = actor.getTarget();
		if(target == null)
		{
			return false;
		}
		MonsterInstance mob = this.getMonster(target);
		if(mob == null || !mob.isDead() || mob.isSweepActive())
		{
			return false;
		}
		SkillEntry skill = actor.getKnownSkill(1151);
		/*技能ID1151 死體能量吸收*/
		if(skill == null)
		{
			return false;
		}
		if(!BotThinkTask.checkSkillMpCost(actor, skill))
		{
			return false;
		}
		if(actor.getCurrentHpPercents() >= 99.0)
		{
			return false;
		}
		if(!skill.checkCondition(actor, mob, false, false, true, false, false))
		{
			return false;
		}
		actor.getAI().Cast(skill, mob, false, false);
		return true;
	}
}