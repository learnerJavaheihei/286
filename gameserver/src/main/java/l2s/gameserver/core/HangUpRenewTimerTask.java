package l2s.gameserver.core;

import l2s.gameserver.GameServer;
import l2s.gameserver.botscript.BotHangUpTimeDao;
import l2s.gameserver.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

public class HangUpRenewTimerTask extends TimerTask {

    private static final Logger _log = LoggerFactory.getLogger(GameServer.class);
    public HangUpRenewTimerTask() {
    }

    @Override
    public void run() {
        try {
            // 如果缓存中 有数据 且不等于36000 更新缓存
            Date date = new Date(System.currentTimeMillis());
            Map<String, String> leftTimeMap = BotEngine.leftTimeMap;
            _log.info("doUpdateTaskCache======="+leftTimeMap.size());
            List<Integer> renewTimeList = new ArrayList<>();
            // 如果在线 缓存有数据 先更新缓存 在更新 数据库
            if(leftTimeMap != null){
                _log.info("doUpdateTask=======111");
                // map("2432","36000")("obj_id","leftTime")
                for(String key :leftTimeMap.keySet()){
                    if(Integer.parseInt(leftTimeMap.get(key)) != Integer.parseInt("36000")){
                        leftTimeMap.put(key,"36000");
                        renewTimeList.add(Integer.parseInt(key));
                    }
                }
            }
            // 玩家不在线 去更新数据库
            RestartServerHangUpTime.getInstance().checkIsRenewHangUpTime();

            long start = System.currentTimeMillis();
            BotHangUpTimeDao.getInstance().updateHangUpRenewTime(renewTimeList,date);
            // 将缓存中所有人的购买状态都清空
            Player._isBuyMap.clear();
            Player._buyTimesByOBJ.clear();

            long end = System.currentTimeMillis();
            _log.info("updateTime====="+String.valueOf(end-start)+"ms");
        } catch (Exception e) {
            _log.info("-------------解析信息发生异常--------------");
        }
    }
}
