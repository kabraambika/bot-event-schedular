package edu.northeastern.cs5500.starterbot.command;

import com.google.common.annotations.VisibleForTesting;
import edu.northeastern.cs5500.starterbot.controller.StudyEventController;
import edu.northeastern.cs5500.starterbot.model.StudyEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

/** This class represents the edit button for editing the event by event owner and its modal */
@Slf4j
public class EditEventButtonHandler implements ButtonHandler {
    private static final String END_TIME_TEXT_FIELD_ID = "end_time";

    private static final String START_TIME_TEXT_FIELD_ID = "start_time";

    private static final String LOCATION_TEXT_FIELD_ID = "location";

    private static final String TITLE_TEXT_FIELD_ID = "title";

    static final String EDIT_EVENT_NAME = "edit_event";

    @Inject StudyEventController studyEventController;

    // Constructor omitted for brevity

    /** Empty constructor for Dagger injection. */
    @Inject
    public EditEventButtonHandler() {
        // Empty constructor for dagger
    }

    @Override
    @Nonnull
    public String getName() {
        return EDIT_EVENT_NAME;
    }

    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        String componentId = event.getComponentId();

        String eventId = componentId.substring("edit_event_".length());

        if (studyEventController.isEventStarted(eventId)) {
            event.reply("You cannot edit this event as it has already started.")
                    .setEphemeral(true)
                    .queue();
        } else {

            event.replyModal(createEventEditModal(eventId)).queue();
        }
    }

    @VisibleForTesting
    Modal createEventEditModal(String eventId) {

        TextInput titleInput =
                TextInput.create(TITLE_TEXT_FIELD_ID, "Title", TextInputStyle.SHORT)
                        .setPlaceholder("Enter new event title")
                        .setRequired(false)
                        .build();

        TextInput startTimeInput =
                TextInput.create(START_TIME_TEXT_FIELD_ID, "Start Time", TextInputStyle.SHORT)
                        .setPlaceholder("Enter new start time (e.g., 2024-01-01T12:00)")
                        .setRequired(false)
                        .build();

        TextInput endTimeInput =
                TextInput.create(END_TIME_TEXT_FIELD_ID, "End Time", TextInputStyle.SHORT)
                        .setPlaceholder("Enter new end time (e.g., 2024-01-01T12:00)")
                        .setRequired(false)
                        .build();

        TextInput location =
                TextInput.create(LOCATION_TEXT_FIELD_ID, "Location", TextInputStyle.SHORT)
                        .setPlaceholder("Enter the new Location")
                        .setRequired(false)
                        .build();

        return Modal.create("edit_event_" + eventId, "Edit Event")
                .addActionRows(
                        ActionRow.of(titleInput),
                        ActionRow.of(startTimeInput),
                        ActionRow.of(endTimeInput),
                        ActionRow.of(location))
                .build();
    }

    public void handleEditEventModalInteraction(
            @Nonnull ModalInteractionEvent event, @Nonnull String eventId) {
        StudyEvent studyEvent = getStudyEventForUpdate(eventId, studyEventController);

        if (studyEvent == null) {
            replyEphemeral(event, "Event not found.");
            return;
        }

        Map<String, Object> updates = new HashMap<>();

        String title = event.getValue(TITLE_TEXT_FIELD_ID).getAsString();
        String startTimeStr = event.getValue(START_TIME_TEXT_FIELD_ID).getAsString();
        String endTimeStr = event.getValue(END_TIME_TEXT_FIELD_ID).getAsString();
        String location = event.getValue(LOCATION_TEXT_FIELD_ID).getAsString();

        if (!validateAndUpdateTitle(event, updates, title)
                || !validateAndUpdateStartTime(
                        event, updates, startTimeStr, endTimeStr, studyEvent.getEnd())
                || !validateAndUpdateEndTime(
                        event, updates, endTimeStr, startTimeStr, studyEvent.getStart())
                || !validateAndUpdateLocation(event, updates, location)) {
            return;
        }

        if (updates.isEmpty()) {
            replyEphemeral(event, "No updates provided.");
            return;
        }

        if (studyEventController.updateEvent(eventId, updates)) {
            replyEphemeral(event, "Event updated successfully!");
        } else {
            replyEphemeral(event, "There was a problem during update");
        }
    }

    private boolean validateAndUpdateTitle(
            @Nonnull ModalInteractionEvent event, Map<String, Object> updates, String title) {
        if (isNonNullEmptyString(title)) {
            if (validateTitle(title)) {
                updates.put(TITLE_TEXT_FIELD_ID, title);
                return true;
            } else {
                replyEphemeral(event, "Error: Title length should be less than 25 characters");
                return false;
            }
        }
        return true;
    }

    private boolean validateAndUpdateStartTime(
            @Nonnull ModalInteractionEvent event,
            @Nonnull Map<String, Object> updates,
            String startTimeStr,
            String endTimeStr,
            Date existingEndDate) {
        if (isNonNullEmptyString(startTimeStr)) {
            if (validateStartTime(startTimeStr, endTimeStr, existingEndDate)) {
                updates.put("startTime", parseDate(startTimeStr));
                return true;
            } else {
                replyEphemeral(
                        event,
                        "Error: Invalid start time format or start date must be before end date of the event.");
                return false;
            }
        }
        return true;
    }

    private boolean validateAndUpdateEndTime(
            @Nonnull ModalInteractionEvent event,
            @Nonnull Map<String, Object> updates,
            String endTimeStr,
            String startTimeStr,
            Date existingStartDate) {
        if (isNonNullEmptyString(endTimeStr)) {
            if (validateEndTime(endTimeStr, startTimeStr, existingStartDate)) {
                updates.put("endTime", parseDate(endTimeStr));
                return true;
            } else {
                replyEphemeral(
                        event,
                        "Error: Invalid end time format and also end time must be after the start time.");
                return false;
            }
        }
        return true;
    }

    private boolean validateAndUpdateLocation(
            @Nonnull ModalInteractionEvent event,
            @Nonnull Map<String, Object> updates,
            String location) {
        if (isNonNullEmptyString(location)) {
            if (validateLocation(location)) {
                updates.put(LOCATION_TEXT_FIELD_ID, location.toUpperCase());
                return true;
            } else {
                replyEphemeral(event, "Error: Invalid location, please enter a valid location.");
                return false;
            }
        }
        return true;
    }

    @VisibleForTesting
    StudyEvent getStudyEventForUpdate(
            @Nonnull String eventId, @Nonnull StudyEventController studyEventController) {
        return studyEventController.getEventById(eventId);
    }

    @VisibleForTesting
    boolean isNonNullEmptyString(String text) {
        return text != null && !text.isEmpty();
    }

    @VisibleForTesting
    boolean validateTitle(@Nonnull String title) {
        if (title.isEmpty()) {
            return false;
        }
        return title.length() <= 25;
    }

    @VisibleForTesting
    boolean validateStartTime(
            @Nonnull String startTimeStr, String endTimeStr, Date existingEndDate) {
        if (startTimeStr.isEmpty()) {
            return false;
        }

        Date startTime = parseDate(startTimeStr);
        if (startTime == null) {
            return false;
        }

        if (isNonNullEmptyString(endTimeStr)) {
            Date newEndDate = parseDate(endTimeStr);
            if (newEndDate != null) {
                return startTime.compareTo(newEndDate) < 0;
            }
        }

        return existingEndDate != null && startTime.compareTo(existingEndDate) < 0;
    }

    @VisibleForTesting
    boolean validateEndTime(
            @Nonnull String endTimeStr, String startTimeStr, Date existingStartDate) {
        if (endTimeStr.isEmpty()) {
            return false;
        }

        Date endTime = parseDate(endTimeStr);
        if (endTime == null) {
            return false;
        }

        if (isNonNullEmptyString(startTimeStr)) {
            Date newStartTime = parseDate(startTimeStr);
            if (newStartTime != null) {
                return newStartTime.compareTo(endTime) < 0;
            }
        }

        return existingStartDate != null && existingStartDate.compareTo(endTime) < 0;
    }

    @VisibleForTesting
    boolean validateLocation(@Nonnull String location) {
        if (location.isEmpty()) {
            return false;
        }

        List<String> availableLocations =
                Arrays.asList("ONLINE", "SEATTLE", "BOSTON", "PORTLAND", "SILICON_VALLEY");
        return availableLocations.contains(location.toUpperCase());
    }

    private void replyEphemeral(@Nonnull ModalInteractionEvent event, @Nonnull String message) {
        event.reply(message).setEphemeral(true).queue();
    }

    // Validate date format and parse
    /**
     * @param dateString date taken from the user
     * @return parsed date
     */
    Date parseDate(@Nonnull String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        format.setLenient(false); // Disable lenient parsing
        try {
            java.util.Date parsed = format.parse(dateString);
            return new Date(parsed.getTime());
        } catch (ParseException e) {
            log.error("Error parsing date", e);
            return null;
        }
    }
}
