package com.ls.cmd_ai;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final Properties props = new Properties();

    public static void main(String[] args) {

        String version = "Unknown";
        Properties properties = new Properties();

        try (InputStream input = Main.class.getClassLoader().getResourceAsStream("version.properties")) {
            if (input != null) {
                properties.load(input);
                version = properties.getProperty("module.version");
            }
        } catch (IOException ex) {
            logger.error("버전 정보를 읽어오는데 실패했습니다.", ex);
        }

        logger.info("==================================================");
        logger.info("  CMD AI Module Started successfully.");
        logger.info("  Module Version: [{}]", version);
        logger.info("==================================================");

        int apiConnectTimeout = 0;
        int apiReadTimeout = 0;
        int alarmHistoryRange = 0;

        try (InputStream input = Main.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                logger.error("application.properties file not found");
                return;
            }
            props.load(input);
            apiConnectTimeout = Integer.parseInt(props.getProperty("api.connect.timeout"));
            apiReadTimeout = Integer.parseInt(props.getProperty("api.read.timeout"));
            alarmHistoryRange = Integer.parseInt(props.getProperty("alarm.history.range"));
            if (alarmHistoryRange > 30) {
                alarmHistoryRange = 30;
            }
        } catch (Exception e) {
            logger.error("seeting file read error: " + e.getMessage());
            return;
        }

        Timer tableCreateTimer = new Timer();
        TableCreateTimerTask tableCreateTimerTask = new TableCreateTimerTask();
        tableCreateTimer.schedule(tableCreateTimerTask, 1000, 86400000); // 하루에 1번 실행

        Timer alarmTimer = new Timer();
        NewAlarmTimerTask alarmTimerTask = new NewAlarmTimerTask(apiConnectTimeout, apiReadTimeout, alarmHistoryRange);
        alarmTimer.schedule(alarmTimerTask, 1000, 10000); // 10초에 1번 실행
    }
}
