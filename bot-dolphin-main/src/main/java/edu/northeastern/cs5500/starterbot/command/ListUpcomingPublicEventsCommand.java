package edu.northeastern.cs5500.starterbot.command;

import com.google.common.annotations.VisibleForTesting;
import edu.northeastern.cs5500.starterbot.controller.StudyEventController;
import edu.northeastern.cs5500.starterbot.model.StudyEvent;
import edu.northeastern.cs5500.starterbot.model.StudyEventLocation;
import edu.northeastern.cs5500.starterbot.model.StudyEventType;
import edu.northeastern.cs5500.starterbot.util.SendPrivateMessageUtil;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

/*
 * Implementation of command to view all upcoming public events happenining anywhere(Online and Offline)
 *
 * @author akarsh033, kabraambika
 *
 */
@Slf4j
@Singleton
public class ListUpcomingPublicEventsCommand implements SlashCommandHandler {

    static final String NAME = "list-upcoming-public-events";

    @Inject StudyEventController studyEventController;
    @Inject RSVPButtonHandler rsvpHandler;
    @Inject JDA jda;

    @Inject
    public ListUpcomingPublicEventsCommand() {
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
        OptionData locationOption = buildLocationOption();
        OptionData periodOption = buildPeriodOption();
        return Commands.slash(getName(), "List all upcoming public events")
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
        log.info("event: /list-upcoming-public-events");
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

        // Get the list of upcoming events from the MongoDB Client
        List<StudyEvent> upcomingPublicEvents =
                studyEventController.getAllUpcomingEvents(
                        userId, location, periods, StudyEventType.PUBLIC_EVENT);

        // Handle if the list is empty
        if (upcomingPublicEvents.isEmpty()) {
            event.reply("There are currently no upcoming events.").setEphemeral(true).queue();
            return;
        }

        for (StudyEvent eventItem : upcomingPublicEvents) {
            MessageCreateData messageCreateData = getMessageCreateData(eventItem, userId);
            SendPrivateMessageUtil.sendMessage(jda, userId, messageCreateData);
        }

        event.reply("To view the upcoming public events, please check your private channel!")
                .setEphemeral(true)
                .queue();
    }

    /**
     * returns message embed along with RSVP/Un-Rsvp button.
     *
     * @param studyEvent study event instance.
     * @param userId the discord id of the user.
     * @return instance of MessageCreateData.
     */
    MessageCreateData getMessageCreateData(@Nonnull StudyEvent studyEvent, @Nonnull String userId) {
        MessageEmbed embed = createEventMessageEmbed(studyEvent, userId);
        MessageCreateBuilder messageBuilder = new MessageCreateBuilder().addEmbeds(embed);
        List<String> attendeeIds = studyEvent.getAttendeesList();

        if (attendeeIds == null) {
            attendeeIds = new ArrayList<>();
        }

        boolean isAlreadyRsvped = attendeeIds.contains(userId);

        if (isAlreadyRsvped) {
            messageBuilder.addActionRow(
                    RSVPButtonHandler.createUnrsvpButton(studyEvent.getId().toString()));
        } else {
            messageBuilder.addActionRow(
                    RSVPButtonHandler.createRsvpButton(studyEvent.getId().toString()));
        }

        return messageBuilder.build();
    }

    @VisibleForTesting
    String getFormattedDate(Date startDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        // Calendar class represents months as a zero-based index, so that's why adding 1
        int month = calendar.get(Calendar.MONTH) + 1;

        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // modulo divided by 100 using the % operator to return the last two digits of the year
        int year = calendar.get(Calendar.YEAR) % 100;

        return String.format("%02d-%02d-%02d", month, day, year);
    }

    @VisibleForTesting
    String getFormattedTime(Date startDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);

        return String.format("%02d:%02d", hours, minutes);
    }

    @VisibleForTesting
    int numberOfAvailableSeats(@Nonnull StudyEvent event) {
        List<String> attendeeIds = event.getAttendeesList();

        if (attendeeIds == null) {
            attendeeIds = new ArrayList<>();
        }

        return Math.max(0, event.getMaxAttendees() - attendeeIds.size());
    }

    @VisibleForTesting
    MessageEmbed createEventMessageEmbed(StudyEvent studyEvent, String userId) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(studyEvent.getTitle());
        String description = studyEvent.getDescription();
        if (description != null) {
            embedBuilder.appendDescription(description);
        }

        Date startDate = studyEvent.getStart();
        String formattedDate = getFormattedDate(startDate);
        Objects.requireNonNull(formattedDate);
        embedBuilder.addField("Date:", formattedDate, true);

        String formattedTime = getFormattedTime(startDate);
        Objects.requireNonNull(formattedTime);
        embedBuilder.addField("Start Time: ", formattedTime, true);

        String location = studyEvent.getLocation().toString();
        Objects.requireNonNull(location);
        embedBuilder.addField("Location: ", location, true);

        int availableSeats = numberOfAvailableSeats(studyEvent);

        if (studyEvent.getMaxAttendees() != 0) {
            embedBuilder.addField("Available Seats: ", availableSeats + " seats remaining", true);
        }

        List<String> attendeeIds = studyEvent.getAttendeesList();

        if (attendeeIds == null) {
            attendeeIds = new ArrayList<>();
        }

        boolean isAlreadyRsvped = attendeeIds.contains(userId);

        if (isAlreadyRsvped) {
            embedBuilder.addField("RSVP Status:", "Already RSVP'D", false).setColor(0x00FF00);
        }
        return embedBuilder.build();
    }
}
