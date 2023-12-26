package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.utils.DimensionalMerchantUtils;

public class RequestExPurchaseLimitShopHtmlOpen extends L2GameClientPacket
{
	private int _type;

	@Override
	protected boolean readImpl()
	{
		_type = readC();
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

//		if(player.isInJail())
//		{
//			player.sendActionFailed();
//			return;
//		}

		switch (_type)
		{
			case 5: // Dimensional Merchant
			{
				DimensionalMerchantUtils.showLimitShopHtml(player, "e_premium_manager001.htm", true);
				break;
			}
		}
	}
}
