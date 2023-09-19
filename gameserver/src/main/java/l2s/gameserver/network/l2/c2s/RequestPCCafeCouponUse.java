package l2s.gameserver.network.l2.c2s;
import l2s.gameserver.Announcements;//修復網咖禮券 CDK
import l2s.gameserver.instancemanager.PCCafeCouponManager;//修復網咖禮券 CDK
import l2s.gameserver.model.Player;//修復網咖禮券 CDK

/**
 * format: chS
 */
public class RequestPCCafeCouponUse extends L2GameClientPacket
{
	// format: (ch)S
	private String _code;//修復網咖禮券 CDK

	@Override
	protected boolean readImpl()
	{
		_code = readS();//修復網咖禮券 CDK
		return true;
	}

	@Override
	protected void runImpl()
	{
	//修復網咖禮券 CDK--
		Player player = getClient().getActiveChar();
		if(player == null)
			return;
		PCCafeCouponManager.getInstance().requestEnterCode(player, _code);
	//--修復網咖禮券 CDK
	}
}