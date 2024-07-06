package edu.northeastern.cs5500.starterbot.command;

import com.google.common.annotations.VisibleForTesting;
import com.mongodb.lang.Nullable;
import edu.northeastern.cs5500.starterbot.controller.EventUserController;
import edu.northeastern.cs5500.starterbot.controller.StudyEventController;
import edu.northeastern.cs5500.starterbot.model.EventUser;
import edu.northeastern.cs5500.starterbot.model.StudyEvent;
import edu.northeastern.cs5500.starterbot.util.SendPrivateMessageUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

/** This class represents the Button and String select menu interactions. */
@Slf4j
public class MyEventButtonHandler implements ButtonHandler, StringSelectHandler {

    static final String CONFIRM_DELETE_YES = "yes";
    static final String CONFIRM_DELETE_NO = "no";
    static final String DELETE_BUTTON_NAME = "delete";
    static final String INVITE_DROPDOWN = "inviteDropdown";
    private static final String SEND_INVITE = "send_invite";
    private static final String NO_BUTTON = "No";
    private static final String YES_BUTTON = "Yes";
    private static final String EVENT_ALREADY_STARTED_MESSAGE =
            "Event has already started! you cannot delete this event now.";
    private static final String CONFIRM_DELETE_EVENT_MESSAGE =
            "Are you sure you want to delete this event?";
    private static final String EVENT_NO_EXIST_MESSAGE =
            "This event no longer exists and cannot be deleted.";
    private static final String SUCCESS_DELETE_MESSAGE = "Event has been successfully deleted.";
    private static final String NO_CHANGES_MESSAGE = "No changes have been made to this event.";
    private static final String SELECT_EMAIL_MESSAGE = "Select an email to send the invitation:";
    private static final String NO_AUTHENTICATED_USERS_MESSAGE =
            "At present, there are no authenticated users for sending invitations. Please attempt again later.";
    private static final String PERSONAL_INVITE_MESSAGE_FORMAT =
            "Hey <@%s>, do you want to join %s event beginning on %s?";
    private static final String INVITATION_SENT_MESSAGE = "Invitation is sent to <@%s>!";
    private static final String CANNOT_SEND_INVITE_MESSAGE =
            "An invitation cannot be sent to <@%s>!";
    private static final String EVENT_CANCELLED_MESSAGE =
            "Hey <@%s>, %s event is cancelled by owner.";
    private static final String EMPTY_MESSAGE = "";

    @Inject StudyEventController studyEventController;
    @Inject EventUserController eventUserController;
    @Inject JDA jda;

    @Inject
    public MyEventButtonHandler() {
        // Empty constructor for dagger
    }

    @Override
    @Nonnull
    public String getName() {
        return DELETE_BUTTON_NAME;
    }

    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        log.info("event: button from /list-my-events");
        String[] buttonInfo = event.getComponentId().split(":");
        String buttonType = buttonInfo[0];
        String eventId = buttonInfo[1];

