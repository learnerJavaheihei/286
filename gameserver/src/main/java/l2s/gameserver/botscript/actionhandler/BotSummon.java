package l2s.gameserver.botscript.actionhandler;

import l2s.gameserver.botscript.BotConfigImp;
import l2s.gameserver.core.BotConfig;
import l2s.gameserver.core.BotThinkTask;
import l2s.gameserver.core.IBotActionHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.SummonInstance;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.stats.Stats;

import java.util.List;
import java.util.stream.Collectors;

public class BotSummon implements IBotActionHandler
{
	@Override
	public boolean doAction(Player actor, BotConfig config, boolean isSitting, boolean movable, boolean simpleActionDisable)
	{
		SummonInstance pet;
		if(simpleActionDisable || isSitting)
		{
			return false;
		}
		if(config.getSummonSkillId() != 0 && (pet = actor.getSummon()) == null)
		{
			SkillEntry skillEntry = actor.getKnownSkill(config.getSummonSkillId());
			if(skillEntry != null && BotThinkTask.checkSkillMpCost(actor, skillEntry) && skillEntry.checkCondition((Creature) actor, (Creature) actor, false, false, true, false, false))
			{
				actor.getAI().Cast(skillEntry, (Creature) actor, false, false);
			}
			return false;
		}
		int size = (int) actor.getStat().calc(Stats.CUBICS_LIMIT, 1.0);
		if(actor.getCubics().size() >= size)
		{
			return false;
		}
		List<Integer> cubicSkills = actor.getCubics().stream().map(cubic -> cubic.getSkill().getId()).collect(Collectors.toList());
		for(int cubicSkill : ((BotConfigImp) config).getAutoCubic())
		{
			SkillEntry skillEntry;
			if(cubicSkills.contains(cubicSkill) || (skillEntry = actor.getKnownSkill(cubicSkill)) == null || !BotThinkTask.checkSkillMpCost(actor, skillEntry) || !skillEntry.checkCondition((Creature) actor, (Creature) actor, false, false, true, false, false))
				continue;
			actor.getAI().Cast(skillEntry, (Creature) actor, false, false);
			return true;
		}
		return false;
	}
}