package edu.northeastern.cs5500.starterbot.command;

import com.google.common.annotations.VisibleForTesting;
import com.mongodb.lang.Nullable;
import edu.northeastern.cs5500.starterbot.controller.StudyEventController;
import edu.northeastern.cs5500.starterbot.model.StudyEvent;
import edu.northeastern.cs5500.starterbot.model.StudyEventType;
import java.util.Collections;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

/**
 * Handler for RSVP and UnRSVP button interactions.
 *
 * @author ambikakabra
 */
@Slf4j
public class RSVPButtonHandler implements ButtonHandler {

    private static final String CONFIRM_UNRSVP_QUESTION =
            "Are you sure you want to un-rsvp from this event?";
    static final String RSVP_NAME = "rsvp";
    static final String UNRSVP_NAME = "unrsvp";
    static final String CONFIRM_UNRSVP_NAME = "confirmunrsvp";
    static final String SOMETHING_WENT_WRONG = "Something went wrong. Please try again later";
    private static final String EVENT_ALREADY_STARTED_UNRSVP =
            "Event has already started, and it's too late to un-RSVP now.";
    private static final String INVITATION_ACCEPTED_MESSAGE =
            "Hey <@%s>, <@%s> has accepted the invitation to your event %s.";

    @Inject StudyEventController studyEventController;
    @Inject JDA jda;

    /** Empty constructor for Dagger injection. */
    @Inject
    public RSVPButtonHandler() {
        // Empty constructor for dagger
    }

    @Override
    @Nonnull
    public String getName() {
        return RSVP_NAME;
    }

    /**
     * Handles the button interaction event for RSVP and UnRSVP buttons
     *
     * @param event The button interaction event
     */
    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        log.info("event: button from /list-upcoming-public-events");
        String[] buttonInfo = event.getComponentId().split(":");
        String buttonType = buttonInfo[0];
        String eventId = buttonInfo[1];
        String userId = event.getUser().getId();

        StudyEvent studyEvent = studyEventController.getEventById(eventId);
        TextChannel textChannel = getTextChannel(event, studyEvent.getChannelId());

        if (textChannel == null) {
            event.reply(SOMETHING_WENT_WRONG).setEphemeral(true).queue();
            return;
        }

