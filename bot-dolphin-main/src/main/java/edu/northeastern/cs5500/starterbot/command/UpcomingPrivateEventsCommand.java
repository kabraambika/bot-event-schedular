package edu.northeastern.cs5500.starterbot.command;

import com.google.common.annotations.VisibleForTesting;
import edu.northeastern.cs5500.starterbot.controller.StudyEventController;
import edu.northeastern.cs5500.starterbot.model.StudyEvent;
import edu.northeastern.cs5500.starterbot.model.StudyEventType;
import edu.northeastern.cs5500.starterbot.util.SendPrivateMessageUtil;
import java.util.List;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

@Slf4j
@Singleton
public class UpcomingPrivateEventsCommand implements SlashCommandHandler {

    private static final String INITIAL_SUCCESS_MESSAGE =
            "To view the upcoming private events, please check your private channel!";

    private static final String NO_PRIVATE_EVENT_MESSAGE =
            "At the moment, you are not scheduled to attend any private events.";

    static final String NAME = "list-upcoming-private-events";

    @Inject StudyEventController studyEventController;
    @Inject JDA jda;

    @Inject
    public UpcomingPrivateEventsCommand() {
        // public constructor for dagger
    }

    @Override
    @Nonnull
    public String getName() {
        return NAME;
    }

    @Override
    @Nonnull
    public CommandData getCommandData() {
        return Commands.slash(
                getName(), "List of all the private events for which I have already RSVP'd");
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        log.info("event: /list-upcoming-private-events");
        String userId = event.getUser().getId();

        List<StudyEvent> upcomingPrivateEvents =
                getPrivateEventFromDatabase(userId, studyEventController);

        if (upcomingPrivateEvents.isEmpty()) {
            event.reply(NO_PRIVATE_EVENT_MESSAGE).setEphemeral(true).queue();
            return;
        }

        boolean isAnyEventRSVPed = false;
        for (StudyEvent studyEvent : upcomingPrivateEvents) {
            if (!hasUserRSVPed(userId, studyEvent.getAttendeesList())) {
                continue;
            }

            isAnyEventRSVPed = true;

            MessageCreateData messageCreateData =
                    new ListUpcomingPublicEventsCommand().getMessageCreateData(studyEvent, userId);
            SendPrivateMessageUtil.sendMessage(jda, userId, messageCreateData);
        }

        if (isAnyEventRSVPed) {
            event.reply(INITIAL_SUCCESS_MESSAGE).setEphemeral(true).queue();
        } else {
            event.reply(NO_PRIVATE_EVENT_MESSAGE).setEphemeral(true).queue();
        }
    }

    @VisibleForTesting
    boolean hasUserRSVPed(@Nonnull String userId, List<String> attendeesList) {
        if (attendeesList == null || attendeesList.isEmpty()) {
            return false;
        }

        return attendeesList.contains(userId);
    }

    @VisibleForTesting
    List<StudyEvent> getPrivateEventFromDatabase(
            @Nonnull String userId, @Nonnull StudyEventController studyEventController) {
        return studyEventController.getAllUpcomingEvents(
                userId, null, null, StudyEventType.PRIVATE_EVENT);
    }
}
