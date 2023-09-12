package l2s.gameserver.core;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.botscript.*;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.AutoFarm;
import l2s.gameserver.network.l2.s2c.ExAutoplaySetting;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.utils.Functions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
public class BotEngine
{
    private static final Logger LOG = LoggerFactory.getLogger(BotEngine.class);
    private static final BotEngine INSTANCE = new BotEngine();
    private IBotConfigDAO dao;
    private IBotRuntimeChecker runTimeChecker;
    private  Map<Integer, ScheduledFuture<?>> tasks;
    private Lock switchLock;
    private Map<Integer, BotConfig> configs;
    public static Map<String,String> leftTimeMap;
    public static String leftTime = null;
    public static String scriptRemainderTime;
    public static boolean runtimeHuangUpTimeLock = false;
    private Timer timer;
    // 挂机时间递减任务
    private  Map<Integer, ScheduledFuture<?>> timeTasks;

    // 连续未攻击时长
    private  Map<Integer, ScheduledFuture<?>> increaseAttackRadiusTasks;

    /* member class not found */
    class Task {}

    public Map<Integer, BotConfig> getConfigs()
    {
        return configs;
    }

    protected BotEngine()
    {
        tasks = new HashMap<Integer, ScheduledFuture<?>>();
        timeTasks = new HashMap<Integer, ScheduledFuture<?>>();
        increaseAttackRadiusTasks = new HashMap<Integer, ScheduledFuture<?>>();
        switchLock = new ReentrantLock();
        configs = new HashMap<Integer, BotConfig>();
        //init();
    }

    public synchronized void init()
    {
        BotScriptsLoader.load();
        dao = new BotConfigDAO();
        runTimeChecker = new BotRuntimeChecker();
        LOG.info("\u6302\u673A\u7CFB\u7EDF\u52A0\u8F7D\u6210\u529F!");
		/*\u6302\u673A\u7CFB\u7EDF\u52A0\u8F7D\u6210\u529F! 挂机系统加载成功!*/
    }

    public IBotConfigDAO getDao()
    {
        return dao;
    }

    public IBotRuntimeChecker getRunTimeChecker()
    {
        return runTimeChecker;
    }

    public BotConfig getBotConfig(Player player)
    {
        BotConfig config = configs.get(Integer.valueOf(player.getObjectId()));
        if(config == null)
        {
            config = new BotConfigImp();
            configs.put(Integer.valueOf(player.getObjectId()), config);
        }
        return configs.get(Integer.valueOf(player.getObjectId()));
    }

