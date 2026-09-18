package com.ls.cmd_ai;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Table {
	String date;
	int tableType;
	String tableSchema;
	String tableName;
	List<String> joinTable = new ArrayList<String>();

	Table(String date, int tableType) {
		this.date = date;
		this.tableType = tableType;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public int getTableType() {
		return tableType;
	}

	public void setTableType(int tableType) {
		this.tableType = tableType;
	}

	public String getTableSchema() {
		return tableSchema;
	}

	public void setTableSchema(String tableSchema) {
		this.tableSchema = tableSchema;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public List<String> getJoinTable() {
		return joinTable;
	}

	public void setJoinTable(List<String> joinTable) {
		this.joinTable = joinTable;
	}

	public void addJoinTable(String joinTable) {
		this.joinTable.add(joinTable);
	}
}

public class NewAlarmTimerTask extends TimerTask {
	private static final Logger logger = LoggerFactory.getLogger(NewAlarmTimerTask.class);

	static int apiConnectTimeout = 0;
	static int apiReadTimeout = 0;
	static int alarmHistoryRange = 0;

	NewAlarmTimerTask(int apiConnectTimeout, int apiReadTimeout, int alarmHistoryRange) {
		this.apiConnectTimeout = apiConnectTimeout;
		this.apiReadTimeout = apiReadTimeout;
		this.alarmHistoryRange = alarmHistoryRange;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		logger.info("New Alarm Timer Task Executed");

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			conn = DBConnectionManager.getConnection();

			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			Date sd = new Date();
			sd.setMonth(sd.getMonth() - 1);
			Date ed = new Date();

			List<String> alarmListDateList = getDataTableList(sd, ed);

			String checkAiAlarmTableSql = "SELECT `table_name` AS `tableName` " +
					"FROM information_schema.tables " +
					"WHERE table_schema = 'ai_agent' AND (TABLE_NAME LIKE 'tb_dist_alarm_history_%' OR TABLE_NAME LIKE 'tb_trans_alarm_history_%' OR TABLE_NAME LIKE 'tb_scada_alarm_history_%' OR TABLE_NAME LIKE 'tb_dist_dau_alarm_history_%' OR TABLE_NAME LIKE 'tb_trans_dau_alarm_history_%' OR TABLE_NAME LIKE 'tb_scada_dau_alarm_history_%' OR TABLE_NAME LIKE 'tb_trans_dau_slot_alarm_history_%' OR TABLE_NAME LIKE 'tb_scada_dau_slot_alarm_history_%' OR TABLE_NAME LIKE 'tb_system_alarm_history_%')";

			pstmt = conn.prepareStatement(checkAiAlarmTableSql); // AI에게 전달한 알람 목록 조회
			rs = pstmt.executeQuery();

			List<String> aiAlarmTableNameList = new ArrayList<String>();

			while (rs.next()) {
				aiAlarmTableNameList.add(rs.getString("tableName"));
			}

			rs.close(); // 기존 결과셋 닫기
			pstmt.close(); // 기존 문장 객체 닫기

			String sendAlarmListSql = ""; // AI에게 전달한 알람 목록 조회

			for (int i = 0; i < alarmListDateList.size(); i++) {

				if (aiAlarmTableNameList.indexOf("tb_dist_alarm_history_" + alarmListDateList.get(i)) > -1) {
					sendAlarmListSql += "( " +
							"SELECT CONCAT ('" + alarmListDateList.get(i) + "', '-dist', alarm_id) AS alarmUniqueID," +
							"alarm_id, acknowledge, reg_date, alarm_level FROM `ai_agent`.`tb_dist_alarm_history_"
							+ alarmListDateList.get(i) + "` WHERE alarm_level > 0 )" +
							"UNION ALL";
				}

				if (aiAlarmTableNameList.indexOf("tb_trans_alarm_history_" + alarmListDateList.get(i)) > -1) {
					sendAlarmListSql += "( " +
							"SELECT CONCAT ('" + alarmListDateList.get(i) + "', '-trans', alarm_id) AS alarmUniqueID," +
							"alarm_id, acknowledge, reg_date, alarm_level FROM `ai_agent`.`tb_trans_alarm_history_"
							+ alarmListDateList.get(i) + "` WHERE alarm_level > 0 )" +
							"UNION ALL";
				}

				if (aiAlarmTableNameList.indexOf("tb_scada_alarm_history_" + alarmListDateList.get(i)) > -1) {
					sendAlarmListSql += "( " +
							"SELECT CONCAT ('" + alarmListDateList.get(i) + "', '-scada', alarm_id) AS alarmUniqueID," +
							"alarm_id, acknowledge, reg_date, alarm_level FROM `ai_agent`.`tb_scada_alarm_history_"
							+ alarmListDateList.get(i) + "` WHERE alarm_level > 0 )" +
							"UNION ALL";
				}

				if (aiAlarmTableNameList.indexOf("tb_dist_dau_alarm_history_" + alarmListDateList.get(i)) > -1) {
					sendAlarmListSql += "( " +
							"SELECT CONCAT ('" + alarmListDateList.get(i) + "', '-dauDist', sid) AS alarmUniqueID," +
							"sid as alarm_id, acknowledge, reg_date, alarm_level FROM `ai_agent`.`tb_dist_dau_alarm_history_"
							+ alarmListDateList.get(i) + "` WHERE alarm_level > 0 )" +
							"UNION ALL";
				}

				if (aiAlarmTableNameList.indexOf("tb_trans_dau_alarm_history_" + alarmListDateList.get(i)) > -1) {
					sendAlarmListSql += "( " +
							"SELECT CONCAT ('" + alarmListDateList.get(i) + "', '-dauTrans', sid) AS alarmUniqueID," +
							"sid as alarm_id, acknowledge, reg_date, alarm_level FROM `ai_agent`.`tb_trans_dau_alarm_history_"
							+ alarmListDateList.get(i) + "` WHERE alarm_level > 0 )" +
							"UNION ALL";
				}

				if (aiAlarmTableNameList.indexOf("tb_scada_dau_alarm_history_" + alarmListDateList.get(i)) > -1) {
					sendAlarmListSql += "( " +
							"SELECT CONCAT ('" + alarmListDateList.get(i) + "', '-dauScada', sid) AS alarmUniqueID," +
							"sid as alarm_id, acknowledge, reg_date, alarm_level FROM `ai_agent`.`tb_scada_dau_alarm_history_"
							+ alarmListDateList.get(i) + "` WHERE alarm_level > 0 )" +
							"UNION ALL";
				}

				if (aiAlarmTableNameList.indexOf("tb_trans_dau_slot_alarm_history_" + alarmListDateList.get(i)) > -1) {
					sendAlarmListSql += "( " +
							"SELECT CONCAT ('" + alarmListDateList.get(i) + "', '-dauSlotTrans', sid) AS alarmUniqueID,"
							+
							"sid as alarm_id, acknowledge, reg_date, alarm_level FROM `ai_agent`.`tb_trans_dau_slot_alarm_history_"
							+ alarmListDateList.get(i) + "` WHERE alarm_level > 0 )" +
							"UNION ALL";
				}

				if (aiAlarmTableNameList.indexOf("tb_scada_dau_slot_alarm_history_" + alarmListDateList.get(i)) > -1) {
					sendAlarmListSql += "( " +
							"SELECT CONCAT ('" + alarmListDateList.get(i) + "', '-dauSlotScada', sid) AS alarmUniqueID,"
							+
							"sid as alarm_id, acknowledge, reg_date, alarm_level FROM `ai_agent`.`tb_scada_dau_slot_alarm_history_"
							+ alarmListDateList.get(i) + "` WHERE alarm_level > 0 )" +
							"UNION ALL";
				}

				if (aiAlarmTableNameList.indexOf("tb_system_alarm_history_" + alarmListDateList.get(i)) > -1) {
					sendAlarmListSql += "( " +
							"SELECT CONCAT ('" + alarmListDateList.get(i) + "', '-System', alarm_id) AS alarmUniqueID,"
							+
							"alarm_id, acknowledge, reg_date, alarm_level FROM `ai_agent`.`tb_system_alarm_history_"
							+ alarmListDateList.get(i) + "` WHERE alarm_level > 0 )" +
							"UNION ALL";
				}
			}

			if (sendAlarmListSql.length() <= "UNION ALL".length()) {
				return;
			}

			sendAlarmListSql = sendAlarmListSql.substring(0, sendAlarmListSql.length() - "UNION ALL".length());

			// System.out.println(sendAlarmListSql);

			pstmt = conn.prepareStatement(sendAlarmListSql); // AI에게 전달한 알람 목록 조회
			rs = pstmt.executeQuery();

			List<String> sendAlarmList = new ArrayList<String>();

			while (rs.next()) {
				sendAlarmList.add(rs.getString("alarmUniqueID"));
			}

			rs.close();
			pstmt.close();

			List<String> findTableNameList = new ArrayList<String>();
			findTableNameList.add("tb_alarm_history_");
			findTableNameList.add("tb_temp_result_alarm_data_");
			findTableNameList.add("tb_thermal_result_alarm_data_");
			findTableNameList.add("tb_humi_result_alarm_data_");
			findTableNameList.add("tb_bushing_result_alarm_data_");
			findTableNameList.add("tb_cbm_result_alarm_data_");
			findTableNameList.add("tb_dga_result_alarm_data_");
			findTableNameList.add("tb_oltc_result_alarm_data_");
			findTableNameList.add("tb_arrester_result_alarm_data_");
			findTableNameList.add("tb_gas_density_result_alarm_data_");
			findTableNameList.add("tb_pcs_result_alarm_data_");
			findTableNameList.add("tb_pqup_result_alarm_data_");
			findTableNameList.add("tb_dau_alarm_history_");
			findTableNameList.add("tb_dau_slot_alarm_history_");
			findTableNameList.add("tb_system_alarm_history_");
			findTableNameList.add("tb_dau_slot_alarm_history_");
			findTableNameList.add("tb_multi_gas_result_alarm_data_");

			String alarmTableCheckSql = " SELECT `table_name` AS `tableName`, table_schema AS `tableSchema` " +
					"FROM information_schema.tables " +
					"WHERE ( " +
					"table_schema = 'dau_data_basic' " +
					"OR table_schema = 'dau_data_dist' " +
					"OR table_schema = 'dau_data_trans' " +
					"OR table_schema = 'dau_data_scada' " +
					") " +
					"AND ( ";

			for (String tableName : findTableNameList) {
				alarmTableCheckSql += "TABLE_NAME LIKE '" + tableName + "%' OR ";
			}

			alarmTableCheckSql = alarmTableCheckSql.substring(0, alarmTableCheckSql.length() - 3);

			alarmTableCheckSql += " );";

			pstmt = conn.prepareStatement(alarmTableCheckSql);
			rs = pstmt.executeQuery();

			HashMap<String, List<String>> exsitTableList = new HashMap<String, List<String>>();

			while (rs.next()) {
				List<String> schemaList = new ArrayList<String>();
				String tableNmae = rs.getString("tableName");
				String tableSchema = rs.getString("tableSchema");
				if (exsitTableList.get(tableNmae) != null) {
					schemaList = exsitTableList.get(tableNmae);
				}
				;

				if (schemaList.indexOf(tableSchema) < 0) {
					schemaList.add(tableSchema);
				}
				;

				exsitTableList.put(tableNmae, schemaList);
			}

			rs.close();
			pstmt.close();

			List<Table> tableList = new ArrayList<Table>();

			for (int i = 0; i < alarmListDateList.size(); i++) {
				String _date = alarmListDateList.get(i);
				if (exsitTableList.get("tb_alarm_history_" + _date) != null) {
					List<String> schemaList = exsitTableList.get("tb_alarm_history_" + _date);

					if (schemaList.indexOf("dau_data_trans") > -1) {
						Table table = new Table(_date, 1);
						if (exsitTableList.get("tb_bushing_result_alarm_data_" + _date) != null) {
							schemaList = exsitTableList.get("tb_bushing_result_alarm_data_" + _date);
							if (schemaList.indexOf("dau_data_trans") > -1) {
								table.addJoinTable("bushingResultAlarmData");
							}
						}
						if (exsitTableList.get("tb_cbm_result_alarm_data_" + _date) != null) {
							schemaList = exsitTableList.get("tb_cbm_result_alarm_data_" + _date);
							if (schemaList.indexOf("dau_data_trans") > -1) {
								table.addJoinTable("cbmResultAlarmData");
							}
						}
						if (exsitTableList.get("tb_dga_result_alarm_data_" + _date) != null) {
							schemaList = exsitTableList.get("tb_dga_result_alarm_data_" + _date);
							if (schemaList.indexOf("dau_data_trans") > -1) {
								table.addJoinTable("dgaResultAlarmData");
							}
						}
						if (exsitTableList.get("tb_oltc_result_alarm_data_" + _date) != null) {
							schemaList = exsitTableList.get("tb_oltc_result_alarm_data_" + _date);
							if (schemaList.indexOf("dau_data_trans") > -1) {
								table.addJoinTable("oltcResultAlarmData");
							}
						}
						if (exsitTableList.get("tb_arrester_result_alarm_data_" + _date) != null) {
							schemaList = exsitTableList.get("tb_arrester_result_alarm_data_" + _date);
							if (schemaList.indexOf("dau_data_trans") > -1) {
								table.addJoinTable("arresterResultAlarmData");
							}
						}
						if (exsitTableList.get("tb_gas_density_result_alarm_data_" + _date) != null) {
							schemaList = exsitTableList.get("tb_gas_density_result_alarm_data_" + _date);
							if (schemaList.indexOf("dau_data_trans") > -1) {
								table.addJoinTable("gasResultAlarmData");
							}
						}

						if (exsitTableList.get("tb_pcs_result_alarm_data_" + _date) != null) {
							schemaList = exsitTableList.get("tb_pcs_result_alarm_data_" + _date);
							if (schemaList.indexOf("dau_data_trans") > -1) {
								table.addJoinTable("pcsResultAlarmData");
							}
						}

						if (exsitTableList.get("tb_pqup_result_alarm_data_" + _date) != null) {
							schemaList = exsitTableList.get("tb_pqup_result_alarm_data_" + _date);
							if (schemaList.indexOf("dau_data_trans") > -1) {
								table.addJoinTable("pqupResultAlarmData");
							}
						}

						tableList.add(table);
					}
					;

					schemaList = exsitTableList.get("tb_alarm_history_" + _date);

					if (schemaList.indexOf("dau_data_dist") > -1) {
						Table table = new Table(_date, 2);
						if (exsitTableList.get("tb_temp_result_alarm_data_" + _date) != null) {
							schemaList = exsitTableList.get("tb_temp_result_alarm_data_" + _date);
							if (schemaList.indexOf("dau_data_dist") > -1) {
								table.addJoinTable("tempResultAlarmData");
							}
						}
						if (exsitTableList.get("tb_thermal_result_alarm_data_" + _date) != null) {
							schemaList = exsitTableList.get("tb_thermal_result_alarm_data_" + _date);
							if (schemaList.indexOf("dau_data_dist") > -1) {
								table.addJoinTable("thermalResultAlarmData");
							}
						}
						if (exsitTableList.get("tb_humi_result_alarm_data_" + _date) != null) {
							schemaList = exsitTableList.get("tb_humi_result_alarm_data_" + _date);
							if (schemaList.indexOf("dau_data_dist") > -1) {
								table.addJoinTable("humiResultAlarmData");
							}
						}

						if (exsitTableList.get("tb_multi_gas_result_alarm_data_" + _date) != null) {
							schemaList = exsitTableList.get("tb_multi_gas_result_alarm_data_" + _date);
							table.addJoinTable("multiGasResultAlarmData");
						}
						tableList.add(table);
					}
					;

					schemaList = exsitTableList.get("tb_alarm_history_" + _date);

					if (schemaList.indexOf("dau_data_scada") > -1) {
						Table table = new Table(_date, 7);
						tableList.add(table);
					}
				}

				if (exsitTableList.get("tb_dau_alarm_history_" + _date) != null) {
					List<String> schemaList = exsitTableList.get("tb_dau_alarm_history_" + _date);

					if (schemaList.indexOf("dau_data_trans") > -1) {
						Table table = new Table(_date, 3);
						tableList.add(table);
					}

					if (schemaList.indexOf("dau_data_dist") > -1) {
						Table table = new Table(_date, 4);
						tableList.add(table);
					}

					if (schemaList.indexOf("dau_data_scada") > -1) {
						Table table = new Table(_date, 6);
						tableList.add(table);
					}
				}

				if (exsitTableList.get("tb_dau_slot_alarm_history_" + _date) != null) {
					List<String> schemaList = exsitTableList.get("tb_dau_slot_alarm_history_" +
							_date);

					if (schemaList.indexOf("dau_data_trans") > -1) {
						Table table = new Table(_date, 8);
						tableList.add(table);
					}

					if (schemaList.indexOf("dau_data_scada") > -1) {
						Table table = new Table(_date, 9);
						tableList.add(table);
					}
				}

				if (exsitTableList.get("tb_system_alarm_history_" + _date) != null) {
					Table table = new Table(_date, 5);
					tableList.add(table);
				}
			}

			String totalAlarmListsql = "SELECT * FROM ( ";

			for (int i = 0; i < tableList.size(); i++) {
				Table table = tableList.get(i);
				if (table.getTableType() == 2) { // 배전 알람
					totalAlarmListsql += "( " +
							" SELECT CONCAT('" + table.getDate() + "-', 'dist', MAX(A.alarm_id)) AS alarmUniqueID, " +
							" MAX(A.`alarm_id`) AS `alarm_id`, " +
							" G.`site_id`, " +
							" G.`site_name`, " +
							" F.`building_id`, " +
							" F.`building_name`, " +
							" E.`room_id`, " +
							" E.`room_name`, " +
							" A.`panel_id`, " +
							" D.`panel_name`, " +
							" A.`diagnosis_id`, " +
							" B.`diagnosis_name`, " +
							" B.`diagnosis_item_id`, " +
							" A.`alarm_type_id`, " +
							" MAX(A.acknowledge) AS `acknowledge`, " +
							" MAX(A.reg_date) AS `reg_date`, " +
							" MAX(A.alarm_level) AS `alarm_level`, " +
							" COUNT(A.`alarm_id`) AS eventGroupCount, " +
							" GROUP_CONCAT(A.`alarm_id`) AS groupAlarmIDList, " +
							" GROUP_CONCAT(C.alarm_type_name ORDER BY A.alarm_id ASC ) AS alarm_type_name, " +
							" GROUP_CONCAT(C.alarm_type_id ORDER BY A.alarm_id ASC ) AS alarm_type_id_list, " +
							" C.alarm_sort_id, " +
							" -1 as dau_id," +
							" -1 as slot_id ";
					if (table.getJoinTable().size() > 0) {
						totalAlarmListsql += ",if( COALESCE(";

						if (table.getJoinTable().indexOf("tempResultAlarmData") > -1) {
							totalAlarmListsql += "I.event_id,";
						}

						if (table.getJoinTable().indexOf("thermalResultAlarmData") > -1) {
							totalAlarmListsql += "J.event_id,";
						}

						if (table.getJoinTable().indexOf("humiResultAlarmData") > -1) {
							totalAlarmListsql += "K.event_id,";
						}

						if (table.getJoinTable().indexOf("multiGasResultAlarmData") > -1) {
							totalAlarmListsql += "L.event_id,";
						}

						totalAlarmListsql = totalAlarmListsql.substring(0, totalAlarmListsql.length() - 1); // 쉼표 제거
						totalAlarmListsql += ") is NOT NULL AND A.alarm_level > 0, COALESCE(";

						if (table.getJoinTable().indexOf("tempResultAlarmData") > -1) {
							totalAlarmListsql += "I.event_id,";
						}

						if (table.getJoinTable().indexOf("thermalResultAlarmData") > -1) {
							totalAlarmListsql += "J.event_id,";
						}

						if (table.getJoinTable().indexOf("humiResultAlarmData") > -1) {
							totalAlarmListsql += "K.event_id,";
						}

						if (table.getJoinTable().indexOf("multiGasResultAlarmData") > -1) {
							totalAlarmListsql += "L.event_id,";
						}

						totalAlarmListsql = totalAlarmListsql.substring(0, totalAlarmListsql.length() - 1); // 쉼표 제거

						totalAlarmListsql += "), CONCAT('" + table.getDate()
								+ "-', 'dist', A.alarm_id) )  AS eventGroupID";

						totalAlarmListsql += ", COALESCE ( ";

						if (table.getJoinTable().indexOf("tempResultAlarmData") > -1) {
							totalAlarmListsql += "I.event_type_id,";
						}

						if (table.getJoinTable().indexOf("thermalResultAlarmData") > -1) {
							totalAlarmListsql += "J.event_type_id,";
						}

						if (table.getJoinTable().indexOf("humiResultAlarmData") > -1) {
							totalAlarmListsql += "K.event_type_id,";
						}

						if (table.getJoinTable().indexOf("multiGasResultAlarmData") > -1) {
							totalAlarmListsql += "L.event_type_id,";
						}

						totalAlarmListsql = totalAlarmListsql.substring(0, totalAlarmListsql.length() - 1); // 쉼표 제거
						totalAlarmListsql += ") AS event_type_id";

						totalAlarmListsql += ", COALESCE ( ";

						if (table.getJoinTable().indexOf("tempResultAlarmData") > -1) {
							totalAlarmListsql += "I.event_type_name,";
						}

						if (table.getJoinTable().indexOf("thermalResultAlarmData") > -1) {
							totalAlarmListsql += "J.event_type_name,";
						}

						if (table.getJoinTable().indexOf("humiResultAlarmData") > -1) {
							totalAlarmListsql += "K.event_type_name,";
						}

						if (table.getJoinTable().indexOf("multiGasResultAlarmData") > -1) {
							totalAlarmListsql += "L.event_type_name,";
						}

						totalAlarmListsql = totalAlarmListsql.substring(0, totalAlarmListsql.length() - 1); // 쉼표 제거
						totalAlarmListsql += ") AS event_type_name";

					} else {
						totalAlarmListsql += ",null AS eventGroupID, null as event_type_id, null as event_type_name";
					}

					totalAlarmListsql += " FROM ( SELECT * FROM `dau_data_dist`.`tb_alarm_history_" + table.getDate()
							+ "` WHERE 1=1 AND alarm_level > 0 ) AS A";

					totalAlarmListsql += " LEFT JOIN ( " +
							"		SELECT diagnosisList.*, IF(diagnosisList.is_61850 = 0, channelList.sensor_type , channelList61850.sensor_type_id ) AS sensor_type FROM `dau_info_h_mv`.tb_diagnosis_list diagnosisList "
							+
							"		LEFT JOIN `dau_info_h_mv`.tb_ch_sensor_list_dist chSensor ON diagnosisList.diagnosis_id = chSensor.diagnosis_id "
							+
							"		LEFT JOIN `dau_info_h_mv`.tb_channel_list_dist channelList ON channelList.channel_dist_id = chSensor.ch_id "
							+
							"		LEFT JOIN `dau_info_h_mv`.tb_ch_sensor_list_61850 chSensor61850 ON diagnosisList.diagnosis_id = chSensor61850.diagnosis_id "
							+
							"		LEFT JOIN `dau_info_h_mv`.tb_channel_list_61850 channelList61850 ON chSensor61850.ch_id = channelList61850.channel_id "
							+
							"		GROUP BY diagnosisList.diagnosis_id " +
							") as B on A.diagnosis_id = B.diagnosis_id " +
							"LEFT JOIN (SELECT alarm_type_id, alarm_type_name, alarm_sort_id FROM `dau_info_h_mv`.tb_alarm_type) as C on A.alarm_type_id = C.alarm_type_id "
							+
							"LEFT JOIN (SELECT * FROM `dau_info_h_mv`.tb_panel_list) as D on A.panel_id = D.panel_id " +
							"LEFT JOIN (SELECT room_id, building_id, room_name FROM `dau_info_h_mv`.tb_room) as E on D.room_id = E.room_id "
							+
							"LEFT JOIN (SELECT building_id, site_id, building_name FROM `dau_info_h_mv`.tb_building) as F on F.building_id = E.building_id "
							+
							"INNER JOIN ( " +
							"	SELECT site_id, site_name FROM `dau_info_h_mv`.tb_site WHERE enabled = 1 " +
							") as G on G.site_id = F.site_id ";

					if (table.getJoinTable().indexOf("tempResultAlarmData") > -1) {
						totalAlarmListsql += "LEFT JOIN (SELECT resultAlarm.*, eventHistory.event_type_id, eventType.event_type_name FROM dau_data_dist.tb_temp_result_alarm_data_"
								+ table.getDate() + " resultAlarm " +
								"LEFT JOIN dau_data_dist.tb_event_history_" + table.getDate()
								+ " eventHistory ON resultAlarm.event_id = eventHistory.event_id " +
								"LEFT JOIN dau_info_h_mv.tb_event_type eventType ON eventHistory.event_type_id = eventType.event_type_id ) AS I ON A.alarm_id = I.alarm_id ";
					}

					if (table.getJoinTable().indexOf("thermalResultAlarmData") > -1) {
						totalAlarmListsql += "LEFT JOIN (SELECT resultAlarm.*, eventHistory.event_type_id, eventType.event_type_name FROM dau_data_dist.tb_thermal_result_alarm_data_"
								+ table.getDate() + " resultAlarm " +
								"LEFT JOIN dau_data_dist.tb_event_history_" + table.getDate()
								+ " eventHistory ON resultAlarm.event_id = eventHistory.event_id " +
								"LEFT JOIN dau_info_h_mv.tb_event_type eventType ON eventHistory.event_type_id = eventType.event_type_id ) AS J ON A.alarm_id = J.alarm_id ";
					}

					if (table.getJoinTable().indexOf("humiResultAlarmData") > -1) {
						totalAlarmListsql += "LEFT JOIN (SELECT resultAlarm.*, eventHistory.event_type_id, eventType.event_type_name FROM dau_data_dist.tb_humi_result_alarm_data_"
								+ table.getDate() + " resultAlarm " +
								"LEFT JOIN dau_data_dist.tb_event_history_" + table.getDate()
								+ " eventHistory ON resultAlarm.event_id = eventHistory.event_id " +
								"LEFT JOIN dau_info_h_mv.tb_event_type eventType ON eventHistory.event_type_id = eventType.event_type_id ) AS K ON A.alarm_id = K.alarm_id ";
					}

					if (table.getJoinTable().indexOf("multiGasResultAlarmData") > -1) {
						totalAlarmListsql += "LEFT JOIN (SELECT resultAlarm.*, eventHistory.event_type_id, eventType.event_type_name FROM dau_data_dist.tb_multi_gas_result_alarm_data_"
								+ table.getDate() + " resultAlarm " +
								"LEFT JOIN dau_data_dist.tb_event_history_" + table.getDate()
								+ " eventHistory ON resultAlarm.event_id = eventHistory.event_id " +
								"LEFT JOIN dau_info_h_mv.tb_event_type eventType ON eventHistory.event_type_id = eventType.event_type_id ) AS L ON A.alarm_id = L.alarm_id ";
					}

					totalAlarmListsql += " WHERE B.matched = 1 " +
							"AND ( CASE " +
							"				 	WHEN ( alarm_sort_id = 6 AND alarm_level = 0 ) THEN 0" +
							"				 	WHEN ( alarm_sort_id = 7 AND alarm_level = 0 ) THEN 0" +
							"			 	    WHEN ( alarm_sort_id = 1 AND alarm_level = 0 ) THEN 0" +
							"					ELSE 1 END ) = 1" +
							"		  AND (" +
							"		  	( B.sensor_type NOT IN (2, 19) ) " +
							"		  	OR (" +
							"		  	  B.sensor_type IN (2, 19)" +
							"		  	  AND A.alarm_type_id = 70" +
							"		  	)" +
							"		  )";

					if (table.getJoinTable().size() > 0) {
						totalAlarmListsql += "GROUP BY eventGroupID)";
					} else {
						totalAlarmListsql += ")";
					}
				}

				if (table.getTableType() == 1) { // 송전 알람
					totalAlarmListsql += "( SELECT " +
							"CONCAT('" + table.getDate() + "-', 'trans', MAX(A.alarm_id)) AS alarmUniqueID,  " +
							"MAX(A.`alarm_id`) AS `alarm_id`, " +
							"G.`site_id`, " +
							" G.`site_name`, " +
							"F.`building_id`, " +
							" F.`building_name`, " +
							"E.`room_id`, " +
							" E.`room_name`, " +
							"A.`panel_id`, " +
							" D.`panel_name`, " +
							"A.`diagnosis_id`, " +
							" B.`diagnosis_name`, " +
							"B.`diagnosis_item_id`, " +
							"A.`alarm_type_id`, " +
							"MAX(A.acknowledge) AS `acknowledge`, " +
							"MAX(A.reg_date) AS `reg_date`, " +
							"MAX(A.alarm_level) AS `alarm_level`, " +
							"COUNT(A.`alarm_id`) AS eventGroupCount, " +
							"GROUP_CONCAT(A.`alarm_id`) AS groupAlarmIDList, " +
							"GROUP_CONCAT(C.alarm_type_name ORDER BY A.alarm_id ASC) AS alarm_type_name, " +
							"GROUP_CONCAT(C.alarm_type_id ORDER BY A.alarm_id ASC ) AS alarm_type_id_list, " +
							"C.alarm_sort_id, " +
							"-1 as dau_id, " +
							"-1 as slot_id";
					if (table.getJoinTable().size() > 0) {
						totalAlarmListsql += ",if( COALESCE(";

						if (table.getJoinTable().indexOf("bushingResultAlarmData") > -1) {
							totalAlarmListsql += "H.event_id,";
						}

						if (table.getJoinTable().indexOf("cbmResultAlarmData") > -1) {
							totalAlarmListsql += "I.event_id,";
						}

						if (table.getJoinTable().indexOf("dgaResultAlarmData") > -1) {
							totalAlarmListsql += "J.event_id,";
						}

						if (table.getJoinTable().indexOf("oltcResultAlarmData") > -1) {
							totalAlarmListsql += "K.event_id,";
						}

						if (table.getJoinTable().indexOf("arresterResultAlarmData") > -1) {
							totalAlarmListsql += "L.event_id,";
						}

						if (table.getJoinTable().indexOf("gasResultAlarmData") > -1) {
							totalAlarmListsql += "M.event_id,";
						}

						if (table.getJoinTable().indexOf("pcsResultAlarmData") > -1) {
							totalAlarmListsql += "N.event_id,";
						}

						if (table.getJoinTable().indexOf("pqupResultAlarmData") > -1) {
							totalAlarmListsql += "O.event_id,";
						}

						totalAlarmListsql = totalAlarmListsql.substring(0, totalAlarmListsql.length() - 1); // 쉼표 제거
						totalAlarmListsql += ") is NOT NULL AND A.alarm_level > 0, COALESCE(";

						if (table.getJoinTable().indexOf("bushingResultAlarmData") > -1) {
							totalAlarmListsql += "H.event_id,";
						}

						if (table.getJoinTable().indexOf("cbmResultAlarmData") > -1) {
							totalAlarmListsql += "I.event_id,";
						}

						if (table.getJoinTable().indexOf("dgaResultAlarmData") > -1) {
							totalAlarmListsql += "J.event_id,";
						}

						if (table.getJoinTable().indexOf("oltcResultAlarmData") > -1) {
							totalAlarmListsql += "K.event_id,";
						}

						if (table.getJoinTable().indexOf("arresterResultAlarmData") > -1) {
							totalAlarmListsql += "L.event_id,";
						}

						if (table.getJoinTable().indexOf("gasResultAlarmData") > -1) {
							totalAlarmListsql += "M.event_id,";
						}

						if (table.getJoinTable().indexOf("pcsResultAlarmData") > -1) {
							totalAlarmListsql += "N.event_id,";
						}

						if (table.getJoinTable().indexOf("pqupResultAlarmData") > -1) {
							totalAlarmListsql += "O.event_id,";
						}

						totalAlarmListsql = totalAlarmListsql.substring(0, totalAlarmListsql.length() - 1); // 쉼표 제거
						totalAlarmListsql += "), CONCAT('" + table.getDate()
								+ "-', 'trans', A.alarm_id) )  AS eventGroupID";

						totalAlarmListsql += ", COALESCE( ";

						if (table.getJoinTable().indexOf("bushingResultAlarmData") > -1) {
							totalAlarmListsql += "H.event_type_id,";
						}

						if (table.getJoinTable().indexOf("cbmResultAlarmData") > -1) {
							totalAlarmListsql += "I.event_type_id,";
						}

						if (table.getJoinTable().indexOf("dgaResultAlarmData") > -1) {
							totalAlarmListsql += "J.event_type_id,";
						}

						if (table.getJoinTable().indexOf("oltcResultAlarmData") > -1) {
							totalAlarmListsql += "K.event_type_id,";
						}

						if (table.getJoinTable().indexOf("arresterResultAlarmData") > -1) {
							totalAlarmListsql += "L.event_type_id,";
						}

						if (table.getJoinTable().indexOf("gasResultAlarmData") > -1) {
							totalAlarmListsql += "M.event_type_id,";
						}

						if (table.getJoinTable().indexOf("pcsResultAlarmData") > -1) {
							totalAlarmListsql += "N.event_type_id,";
						}

						if (table.getJoinTable().indexOf("pqupResultAlarmData") > -1) {
							totalAlarmListsql += "O.event_type_id,";
						}
						totalAlarmListsql = totalAlarmListsql.substring(0, totalAlarmListsql.length() - 1); // 쉼표 제거
						totalAlarmListsql += ") as event_type_id";

						totalAlarmListsql += ", COALESCE( ";

						if (table.getJoinTable().indexOf("bushingResultAlarmData") > -1) {
							totalAlarmListsql += "H.event_type_name,";
						}

						if (table.getJoinTable().indexOf("cbmResultAlarmData") > -1) {
							totalAlarmListsql += "I.event_type_name,";
						}

						if (table.getJoinTable().indexOf("dgaResultAlarmData") > -1) {
							totalAlarmListsql += "J.event_type_name,";
						}

						if (table.getJoinTable().indexOf("oltcResultAlarmData") > -1) {
							totalAlarmListsql += "K.event_type_name,";
						}

						if (table.getJoinTable().indexOf("arresterResultAlarmData") > -1) {
							totalAlarmListsql += "L.event_type_name,";
						}

						if (table.getJoinTable().indexOf("gasResultAlarmData") > -1) {
							totalAlarmListsql += "M.event_type_name,";
						}

						if (table.getJoinTable().indexOf("pcsResultAlarmData") > -1) {
							totalAlarmListsql += "N.event_type_name,";
						}

						if (table.getJoinTable().indexOf("pqupResultAlarmData") > -1) {
							totalAlarmListsql += "O.event_type_name,";
						}
						totalAlarmListsql = totalAlarmListsql.substring(0, totalAlarmListsql.length() - 1); // 쉼표 제거
						totalAlarmListsql += ") as event_type_name";

					} else {
						totalAlarmListsql += ",null AS eventGroupID, null as event_type_id, null as event_type_name";
					}

					totalAlarmListsql += " FROM ( SELECT * FROM `dau_data_trans`.tb_alarm_history_"
							+ table.getDate() + " WHERE alarm_level > 0 ) as A ";

					totalAlarmListsql += " LEFT JOIN ( " +
							"		SELECT diagnosisList.*, IF(diagnosisList.is_61850 = 0, channelList.sensor_type_id , channelList61850.sensor_type_id ) AS sensor_type FROM `dau_info_h_mv`.tb_diagnosis_list diagnosisList "
							+
							"		LEFT JOIN `dau_info_h_mv`.tb_ch_sensor_list_trans chSensor ON diagnosisList.diagnosis_id = chSensor.diagnosis_id "
							+
							"		LEFT JOIN `dau_info_h_mv`.tb_channel_list_trans channelList ON channelList.channel_id = chSensor.ch_id "
							+
							"		LEFT JOIN `dau_info_h_mv`.tb_ch_sensor_list_61850 chSensor61850 ON diagnosisList.diagnosis_id = chSensor61850.diagnosis_id "
							+
							"		LEFT JOIN `dau_info_h_mv`.tb_channel_list_61850 channelList61850 ON chSensor61850.ch_id = channelList61850.channel_id "
							+
							"		GROUP BY diagnosisList.diagnosis_id " +
							") as B on A.diagnosis_id = B.diagnosis_id " +
							"LEFT JOIN (SELECT alarm_type_id, alarm_type_name, alarm_sort_id FROM `dau_info_h_mv`.tb_alarm_type) as C on A.alarm_type_id = C.alarm_type_id "
							+
							"LEFT JOIN (SELECT * FROM `dau_info_h_mv`.tb_panel_list) as D on A.panel_id = D.panel_id " +
							"LEFT JOIN (SELECT room_id, building_id, room_name FROM `dau_info_h_mv`.tb_room) as E on D.room_id = E.room_id "
							+
							"LEFT JOIN (SELECT building_id, site_id, building_name FROM `dau_info_h_mv`.tb_building) as F on F.building_id = E.building_id "
							+
							"INNER JOIN ( " +
							"	SELECT site_id, site_name FROM `dau_info_h_mv`.tb_site WHERE enabled = 1 " +
							") as G on G.site_id = F.site_id ";

					if (table.getJoinTable().indexOf("bushingResultAlarmData") > -1) {
						totalAlarmListsql += "LEFT JOIN (SELECT resultAlarm.*, eventHistory.event_type_id, eventType.event_type_name FROM dau_data_trans.tb_bushing_result_alarm_data_"
								+ table.getDate() + " resultAlarm " +
								"LEFT JOIN dau_data_trans.tb_event_history_" + table.getDate()
								+ " eventHistory ON resultAlarm.event_id = eventHistory.event_id " +
								"LEFT JOIN dau_info_h_mv.tb_event_type eventType ON eventHistory.event_type_id = eventType.event_type_id) AS H ON A.alarm_id = H.alarm_id ";
					}

					if (table.getJoinTable().indexOf("cbmResultAlarmData") > -1) {
						totalAlarmListsql += "LEFT JOIN (SELECT resultAlarm.*, eventHistory.event_type_id, eventType.event_type_name FROM dau_data_trans.tb_cbm_result_alarm_data_"
								+ table.getDate() + " resultAlarm " +
								"LEFT JOIN dau_data_trans.tb_event_history_" + table.getDate()
								+ " eventHistory ON resultAlarm.event_id = eventHistory.event_id " +
								"LEFT JOIN dau_info_h_mv.tb_event_type eventType ON eventHistory.event_type_id = eventType.event_type_id) AS I ON A.alarm_id = I.alarm_id ";
					}

					if (table.getJoinTable().indexOf("dgaResultAlarmData") > -1) {
						totalAlarmListsql += "LEFT JOIN (SELECT resultAlarm.*, eventHistory.event_type_id, eventType.event_type_name FROM dau_data_trans.tb_dga_result_alarm_data_"
								+ table.getDate() + " resultAlarm " +
								"LEFT JOIN dau_data_trans.tb_event_history_" + table.getDate()
								+ " eventHistory ON resultAlarm.event_id = eventHistory.event_id " +
								"LEFT JOIN dau_info_h_mv.tb_event_type eventType ON eventHistory.event_type_id = eventType.event_type_id) AS J ON A.alarm_id = J.alarm_id ";
					}

					if (table.getJoinTable().indexOf("oltcResultAlarmData") > -1) {
						totalAlarmListsql += "LEFT JOIN (SELECT resultAlarm.*, eventHistory.event_type_id, eventType.event_type_name FROM dau_data_trans.tb_oltc_result_alarm_data_"
								+ table.getDate() + " resultAlarm " +
								"LEFT JOIN dau_data_trans.tb_event_history_" + table.getDate()
								+ " eventHistory ON resultAlarm.event_id = eventHistory.event_id " +
								"LEFT JOIN dau_info_h_mv.tb_event_type eventType ON eventHistory.event_type_id = eventType.event_type_id) AS K ON A.alarm_id = K.alarm_id ";
					}

					if (table.getJoinTable().indexOf("arresterResultAlarmData") > -1) {
						totalAlarmListsql += "LEFT JOIN (SELECT resultAlarm.*, eventHistory.event_type_id, eventType.event_type_name FROM dau_data_trans.tb_arrester_result_alarm_data_"
								+ table.getDate() + " resultAlarm " +
								"LEFT JOIN dau_data_trans.tb_event_history_" + table.getDate()
								+ " eventHistory ON resultAlarm.event_id = eventHistory.event_id " +
								"LEFT JOIN dau_info_h_mv.tb_event_type eventType ON eventHistory.event_type_id = eventType.event_type_id) AS L ON A.alarm_id = L.alarm_id ";
					}

					if (table.getJoinTable().indexOf("gasResultAlarmData") > -1) {
						totalAlarmListsql += "LEFT JOIN (SELECT resultAlarm.*, eventHistory.event_type_id, eventType.event_type_name FROM dau_data_trans.tb_gas_density_result_alarm_data_"
								+ table.getDate() + " resultAlarm " +
								"LEFT JOIN dau_data_trans.tb_event_history_" + table.getDate()
								+ " eventHistory ON resultAlarm.event_id = eventHistory.event_id " +
								"LEFT JOIN dau_info_h_mv.tb_event_type eventType ON eventHistory.event_type_id = eventType.event_type_id) AS M ON A.alarm_id = M.alarm_id ";
					}

					if (table.getJoinTable().indexOf("pcsResultAlarmData") > -1) {
						totalAlarmListsql += "LEFT JOIN (SELECT resultAlarm.alarm_id, resultAlarm.event_id, eventHistory.event_type_id, eventType.event_type_name FROM dau_data_trans.tb_pcs_result_alarm_data_"
								+ table.getDate() + " resultAlarm " +
								"LEFT JOIN dau_data_trans.tb_event_history_" + table.getDate()
								+ " eventHistory ON resultAlarm.event_id = eventHistory.event_id " +
								"LEFT JOIN dau_info_h_mv.tb_event_type eventType ON eventHistory.event_type_id = eventType.event_type_id) AS N ON A.alarm_id = N.alarm_id ";
					}

					if (table.getJoinTable().indexOf("pqupResultAlarmData") > -1) {
						totalAlarmListsql += "LEFT JOIN (SELECT resultAlarm.alarm_id, resultAlarm.event_id, eventHistory.event_type_id, eventType.event_type_name FROM dau_data_trans.tb_pqup_result_alarm_data_"
								+ table.getDate() + " resultAlarm " +
								"LEFT JOIN dau_data_trans.tb_event_history_" + table.getDate()
								+ " eventHistory ON resultAlarm.event_id = eventHistory.event_id " +
								"LEFT JOIN dau_info_h_mv.tb_event_type eventType ON eventHistory.event_type_id = eventType.event_type_id) AS O ON A.alarm_id = O.alarm_id ";
					}

					totalAlarmListsql += " WHERE B.matched = 1 " +
							"AND ( CASE " +
							"				 	WHEN ( alarm_sort_id = 6 AND alarm_level = 0 ) THEN 0" +
							"				 	WHEN ( alarm_sort_id = 7 AND alarm_level = 0 ) THEN 0" +
							"			 	    WHEN ( alarm_sort_id = 1 AND alarm_level = 0 ) THEN 0" +
							"					ELSE 1 END ) = 1" +
							"		  AND (" +
							"		  	( B.sensor_type NOT IN (2, 19) ) " +
							"		  	OR (" +
							"		  	  B.sensor_type IN (2, 19)" +
							"		  	  AND A.alarm_type_id = 70" +
							"		  	)" +
							"		  )";

					if (table.getJoinTable().size() > 0) {
						totalAlarmListsql += "GROUP BY eventGroupID )";
					} else {
						totalAlarmListsql += "GROUP BY alarmUniqueID )";
					}
				}

				if (table.getTableType() == 7) { // Scada 알람
					totalAlarmListsql += "( " +
							" SELECT " +
							"CONCAT('" + table.getDate() + "-', 'scada', A.alarm_id) AS alarmUniqueID, " +
							"MAX(A.`alarm_id`) AS `alarm_id`, " +
							"G.`site_id`, " +
							" G.`site_name`, " +
							"F.`building_id`, " +
							" F.`building_name`, " +
							"E.`room_id`, " +
							" E.`room_name`, " +
							"A.`panel_id`, " +
							" D.`panel_name`, " +
							"A.`diagnosis_id`, " +
							" B.`diagnosis_name`, " +
							"B.`diagnosis_item_id`, " +
							"A.`alarm_type_id`, " +
							"MAX(A.acknowledge) AS `acknowledge`, " +
							"MAX(A.reg_date) AS `reg_date`, " +
							"MAX(A.alarm_level) AS `alarm_level`, " +
							"COUNT(A.`alarm_id`) AS eventGroupCount, " +
							"GROUP_CONCAT(A.`alarm_id`) AS groupAlarmIDList, " +
							"GROUP_CONCAT(C.alarm_type_name ORDER BY A.alarm_id ASC ) AS alarm_type_name, " +
							"GROUP_CONCAT(C.alarm_type_id ORDER BY A.alarm_id ASC ) AS alarm_type_id_list, " +
							"C.alarm_sort_id, " +
							"-1 as dau_id, " +
							"-1 as slot_id," +
							"null AS eventGroupID, null as event_type_id, null as event_type_name ";

					totalAlarmListsql += "FROM ( SELECT * FROM `dau_data_scada`.tb_alarm_history_" + table.getDate()
							+ " WHERE alarm_level > 0 ) as A ";

					totalAlarmListsql += "LEFT JOIN ( " +
							"SELECT * FROM `dau_info_h_mv`.tb_diagnosis_list WHERE diagnosis_id IN ( " +
							"			SELECT diagnosisList.diagnosis_id FROM `dau_info_h_mv`.tb_diagnosis_list diagnosisList "
							+
							"			LEFT JOIN `dau_info_h_mv`.tb_ch_sensor_list_modbus chSensor ON diagnosisList.diagnosis_id = chSensor.diagnosis_id "
							+
							"			LEFT JOIN `dau_info_h_mv`.tb_channel_list_modbus channelList ON channelList.channel_id = chSensor.ch_id "
							+
							"			WHERE channelList.sensor_type_id NOT IN (2, 19) " +
							"			GROUP BY diagnosis_id " +
							"		) " +
							"	) as B on A.diagnosis_id = B.diagnosis_id " +
							"	LEFT JOIN (SELECT alarm_type_id, alarm_type_name, alarm_sort_id FROM `dau_info_h_mv`.tb_alarm_type) as C on A.alarm_type_id = C.alarm_type_id "
							+
							"	LEFT JOIN (SELECT * FROM `dau_info_h_mv`.tb_panel_list) as D on A.panel_id = D.panel_id "
							+
							"	LEFT JOIN (SELECT room_id, building_id, room_name FROM `dau_info_h_mv`.tb_room) as E on D.room_id = E.room_id "
							+
							"	LEFT JOIN (SELECT building_id, site_id, building_name FROM `dau_info_h_mv`.tb_building) as F on F.building_id = E.building_id "
							+
							"	INNER JOIN ( " +
							"		SELECT site_id, site_name FROM `dau_info_h_mv`.tb_site WHERE enabled = 1 " +
							"	) as G on G.site_id = F.site_id " +
							"	WHERE B.matched = 1 " +
							" AND ( CASE " +
							"				 	WHEN ( alarm_sort_id = 6 AND alarm_level = 0 ) THEN 0" +
							"				 	WHEN ( alarm_sort_id = 7 AND alarm_level = 0 ) THEN 0" +
							"			 	    WHEN ( alarm_sort_id = 1 AND alarm_level = 0 ) THEN 0" +
							"					ELSE 1 END ) = 1" +
							" GROUP BY eventGroupID)";
				}

				if (table.getTableType() == 3) { // 송전 DAU 알람
					totalAlarmListsql += "( " +
							" SELECT " +
							"CONCAT('" + table.getDate() + "-', 'dauTrans', A.sid) AS alarmUniqueID, " +
							"A.sid AS `alarm_id`, " +
							"null AS `site_id`, " +
							"null AS `site_name`, " +
							"null AS `building_id`, " +
							"null AS `building_name`, " +
							"null AS `room_id`, " +
							"null AS `room_name`, " +
							"null AS `panel_id`, " +
							"null AS `panel_name`, " +
							"null AS `diagnosis_id`, " +
							"null AS `diagnosis_name`, " +
							"null AS `diagnosis_item_id`, " +
							"A.dau_alarm_type_id AS `alarm_type_id`, " +
							"A.acknowledge AS `acknowledge`, " +
							"A.reg_date AS `reg_date`, " +
							"A.alarm_level AS `alarm_level`, " +
							"0 AS eventGroupCount, " +
							"'' AS groupAlarmIDList, " +
							"B.dau_alarm_type_name AS `alarm_type_name`, " +
							"B.dau_alarm_type_id AS alarm_type_id_list, " +
							"null AS `alarm_sort_id`, " +
							"A.dau_id as dau_id, " +
							"-1 as slot_id," +
							"null AS eventGroupID, null as event_type_id, null as event_type_name " +
							" FROM ( SELECT * FROM `dau_data_trans`.tb_dau_alarm_history_" + table.getDate()
							+ " WHERE alarm_level > 0 ) as A " +
							" LEFT JOIN (SELECT * FROM `dau_info_h_mv`.tb_dau_alarm_type) as B on A.dau_alarm_type_id = B.dau_alarm_type_id "
							+
							" INNER JOIN (SELECT * FROM `dau_info_h_mv`.tb_dau_list) as C on A.dau_id = C.dau_id )";
				}

				if (table.getTableType() == 4) { // 배전 DAU 알람
					totalAlarmListsql += "( " +
							" SELECT " +
							"CONCAT('" + table.getDate() + "-', 'dauDist', A.sid) AS alarmUniqueID, " +
							"A.sid AS `alarm_id`,  " +
							"null AS `site_id`, " +
							"null AS `site_name`, " +
							"null AS `building_id`, " +
							"null AS `building_name`, " +
							"null AS `room_id`, " +
							"null AS `room_name`, " +
							"null AS `panel_id`,  " +
							"null AS `panel_name`,  " +
							"null AS `diagnosis_id`,  " +
							"null AS `diagnosis_name`,  " +
							"null AS `diagnosis_item_id`, " +
							"A.dau_alarm_type_id AS `alarm_type_id`,  " +
							"A.acknowledge AS `acknowledge`,  " +
							"A.reg_date AS `reg_date`,  " +
							"A.alarm_level AS `alarm_level`, " +
							"0 AS eventGroupCount, " +
							"'' AS groupAlarmIDList, " +
							"B.dau_alarm_type_name AS `alarm_type_name`, " +
							"B.dau_alarm_type_id AS alarm_type_id_list, " +
							"null AS `alarm_sort_id`, " +
							"A.dau_id as dau_id, " +
							"-1 as slot_id," +
							"null AS eventGroupID, null as event_type_id, null as event_type_name " +
							" FROM ( SELECT * FROM `dau_data_dist`.tb_dau_alarm_history_" + table.getDate()
							+ " WHERE alarm_level > 0 ) as A " +
							" LEFT JOIN (SELECT * FROM `dau_info_h_mv`.tb_dau_alarm_type) as B on A.dau_alarm_type_id = B.dau_alarm_type_id "
							+
							" INNER JOIN (SELECT * FROM `dau_info_h_mv`.tb_dau_list) as C on A.dau_id = C.dau_id )";
				}

				if (table.getTableType() == 6) { // SCADA DAU 알람
					totalAlarmListsql += "( " +
							" SELECT " +
							"CONCAT('" + table.getDate() + "-', 'dauScada', A.sid) AS alarmUniqueID, " +
							"A.sid AS `alarm_id`,  " +
							"null AS `site_id`, " +
							"null AS `site_name`, " +
							"null AS `building_id`, " +
							"null AS `building_name`, " +
							"null AS `room_id`, " +
							"null AS `room_name`, " +
							"null AS `panel_id`,  " +
							"null AS `panel_name`,  " +
							"null AS `diagnosis_id`,  " +
							"null AS `diagnosis_name`,  " +
							"null AS `diagnosis_item_id`, " +
							"A.dau_alarm_type_id AS `alarm_type_id`,  " +
							"A.acknowledge AS `acknowledge`,  " +
							"A.reg_date AS `reg_date`,  " +
							"A.alarm_level AS `alarm_level`, " +
							"0 AS eventGroupCount, " +
							"'' AS groupAlarmIDList, " +
							"B.dau_alarm_type_name AS `alarm_type_name`, " +
							"B.dau_alarm_type_id AS alarm_type_id_list, " +
							"null AS `alarm_sort_id`, " +
							"A.dau_id as dau_id, " +
							"-1 as slot_id," +
							"null AS eventGroupID, null as event_type_id, null as event_type_name " +
							" FROM ( SELECT * FROM `dau_data_scada`.tb_dau_alarm_history_" + table.getDate()
							+ " WHERE alarm_level > 0 ) as A " +
							" LEFT JOIN (SELECT * FROM `dau_info_h_mv`.tb_dau_alarm_type) as B on A.dau_alarm_type_id = B.dau_alarm_type_id "
							+
							" INNER JOIN (SELECT * FROM `dau_info_h_mv`.tb_dau_list) as C on A.dau_id = C.dau_id )";
				}

				if (table.getTableType() == 8) { // 송전 DAU Slot 알람
					totalAlarmListsql += "( " +
							" SELECT " +
							"CONCAT('" + table.getDate() + "-', 'dauSlotTrans', A.sid) AS alarmUniqueID, " +
							"A.sid AS `alarm_id`, " +
							"null AS `site_id`, " +
							"null AS `site_name`, " +
							"null AS `building_id`, " +
							"null AS `building_name`, " +
							"null AS `room_id`, " +
							"null AS `room_name`, " +
							"null AS `panel_id`, " +
							"null AS `panel_name`, " +
							"null AS `diagnosis_id`, " +
							"null AS `diagnosis_name`, " +
							"null AS `diagnosis_item_id`, " +
							"null AS `alarm_type_id`, " +
							"A.acknowledge AS `acknowledge`, " +
							"A.reg_date AS `reg_date`, " +
							"A.alarm_level AS `alarm_level`, " +
							"0 AS eventGroupCount, " +
							"'' AS groupAlarmIDList, " +
							"B.slot_alarm_type_name AS `alarm_type_name`, " +
							"B.slot_alarm_type_id AS alarm_type_id_list, " +
							"null AS `alarm_sort_id`, " +
							"C.dau_id as dau_id, " +
							"A.slot_id as slot_id," +
							"null AS eventGroupID, null as event_type_id, null as event_type_name " +
							" FROM ( SELECT * FROM `dau_data_trans`.tb_dau_slot_alarm_history_" + table.getDate()
							+ " WHERE alarm_level > 0 ) as A " +
							" LEFT JOIN (SELECT * FROM `dau_info_h_mv`.tb_dau_slot_alarm_type) as B on A.slot_alarm_type_id = B.slot_alarm_type_id "
							+
							" INNER JOIN (SELECT * FROM `dau_info_h_mv`.tb_dau_slot_list) as C on A.slot_id = C.slot_id )";
				}

				if (table.getTableType() == 9) { // SCADA DAU Slot 알람
					totalAlarmListsql += "( " +
							" SELECT " +
							"CONCAT('" + table.getDate() + "-', 'dauSlotScada', A.sid) AS alarmUniqueID, " +
							"A.sid AS `alarm_id`, " +
							"null AS `site_id`, " +
							"null AS `site_name`, " +
							"null AS `building_id`, " +
							"null AS `building_name`, " +
							"null AS `room_id`, " +
							"null AS `room_name`, " +
							"null AS `panel_id`, " +
							"null AS `panel_name`, " +
							"null AS `diagnosis_id`, " +
							"null AS `diagnosis_name`, " +
							"null AS `diagnosis_item_id`, " +
							"null AS `alarm_type_id`, " +
							"A.acknowledge AS `acknowledge`, " +
							"A.reg_date AS `reg_date`, " +
							"A.alarm_level AS `alarm_level`, " +
							"0 AS eventGroupCount, " +
							"'' AS groupAlarmIDList, " +
							"B.slot_alarm_type_name AS `alarm_type_name`, " +
							"B.slot_alarm_type_id AS alarm_type_id_list, " +
							"null AS `alarm_sort_id`, " +
							"C.dau_id as dau_id, " +
							"A.slot_id as slot_id," +
							"null AS eventGroupID, null as event_type_id, null as event_type_name " +
							" FROM ( SELECT * FROM `dau_data_scada`.tb_dau_slot_alarm_history_" + table.getDate()
							+ " WHERE alarm_level > 0 ) as A " +
							" LEFT JOIN (SELECT * FROM `dau_info_h_mv`.tb_dau_slot_alarm_type) as B on A.slot_alarm_type_id = B.slot_alarm_type_id "
							+
							" INNER JOIN (SELECT * FROM `dau_info_h_mv`.tb_dau_slot_list) as C on A.slot_id = C.slot_id )";
				}

				if (table.getTableType() == 5) { // System Alarm
					totalAlarmListsql += "( " +
							" SELECT " +
							"CONCAT('" + table.getDate() + "-', 'System', A.alarm_id) AS alarmUniqueID, " +
							"A.alarm_id AS `alarm_id`, " +
							"null AS `site_id`, " +
							"null AS `site_name`, " +
							"null AS `building_id`, " +
							"null AS `building_name`, " +
							"null AS `room_id`, " +
							"null AS `room_name`, " +
							"null AS `panel_id`, " +
							"null AS `panel_name`, " +
							"null AS `diagnosis_id`, " +
							"null AS `diagnosis_name`, " +
							"null AS `diagnosis_item_id`, " +
							"A.`alarm_type_id` AS `alarm_type_id`, " +
							"A.acknowledge AS `acknowledge`, " +
							"A.reg_date AS `reg_date`, " +
							"a.alarm_level AS `alarm_level`, " +
							"0 AS eventGroupCount, " +
							"'' AS groupAlarmIDList, " +
							"C.alarm_type_name AS `alarm_type_name`, " +
							"C.alarm_type_id AS alarm_type_id_list, " +
							"null AS `alarm_sort_id`, " +
							"-1 as dau_id, " +
							"-1 as slot_id," +
							"null AS eventGroupID, null as event_type_id, null as event_type_name " +
							" FROM ( SELECT * FROM `dau_data_basic`.tb_system_alarm_history_" + table.getDate()
							+ " WHERE alarm_level > 0 ) as A " +
							" LEFT JOIN (SELECT alarm_type_id, alarm_type_name, alarm_sort_id FROM `dau_info_h_mv`.tb_alarm_type) as C on A.alarm_type_id = C.alarm_type_id )";
				}
				totalAlarmListsql += " UNION ALL ";
			}

			if (tableList.size() == 0) {
				logger.info("No alarm table ");
				return;
			}

			totalAlarmListsql = totalAlarmListsql.substring(0, totalAlarmListsql.length() - 10);

			totalAlarmListsql += ") AS A ORDER BY reg_date ASC, alarm_id ASC";

			// System.out.println(totalAlarmListsql);

			pstmt = conn.prepareStatement(totalAlarmListsql); // 동일한 변수에 새 객체 할당
			rs = pstmt.executeQuery();

			JSONArray newAlarmListJsonArray = new JSONArray();
			JSONArray totalAlarmListJson = new JSONArray();

			while (rs.next()) {
				JSONObject alarmJson = new JSONObject();
				alarmJson.put("alarmUniqueID", rs.getString("alarmUniqueID"));
				alarmJson.put("alarm_id", rs.getInt("alarm_id"));
				alarmJson.put("site_id", rs.getInt("site_id"));
				alarmJson.put("site_name", rs.getString("site_name"));
				alarmJson.put("building_id", rs.getInt("building_id"));
				alarmJson.put("building_name", rs.getString("building_name"));
				alarmJson.put("room_id", rs.getInt("room_id"));
				alarmJson.put("room_name", rs.getString("room_name"));
				alarmJson.put("panel_id", rs.getInt("panel_id"));
				alarmJson.put("panel_name", rs.getString("panel_name"));
				alarmJson.put("diagnosis_id", rs.getInt("diagnosis_id"));
				alarmJson.put("diagnosis_name", rs.getString("diagnosis_name"));
				alarmJson.put("diagnosis_item_id", rs.getInt("diagnosis_item_id"));
				alarmJson.put("alarm_type_id", rs.getInt("alarm_type_id"));
				alarmJson.put("acknowledge", rs.getInt("acknowledge"));
				alarmJson.put("reg_date", rs.getString("reg_date"));
				alarmJson.put("alarm_level", rs.getInt("alarm_level"));
				alarmJson.put("eventGroupCount", rs.getInt("eventGroupCount")); // 단일 알람 몇개 합쳐졌는지
				alarmJson.put("groupAlarmIDList", rs.getString("groupAlarmIDList"));
				alarmJson.put("alarm_type_name", rs.getString("alarm_type_name"));
				alarmJson.put("alarm_type_id_list", rs.getString("alarm_type_id_list"));
				alarmJson.put("dau_id", rs.getInt("dau_id"));
				alarmJson.put("event_type_id", rs.getInt("event_type_id"));
				alarmJson.put("event_type_name", rs.getString("event_type_name"));

				if (sendAlarmList.indexOf(alarmJson.get("alarmUniqueID")) < 0) {
					newAlarmListJsonArray.put(alarmJson);
				}

				totalAlarmListJson.put(alarmJson);

			}

			pstmt.close();
			rs.close();

			if (newAlarmListJsonArray.length() > 0) {

				String fullAlarmListSql = "SELECT * FROM ( ";

				for (int i = 0; i < tableList.size(); i++) {
					Table table = tableList.get(i);
					if (table.getTableType() == 2) { // 배전 알람
						fullAlarmListSql += "( " +
								" SELECT CONCAT('" + table.getDate() + "-', 'dist', A.alarm_id) AS alarmUniqueID, " +
								" A.`alarm_id`, " +
								" G.`site_id`, " +
								" G.`site_name`, " +
								" F.`building_id`, " +
								" F.`building_name`, " +
								" E.`room_id`, " +
								" E.`room_name`, " +
								" A.`panel_id`, " +
								" D.`panel_name`, " +
								" A.`diagnosis_id`, " +
								" B.`diagnosis_name`, " +
								" B.`diagnosis_item_id`, " +
								" A.`alarm_type_id`, " +
								" A.`acknowledge`, " +
								" A.`reg_date`, " +
								" A.`alarm_level`, " +
								" C.alarm_type_name AS alarm_type_name, " +
								" C.alarm_sort_id, " +
								" -1 as dau_id," +
								" -1 as slot_id ";
						fullAlarmListSql += ",null AS eventGroupID";

						fullAlarmListSql += " FROM ( SELECT * FROM `dau_data_dist`.`tb_alarm_history_" + table.getDate()
								+ "` WHERE 1=1 AND alarm_level > 0 ) AS A";

						fullAlarmListSql += " LEFT JOIN ( " +
								"		SELECT diagnosisList.*, IF(diagnosisList.is_61850 = 0, channelList.sensor_type , channelList61850.sensor_type_id ) AS sensor_type FROM `dau_info_h_mv`.tb_diagnosis_list diagnosisList "
								+
								"		LEFT JOIN `dau_info_h_mv`.tb_ch_sensor_list_dist chSensor ON diagnosisList.diagnosis_id = chSensor.diagnosis_id "
								+
								"		LEFT JOIN `dau_info_h_mv`.tb_channel_list_dist channelList ON channelList.channel_dist_id = chSensor.ch_id "
								+
								"		LEFT JOIN `dau_info_h_mv`.tb_ch_sensor_list_61850 chSensor61850 ON diagnosisList.diagnosis_id = chSensor61850.diagnosis_id "
								+
								"		LEFT JOIN `dau_info_h_mv`.tb_channel_list_61850 channelList61850 ON chSensor61850.ch_id = channelList61850.channel_id "
								+
								"		GROUP BY diagnosisList.diagnosis_id " +
								") as B on A.diagnosis_id = B.diagnosis_id " +
								"LEFT JOIN (SELECT alarm_type_id, alarm_type_name, alarm_sort_id FROM `dau_info_h_mv`.tb_alarm_type) as C on A.alarm_type_id = C.alarm_type_id "
								+
								"LEFT JOIN (SELECT * FROM `dau_info_h_mv`.tb_panel_list) as D on A.panel_id = D.panel_id "
								+
								"LEFT JOIN (SELECT room_id, building_id, room_name FROM `dau_info_h_mv`.tb_room) as E on D.room_id = E.room_id "
								+
								"LEFT JOIN (SELECT building_id, site_id, building_name FROM `dau_info_h_mv`.tb_building) as F on F.building_id = E.building_id "
								+
								"INNER JOIN ( " +
								"	SELECT site_id, site_name FROM `dau_info_h_mv`.tb_site WHERE enabled = 1 " +
								") as G on G.site_id = F.site_id ";

						fullAlarmListSql += " WHERE B.matched = 1 " +
								"AND ( CASE " +
								"				 	WHEN ( alarm_sort_id = 6 AND alarm_level = 0 ) THEN 0" +
								"				 	WHEN ( alarm_sort_id = 7 AND alarm_level = 0 ) THEN 0" +
								"			 	    WHEN ( alarm_sort_id = 1 AND alarm_level = 0 ) THEN 0" +
								"					ELSE 1 END ) = 1" +
								"		  AND (" +
								"		  	( B.sensor_type NOT IN (2, 19) ) " +
								"		  	OR (" +
								"		  	  B.sensor_type IN (2, 19)" +
								"		  	  AND A.alarm_type_id = 70" +
								"		  	)" +
								"		  )" +
								"		  )";

					}

					if (table.getTableType() == 1) { // 송전 알람
						fullAlarmListSql += "( SELECT " +
								"CONCAT('" + table.getDate() + "-', 'trans', A.alarm_id) AS alarmUniqueID,  " +
								"A.`alarm_id`, " +
								"G.`site_id`, " +
								" G.`site_name`, " +
								"F.`building_id`, " +
								" F.`building_name`, " +
								"E.`room_id`, " +
								" E.`room_name`, " +
								"A.`panel_id`, " +
								" D.`panel_name`, " +
								"A.`diagnosis_id`, " +
								" B.`diagnosis_name`, " +
								"B.`diagnosis_item_id`, " +
								"A.`alarm_type_id`, " +
								"A.`acknowledge`, " +
								"A.`reg_date`, " +
								"A.`alarm_level`, " +
								"C.alarm_type_name AS alarm_type_name, " +
								"C.alarm_sort_id, " +
								"-1 as dau_id, " +
								"-1 as slot_id";

						fullAlarmListSql += ",null AS eventGroupID";

						fullAlarmListSql += " FROM ( SELECT * FROM `dau_data_trans`.tb_alarm_history_"
								+ table.getDate() + " WHERE alarm_level > 0 ) as A ";

						fullAlarmListSql += " LEFT JOIN ( " +
								"		SELECT diagnosisList.*, IF(diagnosisList.is_61850 = 0, channelList.sensor_type_id , channelList61850.sensor_type_id ) AS sensor_type FROM `dau_info_h_mv`.tb_diagnosis_list diagnosisList "
								+
								"		LEFT JOIN `dau_info_h_mv`.tb_ch_sensor_list_trans chSensor ON diagnosisList.diagnosis_id = chSensor.diagnosis_id "
								+
								"		LEFT JOIN `dau_info_h_mv`.tb_channel_list_trans channelList ON channelList.channel_id = chSensor.ch_id "
								+
								"		LEFT JOIN `dau_info_h_mv`.tb_ch_sensor_list_61850 chSensor61850 ON diagnosisList.diagnosis_id = chSensor61850.diagnosis_id "
								+
								"		LEFT JOIN `dau_info_h_mv`.tb_channel_list_61850 channelList61850 ON chSensor61850.ch_id = channelList61850.channel_id "
								+
								"		GROUP BY diagnosisList.diagnosis_id " +
								") as B on A.diagnosis_id = B.diagnosis_id " +
								"LEFT JOIN (SELECT alarm_type_id, alarm_type_name, alarm_sort_id FROM `dau_info_h_mv`.tb_alarm_type) as C on A.alarm_type_id = C.alarm_type_id "
								+
								"LEFT JOIN (SELECT * FROM `dau_info_h_mv`.tb_panel_list) as D on A.panel_id = D.panel_id "
								+
								"LEFT JOIN (SELECT room_id, building_id, room_name FROM `dau_info_h_mv`.tb_room) as E on D.room_id = E.room_id "
								+
								"LEFT JOIN (SELECT building_id, site_id, building_name FROM `dau_info_h_mv`.tb_building) as F on F.building_id = E.building_id "
								+
								"INNER JOIN ( " +
								"	SELECT site_id, site_name FROM `dau_info_h_mv`.tb_site WHERE enabled = 1 " +
								") as G on G.site_id = F.site_id ";

						fullAlarmListSql += " WHERE B.matched = 1 " +
								"AND ( CASE " +
								"				 	WHEN ( alarm_sort_id = 6 AND alarm_level = 0 ) THEN 0" +
								"				 	WHEN ( alarm_sort_id = 7 AND alarm_level = 0 ) THEN 0" +
								"			 	    WHEN ( alarm_sort_id = 1 AND alarm_level = 0 ) THEN 0" +
								"					ELSE 1 END ) = 1" +
								"		  AND (" +
								"		  	( B.sensor_type NOT IN (2, 19) ) " +
								"		  	OR (" +
								"		  	  B.sensor_type IN (2, 19)" +
								"		  	  AND A.alarm_type_id = 70" +
								"		  	)" +
								"		  )" +
								" )";
					}

					if (table.getTableType() == 7) { // Scada 알람
						fullAlarmListSql += "( " +
								" SELECT " +
								"CONCAT('" + table.getDate() + "-', 'scada', A.alarm_id) AS alarmUniqueID, " +
								"A.`alarm_id`, " +
								"G.`site_id`, " +
								" G.`site_name`, " +
								"F.`building_id`, " +
								" F.`building_name`, " +
								"E.`room_id`, " +
								" E.`room_name`, " +
								"A.`panel_id`, " +
								" D.`panel_name`, " +
								"A.`diagnosis_id`, " +
								" B.`diagnosis_name`, " +
								"B.`diagnosis_item_id`, " +
								"A.`alarm_type_id`, " +
								"A.`acknowledge`, " +
								"A.`reg_date`, " +
								"A.`alarm_level`, " +
								"C.`alarm_type_name` AS alarm_type_name, " +
								"C.alarm_sort_id, " +
								"-1 as dau_id, " +
								"-1 as slot_id," +
								"null AS eventGroupID ";

						fullAlarmListSql += "FROM ( SELECT * FROM `dau_data_scada`.tb_alarm_history_" + table.getDate()
								+ " WHERE alarm_level > 0 ) as A ";

						fullAlarmListSql += "LEFT JOIN ( " +
								"SELECT * FROM `dau_info_h_mv`.tb_diagnosis_list WHERE diagnosis_id IN ( " +
								"			SELECT diagnosisList.diagnosis_id FROM `dau_info_h_mv`.tb_diagnosis_list diagnosisList "
								+
								"			LEFT JOIN `dau_info_h_mv`.tb_ch_sensor_list_modbus chSensor ON diagnosisList.diagnosis_id = chSensor.diagnosis_id "
								+
								"			LEFT JOIN `dau_info_h_mv`.tb_channel_list_modbus channelList ON channelList.channel_id = chSensor.ch_id "
								+
								"			WHERE channelList.sensor_type_id NOT IN (2, 19) " +
								"			GROUP BY diagnosis_id " +
								"		) " +
								"	) as B on A.diagnosis_id = B.diagnosis_id " +
								"	LEFT JOIN (SELECT alarm_type_id, alarm_type_name, alarm_sort_id FROM `dau_info_h_mv`.tb_alarm_type) as C on A.alarm_type_id = C.alarm_type_id "
								+
								"	LEFT JOIN (SELECT * FROM `dau_info_h_mv`.tb_panel_list) as D on A.panel_id = D.panel_id "
								+
								"	LEFT JOIN (SELECT room_id, building_id, room_name FROM `dau_info_h_mv`.tb_room) as E on D.room_id = E.room_id "
								+
								"	LEFT JOIN (SELECT building_id, site_id, building_name FROM `dau_info_h_mv`.tb_building) as F on F.building_id = E.building_id "
								+
								"	INNER JOIN ( " +
								"		SELECT site_id, site_name FROM `dau_info_h_mv`.tb_site WHERE enabled = 1 " +
								"	) as G on G.site_id = F.site_id " +
								"	WHERE B.matched = 1 " +
								" AND ( CASE " +
								"				 	WHEN ( alarm_sort_id = 6 AND alarm_level = 0 ) THEN 0" +
								"				 	WHEN ( alarm_sort_id = 7 AND alarm_level = 0 ) THEN 0" +
								"			 	    WHEN ( alarm_sort_id = 1 AND alarm_level = 0 ) THEN 0" +
								"					ELSE 1 END ) = 1" +
								" GROUP BY eventGroupID, ifnull(eventGroupID,alarmUniqueID) )";
					}

					if (table.getTableType() == 3) { // 송전 DAU 알람
						fullAlarmListSql += "( " +
								" SELECT " +
								"CONCAT('" + table.getDate() + "-', 'dauTrans', A.sid) AS alarmUniqueID, " +
								"A.sid AS `alarm_id`, " +
								"null AS `site_id`, " +
								"null AS `site_name`, " +
								"null AS `building_id`, " +
								"null AS `building_name`, " +
								"null AS `room_id`, " +
								"null AS `room_name`, " +
								"null AS `panel_id`, " +
								"null AS `panel_name`, " +
								"null AS `diagnosis_id`, " +
								"null AS `diagnosis_name`, " +
								"null AS `diagnosis_item_id`, " +
								"A.dau_alarm_type_id AS `alarm_type_id`, " +
								"A.acknowledge AS `acknowledge`, " +
								"A.reg_date AS `reg_date`, " +
								"A.alarm_level AS `alarm_level`, " +
								"B.dau_alarm_type_name AS `alarm_type_name`, " +
								"null AS `alarm_sort_id`, " +
								"A.dau_id as dau_id, " +
								"-1 as slot_id," +
								"null AS event_id " +
								" FROM ( SELECT * FROM `dau_data_trans`.tb_dau_alarm_history_" + table.getDate()
								+ " WHERE alarm_level > 0 ) as A " +
								" LEFT JOIN (SELECT * FROM `dau_info_h_mv`.tb_dau_alarm_type) as B on A.dau_alarm_type_id = B.dau_alarm_type_id "
								+
								" INNER JOIN (SELECT * FROM `dau_info_h_mv`.tb_dau_list) as C on A.dau_id = C.dau_id )";
					}

					if (table.getTableType() == 4) { // 배전 DAU 알람
						fullAlarmListSql += "( " +
								" SELECT " +
								"CONCAT('" + table.getDate() + "-', 'dauDist', A.sid) AS alarmUniqueID, " +
								"A.sid AS `alarm_id`,  " +
								"null AS `site_id`, " +
								"null AS `site_name`, " +
								"null AS `building_id`, " +
								"null AS `building_name`, " +
								"null AS `room_id`, " +
								"null AS `room_name`, " +
								"null AS `panel_id`,  " +
								"null AS `panel_name`,  " +
								"null AS `diagnosis_id`,  " +
								"null AS `diagnosis_name`,  " +
								"null AS `diagnosis_item_id`, " +
								"A.dau_alarm_type_id AS `alarm_type_id`,  " +
								"A.acknowledge AS `acknowledge`,  " +
								"A.reg_date AS `reg_date`,  " +
								"A.alarm_level AS `alarm_level`, " +
								"B.dau_alarm_type_name AS `alarm_type_name`, " +
								"null AS `alarm_sort_id`, " +
								"A.dau_id as dau_id, " +
								"-1 as slot_id," +
								"null AS event_id " +
								" FROM ( SELECT * FROM `dau_data_dist`.tb_dau_alarm_history_" + table.getDate()
								+ " WHERE alarm_level > 0 ) as A " +
								" LEFT JOIN (SELECT * FROM `dau_info_h_mv`.tb_dau_alarm_type) as B on A.dau_alarm_type_id = B.dau_alarm_type_id "
								+
								" INNER JOIN (SELECT * FROM `dau_info_h_mv`.tb_dau_list) as C on A.dau_id = C.dau_id )";
					}

					if (table.getTableType() == 6) { // SCADA DAU 알람
						fullAlarmListSql += "( " +
								" SELECT " +
								"CONCAT('" + table.getDate() + "-', 'dauScada', A.sid) AS alarmUniqueID, " +
								"A.sid AS `alarm_id`,  " +
								"null AS `site_id`, " +
								"null AS `site_name`, " +
								"null AS `building_id`, " +
								"null AS `building_name`, " +
								"null AS `room_id`, " +
								"null AS `room_name`, " +
								"null AS `panel_id`,  " +
								"null AS `panel_name`,  " +
								"null AS `diagnosis_id`,  " +
								"null AS `diagnosis_name`,  " +
								"null AS `diagnosis_item_id`, " +
								"A.dau_alarm_type_id AS `alarm_type_id`,  " +
								"A.acknowledge AS `acknowledge`,  " +
								"A.reg_date AS `reg_date`,  " +
								"A.alarm_level AS `alarm_level`, " +
								"B.dau_alarm_type_name AS `alarm_type_name`, " +
								"null AS `alarm_sort_id`, " +
								"A.dau_id as dau_id, " +
								"-1 as slot_id," +
								"null AS event_id " +
								" FROM ( SELECT * FROM `dau_data_scada`.tb_dau_alarm_history_" + table.getDate()
								+ " WHERE alarm_level > 0 ) as A " +
								" LEFT JOIN (SELECT * FROM `dau_info_h_mv`.tb_dau_alarm_type) as B on A.dau_alarm_type_id = B.dau_alarm_type_id "
								+
								" INNER JOIN (SELECT * FROM `dau_info_h_mv`.tb_dau_list) as C on A.dau_id = C.dau_id )";
					}

					if (table.getTableType() == 8) { // 송전 DAU Slot 알람
						fullAlarmListSql += "( " +
								" SELECT " +
								"CONCAT('" + table.getDate() + "-', 'dauSlotTrans', A.sid) AS alarmUniqueID, " +
								"A.sid AS `alarm_id`, " +
								"null AS `site_id`, " +
								"null AS `site_name`, " +
								"null AS `building_id`, " +
								"null AS `building_name`, " +
								"null AS `room_id`, " +
								"null AS `room_name`, " +
								"null AS `panel_id`, " +
								"null AS `panel_name`, " +
								"null AS `diagnosis_id`, " +
								"null AS `diagnosis_name`, " +
								"null AS `diagnosis_item_id`, " +
								"null AS `alarm_type_id`, " +
								"A.acknowledge AS `acknowledge`, " +
								"A.reg_date AS `reg_date`, " +
								"A.alarm_level AS `alarm_level`, " +
								"B.slot_alarm_type_name AS `alarm_type_name`, " +
								"null AS `alarm_sort_id`, " +
								"C.dau_id as dau_id, " +
								"A.slot_id as slot_id," +
								"null AS event_id " +
								" FROM ( SELECT * FROM `dau_data_trans`.tb_dau_slot_alarm_history_" + table.getDate()
								+ " WHERE alarm_level > 0 ) as A " +
								" LEFT JOIN (SELECT * FROM `dau_info_h_mv`.tb_dau_slot_alarm_type) as B on A.slot_alarm_type_id = B.slot_alarm_type_id "
								+
								" INNER JOIN (SELECT * FROM `dau_info_h_mv`.tb_dau_slot_list) as C on A.slot_id = C.slot_id )";
					}

					if (table.getTableType() == 9) { // SCADA DAU Slot 알람
						fullAlarmListSql += "( " +
								" SELECT " +
								"CONCAT('" + table.getDate() + "-', 'dauSlotScada', A.sid) AS alarmUniqueID, " +
								"A.sid AS `alarm_id`, " +
								"null AS `site_id`, " +
								"null AS `site_name`, " +
								"null AS `building_id`, " +
								"null AS `building_name`, " +
								"null AS `room_id`, " +
								"null AS `room_name`, " +
								"null AS `panel_id`, " +
								"null AS `panel_name`, " +
								"null AS `diagnosis_id`, " +
								"null AS `diagnosis_name`, " +
								"null AS `diagnosis_item_id`, " +
								"null AS `alarm_type_id`, " +
								"A.acknowledge AS `acknowledge`, " +
								"A.reg_date AS `reg_date`, " +
								"A.alarm_level AS `alarm_level`, " +
								"B.slot_alarm_type_name AS `alarm_type_name`, " +
								"null AS `alarm_sort_id`, " +
								"C.dau_id as dau_id, " +
								"A.slot_id as slot_id," +
								"null AS event_id " +
								" FROM ( SELECT * FROM `dau_data_scada`.tb_dau_slot_alarm_history_" + table.getDate()
								+ " WHERE alarm_level > 0 ) as A " +
								" LEFT JOIN (SELECT * FROM `dau_info_h_mv`.tb_dau_slot_alarm_type) as B on A.slot_alarm_type_id = B.slot_alarm_type_id "
								+
								" INNER JOIN (SELECT * FROM `dau_info_h_mv`.tb_dau_slot_list) as C on A.slot_id = C.slot_id )";
					}

					if (table.getTableType() == 5) { // System Alarm
						fullAlarmListSql += "( " +
								" SELECT " +
								"CONCAT('" + table.getDate() + "-', 'System', A.alarm_id) AS alarmUniqueID, " +
								"A.alarm_id AS `alarm_id`, " +
								"null AS `site_id`, " +
								"null AS `site_name`, " +
								"null AS `building_id`, " +
								"null AS `building_name`, " +
								"null AS `room_id`, " +
								"null AS `room_name`, " +
								"null AS `panel_id`, " +
								"null AS `panel_name`, " +
								"null AS `diagnosis_id`, " +
								"null AS `diagnosis_name`, " +
								"null AS `diagnosis_item_id`, " +
								"A.`alarm_type_id` AS `alarm_type_id`, " +
								"A.acknowledge AS `acknowledge`, " +
								"A.reg_date AS `reg_date`, " +
								"a.alarm_level AS `alarm_level`, " +
								"C.alarm_type_name AS `alarm_type_name`, " +
								"null AS `alarm_sort_id`, " +
								"-1 as dau_id, " +
								"-1 as slot_id," +
								"null AS event_id " +
								" FROM ( SELECT * FROM `dau_data_basic`.tb_system_alarm_history_" + table.getDate()
								+ " WHERE alarm_level > 0 ) as A " +
								" LEFT JOIN (SELECT alarm_type_id, alarm_type_name, alarm_sort_id FROM `dau_info_h_mv`.tb_alarm_type) as C on A.alarm_type_id = C.alarm_type_id )";
					}
					fullAlarmListSql += " UNION ALL ";
				}

				fullAlarmListSql = fullAlarmListSql.substring(0, fullAlarmListSql.length() - 10);

				fullAlarmListSql += ") AS A ORDER BY reg_date ASC, alarm_id ASC";

				// System.out.println(fullAlarmListSql);

				pstmt = conn.prepareStatement(fullAlarmListSql); // 동일한 변수에 새 객체 할당
				rs = pstmt.executeQuery();

				JSONArray fullAlarmListJson = new JSONArray();

				while (rs.next()) {
					JSONObject alarmJson = new JSONObject();
					alarmJson.put("alarmUniqueID", rs.getString("alarmUniqueID"));
					alarmJson.put("alarm_id", rs.getInt("alarm_id"));
					alarmJson.put("site_id", rs.getInt("site_id"));
					alarmJson.put("site_name", rs.getString("site_name"));
					alarmJson.put("building_id", rs.getInt("building_id"));
					alarmJson.put("building_name", rs.getString("building_name"));
					alarmJson.put("room_id", rs.getInt("room_id"));
					alarmJson.put("room_name", rs.getString("room_name"));
					alarmJson.put("panel_id", rs.getInt("panel_id"));
					alarmJson.put("panel_name", rs.getString("panel_name"));
					alarmJson.put("diagnosis_id", rs.getInt("diagnosis_id"));
					alarmJson.put("diagnosis_name", rs.getString("diagnosis_name"));
					alarmJson.put("diagnosis_item_id", rs.getInt("diagnosis_item_id"));
					alarmJson.put("alarm_type_id", rs.getInt("alarm_type_id"));
					alarmJson.put("acknowledge", rs.getInt("acknowledge"));
					alarmJson.put("reg_date", rs.getString("reg_date"));
					alarmJson.put("alarm_level", rs.getInt("alarm_level"));
					alarmJson.put("alarm_type_name", rs.getString("alarm_type_name"));
					alarmJson.put("dau_id", rs.getInt("dau_id"));

					fullAlarmListJson.put(alarmJson);
				}

				pstmt.close();
				rs.close();

				String diagnosisListSql = "SELECT * FROM `dau_info_h_mv`.`tb_diagnosis_list` a ";
				pstmt = conn.prepareStatement(diagnosisListSql); // 동일한 변수에 새 객체 할당
				rs = pstmt.executeQuery();

				List<HashMap<String, Object>> diagnosisList = new ArrayList<HashMap<String, Object>>();

				while (rs.next()) {
					HashMap<String, Object> diagnosis = new HashMap<String, Object>();
					diagnosis.put("diagnosis_id", rs.getInt("diagnosis_id"));
					diagnosis.put("panel_id", rs.getInt("panel_id"));
					diagnosis.put("diagnosis_name", rs.getString("diagnosis_name"));
					diagnosis.put("matched", rs.getInt("matched"));
					diagnosis.put("diagnosis_item_id", rs.getInt("diagnosis_item_id"));
					diagnosis.put("addr_type_ids", rs.getString("addr_type_ids"));
					diagnosis.put("updated", rs.getString("updated"));
					diagnosis.put("equi_type", rs.getInt("equi_type"));
					diagnosis.put("connect", rs.getInt("connect"));
					diagnosis.put("sensor_exist", rs.getInt("sensor_exist"));
					diagnosis.put("is_61850", rs.getInt("is_61850"));

					diagnosisList.add(diagnosis);
				}

				pstmt.close();
				rs.close();

				HashMap<String, String> sendAlarmInsertSql = new HashMap<String, String>();

				for (int i = 0; i < alarmListDateList.size(); i++) {
					sendAlarmInsertSql.put(alarmListDateList.get(i) + "-trans",
							"INSERT INTO `ai_agent`.tb_trans_alarm_history_"
									+ alarmListDateList.get(i)
									+ "(alarm_id, panel_id, diagnosis_id, alarm_type_id, acknowledge, reg_date, alarm_level) VALUES");
					sendAlarmInsertSql.put(alarmListDateList.get(i) + "-dist",
							"INSERT INTO `ai_agent`.tb_dist_alarm_history_"
									+ alarmListDateList.get(i)
									+ "(alarm_id, panel_id, diagnosis_id, alarm_type_id, acknowledge, reg_date, alarm_level) VALUES");

					sendAlarmInsertSql.put(alarmListDateList.get(i) + "-scada",
							"INSERT INTO `ai_agent`.tb_scada_alarm_history_"
									+ alarmListDateList.get(i)
									+ "(alarm_id, panel_id, diagnosis_id, alarm_type_id, acknowledge, reg_date, alarm_level) VALUES");

					sendAlarmInsertSql.put(alarmListDateList.get(i) + "-dauTrans",
							"INSERT INTO `ai_agent`.tb_trans_dau_alarm_history_"
									+ alarmListDateList.get(i)
									+ "(sid, dau_id, dau_alarm_type_id, acknowledge, alarm_level, reg_date) VALUES");

					sendAlarmInsertSql.put(alarmListDateList.get(i) + "-dauDist",
							"INSERT INTO `ai_agent`.tb_dist_dau_alarm_history_"
									+ alarmListDateList.get(i)
									+ "(sid, dau_id, dau_alarm_type_id, acknowledge, alarm_level, reg_date) VALUES");

					sendAlarmInsertSql.put(alarmListDateList.get(i) + "-dauScada",
							"INSERT INTO `ai_agent`.tb_scada_dau_alarm_history_"
									+ alarmListDateList.get(i)
									+ "(sid, dau_id, dau_alarm_type_id, acknowledge, alarm_level, reg_date) VALUES");

					sendAlarmInsertSql.put(alarmListDateList.get(i) + "-dauSlotTrans",
							"INSERT INTO `ai_agent`.tb_trans_dau_slot_alarm_history_"
									+ alarmListDateList.get(i)
									+ "(sid, slot_id, slot_alarm_type_id, acknowledge, alarm_level, reg_date) VALUES");

					sendAlarmInsertSql.put(alarmListDateList.get(i) + "-dauSlotScada",
							"INSERT INTO `ai_agent`.tb_scada_dau_slot_alarm_history_"
									+ alarmListDateList.get(i)
									+ "(sid, slot_id, slot_alarm_type_id, acknowledge, alarm_level, reg_date) VALUES");

					sendAlarmInsertSql.put(alarmListDateList.get(i) + "-System",
							"INSERT INTO `ai_agent`.tb_system_alarm_history_"
									+ alarmListDateList.get(i)
									+ "(alarm_id, alarm_type_id, alarm_level, acknowledge, reg_date) VALUES");
				}

				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.0");
				LocalDateTime now = LocalDateTime.now();

				String aiSettingSQL = "SELECT * FROM `ai_agent`.`tb_ai_system_setting`";
				pstmt = conn.prepareStatement(aiSettingSQL); // 동일한 변수에 새 객체 할당
				rs = pstmt.executeQuery();

				HashMap<String, Object> aiSetting = new HashMap<String, Object>();

				if (rs.next()) {
					aiSetting.put("x_system_ip", rs.getString("x_system_ip"));
					aiSetting.put("x_system_id", rs.getString("x_system_id"));
					aiSetting.put("ai_server_ip", rs.getString("ai_server_ip"));
					aiSetting.put("orchestrator_agent_port", rs.getInt("orchestrator_agent_port"));
					aiSetting.put("analytics_worker_port", rs.getInt("analytics_worker_port"));
					aiSetting.put("admin_port", rs.getInt("admin_port"));
				}

				pstmt.close();
				rs.close();

				for (int i = 0; i < newAlarmListJsonArray.length(); i++) {

					JSONObject newAlarm = newAlarmListJsonArray.getJSONObject(i);

					List<JSONObject> samePanelSameAlarmTypeList = new ArrayList<JSONObject>(); // 동일한 설비의 동일한 알람이 발생했는지
					List<JSONObject> differentPanelSameAlarmTypeList = new ArrayList<JSONObject>(); // 다른 설비의 동일한 알람이 발생
																									// 했는지?
					List<HashMap<String, Object>> samePanelDiagnosisList = new ArrayList<HashMap<String, Object>>(); // 동일한
																														// 설비의
																														// 진단항목
																														// 목록

					String[] alarm_ids = newAlarm.getString("groupAlarmIDList").split(",");
					String[] alarm_type_id_list = newAlarm.getString("alarm_type_id_list").split(",");

					// 단일 알람인 경우 ?
					for (int j = 0; j < fullAlarmListJson.length(); j++) {
						JSONObject fullAlarm = fullAlarmListJson.getJSONObject(j);

						if (Arrays.asList(alarm_type_id_list).contains(fullAlarm.get("alarm_type_id").toString())
								&& // 알람 종류가 포함되어 있는지
								!Arrays.asList(alarm_ids).contains(fullAlarm.get("alarm_id").toString())
								&& // 알람 ID가 포함되어 있지 않은지
								newAlarm.get("panel_id") == fullAlarm.get("panel_id")) {
							LocalDateTime targetTime = LocalDateTime.parse(fullAlarm.get("reg_date").toString(),
									formatter);

							Duration duration = Duration.between(targetTime, now);
							long daysDiff = duration.toDays();

							if (daysDiff <= alarmHistoryRange) {
								samePanelSameAlarmTypeList.add(fullAlarm);
							}
						}

						if (Arrays.asList(alarm_type_id_list).contains(fullAlarm.get("alarm_type_id").toString())
								&& // 알람종류가 포함되어 있는지
								!Arrays.asList(alarm_ids).contains(fullAlarm.get("alarm_id").toString())
								&& // 알람 ID가 포함되어 있지 않은지
								newAlarm.get("panel_id") != fullAlarm.get("panel_id")) {
							LocalDateTime targetTime = LocalDateTime.parse(fullAlarm.get("reg_date").toString(),
									formatter);

							Duration duration = Duration.between(targetTime, now);
							long daysDiff = duration.toDays();

							if (daysDiff <= alarmHistoryRange) {
								differentPanelSameAlarmTypeList.add(fullAlarm);
							}
						}
					}

					for (int j = 0; j < diagnosisList.size(); j++) {
						if (newAlarm.get("panel_id") == diagnosisList.get(j).get("panel_id")
								&& newAlarm.get("diagnosis_id") != diagnosisList.get(j).get("diagnosis_id")) {
							samePanelDiagnosisList.add(diagnosisList.get(j));
						}
					}

					newAlarm.put("samePanelSameAlarmTypeList", samePanelSameAlarmTypeList);
					newAlarm.put("differentPanelSameAlarmTypeList", differentPanelSameAlarmTypeList);
					newAlarm.put("samePanelDiagnosisList", samePanelDiagnosisList);

					logger.info("alarm send ::: " + newAlarm.get("alarmUniqueID"));

					for (Map.Entry<String, String> entry : sendAlarmInsertSql.entrySet()) {
						String key = entry.getKey();
						if (newAlarm.get("alarmUniqueID").toString().indexOf(key) != -1) {
							String value = entry.getValue();
							if (key.indexOf("-trans") > -1 || key.indexOf("-dist") > -1 || key.indexOf("-scada") > -1) {
								value += "(" + newAlarm.get("alarm_id") + "," + newAlarm.get("panel_id") + ","
										+ newAlarm.get("diagnosis_id") + "," + newAlarm.get("alarm_type_id") + ","
										+ newAlarm.get("acknowledge") + ",'" + newAlarm.get("reg_date") + "',"
										+ newAlarm.get("alarm_level") + "),";
							} else if (key.indexOf("-dauTrans") > -1 || key.indexOf("-dauDist") > -1
									|| key.indexOf("-dauScada") > -1) {
								value += "(" + newAlarm.get("alarm_id") + "," + newAlarm.get("dau_id") + ","
										+ newAlarm.get("alarm_type_id") + "," + newAlarm.get("acknowledge") + ","
										+ newAlarm.get("alarm_level") + ",'" + newAlarm.get("reg_date") + "'),";
							} else if (key.indexOf("-dauSlotTrans") > -1 || key.indexOf("-dauSlotScada") > -1) {
								value += "(" + newAlarm.get("alarm_id") + "," + newAlarm.get("slot_id") + ","
										+ newAlarm.get("alarm_type_id") + "," + newAlarm.get("acknowledge") + ","
										+ newAlarm.get("alarm_level") + ",'" + newAlarm.get("reg_date") + "'),";
							} else if (key.indexOf("-System") > -1) {
								value += "(" + newAlarm.get("alarm_id") + "," + newAlarm.get("alarm_type_id") + ","
										+ newAlarm.get("alarm_level") + "," + newAlarm.get("acknowledge") + ",'"
										+ newAlarm.get("reg_date") + "'),";
							}

							sendAlarmInsertSql.put(key, value);
						}
					}
				}

				boolean isAccepted = false;

				for (int i = 0; i < 5; i++) {
					String alarmsAPIUrl = "http://" + aiSetting.get("ai_server_ip") + ":"
							+ aiSetting.get("analytics_worker_port") + "/alarms";
					// String testAPIUrl = "http://localhost:8080/ai/aiNewAlarmTest";
					URL url = new URL(alarmsAPIUrl);
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();

					// 필수 설정: POST 메서드 지정 및 아웃풋 스트림 사용 설정
					connection.setRequestMethod("POST");
					connection.setDoOutput(true); // 서버로 데이터를 전송하기 위해 true로 설정

					// 헤더 설정: 데이터 포맷을 JSON으로 지정
					connection.setRequestProperty("Content-Type", "application/json; utf-8");
					connection.setRequestProperty("Accept", "application/json");
					connection.setRequestProperty("X-System-ID",
							aiSetting.get("x_system_id").toString());
					connection.setRequestProperty("X-System-IP",
							aiSetting.get("x_system_ip").toString());
					connection.setConnectTimeout(5000);
					connection.setReadTimeout(5000);

					// 수정 가능한 변수: 서버로 보낼 JSON 데이터 정의
					String jsonInputString = newAlarmListJsonArray.toString();

					// 3. JSON 데이터 전송 (Output Stream)
					try (OutputStream os = connection.getOutputStream()) {
						byte[] input = jsonInputString.getBytes("utf-8");
						os.write(input, 0, input.length); // 데이터를 바이트 배열로 변환하여 전송
					}
					int responseCode = connection.getResponseCode();
					if (responseCode == HttpURLConnection.HTTP_ACCEPTED) {
						try (BufferedReader in = new BufferedReader(
								new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
							String inputLine;
							StringBuilder response = new StringBuilder();

							while ((inputLine = in.readLine()) != null) {
								response.append(inputLine);
							}
							logger.info("API server response ::: " + response.toString());
						}
						isAccepted = true;
						break;
					} else {
						logger.error("API request failed code ::: " + responseCode);
					}
					connection.disconnect();
				}

				if (isAccepted) {
					// 정상적으로 API가 호출된 경우에 저장
					try (Statement stmt = conn.createStatement()) {

						for (Map.Entry<String, String> entry : sendAlarmInsertSql.entrySet()) {
							String value = entry.getValue();

							if (value != null && value.endsWith(",")) {
								String sql = value.substring(0, value.length() - 1);
								stmt.executeUpdate(sql);
							}
						}

					} catch (SQLException e) {
						e.printStackTrace();
					}
				}

			}

			pstmt.close();
			rs.close();

		} catch (Exception e) {
			logger.error("Alarm send error", e);
		}

	}

	public List<String> getDataTableList(Date startDate, Date endDate) {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMM");
		SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd");
		String startDateString = format2.format(startDate);
		String endDateString = format2.format(endDate);

		int sYear = Integer.parseInt(startDateString.substring(0, 4));
		int sMonth = Integer.parseInt(startDateString.substring(5, 7));
		int eYear = Integer.parseInt(endDateString.substring(0, 4));
		int eMonth = Integer.parseInt(endDateString.substring(5, 7));

		int month_diff = (eYear - sYear) * 12 + (eMonth - sMonth);

		List<String> pdDataTableList = new ArrayList<String>();

		if (month_diff == 0) {
			pdDataTableList.add(format.format(startDate));
		} else if (month_diff == 1) {

			pdDataTableList.add(format.format(startDate));
			pdDataTableList.add(format.format(endDate));

		} else if (month_diff > 1) {

			pdDataTableList.add(format.format(startDate));

			for (int i = 1; i <= month_diff; i++) {
				if (i == month_diff) {
					pdDataTableList.add(format.format(endDate));
				} else {
					Calendar cal = Calendar.getInstance();
					cal.setTime(startDate);
					cal.add(Calendar.MONTH, i);
					String nextDate = format.format(cal.getTime());
					pdDataTableList.add(nextDate);
				}
			}
		}
		return pdDataTableList;
	}

}
