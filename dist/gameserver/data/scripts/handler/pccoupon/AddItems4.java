package handler.pccoupon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbutils.DbUtils;
import l2s.commons.string.StringArrayUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.handler.pccoupon.PCCouponHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.utils.ItemFunctions;

/**
 * @author Bonux
**/
//如果你想新增類型4 ，複製這一個文件，然後下面 AddItemsOneTimes 改成 AddItemsOneTimes4 然後文件名也改成 AddItemsOneTimes4
//第31行也需要改成 4
public class AddItems4 extends ScriptPCCouponHandler//這一個是給玩家觀注公眾號用的
{
	private static final Logger _log = LoggerFactory.getLogger(PCCouponHandler.class);
	private static final String SELECT_PCCAFE_CODE = "SELECT serial_code FROM pccafe_coupons WHERE type= 4 and login=?";
	@Override
	public int getType()//這一個是
	{
		return 4;
	}

	@Override
	public boolean useCoupon(Player player, String value)
	{
		boolean success = false;
		if(!checkCanUse(player))
		{
			player.sendMessage("您已經領取推廣禮包。");//這一個是類型3出的顯示文字，加別的類型自行改改。
			return false;
		}
		long[][] items = StringArrayUtils.stringToLong2X(value, ";", "-");
		for(long[] item : items)
		{
			if(item.length == 0)
				continue;

			int itemId = (int) item[0];
			long count = item.length > 1 ? item[1] : 1L;
			if(!ItemFunctions.addItem(player, itemId, count, false).isEmpty())
			{
				success = true;
				SystemMessagePacket smsg;				
				if(count == 1)
				{
					smsg = new SystemMessagePacket(SystemMsg.YOU_HAVE_OBTAINED_S1);
					smsg.addItemName(itemId);
					player.sendPacket(smsg);
				}
				else
				{
					smsg = new SystemMessagePacket(SystemMsg.YOU_HAVE_OBTAINED_S2_S1);
					smsg.addItemName(itemId);
					smsg.addLong(count);
					player.sendPacket(smsg);
				}
				//player.sendPacket(new SystemMessagePacket(SystemMsg.CONGRATULATIONS_YOU_HAVE_RECEIVED_S1).addItemName(itemId));
			}
		}
		return success;
	}
	private boolean checkCanUse(Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_PCCAFE_CODE);
			statement.setString(1, player.getAccountName());
			rset = statement.executeQuery();
			if(rset.next())
			{
				return false;
			}
		}
		catch(Exception e)
		{
			_log.error(getClass().getSimpleName() + ": Error while use coupon code.", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return true;
	}
}
