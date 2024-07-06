package edu.northeastern.cs5500.starterbot.command;

import com.google.common.annotations.VisibleForTesting;
import edu.northeastern.cs5500.starterbot.controller.EventUserController;
import edu.northeastern.cs5500.starterbot.model.EventUser;
import edu.northeastern.cs5500.starterbot.model.EventUserRole;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/*
 * verify-command let's the user to input email and role to verify their identity and provide them access to other commands.
 *
 * @akarsh033
 */
@Slf4j
public class VerifyCommand implements SlashCommandHandler {

    @Inject EventUserController eventUserController;
    static final String NAME = "verify";

    @Inject
    public VerifyCommand() {
        // Defined public and empty for Dagger injection
    }

    @Override
    @Nonnull
    public String getName() {
        return NAME;
    }

    @Override
    @Nonnull
    public CommandData getCommandData() {

        // Role option
        OptionData roleOption =
                new OptionData(
                        OptionType.STRING,
                        "role",
                        "Please indicate your role as either student or staff",
                        true);
        // Email option
        OptionData emailOption =
                new OptionData(
                        OptionType.STRING,
                        "email",
                        "Please enter your .edu mail to get verified and access all the features.",
                        true);

        // Adding pre-defined choices for the role option
        roleOption.addChoices(
                new Choice("Staff", EventUserRole.STAFF.toString()),
                new Choice("Student", EventUserRole.STUDENT.toString()));

        return Commands.slash(
                        getName(),
                        "Verify your .edu email with the bot to access all the features.")
                .addOptions(emailOption)
                .addOptions(roleOption);
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {

        log.info("event: /verify");
        String eventUserEmail =
                Objects.requireNonNull(event.getOption("email")).getAsString().strip();
        String eventUserRole = Objects.requireNonNull(event.getOption("role")).getAsString();
        User discordUser = event.getUser();

        EventUser eventUser =
                buildEventUser(
                        discordUser.getName(), discordUser.getId(), eventUserEmail, eventUserRole);
        String reply = eventUserController.addVerifiedEventUser(eventUser);
        event.reply(reply).setEphemeral(true).queue();
    }

    @VisibleForTesting
    EventUser buildEventUser(
            @Nonnull String name,
            @Nonnull String discordId,
            @Nonnull String email,
            @Nonnull String role) {

        // Creating a new eventUser object
        EventUser eventUser = new EventUser();

        eventUser.setName(name);
        eventUser.setDiscordId(discordId);
        eventUser.setEmail(email);
        eventUser.setRole(EventUserRole.valueOf(role));
        return eventUser;
    }
}
