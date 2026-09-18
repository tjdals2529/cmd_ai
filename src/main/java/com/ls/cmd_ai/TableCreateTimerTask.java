package com.ls.cmd_ai;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ls.cmd_ai.util.DateUtil;

public class TableCreateTimerTask extends TimerTask {

    private static final Logger logger = LoggerFactory.getLogger(NewAlarmTimerTask.class);

    @Override
    public void run() {
        // TODO Auto-generated method stub
        Connection conn = null;

        try {
            conn = DBConnectionManager.getConnection();
            Date now = new Date();
            Date nextMonthDate = new Date();
            Date previousMonthDate = new Date();
            previousMonthDate.setMonth(now.getMonth() - 1);
            nextMonthDate.setMonth(now.getMonth() + 1);

            String transPreviousAlarmTableCreateSql = "CREATE TABLE IF NOT EXISTS `ai_agent`.`tb_trans_alarm_history_"
                    + DateUtil.getTimeTableDate(previousMonthDate) +
                    "` (\r\n" + //
                    "\t`alarm_id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '알람ID',\r\n" + //
                    "\t`panel_id` INT(11) NOT NULL DEFAULT 0,\r\n" + //
                    "\t`diagnosis_id` INT(11) NOT NULL DEFAULT 0,\r\n" + //
                    "\t`alarm_type_id` INT(11) NOT NULL COMMENT '알람타입ID',\r\n" + //
                    "\t`acknowledge` INT(11) NOT NULL DEFAULT 0,\r\n" + //
                    "\t`reg_date` DATETIME NOT NULL COMMENT '발생일',\r\n" + //
                    "\t`alarm_level` INT(11) NOT NULL DEFAULT 0,\r\n" + //
                    "\t`file_name` LONGTEXT NULL DEFAULT NULL COLLATE 'utf8_unicode_ci',\r\n" + //
                    "\tPRIMARY KEY (`alarm_id`) USING BTREE,\r\n" + //
                    "\tINDEX `FK_tb_alarm_history_tb_alarm_type` (`alarm_type_id`) USING BTREE,\r\n" + //
                    "\tINDEX `FK_tb_alarm_history_tb_panel_list_history` (`panel_id`) USING BTREE\r\n" + //
                    ")\r\n" + //
                    "COLLATE='utf8_unicode_ci'\r\n" + //
                    "ENGINE=InnoDB\r\n" + //
                    ";\r\n" + //
                    "";

            try (PreparedStatement pstmt = conn.prepareStatement(transPreviousAlarmTableCreateSql)) {
                int result = pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            String transAlarmTableCreateSql = "CREATE TABLE IF NOT EXISTS `ai_agent`.`tb_trans_alarm_history_"
                    + DateUtil.getTimeTableDate(now) +
                    "` (\r\n" + //
                    "\t`alarm_id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '알람ID',\r\n" + //
                    "\t`panel_id` INT(11) NOT NULL DEFAULT 0,\r\n" + //
                    "\t`diagnosis_id` INT(11) NOT NULL DEFAULT 0,\r\n" + //
                    "\t`alarm_type_id` INT(11) NOT NULL COMMENT '알람타입ID',\r\n" + //
                    "\t`acknowledge` INT(11) NOT NULL DEFAULT 0,\r\n" + //
                    "\t`reg_date` DATETIME NOT NULL COMMENT '발생일',\r\n" + //
                    "\t`alarm_level` INT(11) NOT NULL DEFAULT 0,\r\n" + //
                    "\t`file_name` LONGTEXT NULL DEFAULT NULL COLLATE 'utf8_unicode_ci',\r\n" + //
                    "\tPRIMARY KEY (`alarm_id`) USING BTREE,\r\n" + //
                    "\tINDEX `FK_tb_alarm_history_tb_alarm_type` (`alarm_type_id`) USING BTREE,\r\n" + //
                    "\tINDEX `FK_tb_alarm_history_tb_panel_list_history` (`panel_id`) USING BTREE\r\n" + //
                    ")\r\n" + //
                    "COLLATE='utf8_unicode_ci'\r\n" + //
                    "ENGINE=InnoDB\r\n" + //
                    ";\r\n" + //
                    "";

            try (PreparedStatement pstmt = conn.prepareStatement(transAlarmTableCreateSql)) {
                int result = pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            String transNextAlarmTableCreateSql = "CREATE TABLE IF NOT EXISTS `ai_agent`.`tb_trans_alarm_history_"
                    + DateUtil.getTimeTableDate(nextMonthDate) +
                    "` (\r\n" + //
                    "\t`alarm_id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '알람ID',\r\n" + //
                    "\t`panel_id` INT(11) NOT NULL DEFAULT 0,\r\n" + //
                    "\t`diagnosis_id` INT(11) NOT NULL DEFAULT 0,\r\n" + //
                    "\t`alarm_type_id` INT(11) NOT NULL COMMENT '알람타입ID',\r\n" + //
                    "\t`acknowledge` INT(11) NOT NULL DEFAULT 0,\r\n" + //
                    "\t`reg_date` DATETIME NOT NULL COMMENT '발생일',\r\n" + //
                    "\t`alarm_level` INT(11) NOT NULL DEFAULT 0,\r\n" + //
                    "\t`file_name` LONGTEXT NULL DEFAULT NULL COLLATE 'utf8_unicode_ci',\r\n" + //
                    "\tPRIMARY KEY (`alarm_id`) USING BTREE,\r\n" + //
                    "\tINDEX `FK_tb_alarm_history_tb_alarm_type` (`alarm_type_id`) USING BTREE,\r\n" + //
                    "\tINDEX `FK_tb_alarm_history_tb_panel_list_history` (`panel_id`) USING BTREE\r\n" + //
                    ")\r\n" + //
                    "COLLATE='utf8_unicode_ci'\r\n" + //
                    "ENGINE=InnoDB\r\n" + //
                    ";\r\n" + //
                    "";

            try (PreparedStatement pstmt = conn.prepareStatement(transNextAlarmTableCreateSql)) {
                int result = pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            String distPreviousAlarmTableCreateSql = "CREATE TABLE IF NOT EXISTS `ai_agent`.`tb_dist_alarm_history_"
                    + DateUtil.getTimeTableDate(previousMonthDate) +
                    "` (\r\n" + //
                    "\t`alarm_id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '알람ID',\r\n" + //
                    "\t`panel_id` INT(11) NOT NULL DEFAULT 0,\r\n" + //
                    "\t`diagnosis_id` INT(11) NOT NULL DEFAULT 0,\r\n" + //
                    "\t`alarm_type_id` INT(11) NOT NULL COMMENT '알람타입ID',\r\n" + //
                    "\t`acknowledge` INT(11) NOT NULL DEFAULT 0,\r\n" + //
                    "\t`reg_date` DATETIME NOT NULL COMMENT '발생일',\r\n" + //
                    "\t`alarm_level` INT(11) NOT NULL DEFAULT 0,\r\n" + //
                    "\t`file_name` LONGTEXT NULL DEFAULT NULL COLLATE 'utf8_unicode_ci',\r\n" + //
                    "\tPRIMARY KEY (`alarm_id`) USING BTREE,\r\n" + //
                    "\tINDEX `FK_tb_alarm_history_tb_alarm_type` (`alarm_type_id`) USING BTREE,\r\n" + //
                    "\tINDEX `FK_tb_alarm_history_tb_panel_list_history` (`panel_id`) USING BTREE\r\n" + //
                    ")\r\n" + //
                    "COLLATE='utf8_unicode_ci'\r\n" + //
                    "ENGINE=InnoDB\r\n" + //
                    ";\r\n" + //
                    "";

            try (PreparedStatement pstmt = conn.prepareStatement(distPreviousAlarmTableCreateSql)) {
                int result = pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            String distAlarmTableCreateSql = "CREATE TABLE IF NOT EXISTS `ai_agent`.`tb_dist_alarm_history_"
                    + DateUtil.getTimeTableDate(now) +
                    "` (\r\n" + //
                    "\t`alarm_id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '알람ID',\r\n" + //
                    "\t`panel_id` INT(11) NOT NULL DEFAULT 0,\r\n" + //
                    "\t`diagnosis_id` INT(11) NOT NULL DEFAULT 0,\r\n" + //
                    "\t`alarm_type_id` INT(11) NOT NULL COMMENT '알람타입ID',\r\n" + //
                    "\t`acknowledge` INT(11) NOT NULL DEFAULT 0,\r\n" + //
                    "\t`reg_date` DATETIME NOT NULL COMMENT '발생일',\r\n" + //
                    "\t`alarm_level` INT(11) NOT NULL DEFAULT 0,\r\n" + //
                    "\t`file_name` LONGTEXT NULL DEFAULT NULL COLLATE 'utf8_unicode_ci',\r\n" + //
                    "\tPRIMARY KEY (`alarm_id`) USING BTREE,\r\n" + //
                    "\tINDEX `FK_tb_alarm_history_tb_alarm_type` (`alarm_type_id`) USING BTREE,\r\n" + //
                    "\tINDEX `FK_tb_alarm_history_tb_panel_list_history` (`panel_id`) USING BTREE\r\n" + //
                    ")\r\n" + //
                    "COLLATE='utf8_unicode_ci'\r\n" + //
                    "ENGINE=InnoDB\r\n" + //
                    ";\r\n" + //
                    "";

            try (PreparedStatement pstmt = conn.prepareStatement(distAlarmTableCreateSql)) {
                int result = pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            String distNextAlarmTableCreateSql = "CREATE TABLE IF NOT EXISTS `ai_agent`.`tb_dist_alarm_history_"
                    + DateUtil.getTimeTableDate(nextMonthDate) +
                    "` (\r\n" + //
                    "\t`alarm_id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '알람ID',\r\n" + //
                    "\t`panel_id` INT(11) NOT NULL DEFAULT 0,\r\n" + //
                    "\t`diagnosis_id` INT(11) NOT NULL DEFAULT 0,\r\n" + //
                    "\t`alarm_type_id` INT(11) NOT NULL COMMENT '알람타입ID',\r\n" + //
                    "\t`acknowledge` INT(11) NOT NULL DEFAULT 0,\r\n" + //
                    "\t`reg_date` DATETIME NOT NULL COMMENT '발생일',\r\n" + //
                    "\t`alarm_level` INT(11) NOT NULL DEFAULT 0,\r\n" + //
                    "\t`file_name` LONGTEXT NULL DEFAULT NULL COLLATE 'utf8_unicode_ci',\r\n" + //
                    "\tPRIMARY KEY (`alarm_id`) USING BTREE,\r\n" + //
                    "\tINDEX `FK_tb_alarm_history_tb_alarm_type` (`alarm_type_id`) USING BTREE,\r\n" + //
                    "\tINDEX `FK_tb_alarm_history_tb_panel_list_history` (`panel_id`) USING BTREE\r\n" + //
                    ")\r\n" + //
                    "COLLATE='utf8_unicode_ci'\r\n" + //
                    "ENGINE=InnoDB\r\n" + //
                    ";\r\n" + //
                    "";

            try (PreparedStatement pstmt = conn.prepareStatement(distNextAlarmTableCreateSql)) {
                int result = pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            String scadaPreviousAlarmTableCreateSql = "CREATE TABLE IF NOT EXISTS `ai_agent`.`tb_scada_alarm_history_"
                    + DateUtil.getTimeTableDate(previousMonthDate) +
                    "` (\r\n" + //
                    "\t`alarm_id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '알람ID',\r\n" + //
                    "\t`panel_id` INT(11) NOT NULL DEFAULT 0,\r\n" + //
                    "\t`diagnosis_id` INT(11) NOT NULL DEFAULT 0,\r\n" + //
                    "\t`alarm_type_id` INT(11) NOT NULL COMMENT '알람타입ID',\r\n" + //
                    "\t`acknowledge` INT(11) NOT NULL DEFAULT 0,\r\n" + //
                    "\t`reg_date` DATETIME NOT NULL COMMENT '발생일',\r\n" + //
                    "\t`alarm_level` INT(11) NOT NULL DEFAULT 0,\r\n" + //
                    "\t`file_name` LONGTEXT NULL DEFAULT NULL COLLATE 'utf8_unicode_ci',\r\n" + //
                    "\tPRIMARY KEY (`alarm_id`) USING BTREE,\r\n" + //
                    "\tINDEX `FK_tb_alarm_history_tb_alarm_type` (`alarm_type_id`) USING BTREE,\r\n" + //
                    "\tINDEX `FK_tb_alarm_history_tb_panel_list_history` (`panel_id`) USING BTREE\r\n" + //
                    ")\r\n" + //
                    "COLLATE='utf8_unicode_ci'\r\n" + //
                    "ENGINE=InnoDB\r\n" + //
                    ";\r\n" + //
                    "";

            try (PreparedStatement pstmt = conn.prepareStatement(scadaPreviousAlarmTableCreateSql)) {
                int result = pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            String scadaAlarmTableCreateSql = "CREATE TABLE IF NOT EXISTS `ai_agent`.`tb_scada_alarm_history_"
                    + DateUtil.getTimeTableDate(now) +
                    "` (\r\n" + //
                    "\t`alarm_id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '알람ID',\r\n" + //
                    "\t`panel_id` INT(11) NOT NULL DEFAULT 0,\r\n" + //
                    "\t`diagnosis_id` INT(11) NOT NULL DEFAULT 0,\r\n" + //
                    "\t`alarm_type_id` INT(11) NOT NULL COMMENT '알람타입ID',\r\n" + //
                    "\t`acknowledge` INT(11) NOT NULL DEFAULT 0,\r\n" + //
                    "\t`reg_date` DATETIME NOT NULL COMMENT '발생일',\r\n" + //
                    "\t`alarm_level` INT(11) NOT NULL DEFAULT 0,\r\n" + //
                    "\t`file_name` LONGTEXT NULL DEFAULT NULL COLLATE 'utf8_unicode_ci',\r\n" + //
                    "\tPRIMARY KEY (`alarm_id`) USING BTREE,\r\n" + //
                    "\tINDEX `FK_tb_alarm_history_tb_alarm_type` (`alarm_type_id`) USING BTREE,\r\n" + //
                    "\tINDEX `FK_tb_alarm_history_tb_panel_list_history` (`panel_id`) USING BTREE\r\n" + //
                    ")\r\n" + //
                    "COLLATE='utf8_unicode_ci'\r\n" + //
                    "ENGINE=InnoDB\r\n" + //
                    ";\r\n" + //
                    "";

            try (PreparedStatement pstmt = conn.prepareStatement(scadaAlarmTableCreateSql)) {
                int result = pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            String scadaNextAlarmTableCreateSql = "CREATE TABLE IF NOT EXISTS `ai_agent`.`tb_scada_alarm_history_"
                    + DateUtil.getTimeTableDate(nextMonthDate) +
                    "` (\r\n" + //
                    "\t`alarm_id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '알람ID',\r\n" + //
                    "\t`panel_id` INT(11) NOT NULL DEFAULT 0,\r\n" + //
                    "\t`diagnosis_id` INT(11) NOT NULL DEFAULT 0,\r\n" + //
                    "\t`alarm_type_id` INT(11) NOT NULL COMMENT '알람타입ID',\r\n" + //
                    "\t`acknowledge` INT(11) NOT NULL DEFAULT 0,\r\n" + //
                    "\t`reg_date` DATETIME NOT NULL COMMENT '발생일',\r\n" + //
                    "\t`alarm_level` INT(11) NOT NULL DEFAULT 0,\r\n" + //
                    "\t`file_name` LONGTEXT NULL DEFAULT NULL COLLATE 'utf8_unicode_ci',\r\n" + //
                    "\tPRIMARY KEY (`alarm_id`) USING BTREE,\r\n" + //
                    "\tINDEX `FK_tb_alarm_history_tb_alarm_type` (`alarm_type_id`) USING BTREE,\r\n" + //
                    "\tINDEX `FK_tb_alarm_history_tb_panel_list_history` (`panel_id`) USING BTREE\r\n" + //
                    ")\r\n" + //
                    "COLLATE='utf8_unicode_ci'\r\n" + //
                    "ENGINE=InnoDB\r\n" + //
                    ";\r\n" + //
                    "";

            try (PreparedStatement pstmt = conn.prepareStatement(scadaNextAlarmTableCreateSql)) {
                int result = pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            String transPreviousDauAlarmTableCreateSql = "CREATE TABLE IF NOT EXISTS `ai_agent`.`tb_trans_dau_alarm_history_"
                    + DateUtil.getTimeTableDate(previousMonthDate) +
                    "` (\r\n" + //
                    "	`sid` INT(11) NOT NULL AUTO_INCREMENT," +
                    "    `dau_id` INT(11) NOT NULL DEFAULT 0," +
                    "    `dau_alarm_type_id` INT(11) NOT NULL DEFAULT 0," +
                    "    `acknowledge` INT(11) NOT NULL DEFAULT 0," +
                    "    `alarm_level` INT(11) NOT NULL DEFAULT 0," +
                    "    `reg_date` DATETIME NOT NULL DEFAULT current_timestamp()," +
                    "    PRIMARY KEY (`sid`)" +
                    ")" +
                    "COLLATE='utf8_unicode_ci'" +
                    "ENGINE=InnoDB";

            try (PreparedStatement pstmt = conn.prepareStatement(transPreviousDauAlarmTableCreateSql)) {
                int result = pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            String transDauAlarmTableCreateSql = "CREATE TABLE IF NOT EXISTS `ai_agent`.`tb_trans_dau_alarm_history_"
                    + DateUtil.getTimeTableDate(now) +
                    "` (\r\n" + //
                    "	`sid` INT(11) NOT NULL AUTO_INCREMENT," +
                    "    `dau_id` INT(11) NOT NULL DEFAULT 0," +
                    "    `dau_alarm_type_id` INT(11) NOT NULL DEFAULT 0," +
                    "    `acknowledge` INT(11) NOT NULL DEFAULT 0," +
                    "    `alarm_level` INT(11) NOT NULL DEFAULT 0," +
                    "    `reg_date` DATETIME NOT NULL DEFAULT current_timestamp()," +
                    "    PRIMARY KEY (`sid`)" +
                    ")" +
                    "COLLATE='utf8_unicode_ci'" +
                    "ENGINE=InnoDB";

            try (PreparedStatement pstmt = conn.prepareStatement(transDauAlarmTableCreateSql)) {
                int result = pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            String transDauNextAlarmTableCreateSql = "CREATE TABLE IF NOT EXISTS `ai_agent`.`tb_trans_dau_alarm_history_"
                    + DateUtil.getTimeTableDate(nextMonthDate) +
                    "` (\r\n" + //
                    "	`sid` INT(11) NOT NULL AUTO_INCREMENT," +
                    "    `dau_id` INT(11) NOT NULL DEFAULT 0," +
                    "    `dau_alarm_type_id` INT(11) NOT NULL DEFAULT 0," +
                    "    `acknowledge` INT(11) NOT NULL DEFAULT 0," +
                    "    `alarm_level` INT(11) NOT NULL DEFAULT 0," +
                    "    `reg_date` DATETIME NOT NULL DEFAULT current_timestamp()," +
                    "    PRIMARY KEY (`sid`)" +
                    ")" +
                    "COLLATE='utf8_unicode_ci'" +
                    "ENGINE=InnoDB";

            try (PreparedStatement pstmt = conn.prepareStatement(transDauNextAlarmTableCreateSql)) {
                int result = pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            String distPreviousDauAlarmTableCreateSql = "CREATE TABLE IF NOT EXISTS `ai_agent`.`tb_dist_dau_alarm_history_"
                    + DateUtil.getTimeTableDate(previousMonthDate) +
                    "` (\r\n" + //
                    "	`sid` INT(11) NOT NULL AUTO_INCREMENT," +
                    "    `dau_id` INT(11) NOT NULL DEFAULT 0," +
                    "    `dau_alarm_type_id` INT(11) NOT NULL DEFAULT 0," +
                    "    `acknowledge` INT(11) NOT NULL DEFAULT 0," +
                    "    `alarm_level` INT(11) NOT NULL DEFAULT 0," +
                    "    `reg_date` DATETIME NOT NULL DEFAULT current_timestamp()," +
                    "    PRIMARY KEY (`sid`)" +
                    ")" +
                    "COLLATE='utf8_unicode_ci'" +
                    "ENGINE=InnoDB";

            try (PreparedStatement pstmt = conn.prepareStatement(distPreviousDauAlarmTableCreateSql)) {
                int result = pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            String distDauAlarmTableCreateSql = "CREATE TABLE IF NOT EXISTS `ai_agent`.`tb_dist_dau_alarm_history_"
                    + DateUtil.getTimeTableDate(now) +
                    "` (\r\n" + //
                    "	`sid` INT(11) NOT NULL AUTO_INCREMENT," +
                    "    `dau_id` INT(11) NOT NULL DEFAULT 0," +
                    "    `dau_alarm_type_id` INT(11) NOT NULL DEFAULT 0," +
                    "    `acknowledge` INT(11) NOT NULL DEFAULT 0," +
                    "    `alarm_level` INT(11) NOT NULL DEFAULT 0," +
                    "    `reg_date` DATETIME NOT NULL DEFAULT current_timestamp()," +
                    "    PRIMARY KEY (`sid`)" +
                    ")" +
                    "COLLATE='utf8_unicode_ci'" +
                    "ENGINE=InnoDB";

            try (PreparedStatement pstmt = conn.prepareStatement(distDauAlarmTableCreateSql)) {
                int result = pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            String distDauNextAlarmTableCreateSql = "CREATE TABLE IF NOT EXISTS `ai_agent`.`tb_dist_dau_alarm_history_"
                    + DateUtil.getTimeTableDate(nextMonthDate) +
                    "` (\r\n" + //
                    "	`sid` INT(11) NOT NULL AUTO_INCREMENT," +
                    "    `dau_id` INT(11) NOT NULL DEFAULT 0," +
                    "    `dau_alarm_type_id` INT(11) NOT NULL DEFAULT 0," +
                    "    `acknowledge` INT(11) NOT NULL DEFAULT 0," +
                    "    `alarm_level` INT(11) NOT NULL DEFAULT 0," +
                    "    `reg_date` DATETIME NOT NULL DEFAULT current_timestamp()," +
                    "    PRIMARY KEY (`sid`)" +
                    ")" +
                    "COLLATE='utf8_unicode_ci'" +
                    "ENGINE=InnoDB";

            try (PreparedStatement pstmt = conn.prepareStatement(distDauNextAlarmTableCreateSql)) {
                int result = pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            String scadaPreviousDauAlarmTableCreateSql = "CREATE TABLE IF NOT EXISTS `ai_agent`.`tb_scada_dau_alarm_history_"
                    + DateUtil.getTimeTableDate(previousMonthDate) +
                    "` (\r\n" + //
                    "	`sid` INT(11) NOT NULL AUTO_INCREMENT," +
                    "    `dau_id` INT(11) NOT NULL DEFAULT 0," +
                    "    `dau_alarm_type_id` INT(11) NOT NULL DEFAULT 0," +
                    "    `acknowledge` INT(11) NOT NULL DEFAULT 0," +
                    "    `alarm_level` INT(11) NOT NULL DEFAULT 0," +
                    "    `reg_date` DATETIME NOT NULL DEFAULT current_timestamp()," +
                    "    PRIMARY KEY (`sid`)" +
                    ")" +
                    "COLLATE='utf8_unicode_ci'" +
                    "ENGINE=InnoDB";

            try (PreparedStatement pstmt = conn.prepareStatement(scadaPreviousDauAlarmTableCreateSql)) {
                int result = pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            String scadaDauAlarmTableCreateSql = "CREATE TABLE IF NOT EXISTS `ai_agent`.`tb_scada_dau_alarm_history_"
                    + DateUtil.getTimeTableDate(now) +
                    "` (\r\n" + //
                    "	`sid` INT(11) NOT NULL AUTO_INCREMENT," +
                    "    `dau_id` INT(11) NOT NULL DEFAULT 0," +
                    "    `dau_alarm_type_id` INT(11) NOT NULL DEFAULT 0," +
                    "    `acknowledge` INT(11) NOT NULL DEFAULT 0," +
                    "    `alarm_level` INT(11) NOT NULL DEFAULT 0," +
                    "    `reg_date` DATETIME NOT NULL DEFAULT current_timestamp()," +
                    "    PRIMARY KEY (`sid`)" +
                    ")" +
                    "COLLATE='utf8_unicode_ci'" +
                    "ENGINE=InnoDB";

            try (PreparedStatement pstmt = conn.prepareStatement(scadaDauAlarmTableCreateSql)) {
                int result = pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            String scadaDauNextAlarmTableCreateSql = "CREATE TABLE IF NOT EXISTS `ai_agent`.`tb_scada_dau_alarm_history_"
                    + DateUtil.getTimeTableDate(nextMonthDate) +
                    "` (\r\n" + //
                    "	`sid` INT(11) NOT NULL AUTO_INCREMENT," +
                    "    `dau_id` INT(11) NOT NULL DEFAULT 0," +
                    "    `dau_alarm_type_id` INT(11) NOT NULL DEFAULT 0," +
                    "    `acknowledge` INT(11) NOT NULL DEFAULT 0," +
                    "    `alarm_level` INT(11) NOT NULL DEFAULT 0," +
                    "    `reg_date` DATETIME NOT NULL DEFAULT current_timestamp()," +
                    "    PRIMARY KEY (`sid`)" +
                    ")" +
                    "COLLATE='utf8_unicode_ci'" +
                    "ENGINE=InnoDB";

            try (PreparedStatement pstmt = conn.prepareStatement(scadaDauNextAlarmTableCreateSql)) {
                int result = pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            String transDauSlotPreviousAlarmTableCreateSql = "CREATE TABLE IF NOT EXISTS `ai_agent`.`tb_trans_dau_slot_alarm_history_"
                    + DateUtil.getTimeTableDate(previousMonthDate) +
                    "` (\r\n" + //
                    "    `sid` INT(11) NOT NULL AUTO_INCREMENT," +
                    "    `slot_id` INT(11) NOT NULL DEFAULT 0," +
                    "    `slot_alarm_type_id` INT(11) NOT NULL DEFAULT 0," +
                    "    `acknowledge` INT(11) NOT NULL DEFAULT 0," +
                    "    `alarm_level` INT(11) NOT NULL DEFAULT 0," +
                    "    `reg_date` DATETIME NOT NULL DEFAULT current_timestamp()," +
                    "    PRIMARY KEY (`sid`)" +
                    ")" +
                    "COLLATE='utf8_unicode_ci'" +
                    "ENGINE=InnoDB";

            try (PreparedStatement pstmt = conn.prepareStatement(transDauSlotPreviousAlarmTableCreateSql)) {
                int result = pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            String transDauSlotAlarmTableCreateSql = "CREATE TABLE IF NOT EXISTS `ai_agent`.`tb_trans_dau_slot_alarm_history_"
                    + DateUtil.getTimeTableDate(now) +
                    "` (\r\n" + //
                    "    `sid` INT(11) NOT NULL AUTO_INCREMENT," +
                    "    `slot_id` INT(11) NOT NULL DEFAULT 0," +
                    "    `slot_alarm_type_id` INT(11) NOT NULL DEFAULT 0," +
                    "    `acknowledge` INT(11) NOT NULL DEFAULT 0," +
                    "    `alarm_level` INT(11) NOT NULL DEFAULT 0," +
                    "    `reg_date` DATETIME NOT NULL DEFAULT current_timestamp()," +
                    "    PRIMARY KEY (`sid`)" +
                    ")" +
                    "COLLATE='utf8_unicode_ci'" +
                    "ENGINE=InnoDB";

            try (PreparedStatement pstmt = conn.prepareStatement(transDauSlotAlarmTableCreateSql)) {
                int result = pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            String transDauSlotNextAlarmTableCreateSql = "CREATE TABLE IF NOT EXISTS `ai_agent`.`tb_trans_dau_slot_alarm_history_"
                    + DateUtil.getTimeTableDate(nextMonthDate) +
                    "` (\r\n" + //
                    "    `sid` INT(11) NOT NULL AUTO_INCREMENT," +
                    "    `slot_id` INT(11) NOT NULL DEFAULT 0," +
                    "    `slot_alarm_type_id` INT(11) NOT NULL DEFAULT 0," +
                    "    `acknowledge` INT(11) NOT NULL DEFAULT 0," +
                    "    `alarm_level` INT(11) NOT NULL DEFAULT 0," +
                    "    `reg_date` DATETIME NOT NULL DEFAULT current_timestamp()," +
                    "    PRIMARY KEY (`sid`)" +
                    ")" +
                    "COLLATE='utf8_unicode_ci'" +
                    "ENGINE=InnoDB";

            try (PreparedStatement pstmt = conn.prepareStatement(transDauSlotNextAlarmTableCreateSql)) {
                int result = pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            String scadaDauSlotPreviousAlarmTableCreateSql = "CREATE TABLE IF NOT EXISTS `ai_agent`.`tb_scada_dau_slot_alarm_history_"
                    + DateUtil.getTimeTableDate(previousMonthDate) +
                    "` (\r\n" + //
                    "    `sid` INT(11) NOT NULL AUTO_INCREMENT," +
                    "    `slot_id` INT(11) NOT NULL DEFAULT 0," +
                    "    `slot_alarm_type_id` INT(11) NOT NULL DEFAULT 0," +
                    "    `acknowledge` INT(11) NOT NULL DEFAULT 0," +
                    "    `alarm_level` INT(11) NOT NULL DEFAULT 0," +
                    "    `reg_date` DATETIME NOT NULL DEFAULT current_timestamp()," +
                    "    PRIMARY KEY (`sid`)" +
                    ")" +
                    "COLLATE='utf8_unicode_ci'" +
                    "ENGINE=InnoDB";

            try (PreparedStatement pstmt = conn.prepareStatement(scadaDauSlotPreviousAlarmTableCreateSql)) {
                int result = pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            String scadaDauSlotAlarmTableCreateSql = "CREATE TABLE IF NOT EXISTS `ai_agent`.`tb_scada_dau_slot_alarm_history_"
                    + DateUtil.getTimeTableDate(now) +
                    "` (\r\n" + //
                    "    `sid` INT(11) NOT NULL AUTO_INCREMENT," +
                    "    `slot_id` INT(11) NOT NULL DEFAULT 0," +
                    "    `slot_alarm_type_id` INT(11) NOT NULL DEFAULT 0," +
                    "    `acknowledge` INT(11) NOT NULL DEFAULT 0," +
                    "    `alarm_level` INT(11) NOT NULL DEFAULT 0," +
                    "    `reg_date` DATETIME NOT NULL DEFAULT current_timestamp()," +
                    "    PRIMARY KEY (`sid`)" +
                    ")" +
                    "COLLATE='utf8_unicode_ci'" +
                    "ENGINE=InnoDB";

            try (PreparedStatement pstmt = conn.prepareStatement(scadaDauSlotAlarmTableCreateSql)) {
                int result = pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            String scadaDauSlotNextAlarmTableCreateSql = "CREATE TABLE IF NOT EXISTS `ai_agent`.`tb_scada_dau_slot_alarm_history_"
                    + DateUtil.getTimeTableDate(nextMonthDate) +
                    "` (\r\n" + //
                    "    `sid` INT(11) NOT NULL AUTO_INCREMENT," +
                    "    `slot_id` INT(11) NOT NULL DEFAULT 0," +
                    "    `slot_alarm_type_id` INT(11) NOT NULL DEFAULT 0," +
                    "    `acknowledge` INT(11) NOT NULL DEFAULT 0," +
                    "    `alarm_level` INT(11) NOT NULL DEFAULT 0," +
                    "    `reg_date` DATETIME NOT NULL DEFAULT current_timestamp()," +
                    "    PRIMARY KEY (`sid`)" +
                    ")" +
                    "COLLATE='utf8_unicode_ci'" +
                    "ENGINE=InnoDB";

            try (PreparedStatement pstmt = conn.prepareStatement(scadaDauSlotNextAlarmTableCreateSql)) {
                int result = pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            String systemPreviousAlarmTableCreateSql = "CREATE TABLE IF NOT EXISTS `ai_agent`.`tb_system_alarm_history_"
                    + DateUtil.getTimeTableDate(previousMonthDate) +
                    "` (\r\n" + //
                    "    `alarm_id` INT(11) NOT NULL AUTO_INCREMENT," +
                    "    `alarm_type_id` INT(11) NOT NULL DEFAULT 0," +
                    "    `alarm_level` INT(11) NOT NULL DEFAULT 0," +
                    "    `acknowledge` INT(11) NOT NULL DEFAULT 0," +
                    "    `reg_date` DATETIME NOT NULL DEFAULT current_timestamp()," +
                    "    PRIMARY KEY (`alarm_id`)" +
                    ")" +
                    "COLLATE='utf8_general_ci'" +
                    "ENGINE=InnoDB;";

            try (PreparedStatement pstmt = conn.prepareStatement(systemPreviousAlarmTableCreateSql)) {
                int result = pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            String systemAlarmTableCreateSql = "CREATE TABLE IF NOT EXISTS `ai_agent`.`tb_system_alarm_history_"
                    + DateUtil.getTimeTableDate(now) +
                    "` (\r\n" + //
                    "    `alarm_id` INT(11) NOT NULL AUTO_INCREMENT," +
                    "    `alarm_type_id` INT(11) NOT NULL DEFAULT 0," +
                    "    `alarm_level` INT(11) NOT NULL DEFAULT 0," +
                    "    `acknowledge` INT(11) NOT NULL DEFAULT 0," +
                    "    `reg_date` DATETIME NOT NULL DEFAULT current_timestamp()," +
                    "    PRIMARY KEY (`alarm_id`)" +
                    ")" +
                    "COLLATE='utf8_general_ci'" +
                    "ENGINE=InnoDB;";

            try (PreparedStatement pstmt = conn.prepareStatement(systemAlarmTableCreateSql)) {
                int result = pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            String systemNextAlarmTableCreateSql = "CREATE TABLE IF NOT EXISTS `ai_agent`.`tb_system_alarm_history_"
                    + DateUtil.getTimeTableDate(nextMonthDate) +
                    "` (\r\n" + //
                    "    `alarm_id` INT(11) NOT NULL AUTO_INCREMENT," +
                    "    `alarm_type_id` INT(11) NOT NULL DEFAULT 0," +
                    "    `alarm_level` INT(11) NOT NULL DEFAULT 0," +
                    "    `acknowledge` INT(11) NOT NULL DEFAULT 0," +
                    "    `reg_date` DATETIME NOT NULL DEFAULT current_timestamp()," +
                    "    PRIMARY KEY (`alarm_id`)" +
                    ")" +
                    "COLLATE='utf8_general_ci'" +
                    "ENGINE=InnoDB;";

            try (PreparedStatement pstmt = conn.prepareStatement(systemNextAlarmTableCreateSql)) {
                int result = pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            // TODO: handle exception
        } finally {
            DBConnectionManager.close(conn);

        }

    }

}
