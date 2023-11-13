package l2s.gameserver.utils.CompensationSystem;

import l2s.gameserver.Announcements;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

public class NewServerCompensationServiceImpl {
    private static NewServerCompensationServiceImpl _instance = new NewServerCompensationServiceImpl();
    public static NewServerCompensationServiceImpl getInstance(){
        return _instance;
    }
    private static Logger _log = LoggerFactory.getLogger(NewServerCompensationServiceImpl.class);
	//补偿修改时间
    public static boolean OnOffset = false;
    public static final String[] Unit = {
            "2023-11-10  19:00:00",
            "2023-11-17  20:00:00",
            "2023-11-24  20:00:00",
            "2023-12-01  20:00:00",
            "2023-12-08  20:00:00"
    };
    private static final long stopCompensationTime = 1702641600000L; // 2023-12-15 01:20:00
    public static int compensationUnit = 0;
    public static int gameCoinId = 91663;
    public static List<NewServerCompensationEntry> filterList = new ArrayList<NewServerCompensationEntry>();

    static long first =0;
    static long second =0;
    static long third =0;
    static long fourth =0;
    static long fifth =0;
    static {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
        try {
            first = simpleDateFormat.parse(Unit[0]).getTime();
            second = simpleDateFormat.parse(Unit[1]).getTime();
            third = simpleDateFormat.parse(Unit[2]).getTime();
            fourth = simpleDateFormat.parse(Unit[3]).getTime();
            fifth = simpleDateFormat.parse(Unit[4]).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
    private static ScheduledFuture<?> checkTask = null;

    private NewServerCompensationServiceImpl(){

    }
    public void onLoad(){
        if (System.currentTimeMillis() >= stopCompensationTime) {
            _log.info("*** more than stopCompensationTime, compensation sys turn off... ***");
            return;
        }

        load();
        if (OnOffset){
            startCheck();
            // 定时关闭 compensationSYS
            ThreadPoolManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    OnOffset = false;
                   // Announcements.announceToAll("各位玩家,补偿系统已关闭!");
                }
            },stopCompensationTime-System.currentTimeMillis());
        }
    }
    // 服务器启动加载数据
    /*
    *   1. 服务器启动需要做的事情：
    *       1.1 拿到一共五期的所有 补偿时间 [如果当前时间超过最晚的时间节点 且 所有账号下全部领取完毕后] **补偿系统关闭
    *
    *       1.2. 遍历比较 确认当前时间 是在哪个期间 开启补偿系统
    *           第一期时间拟定开服时间即记录账户上线即可领取  则  忽略第一期时间节点 currentTime < 第二期时间节点   **第一期补偿
    *           第二期时间节点 <= currentTime < 第三期时间节点 **第二期补偿
    *           第三期时间节点 <= currentTime < 第四期时间节点 **第三期补偿
    *           第四期时间节点 <= currentTime < 第五期时间节点 **第四期补偿
    *           第五期时间节点 <= currentTime **第五期补偿
    *   2. 每个玩家每次领取后 该玩家所在账户下 所有角色都不能显示领取操作, 直到下一期补偿时间节点 才开启
    *       2.1 领取后 定时去做开启 领取补偿的按钮
    *       threadPoolManager.getInstance().schedule(turn on  player.turnOnCompensationBtn)
    *   3. 如果每个账户 全部领取完毕 则 filterList.remove(this)
    *
    *   4. 定时开启下一期补偿
    *   5. 单个账户领取完毕后,玩家的领取补偿按钮隐藏
    * */
    public void load(){
        long currentTime = System.currentTimeMillis();

        // 过滤出剩余补偿数大于 0 的账户
        List<NewServerCompensationEntry> newServerCompensationEntries = NewServerCompensationDao.getInstance().selectCompensation();
        if (!newServerCompensationEntries.isEmpty()) {
            filterList = newServerCompensationEntries.stream().filter(newServerCompensationEntry -> {
                int remain_coin = newServerCompensationEntry.getRemain_coin();
                if (remain_coin > 0) return true;
                return false;
            }).collect(Collectors.toList());
        }
        // 所有玩家领取完毕 关闭系统
        if (filterList.isEmpty()) {
            OnOffset = false;
            _log.info("*** not need compensation account, compensation sys turn off... ***");
            return;
        }
        OnOffset = true;

        if (currentTime >= fifth) {
            compensationUnit = 5;
        }
        else if (currentTime >= fourth) {
            compensationUnit = 4;
        }
        else if (currentTime >= third) {
            compensationUnit = 3;
        }
        else if (currentTime >= second) {
            compensationUnit = 2;
        }
        else if (currentTime >= first) {
            compensationUnit = 1;
        }
        else {
            OnOffset = false;
            startCheck();
            // 定时关闭 compensationSYS
            ThreadPoolManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    OnOffset = false;
                   // Announcements.announceToAll("各位玩家,补偿系统已关闭!");
                }
            },stopCompensationTime-System.currentTimeMillis());
            _log.info("*** compensation sys firstUnit greater than start_time sys turn off... ***");
            return;
        }
        _log.info("*** have need compensation account size("+filterList.size()+"), compensation sys turn on... ***");
    }

    private void startCheck() {

        String unit_time = Unit[Math.min(compensationUnit,Unit.length-1)];
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");

        try {
            long unitTime = simpleDateFormat.parse(unit_time).getTime();
            if (checkTask!=null) {
                checkTask.cancel(true);
            }

            if (compensationUnit==5) {
                return;
            }
            checkTask= ThreadPoolManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    load();
                   // Announcements.announceToAll("各位玩家,补偿系统第"+compensationUnit+"期已开启!");
                    startCheck();
                }
            }, unitTime - System.currentTimeMillis());


        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
}
