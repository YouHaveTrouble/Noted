package me.youhavetrouble.noted.command;

import me.youhavetrouble.noted.Main;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public abstract class Command  extends ListenerAdapter {

    private static final Map<String, Command> commands = new HashMap<>();

    public void register(JDA jda, String name) {

    }

    public void execute(SlashCommandInteractionEvent event) {

    }

    @Nullable
    public String getName() {
        return null;
    }

    boolean canUse(User user) {
        return Main.getAdminIds().contains(user.getIdLong());
    }

    public static void registerCommand(Command command) {
        String name = command.getName();
        if (commands.containsKey(name)) {
            Main.logger.warning("Command " + name + " is already registered.");
            return;
        }
        command.register(Main.jda, name);
        commands.put(name, command);
    }

    public static void executeCommand(SlashCommandInteractionEvent event, String name) {
        Command command = commands.get(name.toLowerCase(Locale.ENGLISH));
        if (command == null) {
            event.reply("Unknown command.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if (!command.canUse(event.getUser())) {
            event.reply("You do not have permission to use this command.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        command.execute(event);
    }

}
