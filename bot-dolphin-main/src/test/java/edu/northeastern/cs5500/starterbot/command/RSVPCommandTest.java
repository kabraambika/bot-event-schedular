package edu.northeastern.cs5500.starterbot.command;

import static com.google.common.truth.Truth.assertThat;

import edu.northeastern.cs5500.starterbot.controller.StudyEventControllerTest;
import org.junit.jupiter.api.Test;

class RSVPCommandTest {
    @Test
    void testExecute() {
        StudyEventControllerTest sControllerTest = new StudyEventControllerTest();
        RSVPCommand command =
                new RSVPCommand(
                        sControllerTest.getStudyEventController(),
                        "123",
                        sControllerTest.eventIDs.get(0));

        assertThat(command.execute())
                .isEqualTo("You have successfully RSVP'd to the CS5500 project research event.");
    }

    @Test
    void testExecuteAlreadyEventStarted() {
        StudyEventControllerTest sControllerTest = new StudyEventControllerTest();
        RSVPCommand command =
                new RSVPCommand(
                        sControllerTest.getStudyEventController(),
                        "123",
                        sControllerTest.eventIDs.get(1));

        assertThat(command.execute())
                .isEqualTo("Event has already started, and it's too late to RSVP now.");
    }
}
