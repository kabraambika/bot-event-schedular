package edu.northeastern.cs5500.starterbot.command;

import static com.google.common.truth.Truth.assertThat;

import edu.northeastern.cs5500.starterbot.controller.EventUserController;
import edu.northeastern.cs5500.starterbot.controller.StudyEventController;
import edu.northeastern.cs5500.starterbot.controller.StudyEventControllerTest;
import edu.northeastern.cs5500.starterbot.model.EventUser;
import edu.northeastern.cs5500.starterbot.model.StudyEvent;
import edu.northeastern.cs5500.starterbot.repository.InMemoryRepository;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MyEventButtonHandlerTest {

    StudyEventControllerTest sControllerTest;
    StudyEventController controller;
    private InMemoryRepository<EventUser> inMemoryRepository;
    private EventUserController eventUserController;

    private void setEventUserMocks() {
        inMemoryRepository = new InMemoryRepository<>();
        eventUserController = new EventUserController(inMemoryRepository);

        EventUser eventUser = new EventUser();
        eventUser.setDiscordId("1003824807");
        eventUser.setEmail("kabraambika@northeastern.edu");
        inMemoryRepository.add(eventUser);

        EventUser verifiedUser = new EventUser();
        verifiedUser.setDiscordId("1003824807");
        verifiedUser.setEmail("xyz@northeastern.edu");
        inMemoryRepository.add(verifiedUser);
    }

    @BeforeEach
    void setUp() {
        sControllerTest = new StudyEventControllerTest();
        controller = sControllerTest.getStudyEventController();
    }

    @Test
    void testGetName() {
        MyEventButtonHandler eventButtonHandler = new MyEventButtonHandler();
        assertThat(eventButtonHandler.getName()).isEqualTo("delete");
    }

    @Test
    void testCreateConfirmDeleteButtons() {
        MyEventButtonHandler eventButtonHandler = new MyEventButtonHandler();
        String eventId = sControllerTest.studyEvents.get(0).getId().toString();
        List<LayoutComponent> actualComponents =
                eventButtonHandler.createConfirmDeleteButtons(eventId);
        List<LayoutComponent> mockComponents = mockDeleteButtons();

        assertThat(actualComponents).isEqualTo(mockComponents);
    }

    private List<LayoutComponent> mockDeleteButtons() {
        List<LayoutComponent> buttonComponents = new ArrayList<>();
        String eventId = sControllerTest.studyEvents.get(0).getId().toString();

        Button yesButton = Button.primary("yes" + ":" + eventId, "Yes");
        Button noButton = Button.secondary("no" + ":" + eventId, "No");
        buttonComponents.add(ActionRow.of(yesButton, noButton));

        return buttonComponents;
    }

    @Test
    void testGetEvent() {
        MyEventButtonHandler eventButtonHandler = new MyEventButtonHandler();
        String eventId = sControllerTest.studyEvents.get(0).getId().toString();
        assertThat(eventButtonHandler.getEvent("a", controller)).isEqualTo(null);
        assertThat(eventButtonHandler.getEvent(eventId, controller)).isNotNull();
    }

    @Test
    void testGetAttendees() {
        MyEventButtonHandler eventButtonHandler = new MyEventButtonHandler();
        StudyEvent studyEvent = sControllerTest.studyEvents.get(0);
        assertThat(eventButtonHandler.getAttendees(studyEvent)).isNotNull();
    }

    @Test
    void testDeleteStudyEventHelper() {
        MyEventButtonHandler eventButtonHandler = new MyEventButtonHandler();
        StudyEvent studyEvent = sControllerTest.studyEvents.get(0);
        List<String> attList = eventButtonHandler.getAttendees(studyEvent);

        String actualResult =
                eventButtonHandler.deleteStudyEventHelper(studyEvent, controller, attList);
        assertThat(actualResult).isEqualTo("Event has been successfully deleted.");

        // trying to delete again same event
        actualResult = eventButtonHandler.deleteStudyEventHelper(studyEvent, controller, attList);
        assertThat(actualResult).isEqualTo("This event no longer exists and cannot be deleted.");
    }

    @Test
    void testDeleteStudyEvent() {
        MyEventButtonHandler eventButtonHandler = new MyEventButtonHandler();
        StudyEvent studyEvent = sControllerTest.studyEvents.get(0);

        String actualResult = eventButtonHandler.deleteStudyEvent(studyEvent, controller);
        assertThat(actualResult).isEqualTo("Event has been successfully deleted.");

        // trying to delete again same event
        actualResult = eventButtonHandler.deleteStudyEvent(studyEvent, controller);
        assertThat(actualResult).isEqualTo("This event no longer exists and cannot be deleted.");
    }

    @Test
    void testCreatePersonalInviteForExistingEvent() {
        MyEventButtonHandler eventButtonHandler = new MyEventButtonHandler();
        String actualMessage =
                eventButtonHandler.createPersonalInviteMessage(
                        "123456", sControllerTest.eventIDs.get(0), controller);
        String expectedMessage =
                "Hey <@"
                        + Long.parseLong("123456")
                        + ">, do you want to join CS5500 project research event beginning on Thu May 01 01:00:00 UTC 2025?";

        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    @Test
    void testCreatePersonalInviteForNonExistingEvent() {
        MyEventButtonHandler eventButtonHandler = new MyEventButtonHandler();
        String actualMessage =
                eventButtonHandler.createPersonalInviteMessage("123456", "12", controller);
        String expectedMessage = "";

        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    @Test
    void testCreateEmailOption() {
        inMemoryRepository = new InMemoryRepository<>();
        eventUserController = new EventUserController(inMemoryRepository);
        MyEventButtonHandler eventButtonHandler = new MyEventButtonHandler();

        StringSelectMenu actualSelectMenu =
                eventButtonHandler.createEmailOption("123", eventUserController);
        assertThat(actualSelectMenu).isNull();
    }

    @Test
    void testGetAllUsers() {
        setEventUserMocks();

        MyEventButtonHandler eventButtonHandler = new MyEventButtonHandler();
        List<EventUser> actualUsersList = eventButtonHandler.getAllUsers(eventUserController);
        List<EventUser> expectedUsersList = eventUserController.getAllUsers();

        assertThat(actualUsersList).isEqualTo(expectedUsersList);
    }
}
