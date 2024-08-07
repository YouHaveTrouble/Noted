package me.youhavetrouble.noted.listener;

import me.youhavetrouble.noted.Main;
import me.youhavetrouble.noted.Storage;
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
            case "add-note" -> {
                Long adminId = Main.getAdminId();
                if (adminId == null || !adminId.equals(event.getUser().getIdLong())) {
                    event.reply("You do not have permission to use this command.")
                            .setEphemeral(true)
                            .queue();
                    return;
                }

                OptionMapping aliasOption = event.getOption("alias");
                OptionMapping titleOption = event.getOption("title");
                OptionMapping contentOption = event.getOption("content");
                if (titleOption == null || contentOption == null || aliasOption == null) {
                    event.reply("Please provide a alias, title and content.")
                            .setEphemeral(true)
                            .queue();
                    return;
                }
                String alias = aliasOption.getAsString();
                String title = titleOption.getAsString();
                String content = contentOption.getAsString();
                addNote(event, alias, title, content);
            }

            default -> event.reply("Unknown command.")
                    .setEphemeral(true)
                    .queue();
        }
    }

    private void getNote(SlashCommandInteractionEvent event, String noteAlias, boolean ephemeral) {
        Note note = Main.getStorage().getNote(noteAlias);
        if (note == null) {
            event.reply("Note with ID %s not found.".formatted(noteAlias))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        event.replyEmbeds(note.toEmbed())
                .setEphemeral(ephemeral)
                .queue();
    }

    private void addNote(SlashCommandInteractionEvent event, String noteAlias, String title, String description) {
        Note note = Note.createNew(title, description);
        Storage.Status status = Main.getStorage().addNote(note, noteAlias);

        if (status == Storage.Status.ALIAS_EXISTS) {
            event.reply("Alias already exists.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        if (status == Storage.Status.ERROR) {
            event.reply("An error occurred.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        event.reply("Note added.")
                .setEphemeral(true)
                .queue();
    }

}
