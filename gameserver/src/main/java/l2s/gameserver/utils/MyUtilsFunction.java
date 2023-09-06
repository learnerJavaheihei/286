package l2s.gameserver.utils;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.dao.ConsignmentSalesDao;
import l2s.gameserver.dao.GoldCoinTransactionRecordDao;
import l2s.gameserver.dao.ItemsDAO;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.listener.actor.player.OnAnswerListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.mail.Mail;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ConfirmDlgPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class MyUtilsFunction {
    private static final Logger _log = LoggerFactory.getLogger(Files.class);
    public MyUtilsFunction() {

    }

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
        if (inputString.startsWith("intoGoldSellSystem")) {
            String[] split = inputString.split(" ");
            //长页面
            HtmlMessage msg = new HtmlMessage(0);
            msg.setItemId(-1);

//            HtmlMessage msg = new HtmlMessage(5);
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
                    html = html.replace("%pages%", maps == null ? "1" : String.valueOf(pages));
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
                    html = html.replace("%pages%", maps == null ? "1" : String.valueOf(pages));
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
}
