package me.youhavetrouble.noted;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;



public class Storage {

    private final Logger logger = Logger.getLogger("Storage");

    private final DataSource dataSource;

    public Storage() {
        File dataFolder = new File("data");
        if (!dataFolder.exists()) {
            if (!dataFolder.mkdir()) {
                throw new RuntimeException("Failed to create data folder");
            }
        }
        HikariConfig config = new HikariConfig();
        config.setPoolName("DataSQLitePool");
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:data/data.db");
        config.setConnectionTestQuery("SELECT 1");
        config.setMaxLifetime(60000); // 60 Sec
        config.setMaximumPoolSize(Math.min(1, Runtime.getRuntime().availableProcessors() / 4));
        dataSource = new HikariDataSource(config);

        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().execute("PRAGMA journal_mode=WAL;");
        } catch (SQLException e) {
            logger.warning("Failed to set journal mode to WAL");
        }

        createTables();

    }

    protected void shutdown() {
        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
        }
    }

    private void createTables() {
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().execute("""
                CREATE TABLE IF NOT EXISTS notes (
                    id VARCHAR(36) NOT NULL PRIMARY KEY,
                    title VARCHAR(256),
                    title_url VARCHAR(2000),
                    description VARCHAR(4096),
                    image_url VARCHAR(2000),
                    thumbnail_url VARCHAR(2000),
                    color INTEGER,
                    author VARCHAR(256),
                    author_url VARCHAR(2000),
                    footer VARCHAR(256),
                    footer_url VARCHAR(2000)
                )
            """);
            connection.createStatement().execute("""
             CREATE TABLE IF NOT EXISTS aliases (
                    alias VARCHAR(256) NOT NULL UNIQUE PRIMARY KEY,
                    note_id VARCHAR(36) NOT NULL
            )
            """);
        } catch (SQLException e) {
            logger.warning("Failed to create tables");
        }
    }

}
