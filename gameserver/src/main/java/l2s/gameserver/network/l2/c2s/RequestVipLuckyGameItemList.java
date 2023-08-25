package l2s.gameserver.network.l2.c2s;

/**
 * @author Bonux
**/
public final class RequestVipLuckyGameItemList extends L2GameClientPacket
{
	private int _unk1;

	protected boolean readImpl()
	{
		_unk1 = readC();
		return true;
	}

	@Override
	protected void runImpl()
	{
		System.out.println("RequestVipLuckyGameItemList _unk1=" + _unk1);
	}
}