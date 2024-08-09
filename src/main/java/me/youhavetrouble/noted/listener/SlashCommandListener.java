package me.youhavetrouble.noted.listener;

import me.youhavetrouble.noted.Main;
import me.youhavetrouble.noted.Storage;
import me.youhavetrouble.noted.note.Note;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;

public class SlashCommandListener extends ListenerAdapter {

    private final List<String> optionMappingIds = List.of(
            "title",
            "content",
            "title-url",
            "image-url",
            "thumbnail-url",
            "color",
            "author",
            "author-url",
            "footer",
            "footer-url"
    );

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if (!event.getName().equals("note") || event.getName().equals("edit-note")) return;
        if (!event.getFocusedOption().getName().equals("alias")) return;
        List<Command.Choice> options = Main.getStorage().aliases.stream()
                .filter(word -> word.startsWith(event.getFocusedOption().getValue()))
                .map(word -> new Command.Choice(word, word))
                .limit(25)
                .collect(Collectors.toList());
        event.replyChoices(options).queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "note" -> {
                OptionMapping noteIdOption = event.getOption("alias");
                OptionMapping ephemeralOption = event.getOption("ephemeral");
                if (noteIdOption == null) {
                    event.reply("Please provide a note alias.")
                            .setEphemeral(true)
                            .queue();
                    return;
                }
                boolean ephemeral = false;
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
                addNote(event);
            }
            case "edit-note" -> {
                Long adminId = Main.getAdminId();
                if (adminId == null || !adminId.equals(event.getUser().getIdLong())) {
                    event.reply("You do not have permission to use this command.")
                            .setEphemeral(true)
                            .queue();
                    return;
                }
                editNote(event);
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

    private void addNote(SlashCommandInteractionEvent event) {
        OptionMapping aliasOption = event.getOption("alias");
        OptionMapping titleOption = event.getOption("title");
        OptionMapping contentOption = event.getOption("content");

        if (titleOption == null || contentOption == null || aliasOption == null) {
            event.reply("Please provide a alias, title and content.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        Note note = Note.createNew(titleOption.getAsString(), contentOption.getAsString());

        OptionMapping titleUrlOption = event.getOption("title-url");
        if (titleUrlOption != null) {
            note = note.withTitleUrl(titleUrlOption.getAsString());
        }

        OptionMapping imageUrlOption = event.getOption("image-url");
        if (imageUrlOption != null) {
            note = note.withImageUrl(imageUrlOption.getAsString());
        }

        OptionMapping thumbnailUrlOption = event.getOption("thumbnail-url");
        if (thumbnailUrlOption != null) {
            note = note.withThumbnailUrl(thumbnailUrlOption.getAsString());
        }

        OptionMapping colorOption = event.getOption("color");
        if (colorOption != null) {
            try {
                note = note.withColor(Color.decode(colorOption.getAsString()));
            } catch (NumberFormatException e) {
                event.reply("Invalid color.")
                        .setEphemeral(true)
                        .queue();
                return;
            }
        }

        OptionMapping authorOption = event.getOption("author");
        if (authorOption != null) {
            note = note.withAuthor(authorOption.getAsString());
        }

        OptionMapping authorUrlOption = event.getOption("author-url");
        if (authorUrlOption != null) {
            note = note.withAuthorUrl(authorUrlOption.getAsString());
        }

        OptionMapping footerOption = event.getOption("footer");
        if (footerOption != null) {
            note = note.withFooter(footerOption.getAsString());
        }

        OptionMapping footerUrlOption = event.getOption("footer-url");
        if (footerUrlOption != null) {
            note = note.withFooterUrl(footerUrlOption.getAsString());
        }

        Storage.Status status = Main.getStorage().addNote(note, aliasOption.getAsString());

        if (status == Storage.Status.SUCCESS) {
            event.reply("Note added.")
                    .setEphemeral(true)
                    .queue();
        } else if (status == Storage.Status.ALIAS_EXISTS) {
            event.reply("Alias already exists.")
                    .setEphemeral(true)
                    .queue();
        } else {
            event.reply("Failed to add note.")
                    .setEphemeral(true)
                    .queue();
        }

    }

    private void editNote(SlashCommandInteractionEvent event) {
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

        boolean shouldOpenModal = true;

        for (String optionMappingId : optionMappingIds) {
            if (event.getOption(optionMappingId) != null) {
                shouldOpenModal = false;
                break;
            }
        }

        if (shouldOpenModal) {
            // TODO open modal with few basic fields
            event.reply("You need to provide arguments what parts of the note to edit.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        OptionMapping titleOption = event.getOption("title");
        if (titleOption != null) {
            note = note.withTitle(titleOption.getAsString());
        }

        OptionMapping titleUrlOption = event.getOption("title-url");
        if (titleUrlOption != null) {
            note = note.withTitleUrl(titleUrlOption.getAsString());
        }

        OptionMapping contentOption = event.getOption("content");
        if (contentOption != null) {
            note = note.withContent(contentOption.getAsString());
        }

        OptionMapping imageUrlOption = event.getOption("image-url");
        if (imageUrlOption != null) {
            note = note.withImageUrl(imageUrlOption.getAsString());
        }

        OptionMapping thumbnailUrlOption = event.getOption("thumbnail-url");
        if (thumbnailUrlOption != null) {
            note = note.withThumbnailUrl(thumbnailUrlOption.getAsString());
        }

        OptionMapping colorOption = event.getOption("color");
        if (colorOption != null) {
            try {
                note = note.withColor(Color.decode(colorOption.getAsString()));
            } catch (NumberFormatException e) {
                event.reply("Invalid color.")
                        .setEphemeral(true)
                        .queue();
                return;
            }
        }

        OptionMapping authorOption = event.getOption("author");
        if (authorOption != null) {
            note = note.withAuthor(authorOption.getAsString());
        }

        OptionMapping authorUrlOption = event.getOption("author-url");
        if (authorUrlOption != null) {
            note = note.withAuthorUrl(authorUrlOption.getAsString());
        }

        OptionMapping footerOption = event.getOption("footer");
        if (footerOption != null) {
            note = note.withFooter(footerOption.getAsString());
        }

        OptionMapping footerUrlOption = event.getOption("footer-url");
        if (footerUrlOption != null) {
            note = note.withFooterUrl(footerUrlOption.getAsString());
        }

        Storage.Status status = Main.getStorage().editNote(note.id, note);

        if (status == Storage.Status.SUCCESS) {
            event.reply("Note edited.")
                    .setEphemeral(true)
                    .queue();
        } else {
            event.reply("Failed to edit note.")
                    .setEphemeral(true)
                    .queue();
        }

    }

}