    public void startBotTask(Player player)
    {
        timer = new Timer();
		/*pvp活动禁用内挂*/
    	if (player.isInPvPEvent())
    	{
    		player.sendMessage("活动中无法开启自動狩獵..");
    		return;
    	}
        /*内挂剩余时间为0禁用*/
        String s = BotEngine.leftTimeMap.get(String.valueOf(player.getObjectId()));
        if (s==null || Integer.parseInt(s) == 0) {
            player.sendMessage("当前自動狩獵剩余使用时间为0,无法开启内挂..");
            return;
        }

		/*pvp活动禁用内挂*/
		switchLock.lock();
		try
		{
			ScheduledFuture<?> botThinkTask = tasks.get(player.getObjectId());
			if(botThinkTask == null)
			{
                player._isInPlugIn = true;/*啟動內掛減少收益*/
                BotConfig botConfig = BotEngine.getInstance().getBotConfig(player);
                botConfig.getIsUsedReviveOwner_target().clear();
                botConfig.setAbort(false, "");
				botThinkTask = ThreadPoolManager.getInstance().schedule(new BotThinkTask(player), 0L);
				player.sendMessage("「开启」自動狩獵... ...");
				botConfig.setStartX(player.getX());
				botConfig.setStartY(player.getY());
				botConfig.setStartZ(player.getZ());
				if(botConfig.getDeathTime() > 0)
				{
					botConfig.setDeathTime(0);
				}
				tasks.put(player.getObjectId(), botThinkTask);
				player.broadcastCharInfo();
			}
            // 每5分钟将挂机剩余时间更新到数据库
            TimerManager.getInstance().startRenewCacheTime(player);

            ScheduledFuture<?> checkTimeTask = timeTasks.get(player.getObjectId());
            if(checkTimeTask == null){
                checkTimeTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        Integer time = Integer.valueOf(BotEngine.leftTimeMap.get(String.valueOf(player.getObjectId())));
                        if (player._isInPlugIn && time > 0) {
                            time--;
                            /** 存储剩余时间的map 中更新时间 */
                            BotEngine.leftTimeMap.put(String.valueOf(player.getObjectId()), String.valueOf(time));
                        }
                        if (time == 0) {
                            BotEngine.getInstance().stopTask(player);
                            BotConfig botConfig = BotEngine.getInstance().getBotConfig(player);
                            botConfig.setAbort(true, "");
                            return;
                        }

                        /* 当前角色所在组的队长是否死亡，死亡 并且队长停止挂机时 停止挂机 */
                        if (!player.isDead()) {
                            Party party = player.getParty();
                            if (party != null) {
                                Player leader = party.getPartyLeader();
                                if (leader != null && leader.isDead() && !leader._isInPlugIn) {
                                    BotEngine.getInstance().stopTask(player);
                                    BotConfig botConfig = BotEngine.getInstance().getBotConfig(player);
                                    botConfig.setAbort(true, "队长死亡状态下，不能开启内挂");
                                    return;
                                }
                            }
                        }
                    }
                }, 0, 1000L, TimeUnit.MILLISECONDS);
                timeTasks.put(player.getObjectId(), checkTimeTask);
            }
            BotConfig botConfig = BotEngine.getInstance().getBotConfig(player);
            Adjust(player,botConfig);

            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    botConfig.releaseMemory(player);
                }
            },60*1000L,60*1000L);

        }
		finally
		{
			switchLock.unlock();
		}
		player.startAbnormalEffect(AbnormalEffect.THIRTEENTH_BUFF);//給與特殊效果標識
    }
    public void Adjust(Player player, BotConfig botConfig) {
        int Boot_setting_range = botConfig.getFindMobMaxDistance();
        ScheduledFuture<?> increaseAttackRadiusTask = increaseAttackRadiusTasks.get(player.getObjectId());
        if (botConfig.is_autoAdjustRange()) {
            autoAdjustRange(player, botConfig, increaseAttackRadiusTask, Boot_setting_range,increaseAttackRadiusTasks);
        }else{
            if(increaseAttackRadiusTask != null)
            {
                increaseAttackRadiusTask.cancel(false);
            }
            increaseAttackRadiusTasks.remove(player.getObjectId());
        }
    }
    private void autoAdjustRange(Player player, BotConfig botConfig, ScheduledFuture<?> increaseAttackRadiusTask, int boot_setting_range, Map<Integer, ScheduledFuture<?>> increaseAttackRadiusTasks) {

        if (increaseAttackRadiusTask==null) {
            increaseAttackRadiusTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {

                    if (player.isDead() || player.isAttackingNow()) {
                        botConfig.setFindMobMaxDistance(boot_setting_range);
                    }
                    // 如果上次攻击时间 距离现在超过 5分钟 将最大距离增加 1000 但不能超过最大 4500
                    if ((System.currentTimeMillis() - player.getLastAttackPacket()) >= 5*60 * 1000) {
                        botConfig.setFindMobMaxDistance(Math.min(boot_setting_range + 1000, 4500));
                    }
                }
            }, 5*60 * 1000, 1000, TimeUnit.MILLISECONDS);
            increaseAttackRadiusTasks.put(player.getObjectId(),increaseAttackRadiusTask);
        }
    }
    public void stopTask(Player player)
    {
		switchLock.lock();
		try
		{

            if (timer !=null) {
                timer.cancel();
            }
			int objectId = player.getObjectId();
			Future<?> task = tasks.get(player.getObjectId());
            Future<?> timeTask = timeTasks.get(player.getObjectId());
            Future<?> increaseAttackRadiusTask = increaseAttackRadiusTasks.get(player.getObjectId());
			player._isInPlugIn = false;/*關閉內掛減少收益*/

			if(task != null)
			{
				task.cancel(true);
			}
            if(timeTask != null)
            {
                timeTask.cancel(false);
            }
            if(increaseAttackRadiusTask != null)
            {
                increaseAttackRadiusTask.cancel(false);
            }
			tasks.remove(objectId);
            timeTasks.remove(objectId);
            increaseAttackRadiusTasks.remove(objectId);
			player.sendMessage("「关闭」自動狩獵 - " + BotEngine.getInstance().getBotConfig(player).getAbortReason());
			player.broadcastCharInfo();
//            Functions.show("<center>辅助已中断<br1>....<br1><br1><table><tr><td><button value=\"重新启动内挂\" action=\"bypass -h htmbypass_bot.start\" width=120 height=20 back=\"Button_DF_Down\" fore=\"Button_DF\"></td></tr></table></center>", player);
            AutoFarm autoFarm = player.getAutoFarm();
            autoFarm.setUnkParam1(16);
            autoFarm.setFarmActivate(false);

            autoFarm.setUnkParam2(1);
            player.sendPacket(new ExAutoplaySetting(player));

            /** 剩余时间放入数据库 */
            String lefttime = BotEngine.leftTimeMap.get(String.valueOf(player.getObjectId()));
            BotHangUpTimeDao.getInstance().updateHangUpTime(player.getObjectId(),Integer.parseInt(lefttime));
		}
		finally
		{
			switchLock.unlock();
		}
		player.stopAbnormalEffect(AbnormalEffect.THIRTEENTH_BUFF);//給與特殊效果標識
    }

    public static BotEngine getInstance()
    {
        return INSTANCE;
    }
}
