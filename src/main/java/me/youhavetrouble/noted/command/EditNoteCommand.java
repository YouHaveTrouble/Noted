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

import java.awt.*;
import java.util.List;

public class EditNoteCommand extends Command {

    private final java.util.List<String> optionMappingIds = List.of(
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
    public void register(JDA jda, String name) {
        jda.upsertCommand(Commands.slash("edit-note", "Edit a note")
                .setIntegrationTypes(IntegrationType.USER_INSTALL)
                .addOptions(
                        new OptionData(OptionType.STRING, "alias", "An alias for the note")
                                .setMinLength(1)
                                .setMaxLength(256)
                                .setRequired(true),
                        new OptionData(OptionType.STRING, "title", "The title of the note")
                                .setMinLength(1)
                                .setMaxLength(256)
                                .setRequired(false),
                        new OptionData(OptionType.STRING, "content", "The content of the note")
                                .setMinLength(1)
                                .setMaxLength(4096)
                                .setRequired(false),
                        new OptionData(OptionType.STRING, "title-url", "The image URL of the note")
                                .setMinLength(1)
                                .setMaxLength(2000)
                                .setRequired(false),
                        new OptionData(OptionType.STRING, "image-url", "The image URL of the note")
                                .setMinLength(1)
                                .setMaxLength(2000)
                                .setRequired(false),
                        new OptionData(OptionType.STRING, "thumbnail-url", "The thumbnail URL of the note")
                                .setMinLength(1)
                                .setMaxLength(2000)
                                .setRequired(false),
                        new OptionData(OptionType.STRING, "color", "The color of the note")
                                .setMinLength(7)
                                .setMaxLength(7)
                                .setRequired(false),
                        new OptionData(OptionType.STRING, "author", "The author of the note")
                                .setMinLength(1)
                                .setMaxLength(256)
                                .setRequired(false),
                        new OptionData(OptionType.STRING, "author-url", "The author URL of the note")
                                .setMinLength(1)
                                .setMaxLength(2000)
                                .setRequired(false),
                        new OptionData(OptionType.STRING, "footer", "The footer of the note")
                                .setMinLength(1)
                                .setMaxLength(256)
                                .setRequired(false),
                        new OptionData(OptionType.STRING, "footer-url", "The footer URL of the note")
                                .setMinLength(1)
                                .setMaxLength(2000)
                                .setRequired(false)
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

    @Override
    public String getName() {
        return "edit-note";
    }

}
