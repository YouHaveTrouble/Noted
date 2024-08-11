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

import java.util.Set;

public class DeleteNoteCommand extends Command {

    @Override
    public void register(JDA jda, String name) {
        jda.upsertCommand(Commands.slash("delete-note", "Delete a note")
                .setIntegrationTypes(IntegrationType.USER_INSTALL)
                .addOptions(
                        new OptionData(OptionType.STRING, "alias", "The ID of the note", true, true)
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
        String noteAlias = noteAliasMapping.getAsString();

        Note note = Main.getStorage().getNote(noteAlias);
        if (note == null) {
            event.reply("Note with alias %s not found.".formatted(noteAlias))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        Set<String> noteAliases = Main.getStorage().getAliases(note.id);
        Storage.Status status = Main.getStorage().deleteNote(note.id);
        if (status == Storage.Status.SUCCESS) {
            noteAliases.forEach(Note.ALIASES::remove);
            event.reply("Note deleted.")
                    .setEphemeral(true)
                    .queue();
        } else {
            event.reply("Failed to delete note.")
                    .setEphemeral(true)
                    .queue();
        }
    }

    @Override
    public String getName() {
        return "delete-note";
    }

}
