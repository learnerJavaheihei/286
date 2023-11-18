package l2s.gameserver.utils;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.dao.CharacterVariablesDAO;
import l2s.gameserver.data.xml.holder.LimitedShopHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.variables.PlayerVariables;
import l2s.gameserver.model.base.LimitedShopEntry;
import l2s.gameserver.model.base.LimitedShopProduction;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class LimitShopDailyLimitTask {
    private static final Logger _log = LoggerFactory.getLogger(LimitShopDailyLimitTask.class);
    public static Calendar calendar =Calendar.getInstance();
    static{
        calendar.set(Calendar.HOUR_OF_DAY, 6); // LCOIN商店定时更新
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 0);
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH,1);
        }
    }
    private static LimitShopDailyLimitTask _instance = new LimitShopDailyLimitTask();
    public static LimitShopDailyLimitTask getInstance() {
        return _instance;
    }
    public Map<Integer,Integer> xmlLimitShopDailyLimit = new LinkedHashMap<Integer,Integer>();

    public void start(){
        LimitedShopHolder.getInstance().get_entries().forEachValue(list -> {
            List<LimitedShopEntry> entries = list.getEntries();
            for (LimitedShopEntry shopEntry : entries) {
                LimitedShopProduction production = shopEntry.getProduction().get(0);
                xmlLimitShopDailyLimit.put(production.getInfo().getInteger("product1Id"),production.getInfo().getInteger("dailyLimit"));
            }
            return false;
        });
        checkUpdate(false);
        // 定时更新
        AutoUpdateDailyLimitTask autoUpdateDailyLimitTask = new AutoUpdateDailyLimitTask();
        ThreadPoolManager.getInstance().scheduleAtFixedDelay(autoUpdateDailyLimitTask, calendar.getTimeInMillis()-System.currentTimeMillis(),24*60*60*1000L);
    }

    private void checkUpdate(boolean update) {
        List<Map<String,Object>> dbDatas= selectDailyLimit();
        if (update) {
            calendar.add(Calendar.DAY_OF_MONTH,1);
        }

        updateDailyLimit(dbDatas);
    }

    private class AutoUpdateDailyLimitTask implements Runnable {
        @Override
        public void run() {
            checkUpdate(true);
        }
    }

    private List<Map<String, Object>> selectDailyLimit() {
        List<Map<String, Object>> datas = new ArrayList<>();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try{
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT * FROM character_variables WHERE name like ?");
            statement.setString(1,PlayerVariables.LIMIT_ITEM_REMAIN+"%");
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                long expire_time = resultSet.getLong("expire_time");
                if (expire_time < System.currentTimeMillis()){
                    Map<String, Object> map = new HashMap<>();
                    map.put("obj_id",resultSet.getInt("obj_id"));
                    map.put("name",resultSet.getString("name"));
                    map.put("value",resultSet.getString("value"));
                    map.put("expire_time",expire_time);
                    datas.add(map);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            try {
                DbUtils.close(con);
                DbUtils.close(statement,resultSet);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return datas;
    }

    private void updateDailyLimit(List<Map<String, Object>> dbDatas) {
        int count =0;
        Connection con = null;
        Statement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.createStatement();
            for (Map<String, Object> dbData : dbDatas) {
                String name = (String) dbData.get("name");
                String product1Id = name.replace(PlayerVariables.LIMIT_ITEM_REMAIN + "_", "");

                Integer dailyLimit = xmlLimitShopDailyLimit.get(Integer.parseInt(product1Id));
                if (dailyLimit == null)
                    continue;
                Integer obj_id = (Integer) dbData.get("obj_id");

                Player player = GameObjectsStorage.getPlayer(obj_id);
                if (player!=null) {
                    player.setVar(name,dailyLimit, calendar.getTimeInMillis());
                    count++;
                }
                else {
                    String sql = "UPDATE character_variables " +
                            "SET value='" + String.valueOf(dailyLimit) + "',expire_time=" + calendar.getTimeInMillis() + " " +
                            "WHERE name = '" + name + "' and obj_id='" + obj_id + "'";
                    statement.addBatch(sql);
                    count++;
                }
            }
            _log.info("updated character_variables dailyLimit size: " + count);
            statement.executeBatch();
            con.close();
            statement.close();
        } catch (SQLException e) {
            _log.error("updated character_variables dailyLimit wrong: " + count);
        }
        count=0;
    }


//    private void updateDailyLimit(List<Map<String, Object>> dbDatas) {
//        Connection con = null;
//        Statement statement = null;
//        try {
//            con = DatabaseFactory.getInstance().getConnection();
//            statement = con.createStatement();
//            for (Map<String, Object> dbData : dbDatas) {
//
//                String name = (String) dbData.get("name");
//                String product1Id = name.replace(PlayerVariables.LIMIT_ITEM_REMAIN + "_", "");
//
//                Integer dailyLimit = xmlLimitShopDailyLimit.get(Integer.parseInt(product1Id));
//                if (dailyLimit == null)
//                    continue;
//                Integer obj_id = (Integer) dbData.get("obj_id");
//                String sql = "UPDATE character_variables " +
//                        "SET value='"+String.valueOf(dailyLimit)+"',expire_time="+calendar.getTimeInMillis()+" " +
//                        "WHERE name = '"+name+"' and obj_id='"+obj_id+"'";
//                statement.addBatch(sql);
//                Player player = GameObjectsStorage.getPlayer(obj_id);
//                if (player!=null) {
//                    player.setVar(name,dailyLimit, calendar.getTimeInMillis());
//                }
//            }
//            statement.executeBatch();
//            _log.info("updated character_variables dailyLimit size: " + statement.getUpdateCount());
//            con.close();
//            statement.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//            _log.error("updated character_variables dailyLimit wrong");
//        }
//
//    }
}
