package l2s.gameserver.core;

import l2s.gameserver.GameServer;
import l2s.gameserver.botscript.BotHangUpTimeDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class RestartServerHangUpTime {

    private static final Logger _log = LoggerFactory.getLogger(GameServer.class);

    public static RestartServerHangUpTime _instance = new RestartServerHangUpTime();

    public static RestartServerHangUpTime getInstance()
    {
        return _instance;
    }

    public static java.util.Date updateTime;

    public RestartServerHangUpTime(){
        Calendar calendar = Calendar.getInstance();
        /*** 定制每日2:00执行方法 ***/
        calendar.set(Calendar.HOUR_OF_DAY, 17);
        calendar.set(Calendar.MINUTE, 52);
        calendar.set(Calendar.SECOND, 0);
        updateTime = calendar.getTime(); //第一次执行定时任务的时间
    }
    // 检查是否更新了挂机时间
    public void checkIsRenewHangUpTime(){
        List<Map<String, Object>> allPlayer = BotHangUpTimeDao.getInstance().selectAllPlayer();

//        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());

        List<Integer> objIdList = new ArrayList<>();
        // 当前时间 服务器启动过了定时重置时间 应该去重置
        if(allPlayer!=null && (date.compareTo(updateTime) == 1 ||date.compareTo(updateTime) == 0)){
            for (Map<String, Object> player : allPlayer) {
                // 如果玩家的剩余时间小于10小时 等于10小时不重置  购买过的人也要更新
                if ((Integer)player.get("left_time") != 36000 || (Integer)player.get("is_buy") == 1) {
                    if(player.get("renew_time") == null){
                        objIdList.add((Integer) player.get("obj_id"));
                        continue;
                    }
                    Date renew_time =(Date) player.get("renew_time");
//                    String format = formatter.format(renew_time);
//                    java.util.Date parse = formatter.parse(format);
                    // 如果更新日前小于当前日期 有可能今天刚更新服务器重启 使用过内挂的不能更新
                    String re = renew_time.toString();
                    String da = date.toString();
                    if (!re.equals(da)) {
                        // 如果缓存中 有数据 且不等于36000 更新缓存
                        if (BotEngine.leftTimeMap != null) {
                            String leftTime = BotEngine.leftTimeMap.get(String.valueOf((Integer) player.get("left_time")));
                            if(leftTime !=null){
                                if (!leftTime.equals("36000")) {
                                    BotEngine.leftTimeMap.put(String.valueOf((Integer) player.get("obj_id")),"36000");
                                }
                            }
                        }
                        objIdList.add((Integer) player.get("obj_id"));
                    }
                }
            }
            // 如果缓存没有数据也就是玩家没在线 更新数据库
            _log.info("=====update"+objIdList.size());
            long start = System.currentTimeMillis();
            BotHangUpTimeDao.getInstance().updateHangUpRenewTime(objIdList,date);
            long end = System.currentTimeMillis();
            _log.info("updateTime====="+String.valueOf(end-start)+"ms");
        }
    }
}
