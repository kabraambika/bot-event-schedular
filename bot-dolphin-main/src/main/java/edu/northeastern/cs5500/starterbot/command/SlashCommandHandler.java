package edu.northeastern.cs5500.starterbot.command;

import edu.northeastern.cs5500.starterbot.ExcludeFromJacocoGeneratedReport;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

@ExcludeFromJacocoGeneratedReport
public interface SlashCommandHandler {
    @Nonnull
    public String getName();

    @Nonnull
    public CommandData getCommandData();

    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event);
}
