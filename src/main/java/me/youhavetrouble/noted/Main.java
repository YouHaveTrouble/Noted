package me.youhavetrouble.noted;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import java.io.*;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.Executors;

public class Main {

    private static final Properties properties = new Properties();
    public static JDA jda;

    public static void main(String[] args) throws InterruptedException {
        loadProperties();



        jda = JDABuilder.createLight(properties.getProperty("DISCORD_TOKEN"), Collections.emptyList())
                .setCallbackPool(Executors.newVirtualThreadPerTaskExecutor())
                .setActivity(Activity.customStatus("Notekeeping..."))
                .addEventListeners(new SlashCommandListener())
                .build();

        jda.awaitReady();

        CommandListUpdateAction commands = jda.updateCommands();
        commands = commands.addCommands(
                Commands.slash("note", "Get a note")
                        .addOptions(
                                new OptionData(OptionType.STRING, "note-id", "The ID of the note").setRequired(true),
                                new OptionData(OptionType.BOOLEAN, "ephermeal", "Whether the note should be ephermal").setRequired(false)
                        )
        );
        commands.queue();
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

}