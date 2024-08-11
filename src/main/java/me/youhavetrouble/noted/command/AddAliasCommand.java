package me.youhavetrouble.noted.command;

import me.youhavetrouble.noted.Main;
import me.youhavetrouble.noted.Storage;
import me.youhavetrouble.noted.note.Note;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class AddAliasCommand extends Command {

    @Override
    public void register(JDA jda, String name) {
        jda.upsertCommand(Commands.slash("add-alias", "Add alias to a note")
                .setIntegrationTypes(IntegrationType.USER_INSTALL)
                .addOptions(
                        new OptionData(OptionType.STRING, "alias", "Existing alias of a note", true, true),
                        new OptionData(OptionType.STRING, "new-alias", "New alias for the note", true)
                )
                .setContexts(InteractionContextType.BOT_DM)
        ).queue();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping noteAliasMapping = event.getOption("alias");
        if (noteAliasMapping == null) {
            event.reply("Please provide a note alias.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        OptionMapping newAliasMapping = event.getOption("new-alias");
        if (newAliasMapping == null) {
            event.reply("Please provide a new alias.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String noteAlias = noteAliasMapping.getAsString();

        Note note = Main.getStorage().getNote(noteAlias);
        if (note == null) {
            event.reply("Note with alias %s not found.".formatted(noteAlias))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        Storage.Status status = Main.getStorage().addAlias(newAliasMapping.getAsString(), note.id);
        if (status == Storage.Status.SUCCESS) {
            Note.ALIASES.add(newAliasMapping.getAsString());
            event.reply("Alias added.")
                    .setEphemeral(true)
                    .queue();
        } else if (status == Storage.Status.ALIAS_EXISTS) {
            event.reply("Alias already exists.")
                    .setEphemeral(true)
                    .queue();
        } else {
            event.reply("Failed to add alias.")
                    .setEphemeral(true)
                    .queue();
        }
    }

    @Override
    public String getName() {
        return "add-alias";
    }

}
