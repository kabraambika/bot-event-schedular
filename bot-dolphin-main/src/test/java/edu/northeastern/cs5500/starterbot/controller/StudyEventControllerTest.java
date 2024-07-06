package edu.northeastern.cs5500.starterbot.controller;

import static com.google.common.truth.Truth.assertThat;

import edu.northeastern.cs5500.starterbot.command.ListMyEventsCommand;
import edu.northeastern.cs5500.starterbot.command.ListUpcomingPublicEventsCommand;
import edu.northeastern.cs5500.starterbot.model.StudyEvent;
import edu.northeastern.cs5500.starterbot.model.StudyEventLocation;
import edu.northeastern.cs5500.starterbot.model.StudyEventType;
import edu.northeastern.cs5500.starterbot.repository.InMemoryRepository;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

public class StudyEventControllerTest {

    public static final String USER_ID_1 = "23h5ikoqaehokljhaoe";
    public static final String USER_ID_2 = "65f79ec5023ead72b483b55d";
    static final String USER_ID_3 = "test";
    public List<String> eventIDs;
    public List<StudyEvent> studyEvents;

    public StudyEventController getStudyEventController() {
        eventIDs = new ArrayList<>();
        studyEvents = new ArrayList<>();
        InMemoryRepository<StudyEvent> inMemoryRepository = new InMemoryRepository<StudyEvent>();
        StudyEvent studyEvent = getRandomStudyEventInFuture(USER_ID_2);
        StudyEvent studyEventInPast = getRandomStudyEventInPast(USER_ID_2);
        Objects.requireNonNull(studyEvent);
        Objects.requireNonNull(studyEventInPast);
        inMemoryRepository.add(studyEvent);
        inMemoryRepository.add(studyEventInPast);

        eventIDs.add(studyEvent.getId().toString());
        studyEvents.add(studyEvent);

        eventIDs.add(studyEventInPast.getId().toString());
        studyEvents.add(studyEventInPast);

        StudyEventController studyEventController = new StudyEventController(inMemoryRepository);
        return studyEventController;
    }

    private StudyEvent getRandomStudyEventInFuture(String organizerId) {

        StudyEvent studyEvent = new StudyEvent();
        studyEvent.setOrganizer(organizerId);
        studyEvent.setTitle("CS5500 project research");

        Calendar calendar = Calendar.getInstance();
        calendar.set(2025, 04, 01, 01, 00, 00);
        Date startEvent = calendar.getTime();
        studyEvent.setStart(startEvent); // starting event after 15 months
        calendar.add(Calendar.MINUTE, -15);
        Date endEvent = calendar.getTime();
        studyEvent.setEnd(endEvent); // end event after 15 mins
        studyEvent.setEventType(StudyEventType.PUBLIC_EVENT);
        studyEvent.setLocation(StudyEventLocation.ONLINE);

        studyEvent.setDescription("Research for bot");
        studyEvent.setChannelId("xxx");
        studyEvent.setAttachmentFiles(
                Arrays.asList("https://jda.wiki/using-jda/interactions/#slash-commands"));
        studyEvent.setMaxAttendees(2);
        List<String> attendeeList = new ArrayList<>();
        attendeeList.add("65f79ec5023ead72b483b55d");
        studyEvent.setAttendeesList(attendeeList);
        studyEvent.setMaxWaitlListAllowed(5);
        studyEvent.setWaitListAttendeesList(Arrays.asList("65f79ec5023ead72b4sd"));

        return studyEvent;
    }

