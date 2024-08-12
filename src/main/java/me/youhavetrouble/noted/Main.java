package me.youhavetrouble.noted;

import me.youhavetrouble.noted.command.*;
import me.youhavetrouble.noted.listener.SlashCommandListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.logging.Logger;


public class Main {

    public static final Logger logger = Logger.getLogger("Main");
    private static final Properties properties = new Properties();
    private static String version = "Unknown version";
    private static Storage storage;
    private static final Set<Long> adminIds = new HashSet<>();
    public static String command;
    public static JDA jda;

    public static void main(String[] args) throws InterruptedException {
        loadProperties();

        try (InputStream resource = Main.class.getClassLoader().getResourceAsStream("version.txt")) {
            if (resource != null) {
                version = new String(resource.readAllBytes());
            } else {
                logger.severe("Version file missing.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        storage = new Storage();

        Runtime.getRuntime().addShutdownHook(new Thread(Main::shutdown));

        logger.info("Starting " + version);

        String adminIdString = properties.getProperty("ADMIN_USER_ID");
        if (adminIdString != null) {
            String[] adminIds = adminIdString.split(",");
            for (String id : adminIds) {
                try {
                    Main.adminIds.add(Long.parseLong(id));
                } catch (NumberFormatException e) {
                    logger.warning("Invalid admin user ID: " + id);
                }
            }
        }

        jda = JDABuilder.createLight(properties.getProperty("DISCORD_TOKEN"), Collections.emptyList())
                .setCallbackPool(Executors.newVirtualThreadPerTaskExecutor())
                .setActivity(Activity.customStatus("Noted v" + version))
                .setStatus(OnlineStatus.ONLINE)
                .addEventListeners(new SlashCommandListener())
                .build();

        jda.awaitReady();

        command = properties.getProperty("COMMAND", "note");
        if (command.equals("add-note") || command.equals("edit-note")) {
            logger.warning("The command name " + command + " is reserved. Please change the command name in noted.properties.");
            logger.warning("The command name has been changed to 'note'.");
            command = "note";
        }

        Command.registerCommand(new NoteCommand(command));
        Command.registerCommand(new AddNoteCommand());
        Command.registerCommand(new EditNoteCommand());
        Command.registerCommand(new DeleteNoteCommand());
        Command.registerCommand(new AddAliasCommand());
        Command.registerCommand(new DeleteAliasCommand());
    }

    private static void loadProperties() {
        saveDefaultProperties();
        try (InputStream inputStream = new FileInputStream("noted.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void saveDefaultProperties() {
        File file = new File("noted.properties");
        if (file.exists()) {
            return;
        }
        try (InputStream resource = Main.class.getClassLoader().getResourceAsStream("noted.properites");
             OutputStream outputStream = new FileOutputStream(file)) {
            if (resource == null) {
                logger.severe("Default noted.properties missing.");
                return;
            }
            outputStream.write(resource.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public static Set<Long> getAdminIds() {
        return adminIds;
    }

    public static String getVersion() {
        return version;
    }

    public static Storage getStorage() {
        return storage;
    }

    private static void shutdown() {
        jda.shutdown();
        storage.shutdown();
    }

}