        switch (buttonType) {
            case RSVP_NAME:
                String rsvpResponse = handleRSVP(userId, eventId, studyEventController);
                Objects.requireNonNull(rsvpResponse);

                if (rsvpResponse.startsWith(StudyEventController.SUCCESS_RSVP_MESSAGE)) {
                    boolean permissionUpdated =
                            updateChannelPermissionAllow(textChannel, userId, event);
                    if (!permissionUpdated) {
                        event.reply(SOMETHING_WENT_WRONG).setEphemeral(true).queue();
                        return;
                    }
                }

                event.reply(rsvpResponse).setEphemeral(true).queue();

                sendAcceptancePrivateMessage(eventId, userId, rsvpResponse, studyEventController);
                break;
            case UNRSVP_NAME:
                handleUnRSVP(event, eventId);
                break;
            case CONFIRM_UNRSVP_NAME:
                String unRsvpResponse = handleConfirmUnRSVP(userId, eventId, studyEventController);
                Objects.requireNonNull(unRsvpResponse);

                if (unRsvpResponse.startsWith(StudyEventController.SUCCESS_UNRSVP_MESSAGE)) {

                    // Remove the user from the channel
                    boolean permissionUpdated =
                            updateChannelPermissionDeny(textChannel, userId, event);
                    if (!permissionUpdated) {
                        event.reply(SOMETHING_WENT_WRONG).setEphemeral(true).queue();
                        return;
                    }
                }

                event.reply(unRsvpResponse).setEphemeral(true).queue();
                break;
            default:
                // Invalid button type
                return;
        }
    }

    private boolean updateChannelPermissionAllow(
            @Nonnull TextChannel textChannel,
            @Nonnull String userId,
            @Nonnull ButtonInteractionEvent event) {
        Guild guild = event.getGuild();
        Member member = event.getMember();
        try {

            if (guild != null && member != null) {
                textChannel
                        .upsertPermissionOverride(member)
                        .setAllowed(Collections.singleton(Permission.VIEW_CHANNEL))
                        .queue();
                return true;
            } else {
                // Get the guild from the text channel
                Guild privateGuild = textChannel.getGuild();
                // Get the member from the guild using the userId in case of private DM
                privateGuild
                        .retrieveMemberById(userId)
                        .queue(
                                memberById -> {
                                    textChannel
                                            .upsertPermissionOverride(memberById)
                                            .setAllowed(
                                                    Collections.singleton(Permission.VIEW_CHANNEL))
                                            .queue();
                                });
                return true;
            }

        } catch (Exception e) {
            log.error(SOMETHING_WENT_WRONG, e);
            return false;
        }
    }

    private boolean updateChannelPermissionDeny(
            @Nonnull TextChannel textChannel,
            @Nonnull String userId,
            @Nonnull ButtonInteractionEvent event) {
        Guild guild = event.getGuild();
        Member member = event.getMember();
        try {

            if (guild != null && member != null) {
                textChannel
                        .upsertPermissionOverride(member)
                        .setDenied(Collections.singleton(Permission.VIEW_CHANNEL))
                        .queue();
                return true;
            } else {
                // Get the guild from the text channel
                Guild privateGuild = textChannel.getGuild();
                // Get the member from the guild using the userId in case of private DM
                privateGuild
                        .retrieveMemberById(userId)
                        .queue(
                                memberById -> {
                                    textChannel
                                            .upsertPermissionOverride(memberById)
                                            .setDenied(
                                                    Collections.singleton(Permission.VIEW_CHANNEL))
                                            .queue();
                                });
                return true;
            }

        } catch (Exception e) {
            log.error(SOMETHING_WENT_WRONG, e);
            return false;
        }
    }

    @Nullable
    private TextChannel getTextChannel(ButtonInteractionEvent event, String channelId) {
        Guild guild = event.getGuild();
        return guild != null
                ? guild.getTextChannelById(channelId)
                : jda.getTextChannelById(channelId);
    }

    @VisibleForTesting
    boolean sendAcceptancePrivateMessage(
            @Nonnull String eventId,
            @Nonnull String userId,
            @Nonnull String rsvpResponse,
            @Nonnull StudyEventController studyEventController) {

        StudyEvent studyEvent = studyEventController.getEventById(eventId);

        if (studyEvent == null
                || !rsvpResponse.contains(StudyEventController.SUCCESS_RSVP_MESSAGE)
                || studyEvent.getEventType().compareTo(StudyEventType.PRIVATE_EVENT) != 0) {
            log.info("Event owner will not recieve notification");
            return false;
        }

        String eventOwner = studyEvent.getOrganizer();

        jda.openPrivateChannelById(eventOwner)
                .flatMap(
                        channel ->
                                channel.sendMessage(
                                        String.format(
                                                INVITATION_ACCEPTED_MESSAGE,
                                                eventOwner,
                                                userId,
                                                studyEvent.getTitle())))
                .queue();

        return true;
    }

    /**
     * Creates an RSVP button with the specified event ID.
     *
     * @param eventId The ID of the event.
     * @return The RSVP button.
     */
    public static Button createRsvpButton(@Nonnull String eventId) {
        return Button.primary(RSVP_NAME + ":" + eventId, "RSVP");
    }

    /**
     * Creates an UnRSVP button with the specified event ID.
     *
     * @param eventId The ID of the event.
     * @return The UnRSVP button.
     */
    public static Button createUnrsvpButton(@Nonnull String eventId) {
        return Button.danger(UNRSVP_NAME + ":" + eventId, "UNRSVP");
    }

    @VisibleForTesting
    String handleRSVP(
            @Nonnull String userId,
            @Nonnull String eventId,
            @Nonnull StudyEventController studyEventController) {
        log.info("event: rsvp button");

        RSVPCommand command = new RSVPCommand(studyEventController, userId, eventId);
        return command.execute();
    }

    @VisibleForTesting
    String handleConfirmUnRSVP(
            @Nonnull String userId,
            @Nonnull String eventId,
            @Nonnull StudyEventController studyEventController) {
        log.info("event: confirm unrsvp button");

        UnRSVPCommand command = new UnRSVPCommand(studyEventController, userId, eventId);
        return command.execute();
    }

    private void handleUnRSVP(@Nonnull ButtonInteractionEvent event, @Nonnull String eventId) {
        log.info("event: unrsvp button");

        if (studyEventController.isEventStarted(eventId)) {
            event.reply(EVENT_ALREADY_STARTED_UNRSVP).setEphemeral(true).queue();
            return;
        }

        event.reply(CONFIRM_UNRSVP_QUESTION)
                .addActionRow(Button.danger(CONFIRM_UNRSVP_NAME + ":" + eventId, "Confirm Un-RSVP"))
                .setEphemeral(true)
                .queue();
    }
}
