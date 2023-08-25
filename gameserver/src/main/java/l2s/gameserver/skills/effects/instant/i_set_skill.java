package l2s.gameserver.skills.effects.instant;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
 */
public final class i_set_skill extends i_abstract_effect
{
	private final SkillEntry _skill;

	public i_set_skill(EffectTemplate template)
	{
		super(template);

		int[] skill = getParams().getIntegerArray("skill", "-");
		_skill = SkillEntry.makeSkillEntry(SkillEntryType.NONE, skill[0], skill.length >= 2 ? skill[1] : 1);
	}

	@Override
	protected boolean checkCondition(Creature effector, Creature effected)
	{
		return _skill != null;
	}

	@Override
	public void instantUse(Creature effector, Creature effected, boolean reflected)
	{
		Player player = effected.getPlayer();
		player.addSkill(_skill, true);
		player.updateStats();
		player.sendSkillList();
		player.updateSkillShortcuts(_skill.getId(), _skill.getLevel());
	}
}