    private StudyEvent getRandomStudyEventInPast(String organizerId) {

        StudyEvent studyEvent = new StudyEvent();
        studyEvent.setOrganizer(organizerId);
        studyEvent.setTitle("Past year event");

        Calendar calendar = Calendar.getInstance();
        calendar.set(2023, 04, 01, 01, 00, 00);
        Date startEvent = calendar.getTime();
        studyEvent.setStart(startEvent); // starting event after 15 months
        calendar.add(Calendar.MINUTE, -15);
        Date endEvent = calendar.getTime();
        studyEvent.setEnd(endEvent); // end event after 15 mins
        studyEvent.setEventType(StudyEventType.PUBLIC_EVENT);
        studyEvent.setLocation(StudyEventLocation.ONLINE);

        studyEvent.setDescription("Research for bot");
        studyEvent.setChannelId("xxx");
        studyEvent.setAttachmentFiles(
                Arrays.asList("https://jda.wiki/using-jda/interactions/#slash-commands"));
        studyEvent.setMaxAttendees(2);
        List<String> attendeeList = new ArrayList<>();
        attendeeList.add("65f79ec5023ead72b483b55d");
        studyEvent.setAttendeesList(attendeeList);
        studyEvent.setMaxWaitlListAllowed(5);
        studyEvent.setWaitListAttendeesList(Arrays.asList("65f79ec5023ead72b4sd"));

        return studyEvent;
    }

    @Test
    void testAlreadyRSVPAttendee() {
        StudyEventController studyEventController = getStudyEventController();
        String expectedAddAttendee =
                studyEventController.addAttendee("65f79ec5023ead72b483b55d", eventIDs.get(0));
        assertThat(expectedAddAttendee)
                .isEqualTo("You have already RSVP'd to the CS5500 project research event.");
    }

    @Test
    void testAddandRemoveNewRSVPAttendee() {
        StudyEventController studyEventController = getStudyEventController();
        String expectedAddAttendee = studyEventController.addAttendee("23er456", eventIDs.get(0));
        assertThat(expectedAddAttendee)
                .isEqualTo("You have successfully RSVP'd to the CS5500 project research event.");
        String expectedRemoveAttendee =
                studyEventController.removeAttendee("23er456", eventIDs.get(0));
        assertThat(expectedRemoveAttendee)
                .isEqualTo(
                        "You have successfully un-RSVP'd from the CS5500 project research event.");
    }

    @Test
    void testRemoveNonAttendee() {
        StudyEventController studyEventController = getStudyEventController();
        String expectedRemoveAttendee =
                studyEventController.removeAttendee("23er456", eventIDs.get(0));
        assertThat(expectedRemoveAttendee)
                .isEqualTo("You have already un-RSVP'd from the CS5500 project research event.");
    }

    @Test
    void testRSVPAtFullCapactiy() {
        StudyEventController studyEventController = getStudyEventController();
        studyEventController.addAttendee("23er456", eventIDs.get(0));
        String addAttendee3rdAttendee = studyEventController.addAttendee("98ut65", eventIDs.get(0));
        assertThat(addAttendee3rdAttendee)
                .isEqualTo(
                        "CS5500 project research has reached full capacity! RSVPs are no longer available.");
    }

    @Test
    void testUserIdOrEventIdEmptyStrinAddAttendee() {
        StudyEventController studyEventController = getStudyEventController();
        String expectedEmptyUser = studyEventController.addAttendee("", eventIDs.get(0));
        assertThat(expectedEmptyUser).isEqualTo("Something went wrong! Please try again.");

        String expectedEmptyEvent = studyEventController.addAttendee("98ut65", "");
        assertThat(expectedEmptyEvent).isEqualTo("Something went wrong! Please try again.");

        String expectedEmptyEventUser = studyEventController.addAttendee("", "");
        assertThat(expectedEmptyEventUser).isEqualTo("Something went wrong! Please try again.");

        String expectedNullUserEvent = studyEventController.addAttendee(null, null);
        assertThat(expectedNullUserEvent).isEqualTo("Something went wrong! Please try again.");

        String expectedNullEvent = studyEventController.addAttendee("", null);
        assertThat(expectedNullEvent).isEqualTo("Something went wrong! Please try again.");

        String expectedNullUser = studyEventController.addAttendee(null, "");
        assertThat(expectedNullUser).isEqualTo("Something went wrong! Please try again.");
    }

