package me.youhavetrouble.noted.command;

import me.youhavetrouble.noted.Main;
import me.youhavetrouble.noted.MessageUtil;
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

public class AddNoteCommand extends Command {

    @Override
    public void register(JDA jda, String name) {
        jda.upsertCommand(Commands.slash(getName(), "Add a note")
                .setIntegrationTypes(IntegrationType.USER_INSTALL)
                .addOptions(
                        new OptionData(OptionType.STRING, "alias", "An alias for the note")
                                .setMinLength(1)
                                .setMaxLength(256)
                                .setRequired(true),
                        new OptionData(OptionType.STRING, "title", "The title of the note")
                                .setMinLength(1)
                                .setMaxLength(256)
                                .setRequired(true),
                        new OptionData(OptionType.STRING, "content", "The content of the note")
                                .setMinLength(1)
                                .setMaxLength(4096)
                                .setRequired(true),
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
        OptionMapping aliasOption = event.getOption("alias");
        OptionMapping titleOption = event.getOption("title");
        OptionMapping contentOption = event.getOption("content");

        if (titleOption == null || contentOption == null || aliasOption == null) {
            event.reply("Please provide a alias, title and content.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        Note note = Note.createNew(
                MessageUtil.formatForDiscord(titleOption.getAsString()),
                MessageUtil.formatForDiscord(contentOption.getAsString())
        );

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
            note = note.withAuthor(MessageUtil.formatForDiscord(authorOption.getAsString()));
        }

        OptionMapping authorUrlOption = event.getOption("author-url");
        if (authorUrlOption != null) {
            note = note.withAuthorUrl(authorUrlOption.getAsString());
        }

        OptionMapping footerOption = event.getOption("footer");
        if (footerOption != null) {
            note = note.withFooter(MessageUtil.formatForDiscord(footerOption.getAsString()));
        }

        OptionMapping footerUrlOption = event.getOption("footer-url");
        if (footerUrlOption != null) {
            note = note.withFooterUrl(footerUrlOption.getAsString());
        }

        String alias = aliasOption.getAsString();
        Storage.Status status = Main.getStorage().addNote(note, alias);

        if (status == Storage.Status.SUCCESS) {
            Note.ALIASES.add(alias);
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

    @Override
    public String getName() {
        return "add-note";
    }

}
