package edu.northeastern.cs5500.starterbot.command;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import edu.northeastern.cs5500.starterbot.model.StudyEvent;
import edu.northeastern.cs5500.starterbot.model.StudyEventLocation;
import edu.northeastern.cs5500.starterbot.model.StudyEventType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreateEventCommandTest {

    private CreateEventCommand createEventCommand;

    @BeforeEach
    void setUp() {
        createEventCommand = new CreateEventCommand();
    }

    @Test
    void testValidateTitle_ValidTitle() {
        CreateEventCommand command = new CreateEventCommand();
        String validTitle = "Sample Title";
        assertThat(command.validateTitle(validTitle)).isTrue();
    }

    @Test
    void testValidateTitle_TitleExceedsMaxLength() {
        CreateEventCommand command = new CreateEventCommand();
        String longTitle = "This is a very long title that exceeds the maximum length allowed";
        assertThat(command.validateTitle(longTitle)).isFalse();
    }

    @Test
    void testValidateTitle_EmptyTitle() {
        CreateEventCommand command = new CreateEventCommand();
        String emptyTitle = "";
        assertThat(command.validateTitle(emptyTitle)).isTrue();
    }

    @Test
    void testGetName() {
        assertThat(createEventCommand.getName()).isEqualTo(CreateEventCommand.NAME);
    }

    @Test
    void testParseDateValidFormat() {
        String dateString = "2024-04-01T12:00";

        Date result = createEventCommand.parseDate(dateString);

        assertNotNull(result);
    }

    @Test
    void testParseDateInvalidFormat() {
        String dateString = "invalid_date_format";

        Date result = createEventCommand.parseDate(dateString);

        assertNull(result);
    }

    @Test
    void testGetCommandData() {
        CommandData commandData = createEventCommand.getCommandData();

        assertThat(commandData.getName()).isEqualTo(CreateEventCommand.NAME);
    }

    @Test
    void testValidateMaxAttendees_PositiveValue() {
        CreateEventCommand command = new CreateEventCommand();
        assertThat(command.validateMaxAttendees(10)).isTrue();
    }

    @Test
    void testValidateMaxAttendees_ZeroValue() {
        CreateEventCommand command = new CreateEventCommand();
        assertThat(command.validateMaxAttendees(0)).isFalse();
    }

    @Test
    void testValidateMaxAttendees_NegativeValue() {
        CreateEventCommand command = new CreateEventCommand();
        assertThat(command.validateMaxAttendees(-10)).isFalse();
    }

    @Test
    void testCreateStudyEvent() {
        // Mock input parameters
        String title = "Sample Event";
        Date start = new Date();
        Date end = new Date();
        StudyEventLocation location = StudyEventLocation.ONLINE;
        boolean isPublic = true;
        String description = "Sample description";
        List<String> attachments = new ArrayList<>();
        String organizerId = "organizer123";
        int maxAttendees = 50;

        // Call the method directly
        StudyEvent studyEvent =
                createEventCommand.createStudyEvent(title, start, end, location, isPublic);
        createEventCommand.addOptionalFieldsInStudyEvent(
                studyEvent, description, attachments, organizerId, maxAttendees);
        // Assert the result
        assertThat(title).isEqualTo(studyEvent.getTitle());
        assertThat(start).isEqualTo(studyEvent.getStart());
        assertThat(end).isEqualTo(studyEvent.getEnd());
        assertThat(location).isEqualTo(studyEvent.getLocation());
        assertThat(StudyEventType.PUBLIC_EVENT).isEqualTo(studyEvent.getEventType());
        assertThat(description).isEqualTo(studyEvent.getDescription());
        assertThat(attachments).isEqualTo(studyEvent.getAttachmentFiles());
        assertThat(organizerId).isEqualTo(studyEvent.getOrganizer());
        assertThat(maxAttendees).isEqualTo(studyEvent.getMaxAttendees());
    }
}
