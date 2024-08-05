package me.youhavetrouble.noted;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.io.*;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.Executors;


public class Main {

    private static final Properties properties = new Properties();
    private static String version = "Unknown version";
    public static JDA jda;

    public static void main(String[] args) throws InterruptedException {
        loadProperties();

        try (InputStream resource = Main.class.getClassLoader().getResourceAsStream("version.txt")) {
            if (resource != null) {
                version = new String(resource.readAllBytes());
            } else {
                System.err.println("Version file missing.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Starting " + version);

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
                )
                .queue();

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
                System.err.println("Default noted.properties missing.");
                return;
            }
            outputStream.write(resource.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getVersion() {
        return version;
    }

}