package l2s.gameserver.utils;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.botscript.BotControlPage;
import l2s.gameserver.dao.ConsignmentSalesDao;
import l2s.gameserver.dao.GoldCoinTransactionRecordDao;
import l2s.gameserver.dao.ItemsDAO;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.data.xml.holder.ZoneHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.instancemanager.RaidBossSpawnManager;
import l2s.gameserver.listener.actor.player.OnAnswerListener;
import l2s.gameserver.model.*;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.mail.Mail;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ConfirmDlgPacket;
import l2s.gameserver.templates.ZoneTemplate;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.npc.NpcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class MyUtilsFunction {
    private static final Logger _log = LoggerFactory.getLogger(Files.class);
    public MyUtilsFunction() {

    }
    private static final int MemberCoins = 29520;//會員幣編號

    static final String[][] ItemId = {
            // 第一個是幣數量  第二個是物品及數量  第三個是等級限制 第四個是購買次數 輸入999 表示可買999 也等於無限次數了
            {"120", "29585,1;49518,5;29009,5;34603,20000;34610,10000", "85", "1"}, //這一個是鎖引0
            {"350", "29595,1;70106,1;90836,1;90885,1;34604,40000;34611,20000", "85", "1"},//這一個是鎖引1
    };
    private static int[] Boss20 = new int[]{25146, 25357, 25366, 25001, 25127, 25019, 25076, 25149, 25166, 25369, 25365, 25038, 25272, 25095};
    private static int[] Boss30 = new int[]{25004, 25079, 25112, 25169, 25352, 25392, 25401, 25128, 25391, 25020, 25023, 25041, 25063, 25098, 25118, 25152, 25223, 25354, 25388, 25398, 25385, 25170, 25082};
    private static int[] Boss40 = new int[]{25064, 25134, 25155, 25410, 25415, 25007, 25088, 25192, 25085, 25431, 25438, 25057, 25102, 25173, 25260, 25395, 25437, 25441, 25498, 25044, 25412, 25158, 25026, 25047, 25456};
    private static int[] Boss50 = new int[]{25013, 25119, 25131, 25217, 25050, 25460, 25067, 25473, 25159, 25010, 25070, 25103, 25137, 25744, 25176, 25241, 25434, 25475, 25745, 25122, 25099, 25463, 25230, 25418, 25420, 25089};
    private static int[] Boss60 = new int[]{25234, 25407, 25106, 25256, 25746, 25747, 25748, 25749, 25750, 25751, 25423, 25226, 25125, 25051, 25255, 25478, 25754, 25755, 25756, 25757, 25758, 25759, 25760, 25761, 25762, 25763, 25263};
    private static int[] Boss70 = new int[]{25035, 25163, 25252, 25453, 25766, 25767, 25768, 25769, 25770, 25772, 25773, 25774, 25775, 25776, 25777, 25738, 25779, 25780, 25781, 25782, 25783, 25784, 25739, 25742, 25743};
    private static int[] Boss80 = new int[]{25140, 25162, 25467, 25470, 25073, 25109, 25054, 25092, 25126, 25143, 25220, 25444, 25447, 25450};
    static DateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm");
    /*-------------------------------金币寄售系统 常量-----------------------------start----*/
    // 金币寄售系统 购买金币 每页显示个数
    static int buyPageSize = 12;
    // 金币寄售系统 金币上架 每页显示个数
    static int sellPageSize = 8;
    // 赞助币
    static int memberGoldId = 39508;
    // 金币
    static int _GOLD = 57;
    // 单个玩家寄售限制数量
    static int limit_player = 10;
    // 总寄售限制
    static int Total_consignment_limit = 99999;
    // 金币单位
    static int gold_unit = 10000;
    // 金币最大输入值
    static long max_gold_input = 9999999999L;
    // 赞助币最大输入值
    static int max_memberGold_input = 99999;
    // 过期时长
    public static long outTime = 72 * 60 * 60 * 1000L;

    /*-------------------------------金币寄售系统 常量----------------------------- end ----*/
    public static void onBypassFeedback(Player player, String inputString) {
        final String[] buypassOptions = inputString.split(" ");
        String page = "welcome.htm";
        if (buypassOptions[buypassOptions.length - 1].contains("htm")) {
            page = buypassOptions[buypassOptions.length - 1];
        }
        if (inputString.startsWith("intoGoldSellSystem")) {
            String[] split = inputString.split(" ");
            //长页面
            HtmlMessage msg = new HtmlMessage(0);
            msg.setItemId(-1);

            String html = "";
            if (split.length == 4 || split.length == 5) {
                if ("buy".equals(split[1])) {
                    html = HtmCache.getInstance().getHtml("scripts/handler/bbs/pages/gold_consignment_sell_index.htm", player);
                    long countOf = player.getInventory().getCountOf(memberGoldId);

                    StringBuilder builder = new StringBuilder();
                    List<Map<String, Object>> maps = null;

                    if (ConsignmentSalesDao.maps == null || ConsignmentSalesDao.maps.size() == 0) {
                        maps = ConsignmentSalesDao.getInstance().selectAllList();
                    } else
                        maps = ConsignmentSalesDao.maps;

                    int pageIndex = 1;
                    int nums = (maps == null ? 0 : maps.size());
                    // 点击时的 总页数
                    int pages = nums % buyPageSize == 0 ? nums / buyPageSize : nums / buyPageSize + 1;
                    if (split.length == 4) {
                        pageIndex = Integer.parseInt(split[3]);
                    } else if ("prev".equals(split[4])) {
                        pageIndex = Math.max(Integer.parseInt(split[3]) - 1, 1);
                    } else if ("next".equals(split[4])) {
                        pageIndex = Math.min(Integer.parseInt(split[3]) + 1, pages);
                    }
                    // 当前页数
                    int pageStart = pageIndex > 0 ? (pageIndex - 1) * buyPageSize : 0; //6
                    int pageEnd = (nums - (pageIndex - 1) * buyPageSize) <= buyPageSize ? (nums - (pageIndex - 1) * buyPageSize) + pageStart - 1 : pageStart + buyPageSize - 1; //

                    if (maps == null || maps.size() == 0) {
                        html = html.replace("%sell_table_data%", "当前没有玩家寄售金币");
                    } else {
                        maps = maps.stream().sorted(new Comparator<Map<String, Object>>() {

                            @Override
                            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                                int price1 = (int) o1.get("unit_price");
                                int price2 = (int) o2.get("unit_price");
                                return Integer.compare(price2, price1);
                            }
                        }).collect(Collectors.toList());

                        List<Map<String, Object>> subList = maps.subList(pageStart, pageEnd + 1);
                        for (int i = 0; i < subList.size(); i++) {
                            String buyButton = "<a action=\"bypass -h MyUtils_intoGoldSellSystem doBuy_" + subList.get(i).get("order_number") + " page 1\">购买</a>";
                            builder.append("<table border=0 cellspacing=1 cellpadding=0 width=290 height=30 bgcolor=").append((i % 2) == 1 ? "1a1914" : "23221d").append(">");
                            builder.append("<tr><td width=290 height=25><table width=290 height=30><tr>");
                            builder.append("<center><td width=\"132\" align=\"center\">" + String.valueOf(subList.get(i).get("sell_gold")) + "</td></center>");
                            builder.append("<center><td width=\"100\" align=\"center\">" + String.valueOf(subList.get(i).get("member_gold")) + "</td></center>");
                            builder.append("<center><td width=\"64\" align=\"center\">" + (String.valueOf(subList.get(i).get("sell_player_name")).equals(player.getName()) ? "我的寄售" : buyButton) + "</td></center>");
                            builder.append("</tr></table></td></tr></table>");
                        }
                        html = html.replace("%sell_table_data%", builder.toString());
                    }
                    html = html.replace("%ownerMemberGold%", String.valueOf(countOf));
                    html = html.replace("%page%", String.valueOf(pageIndex));
                    html = html.replace("%pages%", (maps == null || maps.size()<=0) ? "1" : String.valueOf(pages));
//                    msg.setFile(html);
                    msg.setHtml(html);
                    player.sendPacket(msg);
                } else if ("consignment".equals(split[1])) {
                    html = HtmCache.getInstance().getHtml("scripts/handler/bbs/pages/gold_onsell.htm", player);

                    List<Map<String, Object>> maps = ConsignmentSalesDao.getInstance().selectByPlayerId(player.getObjectId());
                    // 当前页数
                    int nums = maps.size();
                    // 点击时的 总页数
                    int pages = nums % sellPageSize == 0 ? nums / sellPageSize : nums / sellPageSize + 1;
                    int pageIndex = 1;
                    if (split.length == 4) {
                        pageIndex = Integer.parseInt(split[3]);
                    } else if ("prev".equals(split[4])) {
                        pageIndex = Math.max(Integer.parseInt(split[3]) - 1, 1);
                    } else if ("next".equals(split[4])) {
                        pageIndex = Math.min(Integer.parseInt(split[3]) + 1, pages);
                    }
                    int pageStart = pageIndex > 0 ? (pageIndex - 1) * sellPageSize : 0; //6
                    int pageEnd = (nums - (pageIndex - 1) * sellPageSize) <= sellPageSize ? (nums - (pageIndex - 1) * sellPageSize) + pageStart - 1 : pageStart + sellPageSize - 1; //

                    if (maps != null && maps.size() > 0) {
                        StringBuilder builder = new StringBuilder();
                        List<Map<String, Object>> subList = maps.subList(pageStart, pageEnd + 1);
                        for (int i = 0; i < subList.size(); i++) {
                            String buyButton = "<a action=\"bypass -h MyUtils_intoGoldSellSystem cancel_" + subList.get(i).get("order_number") + " page 1\">撤回</a>";

                            builder.append("<table border=0 cellspacing=0 cellpadding=0 width=290 height=30 bgcolor=").append((i % 2) == 1 ? "1a1914" : "23221d").append(">");
                            builder.append("<tr><td width=290 height=25><table width=290 height=30><tr>");
                            builder.append("<center><td width=\"132\" align=\"center\">" + String.valueOf(subList.get(i).get("sell_gold")) + "</td></center>");
                            builder.append("<center><td width=\"100\" align=\"center\">" + String.valueOf(subList.get(i).get("member_gold")) + "</td></center>");
                            builder.append("<center><td width=\"64\" align=\"center\">" + buyButton + "</td></center>");
                            builder.append("</tr></table></td></tr></table>");
                        }
                        html = html.replace("%sellData%", builder.toString());
                    } else {
                        html = html.replace("%sellData%", "你没有正在寄售的金币");
                    }

                    long memberGoldCountOf = player.getInventory().getCountOf(memberGoldId);
                    long goldCountOf = player.getInventory().getCountOf(_GOLD);
                    html = html.replace("%ownerSimplyGold%", goldCountOf >= 10000 ? goldCountOf / 10000 + "万" : String.valueOf(goldCountOf));
                    html = html.replace("%ownerMemberGold%", String.valueOf(memberGoldCountOf));
                    html = html.replace("%page%", String.valueOf(pageIndex));
                    html = html.replace("%pages%", (maps == null || maps.size()<=0) ? "1" : String.valueOf(pages));
//                    msg.setFile(html);
                    msg.setHtml(html);
                    player.sendPacket(msg);
                } else if (split[1].startsWith("cancel")) {
                    String[] s = split[1].split("_");
                    String orderNumber = s[1];
                    onBypassFeedback(player, "intoGoldSellSystem consignment page 1");
                    Map<String, Object> map = ConsignmentSalesDao.getInstance().selectByOrderNum(orderNumber);
                    final boolean[] isCancel = {false};
                    if (map != null && map.size() > 0) {
                        player.ask(new ConfirmDlgPacket(SystemMsg.S1, 10000).addString("确认撤回你寄售售价为「" + map.get("member_gold") + "」赞助币 的「" + map.get("sell_gold") + "」金币吗?"), new OnAnswerListener() {
                            @Override
                            public void sayYes() {
                                if (ConsignmentSalesDao.getInstance().deleteByOrderNumber(orderNumber) > 0) {
                                    isCancel[0] = true;
                                    player.getInventory().addItem(_GOLD, (long) map.get("sell_gold"));
                                    player.sendMessage("获得「" + map.get("sell_gold") + "」个金币");
                                    player.sendMessage("寄售的「" + map.get("sell_gold") + "」个金币已成功撤回!");
                                    onBypassFeedback(player, "intoGoldSellSystem consignment page 1");
                                    // 撤回日志
                                    ThreadPoolManager.getInstance().execute(new WriteGoldConsignmentLog("cancelLog", map, player, null));
                                }
                                if (!isCancel[0]) {
                                    player.sendMessage("你寄售的这单金币已经被购买,注意查看邮件!");
                                    onBypassFeedback(player, "intoGoldSellSystem consignment page 1");
                                    return;
                                }
                            }

                            @Override
                            public void sayNo() {
                                onBypassFeedback(player, "intoGoldSellSystem consignment page 1");
                            }
                        });
                    } else {
                        player.sendMessage("你寄售的这单金币已经被购买或自动下架,注意查看邮件!");
                        onBypassFeedback(player, "intoGoldSellSystem consignment page 1");
                        return;
                    }
                } else if (split[1].startsWith("doBuy")) {
                    String[] s = split[1].split("_");
                    String orderNumber = s[1];
                    Map<String, Object> map = ConsignmentSalesDao.getInstance().selectByOrderNum(orderNumber);
                    final boolean[] isBuy = {false};
                    try {
                        if (map != null && map.size() > 0) {
                            long sell_gold = (long) map.get("sell_gold");
                            long member_gold = (long) map.get("member_gold");
                            player.ask(new ConfirmDlgPacket(SystemMsg.S1, 10000).addString("你要购买「" + sell_gold + "」个金币,将花费「" + member_gold + "」个赞助币,确认购买吗?"), new OnAnswerListener() {
                                public void sayYes() {
                                    if (!player.getInventory().destroyItemByItemId(memberGoldId, member_gold)) {
                                        player.sendMessage("你的赞助币不足!");
                                        onBypassFeedback(player, "intoGoldSellSystem buy page 1");
                                        return;
                                    }
                                    if (ConsignmentSalesDao.getInstance().deleteByOrderNumber(orderNumber) > 0) {
                                        isBuy[0] = true;
                                        player.sendMessage("获得「" + sell_gold + "」个金币");
                                        player.sendMessage("扣除「" + member_gold + "」个赞助币");
                                        player.sendMessage("成功购买「" + sell_gold + "」个金币,花费「" + member_gold + "」个赞助币");
                                        onBypassFeedback(player, "intoGoldSellSystem buy page 1");

                                        Player target = World.getPlayer((int) map.get("sell_player_id"));
                                        // 邮件通知
                                        ThreadPoolManager.getInstance().execute(new SystemInform(player.getObjectId(), target, map, "buySuccess"));

                                        Map<String, Object> recordMap = new HashMap<>();
                                        recordMap.put("transaction_num", UUID.randomUUID().toString());
                                        recordMap.put("sell_player_id", map.get("sell_player_id"));
                                        recordMap.put("sell_player_name", map.get("sell_player_name"));
                                        recordMap.put("buy_player_id", player.getObjectId());
                                        recordMap.put("buy_player_name", player.getName());
                                        recordMap.put("unit_price", map.get("unit_price"));
                                        recordMap.put("member_gold", map.get("member_gold"));
                                        recordMap.put("sell_gold", map.get("sell_gold"));
                                        recordMap.put("transaction_date", new Timestamp(System.currentTimeMillis() + 8 * 60 * 60 * 1000L));
                                        recordMap.put("state", 1);
                                        // 保存交易记录
                                        ThreadPoolManager.getInstance().execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                GoldCoinTransactionRecordDao.getInstance().addRecord(recordMap);
                                            }
                                        });
                                        // 购买日志
                                        ThreadPoolManager.getInstance().execute(new WriteGoldConsignmentLog("transactionLog", recordMap, player, target));
                                    }
                                    if (!isBuy[0]) {
                                        player.sendMessage("这单金币已经被撤回或购买了!");
                                        onBypassFeedback(player, "intoGoldSellSystem buy page 1");
                                        return;
                                    }
                                }

                                public void sayNo() {
                                    onBypassFeedback(player, "intoGoldSellSystem buy page 1");
                                }
                            });
                        } else {
                            player.sendMessage("这单金币已经被购买或者撤回!");
                            onBypassFeedback(player, "intoGoldSellSystem consignment page 1");
                            return;
                        }
                    } catch (Exception e) {
                        _log.error("onBypassFeedback::doBuy", e);
                        onBypassFeedback(player, "intoGoldSellSystem buy page 1");
                    } finally {
                        onBypassFeedback(player, "intoGoldSellSystem buy page 1");
                    }
                } else if ("putaway".equals(split[1])) {
                    player.sendMessage("输入的内容不能为空");
                    onBypassFeedback(player, "intoGoldSellSystem consignment page 1");
                    return;
                }
            } else if (split.length > 5) {
                // 上架
                if ("putaway".equals(split[1])) {
                    String gold_input = split[4];
                    String memberGold_input = split[5];
                    boolean matches1 = gold_input.matches("\\d+");
                    boolean matches2 = memberGold_input.matches("\\d+");
                    if (!matches1 || !matches2) {
                        onBypassFeedback(player, "intoGoldSellSystem consignment page 1");
                        player.ask(new ConfirmDlgPacket(SystemMsg.S1, 0).addString("你输入的格式有误,必须全部为数字"), new OnAnswerListener() {
                            public void sayYes() {
                                onBypassFeedback(player, "intoGoldSellSystem consignment page 1");
                                return;
                            }

                            public void sayNo() {
                                onBypassFeedback(player, "intoGoldSellSystem consignment page 1");
                            }
                        });
                    } else {
                        try {
                            if (Integer.parseInt(gold_input) <= 0 || Integer.parseInt(memberGold_input) <= 0) {
                                player.sendMessage("输入有误,不能为负数");
                                onBypassFeedback(player, "intoGoldSellSystem consignment page 1");
                                return;
                            }
                            if ((long) Integer.parseInt(gold_input) * gold_unit > max_gold_input) {
                                player.sendMessage("超过单次最大寄售金币额度,最大限制为" + max_gold_input + "金币");
                                onBypassFeedback(player, "intoGoldSellSystem consignment page 1");
                                return;
                            }
                            if (Integer.parseInt(memberGold_input) > max_memberGold_input) {
                                player.sendMessage("超过单次最大赞助币售价,最大限制为" + max_memberGold_input + "个赞助币");
                                onBypassFeedback(player, "intoGoldSellSystem consignment page 1");
                                return;
                            }
                            if (player.getInventory().getCountOf(_GOLD) < ((long) Integer.parseInt(gold_input) * gold_unit)) {
                                player.sendMessage("你当前金币不足");
                                onBypassFeedback(player, "intoGoldSellSystem consignment page 1");
                                return;
                            }
                            List<Map<String, Object>> playerConsignmentSales = ConsignmentSalesDao.maps.stream().filter(map -> map.get("sell_player_id").equals(player.getObjectId()))
                                    .collect(Collectors.toList());
                            if (playerConsignmentSales != null && playerConsignmentSales.size() >= limit_player) {
                                player.sendMessage("超过或达到 10 个寄售单数,不能再上架寄售");
                                onBypassFeedback(player, "intoGoldSellSystem consignment page 1");
                                return;
                            }
                            if (ConsignmentSalesDao.maps != null && ConsignmentSalesDao.maps.size() >= Total_consignment_limit) {
                                player.sendMessage("当前寄售系统已达到最大寄售单,请稍后再试");
                                onBypassFeedback(player, "intoGoldSellSystem consignment page 1");
                                return;
                            }
                        } catch (Exception e) {
                            player.sendMessage("输入的数字过大或者其他未知异常,请联系管理员");
                            onBypassFeedback(player, "intoGoldSellSystem consignment page 1");
                            _log.error("intoGoldSellSystem::putaway", e);
                        }
                        int gold = Integer.parseInt(gold_input);
                        int memberGold = Integer.parseInt(memberGold_input);
                        onBypassFeedback(player, "intoGoldSellSystem consignment page 1");
                        player.ask(new ConfirmDlgPacket(SystemMsg.S1, 10000).addString("你要上架的是寄售「" + gold + "万」个金币,售价「" + memberGold + "」个赞助币,确定寄售吗?"), new OnAnswerListener() {
                            public void sayYes() {
                                ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
                                ReentrantReadWriteLock.WriteLock writeLock = reentrantReadWriteLock.writeLock();
                                writeLock.lock();
                                try {
                                    if (!player.getInventory().destroyItemByItemId(_GOLD, (long) Integer.parseInt(gold_input) * gold_unit)) {
                                        player.sendMessage("你当前金币不足");
                                        onBypassFeedback(player, "intoGoldSellSystem consignment page 1");
                                        return;
                                    }
                                    Map<String, Object> map = new HashMap<>();
                                    String uuid = UUID.randomUUID().toString();
                                    map.put("order_number", uuid);
                                    map.put("sell_player_name", player.getName());
                                    map.put("sell_player_id", player.getObjectId());
                                    map.put("unit_price", gold * gold_unit / memberGold);
                                    map.put("member_gold", (long) memberGold);
                                    map.put("sell_gold", (long) gold * gold_unit);
                                    map.put("onsell_date", new Timestamp(System.currentTimeMillis() + 8 * 60 * 60 * 1000L));
                                    int isAddSuccess = ConsignmentSalesDao.getInstance().addSale(map);
                                    if (isAddSuccess > 0) {
                                        player.sendMessage("你已成功寄售「" + gold * gold_unit + "」个金币");
                                        // 寄售日志
                                        ThreadPoolManager.getInstance().execute(new WriteGoldConsignmentLog("consignmentLog", map, player, null));
                                        // 自动下架
                                        ThreadPoolManager.getInstance().schedule(new Runnable() {
                                            @Override
                                            public void run() {
                                                for (Map<String, Object> map : ConsignmentSalesDao.getInstance().selectByPlayerId(player.getObjectId())) {
                                                    if (map.get("order_number").equals(uuid)) {
                                                        int isDown = ConsignmentSalesDao.getInstance().deleteByOrderNumber(uuid);
                                                        if (isDown > 0) {
                                                            // 系统邮件通知
                                                            ThreadPoolManager.getInstance().execute(new SystemInform(player.getObjectId(), null, map, "downSuccess"));
                                                        }
                                                    }
                                                }
                                            }
                                        }, outTime, TimeUnit.MILLISECONDS);
                                        onBypassFeedback(player, "intoGoldSellSystem consignment page 1");
                                    }
                                } catch (Exception e) {
                                    _log.error("onBypassFeedback::putaway", e);
                                    onBypassFeedback(player, "intoGoldSellSystem consignment page 1");
                                } finally {
                                    writeLock.unlock();
                                    onBypassFeedback(player, "intoGoldSellSystem consignment page 1");
                                }
                                return;
                            }

                            public void sayNo() {
                                onBypassFeedback(player, "intoGoldSellSystem consignment page 1");
                            }
                        });
                    }
                }
            }
        }
        else if (inputString.startsWith("ShowView_Welcome")) {
            String html = HtmCache.getInstance().getHtml("welcome.htm", player);
            html = html.replace("<$content$>", "");
            HtmlMessage msg = new HtmlMessage(0);
            msg.setItemId(-1);
            msg.setHtml(html);
            player.sendPacket(msg);
        }
        else if (inputString.startsWith("BuyItems")) {
            String html = HtmCache.getInstance().getHtml(page, player);
            HtmlMessage msg = new HtmlMessage(0);
            msg.setItemId(-1);
            msg.setHtml(html);
            ItemTemplate tmp = ItemHolder.getInstance().getTemplate(MemberCoins);

            int index = Integer.parseInt(buypassOptions[1]);
            int money = Integer.parseInt(ItemId[index][0]); //取出第一個值 假設為"60"
            String items = ItemId[index][1];//取出第二個值假設為 "90957,1;49518,1;29010,1;29023,1;29024,1"

            int level = Integer.parseInt(ItemId[index][2]); //取出等級
            int count = Integer.parseInt(ItemId[index][3]); //取出購買次數


            html = html.replace("<$content$>", "");
            if (player.getLevel() > level)//超過20級  但20級可以買
            {
                player.sendMessage("超过" + level + "级不可购买。");
                player.sendPacket(msg);
                return;
            }
            if (!checkCanBuy(player, index, count)) {
                player.sendMessage("已买过 限定" + count + "次。");
                player.sendPacket(msg);
                return;
            }
            if (!ItemFunctions.deleteItem(player, MemberCoins, money)) {
                player.sendMessage(tmp.getName(player) + "数量不足无法购买。");
                player.sendPacket(msg);
                return;
            }

            if (giveItem(player, items)) {
                player.sendMessage("购买成功。");
                insertBuyItem(player, index);
                html = HtmCache.getInstance().getHtml("welcome1.htm", player);
            } else {
                player.sendMessage("购买失败。");
            }
            final Clan playerClan = player.getClan();
            if (playerClan != null)//有盟的處理方式
            {
                playerClan.addMembersBuff(player.getName(), 46002, 1, (System.currentTimeMillis() + (24 * 60 * 60 * 1000)) / 1000);
                playerClan.broadcastSayPacketToOnlineMembers(ChatType.COMMANDCHANNEL_ALL, player.getName(), "购买破冰之礼赠送联盟礼物，请24小时内找致命蔷薇处领取。");
                //ChatType.COMMANDCHANNEL_ALL 這一句你可以自己換別的類型，就會出現不同的顏色了。
            }
            player.sendPacket(msg);
        }
        else if (inputString.startsWith("Teleport")) {
            if (buypassOptions.length == 5) {
                player.ask((ConfirmDlgPacket) new ConfirmDlgPacket(SystemMsg.S1, 10000).addString("向目的地「" + buypassOptions[4] + "」移動。是否繼續？"), new OnAnswerListener() {

                    @Override
                    public void sayYes() {
                        String[] loc = buypassOptions[1].split(",");
                        int x = Integer.parseInt(loc[0]);
                        int y = Integer.parseInt(loc[1]);
                        int z = Integer.parseInt(loc[2]);
                        Location myloc = new Location(x, y, z);
                        int money = Integer.parseInt(buypassOptions[2]);
                        String group = buypassOptions[3];
                        if (group.equals("group")) {
                            if (player.isInParty()) {
                                Player playerLeader = player.getParty().getPartyLeader();
                                if (player != playerLeader) {
                                    player.sendMessage("組隊按鍵只能是隊長點擊。");
                                    return;
                                }
                                if (MyUtilsFunction.CheckPartyStatus(playerLeader, money)) {
                                    for (Player p : player.getParty()) {
                                        ItemFunctions.deleteItem((Playable) p, 57, (long) money);
                                        p.teleToLocation(myloc);
                                    }
                                }
                            } else {
                                MyUtilsFunction.CheckOneStatus(player, money, myloc);
                            }
                        } else {
                            MyUtilsFunction.CheckOneStatus(player, money, myloc);
                        }
                    }

                    @Override
                    public void sayNo() {
                    }
                });
            }
        }
        else if (inputString.startsWith("DownLevel"))//bypass -h MyUtils_LevelDown
        {
            if (player.getLevel() < 40)//自己新加等級限制
            {
                player.sendMessage("您的等級不滿足最低要求的40級，無法抽取經驗。");
                return;
            }
            if (player.getLevel() > 86)//自己新加等級限制
                return;//自己新加等級限制
            int itemid = 0;
            int item57 = 0;
            long exp57 = 0;
            if (player.getLevel() >= 40 && player.getLevel() < 50) {
                itemid = 91290;
                item57 = 20000;
                exp57 = 5000000;
            }
            if (player.getLevel() >= 50 && player.getLevel() < 60) {
                itemid = 91291;
                item57 = 30000;
                exp57 = 10000000;
            }
            if (player.getLevel() >= 60 && player.getLevel() < 70) {
                itemid = 91292;
                item57 = 40000;
                exp57 = 30000000;
            }
            if (player.getLevel() >= 70 && player.getLevel() < 80) {
                itemid = 91293;
                item57 = 50000;
                exp57 = 100000000;
            }
            if (player.getLevel() >= 80 && player.getLevel() < 85) {
                itemid = 91294;
                item57 = 60000;
                exp57 = 10000000000L;
            }
            if (player.getLevel() >= 85) {
                itemid = 91295;
                item57 = 70000;
                exp57 = 50000000000L;
            }
            if (!ItemFunctions.deleteItem(player, 57, item57)) {
                player.sendMessage("金幣不足無法抽取经验");
                return;
            }
            long exp = -exp57;
            player.addExpAndSp(exp, 0);
            ItemFunctions.addItem(player, itemid, 1);//自己新加道具給與
            String html = HtmCache.getInstance().getHtml("member/18057-1.htm", player);
            HtmlMessage msg = new HtmlMessage(5);
            msg.setHtml(html);
            player.sendPacket((IBroadcastPacket) msg);
			/* IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler("cfg");
			if(vch != null)
				vch.useVoicedCommand("cfg", player, ""); */
        }
        ////bypass -h MyUtils_GetMemberCoin
        else if (inputString.startsWith("DownSp"))//降SP设定
        {
            if (player.getLevel() < 10)//自己新加等級限制
            {
                player.sendMessage("您的等級不滿足最低要求的10級，無法抽取SP。");
                return;
            }
            if (player.getSp() < 200000)//自己新加等級限制
            {
                player.sendMessage("您的SP不滿足最低要求的20萬，無法抽取SP。");
                return;
            }
            int itemid1 = 0;
            int item157 = 0;
            long sp57 = 0;
            if (player.getSp() >= 200000 && player.getSp() < 3000000) {
                itemid1 = 91296;
                item157 = 10000;
                sp57 = 200000;
            }
            if (player.getLevel() >= 3000000 && player.getLevel() < 30000000) {
                itemid1 = 91297;
                item157 = 30000;
                sp57 = 3000000;
            }
            if (player.getLevel() >= 30000000) {
                itemid1 = 91298;
                item157 = 50000;
                sp57 = 30000000;
            }
            if (!ItemFunctions.deleteItem(player, 57, item157)) {
                player.sendMessage("金幣不足無法抽取SP");
                return;
            }
            long Sp = -sp57;
            player.addExpAndSp(0, Sp);
            ItemFunctions.addItem(player, itemid1, 1);//自己新加道具給與
            String html = HtmCache.getInstance().getHtml("member/18057-1.htm", player);
            HtmlMessage msg = new HtmlMessage(5);
            msg.setHtml(html);
            player.sendPacket((IBroadcastPacket) msg);
			/* IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler("cfg");
			if(vch != null)
				vch.useVoicedCommand("cfg", player, ""); */
        }//降SP设定
        else if (inputString.startsWith("ShowRaidBoss")) {
            int index = Integer.parseInt(buypassOptions[1]);
            switch (index) {
                case 20: {
                    MyUtilsFunction.showBossHtml(player, Boss20, 10000);
                    break;
                }
                case 30: {
                    MyUtilsFunction.showBossHtml(player, Boss30, 30000);
                    break;
                }
                case 40: {
                    MyUtilsFunction.showBossHtml(player, Boss40, 50000);
                    break;
                }
                case 50: {
                    MyUtilsFunction.showBossHtml(player, Boss50, 100000);
                    break;
                }
                case 60: {
                    MyUtilsFunction.showBossHtml(player, Boss60, 200000);
                    break;
                }
                case 70: {
                    MyUtilsFunction.showBossHtml(player, Boss70, 400000);
                    break;
                }
                case 80: {
                    MyUtilsFunction.showBossHtml(player, Boss80, 600000);
                }
            }
        }
