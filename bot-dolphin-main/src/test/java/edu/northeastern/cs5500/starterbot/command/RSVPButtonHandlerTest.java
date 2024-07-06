package edu.northeastern.cs5500.starterbot.command;

import static com.google.common.truth.Truth.assertThat;

import edu.northeastern.cs5500.starterbot.controller.StudyEventController;
import edu.northeastern.cs5500.starterbot.controller.StudyEventControllerTest;
import edu.northeastern.cs5500.starterbot.model.StudyEvent;
import edu.northeastern.cs5500.starterbot.model.StudyEventType;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RSVPButtonHandlerTest {
    StudyEventControllerTest sControllerTest;
    StudyEventController controller;

    @BeforeEach
    void setUp() {
        sControllerTest = new StudyEventControllerTest();
        controller = sControllerTest.getStudyEventController();
    }

    @Test
    void testGetName() {
        RSVPButtonHandler rButtonHandler = new RSVPButtonHandler();
        String name = rButtonHandler.getName();
        assertThat(name).isEqualTo(RSVPButtonHandler.RSVP_NAME);
    }

    @Test
    void testCreateRsvpButton() {
        String eventId = "234f567";
        Button rsvpButton = Button.primary(RSVPButtonHandler.RSVP_NAME + ":" + eventId, "RSVP");

        assertThat(RSVPButtonHandler.createRsvpButton("234f567")).isEqualTo(rsvpButton);
    }

    @Test
    void testCreateUnrsvpButton() {
        String eventId = "234f567";
        Button unRsvpButton =
                Button.danger(RSVPButtonHandler.UNRSVP_NAME + ":" + eventId, "UNRSVP");

        RSVPButtonHandler rButtonHandler = new RSVPButtonHandler();
        assertThat(rButtonHandler.createUnrsvpButton("234f567")).isEqualTo(unRsvpButton);
    }

    @Test
    void testSendAcceptanceMessageEventPublic() {
        RSVPButtonHandler rButtonHandler = new RSVPButtonHandler();

        StudyEvent studyEvent = sControllerTest.studyEvents.get(0);

        boolean actualResponse =
                rButtonHandler.sendAcceptancePrivateMessage(
                        studyEvent.getId().toString(),
                        studyEvent.getOrganizer(),
                        StudyEventController.SUCCESS_RSVP_MESSAGE,
                        controller);
        assertThat(actualResponse).isFalse();
    }

    @Test
    void testSendAcceptanceMessageEventNull() {
        RSVPButtonHandler rButtonHandler = new RSVPButtonHandler();

        StudyEvent studyEvent = sControllerTest.studyEvents.get(0);
        studyEvent.setEventType(StudyEventType.PRIVATE_EVENT);
        boolean actualResponse =
                rButtonHandler.sendAcceptancePrivateMessage(
                        "123",
                        studyEvent.getOrganizer(),
                        StudyEventController.SUCCESS_RSVP_MESSAGE,
                        controller);
        assertThat(actualResponse).isFalse();
    }

    @Test
    void testSendAcceptanceMessageRSVPMessage() {
        RSVPButtonHandler rButtonHandler = new RSVPButtonHandler();

        StudyEvent studyEvent = sControllerTest.studyEvents.get(0);
        studyEvent.setEventType(null);
        boolean actualResponse =
                rButtonHandler.sendAcceptancePrivateMessage(
                        studyEvent.getId().toString(),
                        studyEvent.getOrganizer(),
                        "You have already RSVP'd this event",
                        controller);
        assertThat(actualResponse).isFalse();
    }

    @Test
    void testHandleRsvpSuccessMessage() {
        StudyEvent studyEventMocks = sControllerTest.studyEvents.get(0);

        RSVPButtonHandler rButtonHandler = new RSVPButtonHandler();
        String actualResponse =
                rButtonHandler.handleRSVP("123", studyEventMocks.getId().toString(), controller);

        assertThat(actualResponse)
                .isEqualTo("You have successfully RSVP'd to the CS5500 project research event.");
    }

    @Test
    void testHandleUnrsvpAlreadyMessage() {
        StudyEvent studyEventMocks = sControllerTest.studyEvents.get(0);

        RSVPButtonHandler rButtonHandler = new RSVPButtonHandler();

        String actualResponse =
                rButtonHandler.handleConfirmUnRSVP(
                        "123", studyEventMocks.getId().toString(), controller);
        assertThat(actualResponse)
                .isEqualTo("You have already un-RSVP'd from the CS5500 project research event.");
    }
}
