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

public class DeleteAliasCommand extends Command {

    @Override
    public void register(JDA jda, String name) {
        jda.upsertCommand(Commands.slash("delete-alias", "Remove alias to a note")
                .setIntegrationTypes(IntegrationType.USER_INSTALL)
                .addOptions(
                        new OptionData(OptionType.STRING, "alias", "Existing alias of a note", true, true)
                )
                .setContexts(InteractionContextType.BOT_DM)
        ).queue();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping aliasMapping = event.getOption("alias");
        if (aliasMapping == null) {
            event.reply("Please provide a alias.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String alias = aliasMapping.getAsString();

        Storage.Status status = Main.getStorage().deleteAlias(alias);
        switch (status) {
            case SUCCESS -> {
                Note.ALIASES.remove(alias);
                event.reply("Alias deleted.")
                        .setEphemeral(true)
                        .queue();
            }
            case ALIAS_NOT_FOUND -> event.reply("Provided alias does not exist.")
                    .setEphemeral(true)
                    .queue();
            case ALIAS_IS_REQUIRED -> event.reply("At least one alias per note is required.")
                    .setEphemeral(true)
                    .queue();
            case null, default -> event.reply("Failed to delete alias.")
                    .setEphemeral(true)
                    .queue();
        }
    }

    @Override
    public String getName() {
        return "delete-alias";
    }

}
