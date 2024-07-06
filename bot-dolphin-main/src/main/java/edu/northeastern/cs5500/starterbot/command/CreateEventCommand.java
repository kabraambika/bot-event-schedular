package edu.northeastern.cs5500.starterbot.command;

import com.google.common.annotations.VisibleForTesting;
import edu.northeastern.cs5500.starterbot.controller.StudyEventController;
import edu.northeastern.cs5500.starterbot.model.StudyEvent;
import edu.northeastern.cs5500.starterbot.model.StudyEventLocation;
import edu.northeastern.cs5500.starterbot.model.StudyEventType;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/**
 * code for create event command
 *
 * @author gmon-k
 */
@Slf4j
public class CreateEventCommand implements SlashCommandHandler {

    static final String NAME = "create-event";
    static final int DEFAULT_MAX_ATTENDEES = 100;
    private static final String ATTACHMENTS_TEXT = "attachments";
    private static final String DESCRIPTION_TEXT = "description";
    private static final String MAX_ATTENDEES_TEXT = "max_attendees";

    @Inject StudyEventController studyEventController;

    /** constructor for the class */
    @Inject
    public CreateEventCommand() {
        // Empty contructor for dagger
    }

    /**
     * function to get the command
     *
     * @param NAME return the name of the command
     */
    @Override
    @Nonnull
    public String getName() {
        return NAME;
    }

    /** function to get all the input value from the user */
    @Override
    @Nonnull
    public CommandData getCommandData() {
        OptionData locationOption =
                new OptionData(OptionType.STRING, "location", "Event location", true);
        for (StudyEventLocation location : StudyEventLocation.values()) {
            locationOption.addChoice(location.name(), location.name());
        }

        OptionData maxAttendeesOption =
                new OptionData(
                        OptionType.INTEGER,
                        MAX_ATTENDEES_TEXT,
                        "Maximum number of attendees(maximum:100)",
                        false);

        return Commands.slash(getName(), "Create a new event")
                .addOption(OptionType.STRING, "title", "Event title (max 25 characters)", true)
                .addOption(
                        OptionType.STRING,
                        "start_date_time",
                        "Start date & time (e.g., 2024-03-22T12:00)",
                        true)
                .addOption(
                        OptionType.STRING,
                        "end_date_time",
                        "End date & time (e.g., 2024-03-22T14:00)",
                        true)
                .addOptions(locationOption)
                .addOption(
                        OptionType.BOOLEAN, "is_public", "Tag meeting as public or private", true)
                .addOption(OptionType.STRING, DESCRIPTION_TEXT, "Event description")
                .addOption(OptionType.STRING, ATTACHMENTS_TEXT, "Attachments (e.g., links)")
                .addOptions(maxAttendeesOption);
    }

    /**
     * helper function for saving the mandatory values to the database
     *
     * @param title title of the event
     * @param start start date of the event
     * @param end end date of the event
     * @param location location of the event
     * @param isPublic storing if it public or private event
     * @return updated instance of StudyEvent
     */
    @VisibleForTesting
    StudyEvent createStudyEvent(
            @Nonnull String title,
            @Nonnull Date start,
            @Nonnull Date end,
            @Nonnull StudyEventLocation location,
            boolean isPublic) {

        return StudyEvent.builder()
                .title(title)
                .start(start)
                .end(end)
                .location(location)
                .eventType(isPublic ? StudyEventType.PUBLIC_EVENT : StudyEventType.PRIVATE_EVENT)
                .build();
    }

    /**
     * helper function for saving the optional values to the database
     *
     * @param studyEvent object of the study event
     * @param description description of the event
     * @param attachments attachments to be added
     * @param organizerId organizer id of the event
     * @param maxAttendees max attendees in the event
     * @return updated instance of StudyEvent
     */
    @VisibleForTesting
    StudyEvent addOptionalFieldsInStudyEvent(
            @Nonnull StudyEvent studyEvent,
            @Nonnull String description,
            @Nonnull List<String> attachments,
            @Nonnull String organizerId,
            int maxAttendees) {

        studyEvent.setDescription(description);
        studyEvent.setAttachmentFiles(attachments);
        studyEvent.setOrganizer(organizerId);
        studyEvent.setMaxAttendees(maxAttendees);

        return studyEvent;
    }

