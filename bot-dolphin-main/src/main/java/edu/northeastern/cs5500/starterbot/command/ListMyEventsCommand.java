package edu.northeastern.cs5500.starterbot.command;

import com.google.common.annotations.VisibleForTesting;
import edu.northeastern.cs5500.starterbot.controller.StudyEventController;
import edu.northeastern.cs5500.starterbot.model.StudyEvent;
import edu.northeastern.cs5500.starterbot.model.StudyEventLocation;
import edu.northeastern.cs5500.starterbot.model.StudyEventType;
import edu.northeastern.cs5500.starterbot.util.SendPrivateMessageUtil;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

/** ListMyEventsCommand represents the list of upcoming events that user has created */
@Slf4j
public class ListMyEventsCommand implements SlashCommandHandler {

    static final String NAME = "list-my-events";
    static final String SEND_INVITE = "send_invite";
    private static final String DELETE_BUTTON = "Delete";
    private static final String INITIAL_MESSAGE = "A list of your events has been sent to your DM.";
    private static final String NO_EVENT_CREATED = "There are currently no events created by you.";

    @Inject StudyEventController studyEventController;
    @Inject JDA jda;

    @Inject
    public ListMyEventsCommand() {
        // Empty and public for Dagger
    }

    @Override
    @Nonnull
    public String getName() {
        return NAME;
    }

    @Override
    @Nonnull
    public CommandData getCommandData() {

        OptionData locationOption = buildLocationOption();
        OptionData periodOption = buildPeriodOption();

        return Commands.slash(getName(), "List all study group-events that I created")
                .addOptions(periodOption)
                .addOptions(locationOption);
    }

    public OptionData buildLocationOption() {
        OptionData locationOption =
                new OptionData(OptionType.STRING, "location", "Event location", false);
        for (StudyEventLocation location : StudyEventLocation.values()) {
            locationOption.addChoice(location.name(), location.name());
        }
        return locationOption;
    }

    public OptionData buildPeriodOption() {
        OptionData periodOption =
                new OptionData(OptionType.STRING, "periods", "Choose the period", false)
                        .addChoice("This Week", "this_week")
                        .addChoice("This Month", "this_month");
        return periodOption;
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        log.info("event:/list-my-events");
        String userId = event.getUser().getId();

        // Retrieve options provided by the user
        StudyEventLocation location =
                event.getOption("location") != null
                        ? StudyEventLocation.valueOf(
                                Objects.requireNonNull(event.getOption("location"))
                                        .getAsString()
                                        .toUpperCase())
                        : null;
        String periods =
                event.getOption("periods") != null
                        ? Objects.requireNonNull(event.getOption("periods")).getAsString()
                        : null;

        // Retrieve all events created by the user that have not started yet
        List<StudyEvent> userEvents =
                studyEventController.getAllEventsForUser(userId, location, periods);

        if (userEvents.isEmpty()) {
            // If no events found, send a message indicating so
            event.reply(NO_EVENT_CREATED).setEphemeral(true).queue();
            return;
        }

        for (StudyEvent userEvent : userEvents) {
            MessageCreateData messageCreateData = createMessageData(userEvent);
            SendPrivateMessageUtil.sendMessage(jda, userId, messageCreateData);
        }

        event.reply(INITIAL_MESSAGE).setEphemeral(true).queue();
    }

    @VisibleForTesting
    MessageCreateData createMessageData(StudyEvent userEvent) {

        MessageEmbed embed = creatEventMessageEmbed(userEvent);
        MessageCreateBuilder messageBuilder = new MessageCreateBuilder().addEmbeds(embed);
        String eventId = userEvent.getId().toString();

        Button sendInviteButton = Button.primary(SEND_INVITE + ":" + eventId, "Send invite");

        Button deleteButton =
                Button.danger(DELETE_BUTTON.toLowerCase() + ":" + eventId, DELETE_BUTTON);
        Button editEventButton = Button.primary("edit_event:" + eventId, "Edit");

        // In case of private event only, event owner will see send invite button
        if (userEvent.getEventType().compareTo(StudyEventType.PRIVATE_EVENT) == 0) {
            messageBuilder.addActionRow(editEventButton, sendInviteButton, deleteButton);
        } else {
            messageBuilder.addActionRow(editEventButton, deleteButton);
        }

        return messageBuilder.build();
    }

    @VisibleForTesting
    MessageEmbed creatEventMessageEmbed(StudyEvent userEvent) {

        String startDate = userEvent.getStart().toString();
        Objects.requireNonNull(startDate);

        String endDate = userEvent.getEnd().toString();
        Objects.requireNonNull(endDate);

        String location = userEvent.getLocation().toString();
        Objects.requireNonNull(location);

        String eventType = userEvent.getEventType().toString();
        Objects.requireNonNull(eventType);

        EmbedBuilder builder =
                new EmbedBuilder()
                        .setTitle(userEvent.getTitle())
                        .addField("Start", startDate, true)
                        .addField("End", endDate, true)
                        .addField("Location", location, true)
                        .addField("Event Type", eventType, true);

        String desc = userEvent.getDescription();
        if (!desc.isEmpty()) {
            builder.addField("Description", desc, false);
        }

        List<String> attach = userEvent.getAttachmentFiles();

        if (attach != null && !attach.isEmpty()) {
            String finalAttachedList = String.join("\n", attach);
            Objects.requireNonNull(finalAttachedList);

            builder.addField("Attachment Files", finalAttachedList, false);
        }

        if (userEvent.getMaxAttendees() != 0) {
            String numMaxAttendees = String.valueOf(userEvent.getMaxAttendees());
            Objects.requireNonNull(numMaxAttendees);

            builder.addField("Max Attendees", numMaxAttendees, true);
        }

        if (userEvent.getMaxWaitlListAllowed() != 0) {
            String maxAllowed = String.valueOf(userEvent.getMaxWaitlListAllowed());
            Objects.requireNonNull(maxAllowed);

            builder.addField("Max Waitlist Allowed", maxAllowed, true);
        }

        return builder.build();
    }
}
