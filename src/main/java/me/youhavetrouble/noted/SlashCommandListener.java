package me.youhavetrouble.noted;

import me.youhavetrouble.noted.note.Note;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

public class SlashCommandListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "note" -> {
                OptionMapping noteIdOption = event.getOption("note-id");
                OptionMapping ephemeralOption = event.getOption("ephemeral");
                if (noteIdOption == null) {
                    event.reply("Please provide a note ID.").setEphemeral(true).queue();
                    return;
                }
                Note note = new Note(
                        "Sample Note Title",
                        null,
                        "Bottom text",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

                ReplyCallbackAction action = event.replyEmbeds(note.toEmbed());
                if (ephemeralOption != null && ephemeralOption.getAsBoolean()) {
                    action = action.setEphemeral(true);
                }
                action.queue();

            }

            default -> {
                event.reply("Unknown command.").setEphemeral(true).queue();
                return;
            }
        }
    }

}
