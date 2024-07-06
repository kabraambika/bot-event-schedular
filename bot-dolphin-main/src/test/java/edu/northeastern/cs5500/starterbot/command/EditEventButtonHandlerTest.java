package edu.northeastern.cs5500.starterbot.command;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

import edu.northeastern.cs5500.starterbot.controller.StudyEventController;
import edu.northeastern.cs5500.starterbot.controller.StudyEventControllerTest;
import java.util.Date;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EditEventButtonHandlerTest {
    private StudyEventController controller;
    private EditEventButtonHandler handler;

    @BeforeEach
    void setUp() {
        StudyEventControllerTest sControllerTest = new StudyEventControllerTest();
        controller = sControllerTest.getStudyEventController();
        handler = new EditEventButtonHandler();
        handler.studyEventController = controller;
    }

    @Test
    void testGetName() {
        assertThat(handler.getName()).isEqualTo("edit_event");
    }

    @Test
    void testCreateEventEditModal() {
        String eventId = "dummyEventId";
        Modal modal = handler.createEventEditModal(eventId);
        assertThat(modal.getId()).isEqualTo("edit_event_" + eventId);
        assertThat(modal.getTitle()).isEqualTo("Edit Event");
        assertThat(modal.getActionRows()).hasSize(4);
    }

    @Test
    void testParseDateCorrectFormat() {
        String validDateString = "2024-01-01T12:00";
        Date date = handler.parseDate(validDateString);
        assertThat(date).isNotNull();
    }

    @Test
    void testParseDateInvalidFormat() {
        String invalidDateString = "invalid_date";
        Date date = handler.parseDate(invalidDateString);
        assertNull(date);
    }

    @Test
    void testValidateTitleEmpty() {
        assertThat(handler.validateTitle("")).isFalse();
    }

    @Test
    void testValidateTitleValid() {
        assertThat(handler.validateTitle("New event title")).isTrue();
    }

    @Test
    void testValidateTitleMoreThan25Char() {
        assertThat(handler.validateTitle("New event title more than 25 characters")).isFalse();
    }

    @Test
    void testValidStartTimeEmpty() {
        assertThat(handler.validateStartTime("", null, null)).isFalse();
    }

    @Test
    void testValidStartTimeValid() {
        String futureStartTime = "2026-07-17T22:00";
        String futureNewEndTime = "2026-07-17T23:00";
        assertThat(handler.validateStartTime(futureStartTime, futureNewEndTime, null)).isTrue();
    }

    @Test
    void testValidStartTimeNoCompare() {
        String futureStartTime = "2026-07-17T22:00";
        String futureNewEndTime = "2026";
        assertThat(handler.validateStartTime(futureStartTime, futureNewEndTime, null)).isFalse();
    }

    @Test
    void testValidStartTimeCompareExistingEndDate() {
        String futureStartTime = "2026-07-17T22:00";
        String futureNewEndTime = "2026";
        Date correctEndDate = handler.parseDate("2027-07-17T22:00");
        assertThat(handler.validateStartTime(futureStartTime, futureNewEndTime, correctEndDate))
                .isTrue();
    }

    @Test
    void testValidStartTimeInValid() {
        Date pastEndTime = handler.parseDate("2021-07-17T22:00");
        String futureNewEndTime = "2026-07-17T23:00";
        assertThat(handler.validateStartTime("2025-0-17T22:00", futureNewEndTime, pastEndTime))
                .isFalse();
    }

    @Test
    void testValidStartTimeInValidFormat() {
        String pastTime = "14-14-2024";
        assertThat(handler.validateStartTime(pastTime, "", null)).isFalse();
    }

    @Test
    void testValidEndTimeInValidFormat() {
        Date eventStartTime = handler.parseDate("2024-08-10T12:00");
        assertThat(handler.validateEndTime("2024-12-15", null, eventStartTime)).isFalse();
    }

    @Test
    void testValidEndTimeEmpty() {
        Date eventStartTime = handler.parseDate("2024-08-10T12:00");
        assertThat(handler.validateEndTime("", null, eventStartTime)).isFalse();
    }

    @Test
    void testValidEndTimeValidWithExistingStartTime() {
        Date eventStartTime = handler.parseDate("2024-08-10T12:00");
        assertThat(handler.validateEndTime("2024-08-12T22:00", "", eventStartTime)).isTrue();
    }

    @Test
    void testValidEndTimeValidWithNewStartTime() {
        Date eventStartTime = handler.parseDate("2024-08-10T12:00");
        assertThat(handler.validateEndTime("2024-12-12T12:00", "2024-12-10T12:00", eventStartTime))
                .isTrue();
    }

    @Test
    void testValidateLocationEmpty() {
        assertThat(handler.validateLocation("")).isFalse();
    }

    @Test
    void testValidateLocationInvalidName() {
        assertThat(handler.validateLocation("225 terry ave")).isFalse();
    }

    @Test
    void testValidateLocationValidName() {
        assertThat(handler.validateLocation("Seattle")).isTrue();
    }

    @Test
    void testGetEventForUpdate() {
        assertThat(handler.getStudyEventForUpdate("", controller)).isNull();
    }

    @Test
    void testIsNonNullEmptyStringNull() {
        assertThat(handler.isNonNullEmptyString(null)).isFalse();
    }

    @Test
    void testIsNonNullEmptyStringEmpty() {
        assertThat(handler.isNonNullEmptyString("")).isFalse();
    }

    @Test
    void testIsNonNullEmptyStringCorrect() {
        assertThat(handler.isNonNullEmptyString("dummy name")).isTrue();
    }
}
