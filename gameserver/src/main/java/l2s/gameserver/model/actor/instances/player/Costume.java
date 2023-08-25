package l2s.gameserver.model.actor.instances.player;

import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.templates.CostumeTemplate;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 24.05.2019
 * Developed for L2-Scripts.com
 **/
public class Costume {
	public static final int IS_LOCKED = 1 << 0;
	public static final int IS_NEW = 1 << 1;

	private final CostumeTemplate template;
	private int count;
	private int flags;

	public Costume(CostumeTemplate template, int count, int flags) {
		this.template = template;
		this.count = count;
		this.flags = flags;
	}

	public CostumeTemplate getTemplate() {
		return template;
	}

	public int getId() {
		return template.getId();
	}

	public SkillEntry getSkillEntry() {
		return template.getSkillEntry();
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getFlags() {
		return flags;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	public void setFlag(int flag, boolean value) {
		if (value) {
			flags |= flag;
		} else {
			flags ^= (flags & flag);
		}
	}

	public boolean isFlag(int flag) {
		return (flags & flag) == flag;
	}
}
