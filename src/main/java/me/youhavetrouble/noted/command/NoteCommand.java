package me.youhavetrouble.noted.command;

import me.youhavetrouble.noted.Main;
import me.youhavetrouble.noted.note.Note;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Locale;

public class NoteCommand extends Command {

    private final String commandName;

    public NoteCommand(String commandName) {
        if (commandName.equals("add-note") || commandName.equals("edit-note")) {
            Main.logger.warning("The command name " + commandName + " is reserved. Please change the command name in noted.properties.");
            Main.logger.warning("The command name has been changed to 'note'.");
            this.commandName = "note";
            return;
        }
        this.commandName = commandName.toLowerCase(Locale.ENGLISH);
    }

    @Override
    public void register(JDA jda, String name) {
        jda.upsertCommand(Commands.slash(commandName, "Get a note")
                .setIntegrationTypes(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)
                .addOptions(
                        new OptionData(OptionType.STRING, "alias", "The ID of the note", true, true),
                        new OptionData(OptionType.BOOLEAN, "ephermeal", "Whether the note should be ephermal", false)
                )
                .setContexts(InteractionContextType.BOT_DM, InteractionContextType.GUILD, InteractionContextType.PRIVATE_CHANNEL)
        ).queue();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping noteIdOption = event.getOption("alias");
        OptionMapping ephemeralOption = event.getOption("ephemeral");
        if (noteIdOption == null) {
            event.reply("Please provide a note alias.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        boolean ephemeral;
        try {
            ephemeral = ephemeralOption != null && ephemeralOption.getAsBoolean();
        } catch (IllegalArgumentException e) {
            event.reply("Invalid value for ephemeral.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        String noteAlias = noteIdOption.getAsString();
        Note note = Main.getStorage().getNote(noteAlias);
        if (note == null) {
            event.reply("Note with alias %s not found.".formatted(noteAlias))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        event.replyEmbeds(note.toEmbed())
                .setEphemeral(ephemeral)
                .queue();
    }

    @Override
    public boolean canUse(User user) {
        return true;
    }

    @Override
    public String getName() {
        return commandName;
    }
}
