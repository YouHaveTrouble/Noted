package me.youhavetrouble.noted;

import me.youhavetrouble.noted.note.Note;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class SlashCommandListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "note" -> {
                OptionMapping noteIdOption = event.getOption("note-id");
                OptionMapping ephemeralOption = event.getOption("ephemeral");
                if (noteIdOption == null) {
                    event.reply("Please provide a note ID.")
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
                String noteId = noteIdOption.getAsString();
                getNote(event, noteId, ephemeral);
            }

            default -> event.reply("Unknown command.")
                    .setEphemeral(true)
                    .queue();
        }
    }

    private void getNote(SlashCommandInteractionEvent event, String noteId, boolean ephemeral) {
        Note note = Note.cache.getIfPresent(noteId);
        if (note == null) {
            event.reply("Note with ID %s not found.".formatted(noteId))
                    .setEphemeral(true)
                    .queue();
            return;
        }
        event.replyEmbeds(note.toEmbed())
                .setEphemeral(ephemeral)
                .queue();
    }

}
