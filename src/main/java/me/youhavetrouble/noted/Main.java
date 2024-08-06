package me.youhavetrouble.noted;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.logging.Logger;


public class Main {

    private static final Logger logger = Logger.getLogger("Main");
    private static final Properties properties = new Properties();
    private static String version = "Unknown version";
    private static Storage storage;
    private static Long adminId;
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

        adminId = Long.parseLong(properties.getProperty("ADMIN_USER_ID"));

        jda = JDABuilder.createLight(properties.getProperty("DISCORD_TOKEN"), Collections.emptyList())
                .setCallbackPool(Executors.newVirtualThreadPerTaskExecutor())
                .setActivity(Activity.customStatus("Notekeeping..."))
                .addEventListeners(new SlashCommandListener())
                .build();

        jda.awaitReady();

        jda.upsertCommand(Commands.slash("note", "Get a note")
                        .setIntegrationTypes(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)
                        .addOptions(
                                new OptionData(OptionType.STRING, "note-id", "The ID of the note").setRequired(true),
                                new OptionData(OptionType.BOOLEAN, "ephermeal", "Whether the note should be ephermal").setRequired(false)
                        )
                        .setContexts(InteractionContextType.BOT_DM, InteractionContextType.GUILD, InteractionContextType.PRIVATE_CHANNEL)
                ).queue();

        jda.upsertCommand(Commands.slash("add-note", "Add a note")
                        .setIntegrationTypes(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)
                        .addOptions(
                                new OptionData(OptionType.STRING, "alias", "An alias for the note").setRequired(true),
                                new OptionData(OptionType.STRING, "title", "The title of the note").setRequired(true),
                                new OptionData(OptionType.STRING, "content", "The content of the note").setRequired(true),
                                new OptionData(OptionType.STRING, "image-url", "The image URL of the note").setRequired(false),
                                new OptionData(OptionType.STRING, "thumbnail-url", "The thumbnail URL of the note").setRequired(false),
                                new OptionData(OptionType.STRING, "color", "The color of the note").setRequired(false),
                                new OptionData(OptionType.STRING, "author", "The author of the note").setRequired(false),
                                new OptionData(OptionType.STRING, "author-url", "The author URL of the note").setRequired(false),
                                new OptionData(OptionType.STRING, "footer", "The footer of the note").setRequired(false),
                                new OptionData(OptionType.STRING, "footer-url", "The footer URL of the note").setRequired(false)
                        )
                        .setContexts(InteractionContextType.BOT_DM)
                ).queue();

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

    @Nullable
    public static Long getAdminId() {
        return adminId;
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