package me.youhavetrouble.noted.listener;

import me.youhavetrouble.noted.Main;
import me.youhavetrouble.noted.note.Note;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import java.util.List;
import java.util.stream.Collectors;

public class SlashCommandListener extends ListenerAdapter {

    public SlashCommandListener() {
        Note.ALIASES.addAll(Main.getStorage().getAliases());
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if (!event.getFocusedOption().getName().equals("alias")) return;
        List<Command.Choice> options = Note.ALIASES.stream()
                .filter(word -> word.startsWith(event.getFocusedOption().getValue()))
                .map(word -> new Command.Choice(word, word))
                .limit(25)
                .collect(Collectors.toList());
        event.replyChoices(options).queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String command = event.getName();
        me.youhavetrouble.noted.command.Command.executeCommand(event, command);
    }

}
