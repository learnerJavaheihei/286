package l2s.gameserver.skills.effects.instant;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.SkillCoolTimePacket;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
**/
public final class i_reset_skill_reuse extends i_abstract_effect
{
	private final int _skillId;

	public i_reset_skill_reuse(EffectTemplate template)
	{
		super(template);
		_skillId = getParams().getInteger("id");
	}

	@Override
	public void instantUse(Creature effector, Creature effected, boolean reflected)
	{
		SkillEntry skill = effected.getKnownSkill(_skillId);
		if(skill != null)
		{
			effected.enableSkill(skill.getTemplate());
			if(effected.isPlayer())
			{
				Player player = effected.getPlayer();
				player.sendPacket(new SkillCoolTimePacket(player));
			}
		}
	}
}