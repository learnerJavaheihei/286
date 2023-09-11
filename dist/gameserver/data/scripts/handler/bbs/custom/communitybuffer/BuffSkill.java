package handler.bbs.custom.communitybuffer;

import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Skill;
import l2s.gameserver.utils.SkillUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 06.03.2019
 * Developed for L2-Scripts.com
 **/
public class BuffSkill {
	private static final Logger LOGGER = LoggerFactory.getLogger(CommunityBufferDAO.class);

	private final Skill skill;
	private final double timeModifier;
	private final int timeAssign;
	private final boolean premium;

	private BuffSkill(Skill skill, double timeModifier, int timeAssign, boolean premium) {
		this.skill = skill;
		this.timeModifier = timeModifier;
		this.timeAssign = timeAssign;
		this.premium = premium;
	}

	public Skill getSkill() {
		return skill;
	}

	public int getId() {
		return skill.getId();
	}

	public int getLevel() {
		return skill.getLevel();
	}

	public double getTimeModifier() {
		return timeModifier;
	}

	public int getTimeAssign() {
		return timeAssign;
	}

	public boolean isPremium() {
		return premium;
	}

	public static BuffSkill makeBuffSkill(int id, int level, double timeModifier, int timeAssign, boolean premium) {
		if(level <= 0) {
			Skill skill = SkillHolder.getInstance().getSkill(id, 1);
			if(skill == null) {
				LOGGER.warn("BuffSkill: Error while make buff skill. Cannot find skill ID[" + id + "], LEVEL[1]!");
				return null;
			}
			level = skill.getMaxLevel();
		}

		Skill skill = SkillHolder.getInstance().getSkill(id, level);
		if(skill == null) {
			LOGGER.warn("BuffSkill: Error while make buff skill. Cannot find skill ID[" + id + "], LEVEL[" + level + "]!");
			return null;
		}

		return new BuffSkill(skill, timeModifier, timeAssign, premium);
	}
}