//        else if (inputString.startsWith("bot_main")) {
//            BotControlPage.mainPage(player);
//        }

    }
    public static class WriteGoldConsignmentLog implements Runnable {
        String kind;
        Map<String,Object> map;
        Player player;
        Player target;
        public WriteGoldConsignmentLog(String kind, Map<String, Object> map, Player player, Object o) {
            this.kind = kind;
            this.map = map;
            this.player = player;
            this.target = o!=null?(Player) o:null;
        }

        @Override
        public void run() {
            String logFilePath = ""; // 日志文件路径
            StringBuilder logMessage = null;
            // 获取当前日期
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDate = dateFormat.format(new Date());
            switch (kind){
                case "transactionLog":
                    logFilePath = "gold_transaction_log/transactionLog";
                    logMessage =new StringBuilder();
                    Timestamp transaction_date = (Timestamp)map.get("transaction_date");
                    long sellPlayerInventoryGoldCount =0;
                    long sellPlayerInventoryMemberCount = 0;
                    if (target==null) {
                        Collection<ItemInstance> items = ItemsDAO.getInstance().getItemsByOwnerIdAndLoc((int) map.get("sell_player_id"), ItemInstance.ItemLocation.INVENTORY);
                        if(items!=null && items.size()>0){
                            for (ItemInstance item : items) {
                                if (item.getItemId()==_GOLD) {
                                    sellPlayerInventoryGoldCount = item.getCount();
                                    continue;
                                }
                                if (item.getItemId()==memberGoldId) {
                                    sellPlayerInventoryMemberCount = item.getCount();
                                }
                            }
                        }
                    }else {
                        sellPlayerInventoryGoldCount = target.getInventory().getCountOf(_GOLD);
                        sellPlayerInventoryMemberCount = target.getInventory().getCountOf(memberGoldId);
                    }

                    logMessage.append("种类：交易,订单号："+map.get("transaction_num")+"\t,")
                            .append("出售人id："+map.get("sell_player_id")+"\t,")
                            .append("出售人角色名："+map.get("sell_player_name")+"\t,")
                            .append("购买人id："+map.get("buy_player_id")+"\t,")
                            .append("购买人角色名："+map.get("buy_player_name")+"\t,")
                            .append("购买单价："+map.get("unit_price")+"\t,")
                            .append("购买花费赞助币："+map.get("member_gold")+"\t,")
                            .append("购买金币："+map.get("sell_gold")+"\t,")
                            .append("交易日期："+timeFormat.format(new java.sql.Date(transaction_date.getTime()-8*60*60*1000L))+"\t,")
                            .append("买家背包中金币："+player.getInventory().getCountOf(_GOLD)+"\t,")
                            .append("买家背包中赞助币："+player.getInventory().getCountOf(memberGoldId)+"\t,")
                            .append("卖家背包中金币："+sellPlayerInventoryGoldCount+"\t,")
                            .append("卖家背包中赞助币："+sellPlayerInventoryMemberCount+"\t,(由于会邮件通知卖家背包中的赞助币不会立刻增加);");
                    break;
                case "consignmentLog":
                    logFilePath = "gold_transaction_log/consignmentLog";
                    logMessage =new StringBuilder();
                    Timestamp onsell_date = (Timestamp)map.get("onsell_date");
                    logMessage.append("种类：寄售,订单号："+map.get("order_number")+"\t,")
                            .append("寄售人id："+map.get("sell_player_id")+"\t,")
                            .append("寄售人角色名："+map.get("sell_player_name")+"\t,")
                            .append("寄售单价："+map.get("unit_price")+"\t,")
                            .append("售价赞助币："+map.get("member_gold")+"\t,")
                            .append("寄售金币："+map.get("sell_gold")+"\t,")
                            .append("寄售日期："+timeFormat.format(new java.sql.Date(onsell_date.getTime()-8*60*60*1000L))+"\t,")
                            .append("玩家背包中金币："+player.getInventory().getCountOf(_GOLD)+"\t,")
                            .append("玩家背包中赞助币："+player.getInventory().getCountOf(memberGoldId)+"\t;");
                    break;
                case "cancelLog":
                    logFilePath = "gold_transaction_log/cancelLog";
                    logMessage =new StringBuilder();
                    logMessage.append("种类：撤回,订单号："+map.get("order_number")+"\t,")
                            .append("寄售人id："+map.get("sell_player_id")+"\t,")
                            .append("寄售人角色名："+map.get("sell_player_name")+"\t,")
                            .append("寄售单价："+map.get("unit_price")+"\t,")
                            .append("售价赞助币："+map.get("member_gold")+"\t,")
                            .append("寄售金币："+map.get("sell_gold")+"\t,")
                            .append("撤回日期："+timeFormat.format(new java.sql.Date(System.currentTimeMillis()))+"\t,")
                            .append("玩家背包中金币："+player.getInventory().getCountOf(_GOLD)+"\t,")
                            .append("玩家背包中赞助币："+player.getInventory().getCountOf(memberGoldId)+"\t;");
                    break;
                case "downSuccessLog":
                    logFilePath = "gold_transaction_log/downSuccessLog";
                    logMessage =new StringBuilder();
                    long sellPlayerInventoryGoldCount_down =0;
                    long sellPlayerInventoryMemberCount_down = 0;
                    if (player==null) {
                        Collection<ItemInstance> items = ItemsDAO.getInstance().getItemsByOwnerIdAndLoc((int) map.get("sell_player_id"), ItemInstance.ItemLocation.INVENTORY);
                        if(items!=null && items.size()>0){
                            for (ItemInstance item : items) {
                                if (item.getItemId()==_GOLD) {
                                    sellPlayerInventoryGoldCount_down = item.getCount();
                                    continue;
                                }
                                if (item.getItemId()==memberGoldId) {
                                    sellPlayerInventoryMemberCount_down = item.getCount();
                                }
                            }
                        }
                    }else {
                        sellPlayerInventoryGoldCount_down = player.getInventory().getCountOf(_GOLD);
                        sellPlayerInventoryMemberCount_down = player.getInventory().getCountOf(memberGoldId);
                    }
                    logMessage.append("种类：自动下架,订单号："+map.get("order_number")+"\t,")
                            .append("寄售人id："+map.get("sell_player_id")+"\t,")
                            .append("寄售人角色名："+map.get("sell_player_name")+"\t,")
                            .append("寄售单价："+map.get("unit_price")+"\t,")
                            .append("售价赞助币："+map.get("member_gold")+"\t,")
                            .append("寄售金币："+map.get("sell_gold")+"\t,")
                            .append("下架日期："+timeFormat.format(new java.sql.Date(System.currentTimeMillis()))+"\t,")
                            .append("玩家背包中金币："+sellPlayerInventoryGoldCount_down+"\t,")
                            .append("玩家背包中赞助币："+sellPlayerInventoryMemberCount_down+"\t;");
                    break;
            }

            try {
                // 创建日志文件夹（如果不存在）
                File logFolder = new File(logFilePath);
                if (!logFolder.exists()) {
                    logFolder.mkdirs();
                }

                // 创建日志文件
                String logFileName = currentDate + ".txt";
                File logFile = new File(logFolder, logFileName);

                // 创建日志文件写入器
                BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));

                // 写入日志
                assert logMessage != null;
                writer.write(logMessage.toString());
                writer.newLine();

                // 关闭写入器
                writer.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static class SystemInform implements Runnable {
        int id;
        Map<String, Object> map = null;
        String kind;
        Player target;

        public SystemInform(int playerId, Player target, Map<String, Object> map, String Success) {
            this.id = playerId;
            this.target = target;
            this.map = map;
            this.kind = Success;
        }

        @Override
        public void run() {
            String _topic = "";
            String _body = "";
            Player player = World.getPlayer(id);

            switch (kind) {
                case "buySuccess":
                    _topic = "寄售金币购买结果";
                    _body = "购买金币成功！";
                    long sell_gold = (long) map.get("sell_gold");
                    Map<Integer, Long> attachment1 = new HashMap<>();
                    attachment1.put(_GOLD,sell_gold);
                    Functions.sendSystemMail(player,_topic, _body, attachment1);

                    _topic = "寄售金币购买结果";
                    _body = "你寄售的「"+map.get("sell_gold")+"」金币已售出！";
                    if (target ==null) {
                        sendMailIfOutline(_topic, _body,map,memberGoldId,(long) map.get("member_gold"));
                    }else {
                        long member_gold = (long) map.get("member_gold");
                        Map<Integer, Long> attachment2 = new HashMap<>();
                        attachment2.put(memberGoldId,member_gold);
                        Functions.sendSystemMail(target,_topic, _body, attachment2);
                    }

                    break;
                case "downSuccess":
                    _topic = "寄售金币购买结果";
                    _body = "你寄售的金币「"+map.get("sell_gold")+"」金币已超过3天未有人购买,系统自动下架退回！";
                    long sell_gold1 = (long) map.get("sell_gold");

                    if (player ==null) {
                        sendMailIfOutline(_topic, _body,map,_GOLD,(long) map.get("sell_gold"));
                    }else{
                        Map<Integer, Long> attachment3 = new HashMap<>();
                        attachment3.put(_GOLD,sell_gold1);
                        Functions.sendSystemMail(player,_topic, _body, attachment3);
                    }
                    // 自动下架日志
                    ThreadPoolManager.getInstance().execute(new WriteGoldConsignmentLog("downSuccessLog",map,player,null));
                    break;
            }
        }

        private void sendMailIfOutline(String _topic, String _body, Map<String,Object> map,int kindId,long kindNumber) {
            Mail mail = new Mail();
            mail.setSenderId(1);
            mail.setSenderName("Admin");
            mail.setReceiverId((int) map.get("sell_player_id"));
            mail.setReceiverName((String) map.get("sell_player_name"));
            mail.setTopic(_topic);
            mail.setBody(_body);

            ItemInstance item = ItemFunctions.createItem(kindId);
            item.setLocation(ItemInstance.ItemLocation.MAIL);
            item.setCount(kindNumber);
            item.save();
            mail.addAttachment(item);

            mail.setType(Mail.SenderType.NEWS_INFORMER);
            mail.setUnread(true);
            mail.setExpireTime(720 * 3600 + (int) (System.currentTimeMillis() / 1000L));
            mail.save();
        }
    }
    public static boolean checkCanBuy(Player player, int index, int counts) {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        int times = 0;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT count(*) as cnt  FROM _player_buy_gift where obj_Id = ? and num = ?");
            statement.setInt(1, player.getObjectId());
            statement.setInt(2, index);
            rset = statement.executeQuery();
            if (rset.next()) {
                times = rset.getInt("cnt");
                ;
            }
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
        return counts > times;
    }
    private static boolean giveItem(Player player, String inputString) {
        String[] suite = inputString.split(";");
        ItemTemplate item;
        for (String myId : suite) {
            String[] obj = myId.split(",");
            int id = Integer.parseInt(obj[0]);
            int count = Integer.parseInt(obj[1]);
            item = ItemHolder.getInstance().getTemplate(Integer.parseInt(obj[0]));
            if (item != null) {
                ItemFunctions.addItem(player, id, count, true);
            } else {
                _log.warn(player.getName() + "花費會員幣購買物品| " + inputString + "| 有不存在id" + id);
                return false;
            }
        }
        return true;
    }
    private static void insertBuyItem(Player player, int index) {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("INSERT INTO _player_buy_gift (obj_Id ,num ,buytime) VALUES(?,?,?)");
            statement.setInt(1, player.getObjectId());
            statement.setInt(2, index);
            statement.setLong(3, System.currentTimeMillis() / 1000);
            statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
    }
    private static void CheckOneStatus(Player player, int money, Location loc) {
        if (player.getKarma() < 0) {
            player.sendMessage("紅人狀態無法傳送");
            return;
        }

        if (Config.enablePremiumAccountPeaceZone && !isPremiumAccountPeaceZone(player, loc)  && player.getLevel() > 10) {
            String msg = "非会员不能传送到非安全区区域！";
            player.sendMessage(msg);
            return;
        }

        if (money > 0 && !ItemFunctions.deleteItem((Playable) player, 57, (long) money)) {
            player.sendMessage("金幣不足.");
            return;
        }
        player.teleToLocation(loc);
    }

    public static boolean isPremiumAccountPeaceZone(Player player, Location loc) {
        Map<String, ZoneTemplate> zones = ZoneHolder.getInstance().getZones();
        List<ZoneTemplate> collect = zones.values().stream().filter(obj -> obj.getType() == Zone.ZoneType.peace_zone).collect(Collectors.toList());
        boolean canTryIntoZone = false;
        for (ZoneTemplate zoneTemplate : collect) {
            if (zoneTemplate.getTerritory().isInside(loc.x, loc.y, loc.z)) {
                canTryIntoZone =true;
                break;
            }
        }
        if (!canTryIntoZone && !player.hasPremiumAccount())
            return false;
        return true;
    }
    private static boolean CheckPartyStatus(Player player, int money) {
        for (Player p : player.getParty()) {
            if (p.getDistance(player) > 1500) {
                MyUtilsFunction.sendNotOkMessage(player, "隊伍「" + p.getName() + "」不在隊長身邊，無法傳送");
                return false;
            }
            if (money > 0 && p.getInventory().getCountOf(57) < (long) money) {
                MyUtilsFunction.sendNotOkMessage(player, "隊伍「" + p.getName() + "」支付費用不足「" + money + "」金幣.");
                return false;
            }
            if (p.isDead()) {
                MyUtilsFunction.sendNotOkMessage(player, "隊伍中「" + p.getName() + "」已死亡無法傳送.");
                return false;
            }
            if (player.getKarma() >= 0) continue;
            MyUtilsFunction.sendNotOkMessage(player, "隊伍中「" + p.getName() + "」紅人無法傳送.");
            return false;
        }
        return true;
    }
    private static void sendNotOkMessage(Player player, String message) {
        for (Player p : player.getParty()) {
            p.sendMessage(message);
        }
    }
    public static void showBossHtml(Player player, int[] Boss, int money) {
        String html = HtmCache.getInstance().getHtml("teleporter/30146-RaidBoss.htm", player);
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < Boss.length; ++i) {
            NpcInstance npc = RaidBossSpawnManager.getInstance().getRaidBossId(Boss[i]);
            if (npc != null) {
                content.append("<tr>");
                Creature mostHated = npc.getAggroList().getMostHated(-1);
                if (mostHated != null) {
                    content.append("<td fixwidth=35>" + npc.getLevel() + "</td><td fixwidth=215>" + NpcHolder.getInstance().getTemplate(npc.getNpcId()).getName(player) + "<br1><font color=0000FF>" + NpcHolder.getInstance().getTemplate(mostHated.getNpcId()).getName(player) + " \u6311\u6230\u4e2d</font></td>");
                } else if (npc.isAttackingNow()) {
                    content.append("<td fixwidth=35>" + npc.getLevel() + "</td><td fixwidth=215>" + NpcHolder.getInstance().getTemplate(npc.getNpcId()).getName(player)  + "<br1><font color=0000FF>\u8207\u73a9\u5bb6 \u6230\u9b25\u4e2d</font></td>");
                } else {
                    content.append("<td fixwidth=35>" + npc.getLevel() + "</td><td fixwidth=215>" + NpcHolder.getInstance().getTemplate(npc.getNpcId()).getName(player)  + "<br1><font color=00FF00>\u53ef\u6311\u6230</font></td>");
                }
                content.append("<td fixwidth=50>");
                content.append("<button value=\"\u50b3\u9001\" action=\"bypass -h MyUtils_Teleport " + npc.getX() + "," + npc.getY() + "," + npc.getZ() + " " + money + " one " + NpcHolder.getInstance().getTemplate(npc.getNpcId()).getName(player).replace(" ", "") + "\" width=50 height=18 back=\"l2ui_ct1.Button_DF_Msn_down\" fore=\"l2ui_ct1.Button_DF_Msn\">");
                content.append("<button value=\"\u968a\u50b3\" action=\"bypass -h MyUtils_Teleport " + npc.getX() + "," + npc.getY() + "," + npc.getZ() + " " + money + " group " + NpcHolder.getInstance().getTemplate(npc.getNpcId()).getName(player).replace(" ", "") + "\" width=50 height=18 back=\"l2ui_ct1.Button_DF_Msn_down\" fore=\"l2ui_ct1.Button_DF_Msn\">");
                content.append("</td></tr>");
                continue;
            }
            NpcTemplate tmp = NpcHolder.getInstance().getTemplate(Boss[i]);
            long time = RaidBossSpawnManager.getRaidBossReborn(Boss[i]);
            String times = "\u7121\u8a0a\u606f";
            if (time > 0L) {
                times = dateFormat.format(time * 1000L);
            }
            content.append("<tr>");
            content.append("<td fixwidth=35>" + tmp.level + "</td><td fixwidth=215>" + tmp.getName(player) + "<br1><font color=FF0000>" + times + "</font></td>");
            content.append("<td fixwidth=50>");
            content.append("<button value=\"\u5f85\u751f\" action=\"\" width=50 height=18 back=\"l2ui_ct1.Button_DF_Msn_down\" fore=\"l2ui_ct1.Button_DF_Msn\">");
            content.append("<button value=\"情報\" action=\"bypass -h DropCalculator _dropMonsterDetailsByName_" + Boss[i] + "\" width=50 height=18 back=\"l2ui_ct1.Button_DF_Msn_down\" fore=\"l2ui_ct1.Button_DF_Msn\">");
            content.append("</td></tr>");
        }
        html = html.replace("<$content$>", content.toString());
        HtmlMessage msg = new HtmlMessage(5);
        msg.setHtml(html);
        player.sendPacket((IBroadcastPacket) msg);
    }
}
