package edu.northeastern.cs5500.starterbot.command;

import edu.northeastern.cs5500.starterbot.ExcludeFromJacocoGeneratedReport;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

@ExcludeFromJacocoGeneratedReport
public interface StringSelectHandler {
    @Nonnull
    public String getName();

    public void onStringSelectInteraction(@Nonnull StringSelectInteractionEvent event);
}