        switch (buttonType) {
            case DELETE_BUTTON_NAME:
                log.info("event: delete button");

                if (studyEventController.isEventStarted(eventId)) {
                    log.info(
                            "event: On click of delete button. sent reply "
                                    + EVENT_ALREADY_STARTED_MESSAGE);
                    event.reply(EVENT_ALREADY_STARTED_MESSAGE).setEphemeral(true).queue();
                    return;
                }

                List<LayoutComponent> list = createConfirmDeleteButtons(eventId);

                event.reply(CONFIRM_DELETE_EVENT_MESSAGE)
                        .setComponents(list)
                        .setEphemeral(true)
                        .queue();
                break;
            case CONFIRM_DELETE_YES:
                StudyEvent studyEvent = getEvent(eventId, studyEventController);

                if (studyEvent == null) {
                    log.info(
                            "event: On click of yes button while deleting an event. sent reply "
                                    + EVENT_NO_EXIST_MESSAGE);
                    event.reply(EVENT_NO_EXIST_MESSAGE).setEphemeral(true).queue();
                    return;
                }

                String sendResponse = deleteStudyEvent(studyEvent, studyEventController);
                Objects.requireNonNull(sendResponse);

                if (sendResponse.startsWith(SUCCESS_DELETE_MESSAGE)) {
                    deleteChannelForEvent(studyEvent);
                }
                event.reply(sendResponse).setEphemeral(true).queue();
                break;
            case CONFIRM_DELETE_NO:
                log.info("event: pressed no button from confirm delete button");
                event.reply(NO_CHANGES_MESSAGE).setEphemeral(true).queue();
                break;
            case SEND_INVITE:
                log.info("event: pressed send invite button /list-my-events");
                showSendInviteMessage(eventId, event);
                break;
            default:
                log.error("Invalid button type in /list-my-events command");
        }
    }

    private void deleteChannelForEvent(StudyEvent studyEvent) {

        // Get the channel object by its ID
        TextChannel channelToDelete = jda.getTextChannelById(studyEvent.getChannelId());

        if (channelToDelete != null) {
            // Delete the channel
            channelToDelete
                    .delete()
                    .queue(
                            // Success callback
                            success -> log.info("Channel deleted successfully"),
                            // Error callback
                            error ->
                                    log.error(
                                            "Something went wrong while deleting the Channel",
                                            error));
        } else {
            log.info("Channel for event not found or already deleted");
        }
    }

    @VisibleForTesting
    String deleteStudyEvent(
            @Nonnull StudyEvent studyEvent, @Nonnull StudyEventController studyEventController) {

        List<String> attendeesIdList = getAttendees(studyEvent);
        return deleteStudyEventHelper(studyEvent, studyEventController, attendeesIdList);
    }

    @VisibleForTesting
    List<LayoutComponent> createConfirmDeleteButtons(@Nonnull String eventId) {
        List<LayoutComponent> buttonComponents = new ArrayList<>();

        Button yesButton = Button.primary(CONFIRM_DELETE_YES + ":" + eventId, YES_BUTTON);
        Button noButton = Button.secondary(CONFIRM_DELETE_NO + ":" + eventId, NO_BUTTON);
        buttonComponents.add(ActionRow.of(yesButton, noButton));

        return buttonComponents;
    }

    @VisibleForTesting
    StudyEvent getEvent(
            @Nonnull String eventId, @Nonnull StudyEventController studyEventController) {
        return studyEventController.getEventById(eventId);
    }

    @VisibleForTesting
    String deleteStudyEventHelper(
            @Nonnull StudyEvent studyEvent,
            @Nonnull StudyEventController studyEventController,
            List<String> attendees) {
        log.info("event: pressed yes button from confirm delete button");
        boolean isDeleted = studyEventController.deleteEventById(studyEvent.getId());

        if (isDeleted && attendees != null && !attendees.isEmpty()) {
            sendNotificationToAttendees(attendees, studyEvent.getTitle());
        }

        return isDeleted ? SUCCESS_DELETE_MESSAGE : EVENT_NO_EXIST_MESSAGE;
    }

    @VisibleForTesting
    List<String> getAttendees(@Nonnull StudyEvent studyEvent) {
        return studyEvent.getAttendeesList();
    }

    @VisibleForTesting
    void sendNotificationToAttendees(
            @Nonnull List<String> attendeesIdList, @Nonnull String eventName) {
        for (String attendee : attendeesIdList) {
            long attendeeDiscordID;
            try {
                attendeeDiscordID = Long.parseLong(attendee);
            } catch (Exception ex) {
                log.error(
                        "Attendee id " + attendee + " is incorrect in event " + eventName + " !",
                        ex);
                continue;
            }

            MessageCreateData messageCreateData =
                    new MessageCreateBuilder()
                            .addContent(
                                    String.format(
                                            EVENT_CANCELLED_MESSAGE, attendeeDiscordID, eventName))
                            .build();
            SendPrivateMessageUtil.sendMessage(jda, attendee, messageCreateData);
        }
    }

    @Override
    public void onStringSelectInteraction(@Nonnull StringSelectInteractionEvent event) {
        log.info("event: invite dropdown for private event");

        String[] menuInfo = event.getInteraction().getValues().get(0).split(":");
        String userDiscordId = menuInfo[0];
        String eventId = menuInfo[1];

        String finalMessage =
                createPersonalInviteMessage(userDiscordId, eventId, studyEventController);

        if (finalMessage.isEmpty()) {
            event.reply(String.format(CANNOT_SEND_INVITE_MESSAGE, userDiscordId))
                    .setEphemeral(true)
                    .queue();
        } else {
            jda.openPrivateChannelById(userDiscordId)
                    .flatMap(
                            channel ->
                                    channel.sendMessage(finalMessage)
                                            .addActionRow(
                                                    RSVPButtonHandler.createRsvpButton(eventId)))
                    .queue();

            event.reply(String.format(INVITATION_SENT_MESSAGE, userDiscordId))
                    .setEphemeral(true)
                    .queue();
        }
    }

    @VisibleForTesting
    String createPersonalInviteMessage(
            @Nonnull String userDiscordId,
            @Nonnull String eventId,
            @Nonnull StudyEventController studyEventController) {

        StudyEvent studyEvent = getEvent(eventId, studyEventController);

        return studyEvent != null
                ? String.format(
                        PERSONAL_INVITE_MESSAGE_FORMAT,
                        userDiscordId,
                        studyEvent.getTitle(),
                        studyEvent.getStart())
                : EMPTY_MESSAGE;
    }

    private void showSendInviteMessage(
            @Nonnull String eventId, @Nonnull ButtonInteractionEvent event) {

        StringSelectMenu emailMenuOption = createEmailOption(eventId, eventUserController);

        if (emailMenuOption != null) {
            event.reply(SELECT_EMAIL_MESSAGE)
                    .setEphemeral(true)
                    .addActionRow(emailMenuOption)
                    .queue();
        } else {
            event.reply(NO_AUTHENTICATED_USERS_MESSAGE).setEphemeral(true).queue();
        }
    }

    @VisibleForTesting
    @Nullable
    StringSelectMenu createEmailOption(
            @Nonnull String eventId, @Nonnull EventUserController eventUserController) {

        StringSelectMenu allOptions = null;
        StringSelectMenu.Builder menu =
                StringSelectMenu.create(INVITE_DROPDOWN)
                        .setPlaceholder("Select an user to send the invitation:");

        List<EventUser> allUsers = getAllUsers(eventUserController);

        try {
            for (EventUser user : allUsers) {
                String id = user.getDiscordId();
                String userName = getNameByDiscordId(id);

                if (userName != null) {
                    menu.addOption(
                            userName + " " + user.getEmail(), user.getDiscordId() + ":" + eventId);
                } else {
                    log.error("Failed to retrieve user name by id : " + id);
                }
            }

            allOptions = menu.build();

        } catch (IllegalArgumentException e) {
            log.error("Unable to create menu for invite", e);
        }

        return allOptions;
    }

    @VisibleForTesting
    List<EventUser> getAllUsers(@Nonnull EventUserController eventUserController) {
        return eventUserController.getAllUsers();
    }

    @Nullable
    private String getNameByDiscordId(@Nonnull String discordId) {
        User user = jda.retrieveUserById(discordId).complete();
        return user != null ? user.getName() : null;
    }
}
