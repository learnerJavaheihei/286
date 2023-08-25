package l2s.gameserver.network.l2.s2c;

public class ExBR_BuyProductPacket extends L2GameServerPacket
{
	public static final L2GameServerPacket RESULT_OK = new ExBR_BuyProductPacket(1); // ok
	public static final L2GameServerPacket RESULT_NOT_ENOUGH_POINTS = new ExBR_BuyProductPacket(-1);
	public static final L2GameServerPacket RESULT_WRONG_PRODUCT = new ExBR_BuyProductPacket(-2); // also -5
	public static final L2GameServerPacket RESULT_INVENTORY_FULL = new ExBR_BuyProductPacket(-4);
	public static final L2GameServerPacket RESULT_WRONG_ITEM = new ExBR_BuyProductPacket(-5);
	public static final L2GameServerPacket RESULT_SALE_PERIOD_ENDED = new ExBR_BuyProductPacket(-7); // also -8
	public static final L2GameServerPacket RESULT_WRONG_USER_STATE = new ExBR_BuyProductPacket(-9); // also -11
	public static final L2GameServerPacket RESULT_WRONG_PRODUCT_ITEM = new ExBR_BuyProductPacket(-10);
	public static final L2GameServerPacket RESULT_WRONG_DAY_OF_WEEK = new ExBR_BuyProductPacket(-12);
	public static final L2GameServerPacket RESULT_WRONG_SALE_PERIOD = new ExBR_BuyProductPacket(-13);
	public static final L2GameServerPacket RESULT_ITEM_WAS_SALED = new ExBR_BuyProductPacket(-14);
	public static final L2GameServerPacket RESULT_RECIPIENT_DOESNT_EXIST = new ExBR_BuyProductPacket(-17);
	public static final L2GameServerPacket RESULT_CAN_NOT_SEND_PACKAGE_TO_YOURSELF = new ExBR_BuyProductPacket(-18);
	// -19 - Вы превысили лимит почты (240 шт.), поэтому отправка невозможна.
	// -20 - У получателя переполнен почтовый ящик (240 ед.), поэтому отправка невозможна.
	public static final L2GameServerPacket RESULT_BLOCKED_YOU_YOU_CANNOT_SEND_MAIL = new ExBR_BuyProductPacket(-21);
	// -22 - Можно подарить максимум 8 типов предметов неограниченного количества.\\nПроверьте количество.\
	// -23 - Данный предмет нельзя подарить.
	// -24 - Невозможно приобрести: несоответствие уровня.
	public static final L2GameServerPacket RESULT_NOT_ENOUGH_ADENA = new ExBR_BuyProductPacket(-25);
	public static final L2GameServerPacket RESULT_NOT_ENOUGH_FREE_COINS = new ExBR_BuyProductPacket(-26);
	// -27 - Не выполнено условие даты создания персонажа, покупка невозможна.
	public static final L2GameServerPacket RESULT_ITEM_LIMITED = new ExBR_BuyProductPacket(-28);	// При покупке количество этих предметов на один аккаунт ограничено. Куплено максимальное количество, больше купить нельзя.

	private final int _result;

	public ExBR_BuyProductPacket(int result)
	{
		_result = result;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_result);
	}
}