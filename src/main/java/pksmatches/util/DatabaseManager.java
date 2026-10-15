package pksmatches.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseManager {
    private static final Properties props = new Properties();

    static {
        // Дефолтные параметры для локальной разработки
        props.setProperty("db.url", "jdbc:postgresql://localhost:5433/pks_matches");
        props.setProperty("db.user", "postgres");
        props.setProperty("db.password", "postgres");

//        try (InputStream in = DatabaseManager.class.getClassLoader().getResourceAsStream("db.properties")) {
//            if (in != null) {
//                props.load(in);
//            }
//        } catch (IOException ignored) {
//            // Используются дефолтные параметры
//        }
    }

    public static Connection getConnection() throws SQLException {
        String envUrl = System.getenv("DB_URL");
        String url = (envUrl != null && !envUrl.isBlank()) ? envUrl : props.getProperty("db.url", "jdbc:postgresql://localhost:5432/pks_matches");
        String envUser = System.getenv("DB_USER");
        String user = (envUser != null && !envUser.isBlank()) ? envUser : props.getProperty("db.user", "postgres");
        String envPass = System.getenv("DB_PASSWORD");
        String pass = (envPass != null && !envPass.isBlank()) ? envPass : props.getProperty("db.password", "postgres");
        try {
            return DriverManager.getConnection(url, user, pass);
        } catch (SQLException e) {
            throw new SQLException(
                "Не удалось подключиться к базе данных PostgreSQL (" + url + ", user=" + user + ").\n" +
                "Убедитесь, что служба PostgreSQL запущена, база данных 'pks_matches' создана и пароль в db.properties верен.\n" +
                "Ошибка: " + e.getMessage(), e
            );
        }
    }
}