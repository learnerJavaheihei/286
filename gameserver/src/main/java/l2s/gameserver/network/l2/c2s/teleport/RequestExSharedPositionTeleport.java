package l2s.gameserver.network.l2.c2s.teleport;

import l2s.gameserver.Config;
import l2s.gameserver.instancemanager.ServerVariables;
import l2s.gameserver.listener.actor.player.OnAnswerListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.variables.PlayerVariables;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.c2s.L2GameClientPacket;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ConfirmDlgPacket;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.item.ItemTemplate;

public class RequestExSharedPositionTeleport extends L2GameClientPacket
{
	private int _allow, _tpId;
	
	@Override
	protected boolean readImpl()
	{
		_allow = readC();
		_tpId = readH();
		readC(); // ?
		return true;
	}

	@Override
	protected void runImpl()
	{
		if (_allow == 1)
		{
			Player player = getClient().getActiveChar();

			if (player.isInJail()) {
				player.sendMessage("监狱中不能使用共享传送!");
                return;
			}
			if (player.isInCombat()) {
				player.ask(new ConfirmDlgPacket(SystemMsg.S1, 0).addString("战斗状态无法传送!"), new OnAnswerListener() {
					@Override
					public void sayYes() {

					}

					@Override
					public void sayNo() {

					}
				});
				return;
			}
			if (player.getPvpFlag()>0) {
				player.ask(new ConfirmDlgPacket(SystemMsg.S1, 0).addString("紫名状态无法传送!"), new OnAnswerListener() {
					@Override
					public void sayYes() {

					}

					@Override
					public void sayNo() {

					}
				});
				return;
			}
			if (player.getKarma() < 0) {
				player.ask(new ConfirmDlgPacket(SystemMsg.S1, 0).addString("红名状态无法传送!"), new OnAnswerListener() {
					@Override
					public void sayYes() {

					}

					@Override
					public void sayNo() {

					}
				});
				return;
			}


			int previousRank = player.getPreviousPvpRank();
			boolean allowFree = false;
			
			ItemInstance l2coin = player.getInventory().getItemByItemId(ItemTemplate.ITEM_ID_MONEY_L);
			
			if ((previousRank > 0) && (previousRank < 4))
			{
				allowFree = true;
			}
				
			if (!allowFree && ((l2coin == null) || (l2coin.getCount() < 400)))
			{
				player.sendPacket(new SystemMessage(SystemMsg.NOT_ENOUGH_L2_COINS));
				return;
			}
			
			if (allowFree)
			{
				manageTeleport(player, true);
			}
			else if (player.getInventory().destroyItem(l2coin, 400))
			{
				manageTeleport(player, false);
			}
		}
	}
	
	private void manageTeleport(Player player, boolean free)
	{
		int x = ServerVariables.getInt("tpId_" + _tpId + "_x");
		int y = ServerVariables.getInt("tpId_" + _tpId + "_y");
		int z = ServerVariables.getInt("tpId_" + _tpId + "_z");
		System.out.println("tpId: " + _tpId + " x: " + x + " y: " + y + " z: " + z);
		player.teleToLocation(x, y, z);
		int tpCounts = player.getVarInt(PlayerVariables.SHARED_POSITION_TELEPORTS, Config.SHARED_TELEPORTS_PER_DAY) - 1;
		player.setVar(PlayerVariables.SHARED_POSITION_TELEPORTS, tpCounts);
		
		if (!free)
		{
			player.sendPacket(SystemMessagePacket.removeItems(ItemTemplate.ITEM_ID_MONEY_L, 400));
		}
	}
}