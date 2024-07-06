package edu.northeastern.cs5500.starterbot.command;

import static com.google.common.truth.Truth.assertThat;

import edu.northeastern.cs5500.starterbot.controller.StudyEventController;
import edu.northeastern.cs5500.starterbot.controller.StudyEventControllerTest;
import edu.northeastern.cs5500.starterbot.model.StudyEvent;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UpcomingPrivateEventsCommandTest {
    StudyEventControllerTest sControllerTest;
    StudyEventController controller;

    @BeforeEach
    void setUp() {
        sControllerTest = new StudyEventControllerTest();
        controller = sControllerTest.getStudyEventController();
    }

    @Test
    void testGetName() {
        UpcomingPrivateEventsCommand command = new UpcomingPrivateEventsCommand();
        assertThat(command.getName()).isEqualTo("list-upcoming-private-events");
    }

    @Test
    void testGetCommandData() {
        UpcomingPrivateEventsCommand command = new UpcomingPrivateEventsCommand();
        CommandData commandData = command.getCommandData();
        String name = "list-upcoming-private-events";
        assertThat(commandData.getName()).isEqualTo(name);
    }

    @Test
    void testGetPrivateEventFromDatabaseEmpty() {
        UpcomingPrivateEventsCommand command = new UpcomingPrivateEventsCommand();
        List<StudyEvent> actualPrivateEvents =
                command.getPrivateEventFromDatabase(StudyEventControllerTest.USER_ID_1, controller);
        assertThat(actualPrivateEvents).isEmpty();
    }

    @Test
    void testHasUserRSVPedWithEmptyAttendeesList() {
        UpcomingPrivateEventsCommand command = new UpcomingPrivateEventsCommand();
        boolean actualResponse =
                command.hasUserRSVPed(StudyEventControllerTest.USER_ID_1, new ArrayList<String>());

        assertThat(actualResponse).isFalse(); // No attendees present
    }

    @Test
    void testHasUserRSVPedWithNoAttendeesList() {
        UpcomingPrivateEventsCommand command = new UpcomingPrivateEventsCommand();
        boolean actualResponse = command.hasUserRSVPed(StudyEventControllerTest.USER_ID_1, null);

        assertThat(actualResponse).isFalse(); // No attendees present
    }

    @Test
    void testHasUserRSVPedNotYet() {
        UpcomingPrivateEventsCommand command = new UpcomingPrivateEventsCommand();
        List<String> attendeesList = sControllerTest.studyEvents.get(0).getAttendeesList();
        boolean actualResponse =
                command.hasUserRSVPed(StudyEventControllerTest.USER_ID_1, attendeesList);

        assertThat(actualResponse).isFalse(); // attendees present but not this user id
    }

    @Test
    void testHasUserRSVPedTrue() {
        UpcomingPrivateEventsCommand command = new UpcomingPrivateEventsCommand();
        List<String> attendeesList = sControllerTest.studyEvents.get(0).getAttendeesList();
        boolean actualResponse = command.hasUserRSVPed("65f79ec5023ead72b483b55d", attendeesList);

        assertThat(actualResponse).isTrue(); // attendees present
    }
}
