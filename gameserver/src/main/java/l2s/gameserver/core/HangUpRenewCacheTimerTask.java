package l2s.gameserver.core;

import l2s.gameserver.GameServer;
import l2s.gameserver.botscript.BotHangUpTimeDao;
import l2s.gameserver.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

public class HangUpRenewCacheTimerTask extends TimerTask {

    private static final Logger _log = LoggerFactory.getLogger(GameServer.class);

    private Player player;

    private Timer timer;

    public HangUpRenewCacheTimerTask(Player actor,Timer t){
        player = actor;
        timer = t;
    }

    @Override
    public void run() {
        long saveTime = System.currentTimeMillis();

        //如果是停止挂机状态，定时任务结束，在stop方法里面回去保存缓存中的数据，所以数据是一致的
        if(!player._isInPlugIn){
            timer.cancel();
        }

        String leftTime5minutes = BotEngine.leftTimeMap.get(String.valueOf(player.getObjectId()));
        if (leftTime5minutes != null) {
            BotHangUpTimeDao.getInstance().updateHangUpTime(player.getObjectId(),Integer.parseInt(leftTime5minutes));
        }
    }
}
