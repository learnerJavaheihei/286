package l2s.gameserver.botscript.actionhandler;

import l2s.gameserver.botscript.BotConfigImp;
import l2s.gameserver.core.BotConfig;
import l2s.gameserver.core.IBotActionHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.skills.SkillEntry;

public class BotHpMpShift implements IBotActionHandler
{
	@Override
	public boolean doAction(Player actor, BotConfig config, boolean isSitting, boolean movable, boolean simpleActionDisable)
	{
		if(!config.isHpmpShift())
		{
			return false;
		}
		if(isSitting || simpleActionDisable)
		{
			return false;
		}
		SkillEntry skill = actor.getKnownSkill(1157);
		/*技能ID1157 心靈轉換*/
		if(skill == null)
		{
			return false;
		}
		if(actor.getCurrentMpPercents() >= (double) ((BotConfigImp) config).getMpShiftPercent())
		{
			return false;
		}
		if(actor.getCurrentHpPercents() <= (double) ((BotConfigImp) config).getHpShiftPercent())
		{
			return false;
		}
		if(!skill.checkCondition((Creature) actor, (Creature) actor, false, false, true, false, false))
		{
			return false;
		}
		actor.getAI().Cast(skill, (Creature) actor, false, false);
		return true;
	}
}