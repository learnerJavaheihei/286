package l2s.gameserver.model.actor.instances.player;

import java.util.concurrent.atomic.AtomicInteger;

public class Macro
{
	public enum MacroCmdType {
		/*0*/NONE,
		/*1*/SKILL,
		/*2*/ACTION,
		/*3*/TEXT,
		/*4*/SHORTCUT,
		/*5*/ITEM,
		/*6*/DELAY;

		public static final MacroCmdType[] VALUES = values();
	}

	public static class L2MacroCmd
	{
		private final int entry;
		private final int type;
		private final int param1; // skill_id or page for shortcuts
		private final int param2; // shortcut
		private final String cmd;

		public L2MacroCmd(int entry, int type, int param1, int param2, String cmd)
		{
			this.entry = entry;
			this.type = type;
			this.param1 = param1;
			this.param2 = param2;
			this.cmd = cmd;
		}

		public int getEntry() {
			return entry;
		}

		public int getType() {
			return type;
		}

		public int getParam1() {
			return param1;
		}

		public int getParam2() {
			return param2;
		}

		public String getCmd() {
			return cmd;
		}
	}

	private final AtomicInteger id;
	private final int icon;
	private final String name;
	private final String descr;
	private final String acronym;
	private final L2MacroCmd[] commands;
	private final boolean enabled;

	public Macro(int id, int icon, String name, String descr, String acronym, L2MacroCmd[] commands, boolean enabled)
	{
		this.id = new AtomicInteger(id);
		this.icon = icon;
		this.name = name;
		this.descr = descr;
		this.acronym = acronym.length() > 4 ? acronym.substring(0, 4) : acronym;
		this.commands = commands;
		this.enabled = enabled;
	}

	public Macro(int id, int icon, String name, String descr, String acronym, L2MacroCmd[] commands)
	{
		this(id, icon, name, descr, acronym, commands, true);
	}

	public int getId() {
		return id.get();
	}

	public void incrementId() {
		id.incrementAndGet();
	}

	public int getIcon() {
		return icon;
	}

	public String getName() {
		return name;
	}

	public String getDescr() {
		return descr;
	}

	public String getAcronym() {
		return acronym;
	}

	public L2MacroCmd[] getCommands() {
		return commands;
	}

	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public String toString()
	{
		return "macro id=" + id + " icon=" + icon + "name=" + name + " descr=" + descr + " acronym=" + acronym + " commands=" + commands;
	}
}
