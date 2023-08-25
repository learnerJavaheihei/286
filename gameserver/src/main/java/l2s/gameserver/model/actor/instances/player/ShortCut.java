package l2s.gameserver.model.actor.instances.player;

public class ShortCut
{
	public enum ShortCutType {
		/*0*/NONE,
		/*1*/ITEM,
		/*2*/SKILL,
		/*3*/ACTION,
		/*4*/MACRO,
		/*5*/RECIPE,
		/*6*/TPBOOKMARK;

		public static final ShortCutType[] VALUES = values();
	}

	// номера панельек для шарткатов
	public final static int PAGE_NORMAL_0 = 0;
	public final static int PAGE_NORMAL_1 = 1;
	public final static int PAGE_NORMAL_2 = 2;
	public final static int PAGE_NORMAL_3 = 3;
	public final static int PAGE_NORMAL_4 = 4;
	public final static int PAGE_NORMAL_5 = 5;
	public final static int PAGE_NORMAL_6 = 6;
	public final static int PAGE_NORMAL_7 = 7;
	public final static int PAGE_NORMAL_8 = 8;
	public final static int PAGE_NORMAL_9 = 9;
	public final static int PAGE_NORMAL_10 = 10;
	public final static int PAGE_NORMAL_11 = 11;
	public final static int PAGE_NORMAL_12 = 12;
	public final static int PAGE_NORMAL_13 = 13;
	public final static int PAGE_NORMAL_14 = 14;
	public final static int PAGE_NORMAL_15 = 15;
	public final static int PAGE_NORMAL_16 = 16;
	public final static int PAGE_NORMAL_17 = 17;
	public final static int PAGE_NORMAL_18 = 18;
	public final static int PAGE_NORMAL_19 = 19;
	public final static int PAGE_FLY_TRANSFORM = 20;
	public final static int PAGE_AIRSHIP = 21;
	public final static int PAGE_AUTOCONSUME = 22;
	public final static int PAGE_AUTOPLAY = 23;

	public final static int PAGE_MAX = PAGE_AUTOPLAY;

	private final int _slot;
	private final int _page;
	private final ShortCutType _type;
	private final int _id;
	private final int _level;
	private final int _characterType;

	public ShortCut(int slot, int page, ShortCutType type, int id, int level, int characterType)
	{
		_slot = slot;
		_page = page;
		_type = type;
		_id = id;
		_level = level;
		_characterType = characterType;
	}

	public int getSlot()
	{
		return _slot;
	}

	public int getPage()
	{
		return _page;
	}

	public ShortCutType getType()
	{
		return _type;
	}

	public int getId()
	{
		return _id;
	}

	public int getLevel()
	{
		return _type != ShortCutType.SKILL ? 0 : _level;
	}

	public int getCharacterType()
	{
		return _characterType;
	}

	@Override
	public String toString()
	{
		return "ShortCut: " + _slot + "/" + _page + " ( " + _type + "," + _id + "," + _level + "," + _characterType + ")";
	}
}