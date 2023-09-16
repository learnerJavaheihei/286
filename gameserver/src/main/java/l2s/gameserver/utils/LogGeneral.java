package l2s.gameserver.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogGeneral implements Runnable {
    String logFilePath;
    String msg;

    public LogGeneral(String logFilePath,String msg, Object... o) {
        this.logFilePath = logFilePath;
        this.msg = msg;
    }

    @Override
    public void run() {
        String logFilePath = this.logFilePath; // 日志文件路径
        String logMessage = this.msg;
        // 获取当前日期
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDate = dateFormat.format(new Date());

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
