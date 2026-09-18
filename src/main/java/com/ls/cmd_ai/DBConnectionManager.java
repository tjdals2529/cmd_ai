package com.ls.cmd_ai;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 데이터베이스 연결을 전담하는 관리 클래스입니다.
 */
public class DBConnectionManager {

    private static final Logger logger = LoggerFactory.getLogger(DBConnectionManager.class);
    private static final Properties props = new Properties();
    static {
        try (InputStream input = DBConnectionManager.class.getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (input == null) {
                System.out.println("application.properties 파일을 찾을 수 없습니다.");
            } else {

                props.load(input);

                Class.forName(props.getProperty("db.driver"));
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.user"),
                props.getProperty("db.password"));
    }

    public static void close(AutoCloseable... resources) {
        for (AutoCloseable res : resources) {
            if (res != null) {
                try {
                    res.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}