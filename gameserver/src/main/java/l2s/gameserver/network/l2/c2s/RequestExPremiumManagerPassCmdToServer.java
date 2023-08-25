package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.PackageToListPacket;
import l2s.gameserver.utils.BypassStorage.BypassType;
import l2s.gameserver.utils.BypassStorage.ValidBypass;
import l2s.gameserver.utils.DimensionalMerchantUtils;
import l2s.gameserver.utils.WarehouseFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.StringTokenizer;

public class RequestExPremiumManagerPassCmdToServer extends L2GameClientPacket
{
	private static final Logger LOGGER = LoggerFactory.getLogger(RequestExPremiumManagerPassCmdToServer.class);

	private String _bypass = null;

	@Override
	protected boolean readImpl()
	{
		_bypass = readS();
		return true;
	}

	@Override
	protected void runImpl()
	{
		if(_bypass.isEmpty())
			return;

		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if(player.isInJail())
		{
			player.sendActionFailed();
			return;
		}

		ValidBypass bp = player.getBypassStorage().validate(_bypass);
		if(bp == null)
		{
			LOGGER.debug("RequestExPremiumManagerPassCmdToServer: Unexpected bypass : " + _bypass + " client : " + getClient() + "!");
			return;
		}

		if(bp.type != BypassType.ITEM)
			return;

		try
		{
			if(player.isGM())
				player.sendMessage("premium bypass:" + bp.bypass);

			if(bp.bypass.startsWith("menu_select?"))
			{
				String params = bp.bypass.substring(bp.bypass.indexOf("?") + 1);
				StringTokenizer st = new StringTokenizer(params, "&");
				int ask = Integer.parseInt(st.nextToken().split("=")[1]);
				long reply = st.hasMoreTokens() ? Long.parseLong(st.nextToken().split("=")[1]) : 0L;
				int state = st.hasMoreTokens() ? Integer.parseInt(st.nextToken().split("=")[1]) : 0;
				DimensionalMerchantUtils.onMenuSelect(null, player, ask, reply, state);
			}
			else if(bp.bypass.equals("package_deposit"))
			{
				player.sendPacket(new PackageToListPacket(player));
			}
			else if(bp.bypass.equals("package_withdraw"))
			{
				WarehouseFunctions.showFreightWindow(player);
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Error while handling bypass: " + bp.bypass, e);
		}
	}
}