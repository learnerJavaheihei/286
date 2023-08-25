package l2s.gameserver.network.l2.c2s;

/**
 * @author Bonux
**/
public final class RequestVipLuckyGameBonus extends L2GameClientPacket
{
	private int _unk1;
	private int _unk2;

	protected boolean readImpl()
	{
		_unk1 = readC();
		_unk2 = readC();
		return true;
	}

	@Override
	protected void runImpl()
	{
		System.out.println("RequestVipLuckyGameBonus _unk1=" + _unk1 + ", _unk2=" + _unk2);
	}
}