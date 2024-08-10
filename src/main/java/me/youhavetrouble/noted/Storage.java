package me.youhavetrouble.noted;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.youhavetrouble.noted.note.Note;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.awt.Color;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

public class Storage {

    public final List<String> aliases = new ArrayList<>();

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
        config.setMaximumPoolSize(Math.min(4, Runtime.getRuntime().availableProcessors() / 4));
        dataSource = new HikariDataSource(config);

        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().execute("PRAGMA journal_mode=WAL;");
        } catch (SQLException e) {
            logger.warning("Failed to set journal mode to WAL");
        }

        createTables();
        aliases.addAll(getAliases());
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

    public Status addNote(@NotNull Note note, @NotNull String alias) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO notes (id, title, title_url, description, image_url, thumbnail_url, color, author, author_url, footer, footer_url)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """);

            statement.setString(1, note.id.toString());
            statement.setString(2, note.title);
            statement.setString(3, note.titleUrl);
            statement.setString(4, note.content);
            statement.setString(5, note.imageUrl);
            statement.setString(6, note.thumbnailUrl);
            statement.setInt(7, note.color != null ? note.color.getRGB() : 0);
            statement.setString(8, note.author);
            statement.setString(9, note.authorUrl);
            statement.setString(10, note.footer);
            statement.setString(11, note.footerUrl);
            statement.executeUpdate();

            statement = connection.prepareStatement("""
                INSERT INTO aliases (alias, note_id) VALUES (?, ?)
            """);
            statement.setString(1, alias);
            statement.setString(2, note.id.toString());
            statement.executeUpdate();

            aliases.add(alias);
            connection.commit();
            return Status.SUCCESS;
        } catch (SQLException e) {
            if (e.getErrorCode() == 19) {
                return Status.ALIAS_EXISTS;
            }
            return Status.ERROR;
        }
    }

    public Status editNote(UUID noteId, Note note) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("""
                UPDATE notes
                SET title = ?, title_url = ?, description = ?, image_url = ?, thumbnail_url = ?, color = ?, author = ?, author_url = ?, footer = ?, footer_url = ?
                WHERE id = ?
            """);
            statement.setString(1, note.title);
            statement.setString(2, note.titleUrl);
            statement.setString(3, note.content);
            statement.setString(4, note.imageUrl);
            statement.setString(5, note.thumbnailUrl);
            statement.setInt(6, note.color != null ? note.color.getRGB() : 0);
            statement.setString(7, note.author);
            statement.setString(8, note.authorUrl);
            statement.setString(9, note.footer);
            statement.setString(10, note.footerUrl);
            statement.setString(11, noteId.toString());
            statement.executeUpdate();
            return Status.SUCCESS;
        } catch (SQLException e) {
            return Status.ERROR;
        }
    }

    public Status deleteNote(@NotNull UUID noteId) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM notes WHERE id = ?");
            statement.setString(1, noteId.toString());
            statement.executeUpdate();
            statement = connection.prepareStatement("DELETE FROM aliases WHERE note_id = ?");
            statement.setString(1, noteId.toString());
            statement.executeUpdate();
            return Status.SUCCESS;
        } catch (SQLException e) {
            return Status.ERROR;
        }
    }

    public Status addAlias(@NotNull String alias, @NotNull UUID noteId) throws RuntimeException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO aliases (alias, note_id)
                VALUES (?, ?)
            """);
            statement.setString(1, alias);
            statement.setString(2, noteId.toString());
            statement.executeUpdate();
            aliases.add(alias);
            return Status.SUCCESS;
        } catch (SQLException e) {
            logger.warning(e.getMessage());
            return Status.ALIAS_EXISTS;
        }
    }

    public Status deleteAlias(@NotNull String alias) {
        try (Connection connection = dataSource.getConnection()) {

            Note note = getNote(alias);
            if (note == null) {
                return Status.ALIAS_NOT_FOUND;
            }

            PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM aliases WHERE alias = ?");
            statement.setString(1, alias);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                if (resultSet.getInt(1) <= 1) {
                    // Only one alias left, don't allow deletion
                    return Status.ALIAS_IS_REQUIRED;
                }
            }

            statement = connection.prepareStatement("DELETE FROM aliases WHERE alias = ?");
            statement.setString(1, alias);
            statement.executeUpdate();
            aliases.remove(alias);
            return Status.SUCCESS;
        } catch (SQLException e) {
            return Status.ERROR;
        }
    }

    public Set<String> getAliases() {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT alias FROM aliases");
            ResultSet resultSet = statement.executeQuery();
            Set<String> aliases = new HashSet<>();
            while (resultSet.next()) {
                aliases.add(resultSet.getString("alias"));
            }
            return aliases;
        } catch (SQLException e) {
            logger.warning("Failed to get aliases");
            return new HashSet<>();
        }
    }

    @Nullable
    public Note getNote(@NotNull String alias) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("""
                SELECT * FROM notes
                WHERE id = (
                    SELECT note_id FROM aliases
                    WHERE alias = ?
                )
            """);
            statement.setString(1, alias);
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                return null;
            }

            Color color = null;
            try {
                color = new Color(resultSet.getInt("color"));
            } catch (SQLException ignored) {
            }

            return new Note(
                UUID.fromString(resultSet.getString("id")),
                resultSet.getString("title"),
                resultSet.getString("title_url"),
                resultSet.getString("description"),
                resultSet.getString("image_url"),
                resultSet.getString("thumbnail_url"),
                color,
                resultSet.getString("author"),
                resultSet.getString("author_url"),
                resultSet.getString("footer"),
                resultSet.getString("footer_url")
            );
        } catch (SQLException e) {
            logger.warning("Failed to get note");
            return null;
        }
    }

    public enum Status {
        SUCCESS,
        ALIAS_EXISTS,
        ALIAS_NOT_FOUND,
        ALIAS_IS_REQUIRED,
        ERROR
    }

}
