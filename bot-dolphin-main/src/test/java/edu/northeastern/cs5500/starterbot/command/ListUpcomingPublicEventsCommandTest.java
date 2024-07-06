package edu.northeastern.cs5500.starterbot.command;

import static com.google.common.truth.Truth.assertThat;

import edu.northeastern.cs5500.starterbot.controller.StudyEventController;
import edu.northeastern.cs5500.starterbot.controller.StudyEventControllerTest;
import edu.northeastern.cs5500.starterbot.model.StudyEvent;
import java.util.Calendar;
import java.util.Date;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.junit.jupiter.api.Test;

class ListUpcomingPublicEventsCommandTest {
    StudyEventControllerTest sControllerTest = new StudyEventControllerTest();
    StudyEventController controller = sControllerTest.getStudyEventController();

    @Test
    void testGetCommandData() {
        ListUpcomingPublicEventsCommand command = new ListUpcomingPublicEventsCommand();
        CommandData commandData = command.getCommandData();
        String name = "list-upcoming-public-events";
        assertThat(commandData.getName()).isEqualTo(name);
    }

    @Test
    void testGetName() {
        ListUpcomingPublicEventsCommand command = new ListUpcomingPublicEventsCommand();
        assertThat(command.getName()).isEqualTo("list-upcoming-public-events");
    }

    @Test
    void testGetFormattedDate() {
        ListUpcomingPublicEventsCommand command = new ListUpcomingPublicEventsCommand();

        Calendar calendar = Calendar.getInstance();
        calendar.set(2024, 04, 01, 01, 00);

        Date startEvent = calendar.getTime();
        assertThat(command.getFormattedDate(startEvent)).isEqualTo("05-01-24");
    }

    @Test
    void testGetFormattedTime() {
        ListUpcomingPublicEventsCommand command = new ListUpcomingPublicEventsCommand();

        Calendar calendar = Calendar.getInstance();
        calendar.set(2024, 04, 01, 01, 00);
        calendar.set(Calendar.MINUTE, 15);
        Date startEvent = calendar.getTime();
        assertThat(command.getFormattedTime(startEvent)).isEqualTo("01:15");
    }

    @Test
    void testNumberOfAvailableSeats() {
        ListUpcomingPublicEventsCommand command = new ListUpcomingPublicEventsCommand();
        StudyEvent event = sControllerTest.studyEvents.get(0);
        int actualResult = command.numberOfAvailableSeats(event);
        assertThat(actualResult).isEqualTo(1);
    }

    @Test
    void testCreateEventEmbed() {
        ListUpcomingPublicEventsCommand command = new ListUpcomingPublicEventsCommand();
        StudyEvent event = sControllerTest.studyEvents.get(0);
        MessageEmbed actualResult =
                command.createEventMessageEmbed(event, "65f79ec5023ead72b483b55d");
        MessageEmbed expectBuilder = createMockBuilder();
        assertThat(expectBuilder.toData().toMap()).isEqualTo(actualResult.toData().toMap());
    }

    private MessageEmbed createMockBuilder() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("CS5500 project research");
        embedBuilder.appendDescription("Research for bot");

        embedBuilder.addField("Date:", "05-01-25", true);
        embedBuilder.addField("Start Time: ", "01:00", true);
        embedBuilder.addField("Location: ", "ONLINE", true);

        int availableSeats = 1;
        embedBuilder.addField("Available Seats: ", availableSeats + " seats remaining", true);

        embedBuilder.addField("RSVP Status:", "Already RSVP'D", false).setColor(0x00FF00);
        return embedBuilder.build();
    }

    @Test
    void testGetMessageCreateData() {
        ListUpcomingPublicEventsCommand command = new ListUpcomingPublicEventsCommand();
        StudyEvent event = sControllerTest.studyEvents.get(0);
        MessageCreateData actualCreateData =
                command.getMessageCreateData(event, "65f79ec5023ead72b483b55d");
        MessageCreateData expectedCreateData = createMessageDataMocks();

        assertThat(expectedCreateData.toData().toJson())
                .isEqualTo(actualCreateData.toData().toJson());
    }

    private MessageCreateData createMessageDataMocks() {
        StudyEvent event = sControllerTest.studyEvents.get(0);
        MessageEmbed mockEmbed = createMockBuilder();
        MessageCreateBuilder mockBuilder = new MessageCreateBuilder().addEmbeds(mockEmbed);
        mockBuilder.addActionRow(RSVPButtonHandler.createUnrsvpButton(event.getId().toString()));
        return mockBuilder.build();
    }
}
