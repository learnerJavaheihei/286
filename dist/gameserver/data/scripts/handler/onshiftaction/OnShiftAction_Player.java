package handler.onshiftaction;

import l2s.gameserver.Config;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.handler.admincommands.impl.AdminEditChar;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.s2c.ExGMViewQuestItemListPacket;
import l2s.gameserver.network.l2.s2c.GMHennaInfoPacket;
import l2s.gameserver.network.l2.s2c.GMViewCharacterInfoPacket;
import l2s.gameserver.network.l2.s2c.GMViewItemListPacket;
import l2s.gameserver.network.l2.s2c.updatetype.InventorySlot;

/**
 * @author VISTALL
 * @date 2:51/19.08.2011
 */
public class OnShiftAction_Player extends ScriptOnShiftActionHandler<Player>
{
	@Override
	public Class<Player> getClazz()
	{
		return Player.class;
	}

	@Override
	public boolean call(Player p, Player player)
	{
		if(!player.getPlayerAccess().CanViewChar)
		{
			//顯示別人
			if(Config.SHOW_TARGET_PLAYER_INVENTORY_ON_SHIFT_CLICK)
			{
				String html = HtmCache.getInstance().getHtml("viewself/otherPlayers.htm", player);
//					player.sendPacket(new GMViewItemListPacket(2, p, items, items.length - questSize));
//					player.sendPacket(new ExGMViewQuestItemListPacket(1, p, items, questSize));
//					player.sendPacket(new ExGMViewQuestItemListPacket(2, p, items, questSize));
//					player.sendPacket(new GMHennaInfoPacket(p));
				ItemInstance[] paperdollItems = p.getInventory().getPaperdollItems();

				StringBuilder headBuilder = new StringBuilder();
				StringBuilder backBuilder = new StringBuilder();
				StringBuilder beltBuilder = new StringBuilder();
				StringBuilder shieldBuilder = new StringBuilder();
				StringBuilder weaponBuilder = new StringBuilder();

				StringBuilder accessory_LEARBuilder = new StringBuilder();
				StringBuilder accessory_REARBuilder = new StringBuilder();
				StringBuilder accessory_PENDANTBuilder = new StringBuilder();
				StringBuilder accessory_LFINGERBuilder = new StringBuilder();
				StringBuilder accessory_RFINGERBuilder = new StringBuilder();
				StringBuilder accessory_NECKBuilder = new StringBuilder();


				StringBuilder broochBuilder = new StringBuilder();
				StringBuilder rbraceletBuilder = new StringBuilder();
				StringBuilder rbracelet_DECOBuilder = new StringBuilder();

				StringBuilder lbraceletBuilder = new StringBuilder();
				StringBuilder lbracelet_AGATHIONBuilder = new StringBuilder();
				StringBuilder armor_LEGSBuilder = new StringBuilder();
				StringBuilder armor_GLOVESBuilder = new StringBuilder();
				StringBuilder armor_FEETBuilder = new StringBuilder();
				StringBuilder armor_HEADBuilder = new StringBuilder();
				StringBuilder armor_CHESTBuilder = new StringBuilder();
				int JEWEL_nums = 0;
				int DECO_nums = 0;
				int AGATHION_nums = 0;

				for (ItemInstance paperdollItem : paperdollItems) {
					if (paperdollItem == null)
						continue;
					int slot = paperdollItem.getEquipSlot();

					// 下半身
					if (slot == InventorySlot.LEGS.getSlot()) {
						addIcon(armor_LEGSBuilder, paperdollItem,p);
					}
					// 手套
					else if(slot == InventorySlot.GLOVES.getSlot()){
						addIcon(armor_GLOVESBuilder, paperdollItem,p);
					}
					// 靴子
					else if(slot == InventorySlot.FEET.getSlot()){
						addIcon(armor_FEETBuilder, paperdollItem,p);
					}
					// 头盔
					else if(slot == InventorySlot.HEAD.getSlot()){
						addIcon(armor_HEADBuilder, paperdollItem,p);
					}
					// 衣服
					else if(slot == InventorySlot.CHEST.getSlot()){
						addIcon(armor_CHESTBuilder, paperdollItem,p);
					}
					// 头饰
					else if(slot == InventorySlot.HAIR.getSlot()){
						addIcon(headBuilder, paperdollItem,p);

					}
					// 披风
					else if(slot == InventorySlot.CLOAK.getSlot()){
						addIcon(backBuilder, paperdollItem,p);

					}
					// 腰带
					else if(slot == InventorySlot.BELT.getSlot()){
						addIcon(beltBuilder, paperdollItem,p);

					}
					// 武器
					else if(slot == InventorySlot.LRHAND.getSlot() || slot == InventorySlot.RHAND.getSlot()){
						addIcon(weaponBuilder, paperdollItem,p);

					}
					// 盾牌
					else if(slot == InventorySlot.LHAND.getSlot()){
						addIcon(shieldBuilder, paperdollItem,p);

					}
					// 耳环
					else if(slot == InventorySlot.LEAR.getSlot()){
						addIcon(accessory_LEARBuilder, paperdollItem,p);

					}
					// 耳环
					else if(slot == InventorySlot.REAR.getSlot()){
						addIcon(accessory_REARBuilder, paperdollItem,p);

					}
					// 坠饰
					else if(slot == InventorySlot.PENDANT.getSlot()){

						addIcon(accessory_PENDANTBuilder, paperdollItem,p);

					}
					// 戒指
					else if(slot == InventorySlot.LFINGER.getSlot()){
						addIcon(accessory_LFINGERBuilder, paperdollItem,p);

					}
					// 戒指
					else if(slot == InventorySlot.RFINGER.getSlot()){
						addIcon(accessory_RFINGERBuilder, paperdollItem,p);

					}
					// 项链
					else if(slot == InventorySlot.NECK.getSlot()){
						addIcon(accessory_NECKBuilder, paperdollItem,p);

					}
					// 胸针
					else if(slot == InventorySlot.BROOCH.getSlot()){
						addIcon(broochBuilder, paperdollItem,p);

					}
					// 右手镯
					else if(slot == InventorySlot.RBRACELET.getSlot()){
						addIcon(rbraceletBuilder, paperdollItem,p);

					}
					// 护符
					else if(slot == InventorySlot.DECO1.getSlot()|| slot == InventorySlot.DECO2.getSlot() || slot == InventorySlot.DECO3.getSlot() || slot == InventorySlot.DECO4.getSlot() || slot == InventorySlot.DECO5.getSlot() || slot == InventorySlot.DECO6.getSlot()){
						DECO_nums++;
						if(DECO_nums == 4){
							rbracelet_DECOBuilder.append("<==>");
						}
						rbracelet_DECOBuilder.append("<td FIXWIDTH=\"90\" valign=\"top\" align=center>");
						addIcon(rbracelet_DECOBuilder, paperdollItem,p);
						rbracelet_DECOBuilder.append("</td>");
					}
					// 左手镯
					else if(slot == InventorySlot.LBRACELET.getSlot()){
						addIcon(lbraceletBuilder, paperdollItem,p);

					}
					// 壶精
					else if(slot == InventorySlot.AGATHION_MAIN.getSlot()|| slot == InventorySlot.AGATHION_1.getSlot() || slot == InventorySlot.AGATHION_2.getSlot() || slot == InventorySlot.AGATHION_3.getSlot() || slot == InventorySlot.AGATHION_4.getSlot()){
						AGATHION_nums++;
						if(AGATHION_nums == 4){
							lbracelet_AGATHIONBuilder.append("<==>");
						}
						lbracelet_AGATHIONBuilder.append("<td FIXWIDTH=\"90\" valign=\"top\" align=center>");
						addIcon(lbracelet_AGATHIONBuilder, paperdollItem,p);
						lbracelet_AGATHIONBuilder.append("</td>");
					}
				}
				html = html.replace("%playerName%",p.getName());
				html = html.replace("%head%",headBuilder.toString().isEmpty()?"未装备":headBuilder.toString());
				html = html.replace("%back%",backBuilder.toString().isEmpty()?"未装备":backBuilder.toString());
				html = html.replace("%belt%",beltBuilder.toString().isEmpty()?"未装备":beltBuilder.toString());
				html = html.replace("%shield%",shieldBuilder.toString().isEmpty()?"未装备":shieldBuilder.toString());
				html = html.replace("%weapon%",weaponBuilder.toString().isEmpty()?"未装备":weaponBuilder.toString());

				html = html.replace("%armor_CHEST%",armor_CHESTBuilder.toString().isEmpty()?"衣服未装备":armor_CHESTBuilder.toString());
				html = html.replace("%armor_HEAD%",armor_HEADBuilder.toString().isEmpty()?"头饰未装备":armor_HEADBuilder.toString());
				html = html.replace("%armor_FEET%",armor_FEETBuilder.toString().isEmpty()?"靴子未装备":armor_FEETBuilder.toString());
				html = html.replace("%armor_GLOVES%",armor_GLOVESBuilder.toString().isEmpty()?"手套未装备":armor_GLOVESBuilder.toString());
				html = html.replace("%armor_LEGS%",armor_LEGSBuilder.toString().isEmpty()?"下身未装备":armor_LEGSBuilder.toString());

				html = html.replace("%accessory_LEAR%",accessory_LEARBuilder.toString().isEmpty()?"左耳环未装备":accessory_LEARBuilder.toString());
				html = html.replace("%accessory_REAR%",accessory_REARBuilder.toString().isEmpty()?"右耳环未装备":accessory_REARBuilder.toString());
				html = html.replace("%accessory_PENDANT%",accessory_PENDANTBuilder.toString().isEmpty()?"坠饰未装备":accessory_PENDANTBuilder.toString());
				html = html.replace("%accessory_LFINGER%",accessory_LFINGERBuilder.toString().isEmpty()?"左戒指未装备":accessory_LFINGERBuilder.toString());
				html = html.replace("%accessory_RFINGER%",accessory_RFINGERBuilder.toString().isEmpty()?"右戒指未装备":accessory_RFINGERBuilder.toString());
				html = html.replace("%accessory_NECK%",accessory_NECKBuilder.toString().isEmpty()?"项链未装备":accessory_NECKBuilder.toString());

				html = html.replace("%BROOCH%",broochBuilder.toString().isEmpty()?"未装备":broochBuilder.toString());
				String line = "<td FIXWIDTH=\"90\" valign=\"top\" align=center></td>";
				StringBuilder lines = new StringBuilder();
				if (DECO_nums > 0 && DECO_nums <= 3) {
					for (int i = 1; i <= (3-DECO_nums); i++) {
						lines.append(line);
					}
					html = html.replace("%DECO%","<tr>" +
							rbracelet_DECOBuilder.toString()+lines+
							"</tr>");
				}else if(DECO_nums >3){
					for (int i = 1; i <= (3-(DECO_nums-3)); i++) {
						lines.append(line);
					}
					String deco = rbracelet_DECOBuilder.toString();
					String[] split = deco.split("<==>");
					if (split.length>1) {
						html = html.replace("%DECO%","<tr>" +split[0]+"</tr><tr>"+split[1]+lines+"</tr>");
					}
				}else if(DECO_nums == 0){
					html = html.replace("%DECO%","未装备护符");
				}

				lines.setLength(0);
				if (AGATHION_nums > 0 && AGATHION_nums <= 3) {

					for (int i = 1; i <= (3-AGATHION_nums); i++) {
						lines.append(line);
					}

					html = html.replace("%AGATHION%","<tr>" +
							lbracelet_AGATHIONBuilder.toString()+lines+
							"</tr>");
				}else if(AGATHION_nums >3){
					for (int i = 1; i <= (3-(AGATHION_nums-3)); i++) {
						lines.append(line);
					}
					String agathion = lbracelet_AGATHIONBuilder.toString();
					String[] split = agathion.split("<==>");
					if (split.length>1) {
						html = html.replace("%AGATHION%","<tr>" +split[0]+"</tr><tr>"+split[1]+lines+"</tr>");
					}
				}else if(AGATHION_nums == 0){
					html = html.replace("%AGATHION%","未装备壶精");
				}
				html = html.replace("%R_BRACELET%",rbraceletBuilder.toString().isEmpty()?"未装备":rbraceletBuilder.toString());
				html = html.replace("%L_BRACELET%",lbraceletBuilder.toString().isEmpty()?"未装备":lbraceletBuilder.toString());
				HtmlMessage msg = new HtmlMessage(5);
				msg.setHtml(html);
				player.sendPacket(msg);
				player.sendActionFailed();
			}

			return false;
		}

		AdminEditChar.showCharacterList(player, p);
		return true;
	}
	private void addIcon(StringBuilder accessoryBuilder, ItemInstance paperdollItem,Player target) {
		accessoryBuilder.append(getStr(paperdollItem,target));
		accessoryBuilder.append(paperdollItem.getName(target)+"<br>");
	}
	private String getStr(ItemInstance paperdollItem,Player target) {
		return "<table " +
				"width=\"34\" height=\"34\" border=\"0\"" +
				"cellpadding=\"0\" cellspacing=\"0\">" +
				"<tr>" +
				"<td align=\"center\"><button " +
				"value=\" \" " +
				"action=\" \"" +
				"width=\"34\" height=\"34\" " +
				"back=\"L2UI_CH3.inventory_outline_over\" " +
				"fore=\"L2UI_CH3.inventory_outline\" " +
				"itemtooltip=\""+paperdollItem.getItemId()+"\">"+
				"</td> " +
				"</tr> " +
				"</table>";
	}
	private static void showPage(Player player, String page)
	{
		String html = HtmCache.getInstance().getHtml("viewself/" + page, player);
		HtmlMessage msg = new HtmlMessage(5);
		msg.setHtml(html);
		player.sendPacket(msg);
		player.sendActionFailed();
	}
}