    @Test
    void testUserIdOrEventIdEmptyStrinRemoveAttendee() {
        StudyEventController studyEventController = getStudyEventController();
        String expectedEmptyUser = studyEventController.removeAttendee("", eventIDs.get(0));
        assertThat(expectedEmptyUser).isEqualTo("Something went wrong! Please try again.");

        String expectedEmptyEvent = studyEventController.removeAttendee("98ut65", "");
        assertThat(expectedEmptyEvent).isEqualTo("Something went wrong! Please try again.");

        String expectedEmptyEventUser = studyEventController.removeAttendee("", "");
        assertThat(expectedEmptyEventUser).isEqualTo("Something went wrong! Please try again.");

        String expectedNullUserEvent = studyEventController.removeAttendee(null, null);
        assertThat(expectedNullUserEvent).isEqualTo("Something went wrong! Please try again.");

        String expectedNullEvent = studyEventController.removeAttendee("", null);
        assertThat(expectedNullEvent).isEqualTo("Something went wrong! Please try again.");

        String expectedNullUser = studyEventController.removeAttendee(null, "");
        assertThat(expectedNullUser).isEqualTo("Something went wrong! Please try again.");
    }

    void testCreateEventWithNullValues() {
        StudyEvent nullEvent = new StudyEvent();
        StudyEventController studyEventController = getStudyEventController();
        studyEventController.createEvent(nullEvent);
    }

    @Test
    void testIsEventStartedForFutureEvent() {
        StudyEventController studyEventController = getStudyEventController();
        String eventId = studyEvents.get(0).getId().toString();
        boolean actualResult = studyEventController.isEventStarted(eventId);
        assertThat(actualResult).isFalse();
    }

    @Test
    void testIsEventStartedForPastEvent() {
        StudyEventController studyEventController = getStudyEventController();
        String eventId = studyEvents.get(1).getId().toString();
        boolean actualResult = studyEventController.isEventStarted(eventId);
        assertThat(actualResult).isTrue();
    }

    @Test
    void testDeleteEventByIdForPastEvent() {
        StudyEventController studyEventController = getStudyEventController();
        String eventId = studyEvents.get(1).getId().toString();
        ObjectId eventObjectId = new ObjectId(eventId);

        boolean isDeleted = studyEventController.deleteEventById(eventObjectId);
        assertThat(isDeleted).isFalse();
    }

    @Test
    void testDeleteEventException() {
        StudyEventController studyEventController = getStudyEventController();
        String eventId = studyEvents.get(1).getId().toString();
        ObjectId eventObjectId = new ObjectId(eventId);

        boolean isDeleted = studyEventController.deleteEventById(eventObjectId);
        assertThat(isDeleted).isFalse();
    }

    @Test
    void testGetEventById() {
        StudyEventController studyEventController = getStudyEventController();
        String eventId = studyEvents.get(1).getId().toString();
        StudyEvent actualEvent = studyEventController.getEventById(eventId);
        StudyEvent expected = studyEvents.get(1);
        assertThat(actualEvent).isEqualTo(expected);
    }

    @Test
    void testFilterEventsByLocationNotNull() {
        // Create a list of study events
        List<StudyEvent> events = new ArrayList<>();
        StudyEventLocation location = StudyEventLocation.ONLINE;

        StudyEventController study = new StudyEventController(null);
        // Choose a non-null location

        // Add some study events with the chosen location
        StudyEvent event1 = new StudyEvent();
        event1.setLocation(location);
        events.add(event1);

        StudyEvent event2 = new StudyEvent();
        event2.setLocation(location);
        events.add(event2);

        // Call the method to be tested
        List<StudyEvent> filteredEvents = study.filterEventsByLocation(events, location);

        // Assert that only events with the chosen location are returned
        assertThat(2).isEqualTo(filteredEvents.size());
        for (StudyEvent event : filteredEvents) {
            assertThat(location).isEqualTo(event.getLocation());
        }
    }

    @Test
    void testFilterEventsByLocationNull() {
        // Create a list of study events
        List<StudyEvent> events = new ArrayList<>();
        StudyEventLocation location = null; // Choose a null location

        // Add some study events with various locations
        StudyEvent event1 = new StudyEvent();
        event1.setLocation(StudyEventLocation.ONLINE);
        events.add(event1);

        StudyEventController study = new StudyEventController(null);

        // Call the method to be tested
        List<StudyEvent> filteredEvents = study.filterEventsByLocation(events, location);

        // Assert that all events are returned since location is null
        assertThat(events.size()).isEqualTo(filteredEvents.size());
    }
    // Helper method to generate a study event with a specified start date
    private StudyEvent createStudyEvent(Date start) {
        StudyEvent event = new StudyEvent();
        event.setStart(start);
        return event;
    }

