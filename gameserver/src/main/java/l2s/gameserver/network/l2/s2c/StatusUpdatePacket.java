package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.model.Creature;

public class StatusUpdatePacket extends L2GameServerPacket
{
	public static enum UpdateType
	{
		/*0*/DEFAULT,	// Первая посылка.
		/*1*/REGEN,	// Посылается при восстановлении HP/MP/CP.
		/*2*/ON_PLAYER_INIT,	// За одну сессию посылается 1 раз, с Cur HP, Max HP, Cur MP, Max MP, возможно при входе в игру.
		/*3*/CONSUME,	// Посылается при потреблении HP/MP (использование скилла и т.д.).
		/*4*/UNK_4,
		/*5*/UNK_5,
		/*6*/DAMAGED	// Посылается при нанесении урона DOT эффектами.
	}

	/**
	 * Даный параметр отсылается оффом в паре с MAX_HP
	 * Сначала CUR_HP, потом MAX_HP
	 */
	public final static int CUR_HP = 0x09;
	public final static int MAX_HP = 0x0a;

	/**
	 * Даный параметр отсылается оффом в паре с MAX_MP
	 * Сначала CUR_MP, потом MAX_MP
	 */
	public final static int CUR_MP = 0x0b;
	public final static int MAX_MP = 0x0c;

	/**
	 * Меняется отображение только в инвентаре, для статуса требуется UserInfo
	 */
	public final static int CUR_LOAD = 0x0e;

	/**
	 * Меняется отображение только в инвентаре, для статуса требуется UserInfo
	 */
	public final static int MAX_LOAD = 0x0f;

	public final static int PVP_FLAG = 0x1a;
	public final static int KARMA = 0x1b;

	/**
	 * Даный параметр отсылается оффом в паре с MAX_CP
	 * Сначала CUR_CP, потом MAX_CP
	 */
	public final static int CUR_CP = 0x21;
	public final static int MAX_CP = 0x22;

	private final UpdateType _updateType;
	private final int _objectId, _receiverId;

	private final List<Attribute> _attributes = new ArrayList<Attribute>();

	class Attribute
	{
		public final int id;
		public final int value;

		Attribute(int id, int value)
		{
			this.id = id;
			this.value = value;
		}
	}

	public StatusUpdatePacket(UpdateType updateType, Creature creature)
	{
		_updateType = updateType;
		_objectId = creature.getObjectId();
		_receiverId = 0;
	}

	public StatusUpdatePacket(UpdateType updateType, Creature creature, Creature receiver)
	{
		_updateType = updateType;
		_objectId = creature.getObjectId();
		_receiverId = receiver == null ? 0 : receiver.getObjectId();
	}

	public StatusUpdatePacket addAttribute(int id, int level)
	{
		_attributes.add(new Attribute(id, level));
		return this;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_objectId);
		writeD(_receiverId);
		writeC(_updateType.ordinal());
		writeC(_attributes.size());

		for(Attribute temp : _attributes)
		{
			writeC(temp.id);
			writeD(temp.value);
		}
	}

	public boolean hasAttributes()
	{
		return !_attributes.isEmpty();
	}
}