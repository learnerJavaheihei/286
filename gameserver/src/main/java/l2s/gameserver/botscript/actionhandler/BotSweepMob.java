package l2s.gameserver.botscript.actionhandler;

import l2s.gameserver.core.BotConfig;
import l2s.gameserver.core.BotThinkTask;
import l2s.gameserver.core.IBotActionHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.skills.SkillEntry;

public class BotSweepMob implements IBotActionHandler
{
	@Override
	public boolean doAction(Player actor, BotConfig config, boolean isSitting, boolean movable, boolean simpleActionDisable)
	{
		if(!config.isAutoSweep())
		{
			return false;
		}
		GameObject target = actor.getTarget();
		if(target == null || !target.isMonster())
		{
			return false;
		}
		MonsterInstance mob = this.getMonster(target);
		int mobId = mob.getObjectId();
		if(mobId == config.getLastSpoilTargeId())
		{
			return false;
		}
		if(!mob.isDead() || !mob.isSweepActive())
		{
			return false;
		}
		SkillEntry skill = actor.getKnownSkill(42);
		/*技能ID42 回收者*/
		if(skill == null)
		{
			return false;
		}
		if(!BotThinkTask.checkSkillMpCost(actor, skill))
		{
			return false;
		}
		if(!skill.checkCondition((Creature) actor, (Creature) mob, false, false, true, false, false))
		{
			return false;
		}
		actor.getAI().Cast(skill, (Creature) mob, false, false);
		return true;
	}
}