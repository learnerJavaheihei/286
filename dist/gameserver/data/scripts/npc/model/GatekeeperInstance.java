package npc.model;

import handler.bbs.custom.BBSConfig;
import l2s.commons.collections.MultiValueSet;
import l2s.commons.util.Rnd;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.MerchantInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.s2c.teleport.ExShowTeleportUi;
import l2s.gameserver.templates.npc.NpcTemplate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class GatekeeperInstance extends MerchantInstance {
	public GatekeeperInstance(int objectId, NpcTemplate template, MultiValueSet<String> set) {
		super(objectId, template, set);
	}

	@Override
	public void onTeleportRequest(Player talker) {
		talker.sendPacket(ExShowTeleportUi.STATIC);
	}

	@Override
	public void onBypassFeedback(Player player, String command){
		final String[] buypassOptions = command.split(" ");

		if(command.startsWith("Chat"))
		{
			//bypass -h npc_%objectId%_Chat 1
			showChatWindow(player, "teleporter/" + getNpcId() + "-" + buypassOptions[1] + ".htm", false);
		}
		//<Button ALIGN=LEFT ICON="TELEPORT" action="bypass -h npc?FindAndMove 25146">Lv.21 死靈 畢佛隆</Button>
		//bypass -h npc?FindAndMove
		else if(buypassOptions[0].equals("FindAndMove")) //是否需要費用在說。 這指令是針對首領
		{
			if(buypassOptions.length == 2)
			{
				if(checkUseCondition(player))//判斷是否可以傳送
				{
					List<NpcInstance> npcs;
					try
					{
						npcs = GameObjectsStorage.getNpcs(true, Integer.parseInt(buypassOptions[1]));
						if(!npcs.isEmpty())
						{
							teleportToCharacter(player, Rnd.get(npcs));
							return;
						}
					}
					catch (Exception e)
					{
					}
					player.sendMessage("首領未重生。");
				}
			}
		}
		//bypass -h npc?FindAndMove 25146 6525 300
		else if(buypassOptions[0].equals("MoveToPlace")) //是否需要費用在說。 這指令是針對首領
		{
			if(buypassOptions.length == 4)
			{
				if(checkUseCondition(player))//判斷是否可以傳送
				{
					int x = Integer.parseInt(buypassOptions[1]);
					int y = Integer.parseInt(buypassOptions[2]);
					int z = Integer.parseInt(buypassOptions[3]);
					//要設置無敵 明日在試試
					player.teleToLocation(x + Rnd.get(-50, 50), y + Rnd.get(-50, 50), z, player.getReflection());
				}
			}
		}
		else
			super.onBypassFeedback(player, command);
	}
	private void teleportToCharacter(Player activeChar, GameObject target)
	{
		if(target == null)
			return;
		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);

		//有加隨機xy坐標+-200
		activeChar.teleToLocation(target.getX() + Rnd.get(-200, 200), target.getY() + Rnd.get(-200, 200), target.getZ(), target.getReflection());

		activeChar.sendMessage("你已傳送到目標 " + target);
	}
	private boolean checkUseCondition(Player player)
	{
		if(player.getKarma() < 0)
		{
			//紅人不能傳送
			String html = HtmCache.getInstance().getHtml("teleporter/"+getNpcId()+"-teleports-pk.htm", player);
			HtmlMessage msg = new HtmlMessage(5);
			msg.setHtml(html);
			player.sendPacket(msg);
			player.sendActionFailed();
			return false;
		}

		if(player.getVar("jailed") != null)	// Если в тюрьме
			return false;

		if(player.isInTrainingCamp())
			return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_WHEN_DEAD)	// 死亡後允許使用自定義佈告欄
			if(player.isAlikeDead())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_IN_A_BATTLE)	// 允許戰鬥中使用自定義佈告欄
			if(player.isCastingNow() || player.isInCombat() || player.isAttackingNow())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_IN_PVP)	// 允許PVP狀態使用
			if(player.getPvpFlag() > 0)
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_IN_INVISIBLE)//允許玩家隱身時使用自定義佈告欄
			if(player.isInvisible(null))
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_ON_OLLYMPIAD)	// 允許在奧林匹亞競賽期間使用佈告欄
			if(player.isInOlympiadMode() || player.isInArenaObserverMode())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_IF_IN_VEHICLE)	// На корабле
			if(player.isInBoat())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_IF_MOUNTED)	// На ездовом животном
			if(player.isMounted())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_IF_CANNOT_MOVE)	// 允許角色無法移動狀態下(麻痺,睡眠,超重)
			if(player.isMovementDisabled())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_WHEN_IN_TRADE)	// 交易中是否允許使用
			if(player.isInStoreMode() || player.isInTrade() || player.isInOfflineMode())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_IF_TELEPORTING)	// 傳送中是否允許使用
			if(player.isLogoutStarted() || player.isTeleporting())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_IN_DUEL)	// 決鬥中是否允許使用
			if(player.isInDuel())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_WHEN_IS_PK)	// PK狀態是否允許使用
			if(player.isPK())
				return false;

		if(BBSConfig.CAN_USE_FUNCTIONS_CLAN_LEADERS_ONLY)	//
			if(!player.isClanLeader())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_ON_SIEGE)	// На осаждаемой территории
			if(player.isInSiegeZone())
				return false;

		if(BBSConfig.CAN_USE_FUNCTIONS_IN_PEACE_ZONE_ONLY)	// В мирной зоне
			if(!player.isInPeaceZone())
				return false;

		return true;
	}
	private void WitreFileTxt(String WriteFileName,String WiteTxt )
	{
		try
		{ // 防止檔案建立或讀取失敗，用catch捕捉錯誤並列印，也可以throw
			/* 寫入Txt檔案 */
			File writename = new File(WriteFileName); // 相對路徑，如果沒有則要建立一個新的output。txt檔案
			writename.createNewFile(); // 建立新檔案
			BufferedWriter out = new BufferedWriter(new FileWriter(writename));
			out.write(WiteTxt); // \r\n即為換行
			out.flush(); // 把快取區內容壓入檔案
			out.close(); // 最後記得關閉檔案
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
