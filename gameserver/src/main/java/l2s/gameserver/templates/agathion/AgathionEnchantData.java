package l2s.gameserver.templates.agathion;

import l2s.gameserver.skills.SkillEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bonux (bonuxq@gmail.com)
 * @date 27.07.2019
 **/
public class AgathionEnchantData {
	private final int level;
	private final List<SkillEntry> mainSkills = new ArrayList<>();
	private final List<SkillEntry> subSkills = new ArrayList<>();

	public AgathionEnchantData(int level) {
		this.level = level;
	}

	public int getLevel() {
		return level;
	}

	public List<SkillEntry> getMainSkills() {
		return mainSkills;
	}

	public List<SkillEntry> getSubSkills() {
		return subSkills;
	}
}
