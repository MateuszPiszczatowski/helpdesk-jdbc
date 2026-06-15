package local.pk154938.helpdesk.infrastructure.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Owns the single JDBC connection used by the app. Reads connection
 * parameters from environment variables (with sensible local-MySQL
 * defaults) and lazily ensures the {@code tickets} and {@code users} tables exist.
 *
 * <p>Env vars (all optional):
 * <ul>
 *   <li>{@code APP_DB_HOST}  — default {@code localhost}</li>
 *   <li>{@code APP_DB_PORT}  — default {@code 3306}</li>
 *   <li>{@code APP_DB_NAME}  — default {@code tickets}</li>
 *   <li>{@code APP_DB_USER}  — default {@code root}</li>
 *   <li>{@code APP_DB_PASS}  — default empty</li>
 * </ul>
 */
public class DbConnection {

    private static final String SCHEMA_SQL =
            "CREATE TABLE IF NOT EXISTS tickets (" +
            "  id                          INT AUTO_INCREMENT PRIMARY KEY," +
            "  created_at                  DATETIME     NOT NULL," +
            "  first_name                  VARCHAR(100) NOT NULL," +
            "  last_name                   VARCHAR(100) NOT NULL," +
            "  client_message              TEXT         NOT NULL," +
            "  client_message_updated_at   DATETIME     NULL," +
            "  worker_message              TEXT         NULL," +
            "  worker_message_author       VARCHAR(100) NULL," +
            "  worker_message_updated_at   DATETIME     NULL," +
            "  predicted_completion_at     DATETIME     NULL," +
            "  status                      VARCHAR(20)  NOT NULL," +
            "  closed_at                   DATETIME     NULL" +
            ") CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";

    private static final String USERS_SQL =
            "CREATE TABLE IF NOT EXISTS users (" +
            "  id              CHAR(36)     PRIMARY KEY," +
            "  username        VARCHAR(100) NOT NULL UNIQUE," +
            "  hashed_password VARCHAR(255) NOT NULL," +
            "  salt            VARCHAR(255) NOT NULL," +
            "  role            VARCHAR(20)  NOT NULL" +
            ") CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";

    private final Connection connection;

    public DbConnection() throws SQLException {
        String host = envOr("APP_DB_HOST", "localhost");
        String port = envOr("APP_DB_PORT", "3306");
        String name = envOr("APP_DB_NAME", "tickets");
        String user = envOr("APP_DB_USER", "root");
        String pass = envOr("APP_DB_PASS", "");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + name
                + "?useUnicode=true&characterEncoding=utf8"
                + "&useSSL=false&allowPublicKeyRetrieval=true"
                + "&serverTimezone=UTC"
                + "&createDatabaseIfNotExist=true";

        this.connection = DriverManager.getConnection(url, user, pass);
    }

    public void initSchema() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(SCHEMA_SQL);
            stmt.execute(USERS_SQL);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException ignored) {
            // best-effort on shutdown
        }
    }

    private static String envOr(String name, String def) {
        String v = System.getenv(name);
        return (v == null || v.isEmpty()) ? def : v;
    }
}