    /**
     * extended the onslash command to validate and store the value to the database
     *
     * @param SlashCommandInteractionEvent event constructor of the class to create a event
     */
    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        log.info("event: /create-event");
        String title = Objects.requireNonNull(event.getOption("title")).getAsString();
        if (!validateTitle(title)) {
            event.reply("Error: Event title must not exceed 25 characters.").queue();
            return;
        }
        String startDateTime =
                Objects.requireNonNull(event.getOption("start_date_time")).getAsString();
        String endDateTime = Objects.requireNonNull(event.getOption("end_date_time")).getAsString();
        StudyEventLocation location =
                StudyEventLocation.valueOf(
                        Objects.requireNonNull(event.getOption("location")).getAsString());
        boolean isPublic = Objects.requireNonNull(event.getOption("is_public")).getAsBoolean();
        String description =
                event.getOption(DESCRIPTION_TEXT) != null
                        ? Objects.requireNonNull(event.getOption(DESCRIPTION_TEXT)).getAsString()
                        : "";
        List<String> attachments = new ArrayList<>();
        if (event.getOption(ATTACHMENTS_TEXT) != null) {
            String attachmentsString =
                    Objects.requireNonNull(event.getOption(ATTACHMENTS_TEXT)).getAsString();
            attachments = Arrays.asList(attachmentsString.split(","));
        }
        final List<String> attachmentsFinal = attachments;

        // Convert String to Date for start and end date
        Date start = parseDate(startDateTime);
        Date end = parseDate(endDateTime);

        if (start == null || end == null) {
            event.reply("Error: Invalid date format. Please use the format YYYY-MM-DDTHH:MM.")
                    .queue();
            return;
        }

        if (start.after(end)) {
            event.reply("Error: Start date cannot be greater than end date.").queue();
            return;
        }

        String organizerId = event.getUser().getId();
        Guild guild = event.getGuild();
        Member member = event.getMember();

        if (guild == null) {
            return;
        }

        int maxAttendees =
                event.getOption(MAX_ATTENDEES_TEXT) != null
                        ? (int)
                                Objects.requireNonNull(event.getOption(MAX_ATTENDEES_TEXT))
                                        .getAsLong()
                        : DEFAULT_MAX_ATTENDEES;

        if (!validateMaxAttendees(maxAttendees)) {
            event.reply("Error: Maximum attendees must be a positive integer.").queue();
            return;
        }

        StudyEvent newStudyEvent = createStudyEvent(title, start, end, location, isPublic);

        addOptionalFieldsInStudyEvent(
                newStudyEvent, description, attachmentsFinal, organizerId, maxAttendees);

        guild.createTextChannel(title)
                .addPermissionOverride(
                        Objects.requireNonNull(member),
                        Collections.singleton(Permission.VIEW_CHANNEL),
                        null)
                .addPermissionOverride(
                        guild.getPublicRole(), null, Collections.singleton(Permission.VIEW_CHANNEL))
                .queue(
                        textChannel -> {
                            event.reply(
                                            "Event created successfully!\nMaximum attendees set to: "
                                                    + maxAttendees
                                                    + "\nCreated channel: #"
                                                    + textChannel.getName())
                                    .queue();
                            newStudyEvent.setChannelId(textChannel.getId());
                            studyEventController.createEvent(newStudyEvent);
                        },
                        error -> {
                            log.error("Failed to create text channel", error);
                            event.reply("Failed to create event.").queue();
                        });
    }

    /**
     * function to validate the tile that, it is less than 25 character
     *
     * @param title varibale to store the name of the event
     * @return true if it less than 25, else return false
     */
    public boolean validateTitle(@Nonnull String title) {
        return title.length() <= 25;
    }

    /**
     * function to parse the string and validate if it is in right format
     *
     * @param dateString variable for storing the date
     * @return dateString in the parsed way, if not return an error
     */
    Date parseDate(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        format.setLenient(false);
        try {
            java.util.Date parsed = format.parse(dateString);
            return new Date(parsed.getTime());
        } catch (ParseException e) {
            log.error("Error parsing date", e);
            return null;
        }
    }
    /**
     * function to validate the maxim attendees in the event
     *
     * @param maxAttendees variable for the maximum attendees
     * @return true if it is postive value, else return a negative value
     */
    public boolean validateMaxAttendees(int maxAttendees) {
        return maxAttendees > 0;
    }
}
