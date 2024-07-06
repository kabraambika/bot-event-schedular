package edu.northeastern.cs5500.starterbot.command;

import edu.northeastern.cs5500.starterbot.ExcludeFromJacocoGeneratedReport;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

@ExcludeFromJacocoGeneratedReport
public interface ButtonHandler {
    @Nonnull
    public String getName();

    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event);
}