    @Test
    void testFilterEventsForThisWeek() {
        // Get the current week's start and end dates
        LocalDateTime startOfWeek =
                LocalDateTime.now().with(DayOfWeek.MONDAY).truncatedTo(ChronoUnit.DAYS);
        LocalDateTime endOfWeek = startOfWeek.plusDays(7).truncatedTo(ChronoUnit.DAYS);

        // Create study events for the current week
        List<StudyEvent> events = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            events.add(
                    createStudyEvent(
                            Date.from(
                                    startOfWeek
                                            .plusDays(i)
                                            .atZone(ZoneId.systemDefault())
                                            .toInstant())));
        }

        // Create study events outside of the current week
        events.add(
                createStudyEvent(
                        Date.from(
                                startOfWeek
                                        .minusDays(1)
                                        .atZone(ZoneId.systemDefault())
                                        .toInstant())));
        events.add(
                createStudyEvent(
                        Date.from(
                                endOfWeek.plusDays(1).atZone(ZoneId.systemDefault()).toInstant())));
        StudyEventController study = new StudyEventController(null);

        // Filter events for the current week
        List<StudyEvent> filteredEvents = study.filterEventsForThisWeek(events);

        // Assert that only events within the current week are returned
        for (StudyEvent event : filteredEvents) {
            assertThat(
                            event.getStart()
                                    .after(
                                            Date.from(
                                                    startOfWeek
                                                            .atZone(ZoneId.systemDefault())
                                                            .toInstant())))
                    .isTrue();
            assertThat(
                            event.getStart()
                                    .before(
                                            Date.from(
                                                    endOfWeek
                                                            .atZone(ZoneId.systemDefault())
                                                            .toInstant())))
                    .isTrue();
        }
    }

    @Test
    void testFilterEventsForThisMonth() {
        // Get the current month's start and end dates
        LocalDateTime startOfMonth =
                LocalDateTime.now().withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
        LocalDateTime endOfMonth =
                startOfMonth.plusMonths(1).minusDays(1).truncatedTo(ChronoUnit.DAYS);

        // Create study events for the current month
        List<StudyEvent> events = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            events.add(
                    createStudyEvent(
                            Date.from(
                                    startOfMonth
                                            .plusDays(i)
                                            .atZone(ZoneId.systemDefault())
                                            .toInstant())));
        }

        // Create study events outside of the current month
        events.add(
                createStudyEvent(
                        Date.from(
                                startOfMonth
                                        .minusDays(1)
                                        .atZone(ZoneId.systemDefault())
                                        .toInstant())));
        events.add(
                createStudyEvent(
                        Date.from(
                                endOfMonth
                                        .plusDays(1)
                                        .atZone(ZoneId.systemDefault())
                                        .toInstant())));

        // Filter events for the current month
        StudyEventController study = new StudyEventController(null);

        List<StudyEvent> filteredEvents = study.filterEventsForThisMonth(events);

        // Assert that only events within the current month are returned
        for (StudyEvent event : filteredEvents) {
            assertThat(
                            event.getStart()
                                    .after(
                                            Date.from(
                                                    startOfMonth
                                                            .atZone(ZoneId.systemDefault())
                                                            .toInstant())))
                    .isTrue();
            assertThat(
                            event.getStart()
                                    .before(
                                            Date.from(
                                                    endOfMonth
                                                            .atZone(ZoneId.systemDefault())
                                                            .toInstant())))
                    .isTrue();
        }
    }

    @Test
    public void testBuildLocationOption() {
        ListMyEventsCommand builder = new ListMyEventsCommand();
        OptionData locationOption = builder.buildLocationOption();

        assertThat(locationOption).isNotNull();
        assertThat(OptionType.STRING).isEqualTo(locationOption.getType());
        assertThat("location").isEqualTo(locationOption.getName());
        assertThat("Event location").isEqualTo(locationOption.getDescription());

        // Assuming StudyEventLocation.values() returns an array of StudyEventLocation
        assertThat(StudyEventLocation.values().length)
                .isEqualTo(locationOption.getChoices().size());
        // Add more assertions to check specific choices if needed
    }

    @Test
    public void testBuildPeriodOption() {
        ListMyEventsCommand builder = new ListMyEventsCommand();
        OptionData periodOption = builder.buildPeriodOption();

        assertThat(periodOption).isNotNull();
        assertThat(OptionType.STRING).isEqualTo(periodOption.getType());
        assertThat("periods").isEqualTo(periodOption.getName());
        assertThat("Choose the period").isEqualTo(periodOption.getDescription());

        // Assuming two choices are added
        assertThat(2).isEqualTo(periodOption.getChoices().size());
        // Add more assertions to check specific choices if needed
    }

    @Test
    public void testBuildLocationOptionPublicEvent() {
        ListUpcomingPublicEventsCommand builder = new ListUpcomingPublicEventsCommand();
        OptionData locationOption = builder.buildLocationOption();

        assertThat(locationOption).isNotNull();
        assertThat(OptionType.STRING).isEqualTo(locationOption.getType());
        assertThat("location").isEqualTo(locationOption.getName());
        assertThat("Event location").isEqualTo(locationOption.getDescription());

        // Assuming StudyEventLocation.values() returns an array of StudyEventLocation
        assertThat(StudyEventLocation.values().length)
                .isEqualTo(locationOption.getChoices().size());
        // Add more assertions to check specific choices if needed
    }

    @Test
    public void testBuildPeriodOptionPublicEvent() {
        ListUpcomingPublicEventsCommand builder = new ListUpcomingPublicEventsCommand();
        OptionData periodOption = builder.buildPeriodOption();

        assertThat(periodOption).isNotNull();
        assertThat(OptionType.STRING).isEqualTo(periodOption.getType());
        assertThat("periods").isEqualTo(periodOption.getName());
        assertThat("Choose the period").isEqualTo(periodOption.getDescription());

        // Assuming two choices are added
        assertThat(2).isEqualTo(periodOption.getChoices().size());
        // Add more assertions to check specific choices if needed
    }

    void testUpdateEvent() {

        StudyEventController studyEventController = getStudyEventController();
        StudyEvent existingEvent = studyEvents.get(0);
        String eventId = existingEvent.getId().toString();

        Map<String, Object> updates = new HashMap<>();
        updates.put("title", "Updated Event Title");
        updates.put("startTime", new Date());
        updates.put("endTime", new Date());
        updates.put("location", StudyEventLocation.BOSTON.name());

        boolean updateResult = studyEventController.updateEvent(eventId, updates);
        assertThat(updateResult).isTrue();

        StudyEvent updatedEvent = studyEventController.getEventById(eventId);

        assertThat(updatedEvent.getTitle()).isEqualTo("Updated Event Title");
        assertThat(updatedEvent.getLocation()).isEqualTo(StudyEventLocation.BOSTON);
    }

    @Test
    void testUpdateEventNonExistingFieldOnly() {

        StudyEventController studyEventController = getStudyEventController();
        StudyEvent existingEvent = studyEvents.get(0);
        String eventId = existingEvent.getId().toString();

        Map<String, Object> updates = new HashMap<>();
        updates.put("created", "today"); // Invalid field

        boolean updateResult = studyEventController.updateEvent(eventId, updates);
        assertThat(updateResult).isTrue();

        StudyEvent updatedEvent = studyEventController.getEventById(eventId);

        assertThat(updatedEvent.getTitle()).isEqualTo("CS5500 project research");
    }

    @Test
    void testUpdateEventNonExistingEventId() {

        StudyEventController studyEventController = getStudyEventController();
        String eventId = "random";

        Map<String, Object> updates = new HashMap<>();
        updates.put("created", "today"); // Invalid field

        boolean updateResult = studyEventController.updateEvent(eventId, updates);
        assertThat(updateResult).isFalse(); // event does not exist
    }
}
