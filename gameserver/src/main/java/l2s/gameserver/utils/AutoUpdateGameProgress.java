package l2s.gameserver.utils;

import l2s.commons.util.Rnd;
import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.dao.AutoEnableGameProgressDao;
import l2s.gameserver.dao.AutoEnableGameProgressRecordDao;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class AutoUpdateGameProgress {
    private static final AutoUpdateGameProgress _instance = new AutoUpdateGameProgress();

    public static AutoUpdateGameProgress getInstance()
    {
        return _instance;
    }

    private static final Logger _log = LoggerFactory.getLogger(AutoUpdateGameProgress.class);
    int[] schedules;
    String[] progress_EndTimes;
    int[] progressMaxLevel;
    Map<Integer, List<String>> progressValues;
    double maxPercent;
    public static double percent =0.;
    public static int Schedule =1;
    private static ScheduledFuture<?> _updateTask = null;

    // 阶段 boss id boss 死后下次刷新的 BOSS id
    public static String[] progressBossIds = new String[]{
            // 每行 最多填 5 个值(与开设几个阶段对应值) 分别对应 从第一 到 第五阶段 单个boss 应该替换的 id
            "29001;18913;18920;18926;18926",// 巨蟻女王
            "29006;18917;18923;18929;18929",// 核心
            "29014;18918;18924;18930;18930",// 奧爾芬
/*             "29068;29068;29068;29068;29068",// 安塔瑞斯
            "29020;29020;29020;29020;29020",// 巴溫
            "29022;29022;29022;29022;29022",// 札肯 */
    };

    private AutoUpdateGameProgress(){
        this.maxPercent =100.;
        load();
    }

    public void shutDown() {
        if (_updateTask !=null)
            _updateTask.cancel(false);
    }

    public void load() {
        this.schedules = new int[]{
                Config.FIRST_SCHEDULE,
                Config.SECOND_SCHEDULE,
                Config.THIRD_SCHEDULE,
                Config.FOURTH_SCHEDULE,
                Config.FIFTH_SCHEDULE
        };
        this.progress_EndTimes = new String[]{
                Config.FIRST_SCHEDULE_ENDTIME,
                Config.SECOND_SCHEDULE_ENDTIME,
                Config.THIRD_SCHEDULE_ENDTIME,
                Config.FOURTH_SCHEDULE_ENDTIME,
                Config.FIFTH_SCHEDULE_ENDTIME
        };
        this.progressMaxLevel = new int[]{
                Config.FIRST_SCHEDULE_MAX_LEVEL,
                Config.SECOND_SCHEDULE_MAX_LEVEL,
                Config.THIRD_SCHEDULE_MAX_LEVEL,
                Config.FOURTH_SCHEDULE_MAX_LEVEL,
                Config.FIFTH_SCHEDULE_MAX_LEVEL
        };
        this.progressValues = new HashMap<>();
        for (int i = 0; i < schedules.length; i++) {
            ArrayList<String> values = new ArrayList<>();
            values.add(progress_EndTimes[i]);
            values.add(String.valueOf(progressMaxLevel[i]));
            progressValues.put(schedules[i],values);
        }
    }

    public void start(){
        if (_updateTask !=null)
            _updateTask.cancel(false);

        Map<String, Object> map = AutoEnableGameProgressDao.getInstance().select();
        double startPercent =0.;
        if (map==null) {
            if (Config.START_SCHEDULE<=0) {
                _log.warn("start AutoUpdateGameProgress failed !");
                return;
            }
            List<String> currentScheduleData = progressValues.get(Config.START_SCHEDULE);
            if (!checkSchedule(currentScheduleData)) {
                _log.warn("start AutoUpdateGameProgress failed !");
                return;
            }
            startPercent = Config.START_PERCENT;
            Schedule = Config.START_SCHEDULE;
            percent = startPercent;
            _updateTask = ThreadPoolManager.getInstance().schedule(new UpdateProgress(Config.START_SCHEDULE, startPercent), Config.AUTO_UPDATE_PROGRESS_INTERVAL * 60 * 1000L);
        }else{
            Schedule= (int) map.get("progress_limit");
            double lastPercent =0.;
            String current_percent = (String) map.get("current_percent");
            lastPercent = Double.parseDouble(current_percent.replace("%", ""));
            percent =lastPercent;
            // 达到最大阶段且进度是100%时 自动进度任务 停止
            if (Schedule < Config.MAX_SCHEDULE || (Schedule == Config.MAX_SCHEDULE && lastPercent < 100.) ) {
                _updateTask = ThreadPoolManager.getInstance().schedule(new UpdateProgress(Schedule, lastPercent), Config.AUTO_UPDATE_PROGRESS_INTERVAL * 60 * 1000L);
            }

        }

        if (Config.ENABLE_AUTO_GAME_PROGRESS_SCHEDULE && Config.IS_OUTTIME_ON_ALT_MAX_LEVEL) {
            List<String> currentScheduleData = progressValues.get(Schedule);
            Config.ALT_MAX_LEVEL = Integer.parseInt(currentScheduleData.get(1));
            /* 超過 当前阶段的等级 級別不掉東西沒經驗 */
            Config.CHAO_DROP_ITEM_COUNT40 = Integer.parseInt(currentScheduleData.get(1));
        }

    }

    private class UpdateProgress implements Runnable{
        double startPercent;
        List<String> currentScheduleData;
        int beforeExecuteSchedule;
        public UpdateProgress() {
        }

        public UpdateProgress(int schedule, double startPercent) {
            this.startPercent = startPercent;
            this.currentScheduleData =progressValues.get(schedule);
            this.beforeExecuteSchedule = schedule;
        }

        @Override
        public void run() {
            double wouldUpdatePercent = Calculator.percentOnTime(this.startPercent, maxPercent, new Timestamp(parseDate(currentScheduleData.get(0)).getTime()));
            double slidingPercent = 0.;
            slidingPercent = wouldUpdatePercent-this.startPercent;
            if (wouldUpdatePercent>=100.) {
                /*  */
                Schedule=Math.min(Schedule+1,Config.MAX_SCHEDULE);
                this.currentScheduleData =progressValues.get(Schedule);
                if (Schedule < Config.MAX_SCHEDULE || beforeExecuteSchedule != Schedule) {
                    wouldUpdatePercent=0.;
                }else
                    wouldUpdatePercent=100.;
                // 公告
                if (Config.ENABLE_AUTO_GAME_PROGRESS_SCHEDULE)
                    announcements("当前世界游戏阶段是第「" + Schedule + "」阶段,祝你游戏愉快！");
            }
            percent = reservePoint(wouldUpdatePercent);

            Map<String, Object> map = new HashMap<>();
            map.put("progress_limit",Schedule);
            map.put("progress_schedule","阶段等级上线:"+currentScheduleData.get(1)+";阶段结束日期:"+new Timestamp(parseDate(currentScheduleData.get(0)).getTime()));
            map.put("current_percent",reservePoint(wouldUpdatePercent)+"%");
            map.put("update_time",new Timestamp(new Date(System.currentTimeMillis()).getTime()+8*60*60*1000L));
            map.put("update_percent_onTime",reservePoint(Calculator.pt)+"%");
            map.put("current_progress_endTime",new Timestamp(parseDate(currentScheduleData.get(0)).getTime()+8*60*60*1000L));
            
            AutoEnableGameProgressDao.getInstance().update(map);
            map.clear();
            HashMap<String, Object> record = new HashMap<>();
            record.put("progressLimit",Schedule);
            record.put("last_percent",this.startPercent);
            record.put("current_percent",reservePoint(wouldUpdatePercent));
            record.put("sliding_percent",reservePoint(slidingPercent));
            record.put("update_time",new Timestamp(new Date(System.currentTimeMillis()).getTime()+8*60*60*1000L));
            AutoEnableGameProgressRecordDao.getInstance().insert(record);
            record.clear();

            // 阶段变化时 更改新阶段更改任务
            // 1.世界等级上线
            // 2.世界BOSS等级、掉落
            if (beforeExecuteSchedule < AutoUpdateGameProgress.Schedule){
                if (Config.IS_OUTTIME_ON_ALT_MAX_LEVEL) {
                    List<String> currentScheduleData = progressValues.get(Schedule);
                    Config.ALT_MAX_LEVEL = Integer.parseInt(currentScheduleData.get(1));
                    /* 超過 当前阶段的等级 級別不掉東西沒經驗 */
                    Config.CHAO_DROP_ITEM_COUNT40 = Integer.parseInt(currentScheduleData.get(1));
                }

            }
            // 达到最大阶段且进度是100%时 自动进度任务 停止
            if ( Schedule == Config.MAX_SCHEDULE && wouldUpdatePercent == 100.) {
                return;
            }
            if (_updateTask !=null) {
                _updateTask.cancel(false);
                _updateTask = ThreadPoolManager.getInstance().schedule( new UpdateProgress(Schedule,wouldUpdatePercent), Config.AUTO_UPDATE_PROGRESS_INTERVAL*60*1000L);
            }
        }
    }

    public static void announcements(String text) {
        Collection<Player> players = GameObjectsStorage.getPlayers(false, false);
        Announcements instance = Announcements.getInstance();
        Announcements.Announce announce = instance.new Announce(0, text);
        for (Player player : players) {
            announce.showAnnounce(player);
        }
    }

    private static class Calculator{
        static double pt = 0.;
        public static double percentOnTime(double currentPercent, double maxPercent,Timestamp progressEndTime,String... args){
            long difference = progressEndTime.getTime() - System.currentTimeMillis();
            if (difference<=0) {
                return 100.;
            }
            // 测试时间
            if(difference <= Config.AUTO_UPDATE_PROGRESS_INTERVAL*60*1000L){
                pt = maxPercent - currentPercent;
                return pt + currentPercent;
            }
            /* 每小时更新的进度 (测试 每分钟)*/
            pt = (maxPercent - currentPercent) / ((double)difference / (double)(Config.AUTO_UPDATE_PROGRESS_INTERVAL*60000));

            if (pt>0.){
                if (Config.ADD_SLIDING_SCALES<=0) {
                    return pt+currentPercent;
                }
                int random = Rnd.get(Config.ADD_SLIDING_SCALES*(-1), Config.ADD_SLIDING_SCALES);
                pt = pt * (100 + random) / 100;
                return pt+currentPercent;
            }
            return 0.+currentPercent;
        }
    }

    private static double reservePoint(double pt) {
        BigDecimal decimal = new BigDecimal(pt);
        BigDecimal roundedNumber = decimal.setScale(2, RoundingMode.HALF_UP);

        return roundedNumber.doubleValue();
    }

    private boolean checkSchedule(List<String> currentScheduleData) {
//        String endTime = currentScheduleData.get(0);
//        Date parse  = parseDate(endTime);
//        String key = "startSchedule";
//        String value =String.valueOf(Schedule+1);

//        if (parse.before(new Date(System.currentTimeMillis()))) {
//        HashMap<String, String> map = new HashMap<>();
//        modifyPropertiesParam(Config.ALT_SETTINGS_FILE,map);
//        }

        int maxLevel = Integer.parseInt(currentScheduleData.get(1));
        if (maxLevel < 0) {
            _log.warn("maxLevel is not in the correct range; it should be gt 0!");
            return false;
        }
        return true;
    }

    private void modifyPropertiesParam(String file,Map<String,String> params) {
        Set<Map.Entry<String, String>> entries = params.entrySet();
        Iterator<Map.Entry<String, String>> iterator = entries.iterator();
        try {
            // 读取Properties文件
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            List<String> lines = new ArrayList<>();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
            bufferedReader.close();

            // 修改指定的键值对
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                for (int i = 0; i < lines.size(); i++) {
                    String currentLine = lines.get(i);
                    if (currentLine.startsWith("#")) {
                        continue;
                    }
                    if (!currentLine.contains("=")) {
                        continue;
                    }
                    String[] split = currentLine.split("=");
                    if (!"".equals(split[0]) && split[0].trim().equals(entry.getKey())) {
                        lines.set(i, entry.getKey() + "=" + entry.getValue());
                        break;
                    }
                }
            }


            // 保存修改后的Properties文件
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "UTF-8");
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

            for (String updatedLine : lines) {
                bufferedWriter.write(updatedLine);
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Date parseDate(String endTime) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date parse = null;
        try {
            parse = simpleDateFormat.parse(endTime);
        } catch (ParseException e) {
            _log.error("AutoUpdateGameProgress:: parse date wrong!"+e);
        }
        return parse;
    }
    public static int getBossIsCurrentProgress(int npcId, int schedule) {
        AtomicInteger result = new AtomicInteger(npcId);
        List<Integer> bossKind = inArrays(progressBossIds,npcId);
        if (bossKind!=null) {
            Integer replaceBossId = bossKind.get(schedule - 1);
            if (replaceBossId!=npcId) {
                result.set(replaceBossId);
            }
        }
        return result.get();
    }

    public static List<Integer> inArrays(String[] bosses, int id){
        for (String boss : bosses) {
            List<Integer> bossIds = new ArrayList<Integer>();
            String[] split = boss.split(";");
            for (int i = 0; i < split.length; i++) {
                bossIds.add(Integer.parseInt(split[i]));
            }
            if (bossIds.contains(id)) {
                return bossIds;
            }
        }
        return null;
    }

    public static boolean isInArrays(String[] bosses, int id){
        List<Integer> bossIds = new ArrayList<Integer>();
        for (String boss : bosses) {
            String[] split = boss.split(";");
            for (int i = 0; i < split.length; i++) {
                bossIds.add(Integer.parseInt(split[i]));
            }
        }
        return bossIds.contains(id);
    }

}
