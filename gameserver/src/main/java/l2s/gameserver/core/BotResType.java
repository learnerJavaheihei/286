package l2s.gameserver.core;

public enum BotResType
{
	BLESSED("祝福的复活卷"),
	MAGIC("返生术"),
	DEFAULT("复活卷");

	String _name;

	private BotResType(String name)
	{
		_name = name;
	}

	public String getName()
	{
		return _name;
	}
}