package edu.northeastern.cs5500.starterbot.controller;

import edu.northeastern.cs5500.starterbot.model.StudyEvent;
import edu.northeastern.cs5500.starterbot.model.StudyEventLocation;
import edu.northeastern.cs5500.starterbot.model.StudyEventType;
import edu.northeastern.cs5500.starterbot.repository.GenericRepository;
import edu.northeastern.cs5500.starterbot.service.FakeOpenTelemetryService;
import edu.northeastern.cs5500.starterbot.service.OpenTelemetry;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

/** Controller for managing study events */
@Slf4j
@Singleton
public class StudyEventController {
    public static final String GENERIC_ERROR = "Something went wrong! Please try again.";
    public static final String RSVP_FULL_CAPACITY_MESSAGE =
            " has reached full capacity! RSVPs are no longer available.";
    public static final String ALREADY_UNRSVP_MESSAGE = "You have already un-RSVP'd from the ";
    public static final String SUCCESS_UNRSVP_MESSAGE = "You have successfully un-RSVP'd from the ";
    public static final String SUCCESS_RSVP_MESSAGE = "You have successfully RSVP'd to the ";
    public static final String ALREADY_RSVP_MESSAGE = "You have already RSVP'd to the ";
    public static final String EVENT_STRING = " event.";

    GenericRepository<StudyEvent> studyEventRepository;
    @Inject OpenTelemetry openTelemetry;

    /**
     * Constructs a new StudyEventController
     *
     * @param eventUserRepository dbcollections of StudyEvent
     */
    @Inject
    StudyEventController(GenericRepository<StudyEvent> studyEventRepository) {
        this.studyEventRepository = studyEventRepository;
        openTelemetry = new FakeOpenTelemetryService();
    }

    /**
     * Adds an attendee to a study event.
     *
     * @param userId The ID of the user to be added as an attendee.
     * @param eventId The ID of the event to which the attendee is being added.
     * @return A message indicating the result of the operation.
     */
    public String addAttendee(String userId, String eventId) {
        if (isInvalidString(eventId) || isInvalidString(userId)) {
            return GENERIC_ERROR;
        }

        StudyEvent event = getEventById(eventId);
        List<String> attendeesList = event.getAttendeesList();

        if (attendeesList == null) {
            attendeesList = new ArrayList<>();
        }

        if (attendeesList.contains(userId)) {
            return ALREADY_RSVP_MESSAGE + event.getTitle() + EVENT_STRING;
        }

        if (event.getMaxAttendees() == 0 || attendeesList.size() < event.getMaxAttendees()) {
            attendeesList.add(userId);
            event.setAttendeesList(attendeesList);
            studyEventRepository.update(event);
            return SUCCESS_RSVP_MESSAGE + event.getTitle() + EVENT_STRING;
        }

        return event.getTitle() + RSVP_FULL_CAPACITY_MESSAGE;
    }

    /**
     * Removes an attendee from a study event.
     *
     * @param userId The ID of the user to be removed as an attendee.
     * @param eventId The ID of the event from which the attendee is being removed.
     * @return A message indicating the result of the operation.
     */
    public String removeAttendee(String userId, String eventId) {
        if (isInvalidString(eventId) || isInvalidString(userId)) {
            return GENERIC_ERROR;
        }

        StudyEvent event = getEventById(eventId);
        List<String> attendeesList = event.getAttendeesList();

        if (attendeesList == null) {
            attendeesList = new ArrayList<>();
        }

        if (attendeesList.remove(userId)) {
            event.setAttendeesList(attendeesList);
            studyEventRepository.update(event);
            return SUCCESS_UNRSVP_MESSAGE + event.getTitle() + EVENT_STRING;
        }

        return ALREADY_UNRSVP_MESSAGE + event.getTitle() + EVENT_STRING;
    }

    /**
     * Checks if an event has already started based on the provided event ID.
     *
     * @param eventId the ID of the event to check
     * @return true if the event has started, false otherwise
     */
    public boolean isEventStarted(@Nonnull String eventId) {

        StudyEvent event = getEventById(eventId);

        if (event == null) {
            return false;
        }

        Date currentDate = new Date();

        return event.getStart().compareTo(currentDate) <= 0;
    }

    /**
     * function for saving the event detail to the datbase
     *
     * @param StudyEvent event
     */
    public void createEvent(StudyEvent event) {
        studyEventRepository.add(event);
    }

    /**
     * Deletes event from database based on event objectId.
     *
     * @param eventId ObjectId of the event.
     * @return true if the event is deleted, false incase of event already started or not exists.
     */
    public boolean deleteEventById(@Nonnull ObjectId eventId) {

        if (isEventStarted(eventId.toString())) {
            return false;
        }

        StudyEvent event = studyEventRepository.get(eventId);
        if (event != null) {
            try {
                studyEventRepository.delete(eventId);
            } catch (Exception ex) {
                log.error("Unable to delete an event with id: " + eventId.toString(), ex);
                return false;
            }
            return true;
        }

        return false;
    }

    /**
     * Gets Object of Studyevent based on the event id.
     *
     * @param eventId String id of the event.
     * @return Object of the StudyEvent
     */
    public StudyEvent getEventById(@Nonnull String eventId) {
        ObjectId evObjectId = getEventObjectId(eventId);

        if (evObjectId != null) {
            return studyEventRepository.get(evObjectId);
        }

        return null;
    }

    private ObjectId getEventObjectId(@Nonnull String eventId) {
        ObjectId eventObjectId = null;

        try {
            eventObjectId = new ObjectId(eventId);
        } catch (IllegalArgumentException ex) {
            log.error("eventId cannot be converted into ObjectId", ex);
        }

        return eventObjectId;
    }

    private boolean isInvalidString(String text) {
        return text == null || text.isEmpty();
    }

    /**
     * function to get all the upcoming public and private events
     *
     * @param discordMemberId discord id of the user
     * @param location location of the event
     * @param periods time frame for the search
     * @param eventType study event type, either public or private
     * @return upcomingPublicEvents
     */
    public List<StudyEvent> getAllUpcomingEvents(
            String discordMemberId,
            StudyEventLocation location,
            String periods,
            StudyEventType eventType) {
        Collection<StudyEvent> allUpcomingPublicEvents = studyEventRepository.getAll();

        List<StudyEvent> upcomingPublicEvents =
                allUpcomingPublicEvents.stream()
                        .filter(event -> event.getOrganizer().equals(discordMemberId))
                        .filter(event -> event.getEventType().equals(eventType))
                        .filter(event -> event.getStart().after(new Date()))
                        .collect(Collectors.toList());

        upcomingPublicEvents = filterEventsByLocation(upcomingPublicEvents, location);
        upcomingPublicEvents = filterEventsByPeriod(upcomingPublicEvents, periods);

        // Sort user events based on start date and time
        upcomingPublicEvents.sort(
                (event1, event2) -> event1.getStart().compareTo(event2.getStart()));

        return upcomingPublicEvents;
    }

    /**
     * function to get all the upcoming events
     *
     * @param discordMemberId discord id of the user
     * @param location location of the event
     * @param periods time frame for the search
     * @return userEvents
     */
    public List<StudyEvent> getAllEventsForUser(
            String discordMemberId, StudyEventLocation location, String periods) {
        Collection<StudyEvent> allUserEvents = studyEventRepository.getAll();

        // Filter user preferences for the user that have not started yet
        List<StudyEvent> userEvents =
                allUserEvents.stream()
                        .filter(
                                pref ->
                                        pref.getOrganizer()
                                                .equals(discordMemberId)) // Filter by user ID
                        .filter(pref -> pref.getStart().after(new Date()))
                        // Filter events that have
                        // not
                        // started yet
                        .collect(Collectors.toList());

        // Filter events by location if provided
        userEvents = filterEventsByLocation(userEvents, location);
        userEvents = filterEventsByPeriod(userEvents, periods);

        // Sort user events based on start date and time
        userEvents.sort((event1, event2) -> event1.getStart().compareTo(event2.getStart()));

        return userEvents;
    }

    //
    /**
     * Helper method to filter events by location
     *
     * @param events events list
     * @param location location of the event
     * @return events after filtering
     */
    public List<StudyEvent> filterEventsByLocation(
            List<StudyEvent> events, StudyEventLocation location) {
        if (location != null) {
            return events.stream()
                    .filter(event -> event.getLocation() == location)
                    .collect(Collectors.toList());
        }
        return events;
    }

    /**
     * Helper method to filter events by period
     *
     * @param events events list
     * @param periods time frame for the filtering
     * @return events after filtering
     */
    public List<StudyEvent> filterEventsByPeriod(List<StudyEvent> events, String periods) {
        if (periods == null) {
            return events;
        }

        switch (periods) {
            case "this_week":
                return filterEventsForThisWeek(events);
            case "this_month":
                return filterEventsForThisMonth(events);
            default:
                return events;
        }
    }

    /**
     * helper function to filter for this week events
     *
     * @param events events list
     * @return list of event within this week
     */
    public List<StudyEvent> filterEventsForThisWeek(List<StudyEvent> events) {
        LocalDateTime startOfWeek =
                LocalDateTime.now()
                        .with(DayOfWeek.MONDAY)
                        .withHour(0)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0);
        LocalDateTime endOfWeek = startOfWeek.plusDays(7);
        return events.stream()
                .filter(
                        event ->
                                event.getStart()
                                        .after(
                                                Date.from(
                                                        startOfWeek
                                                                .atZone(ZoneId.systemDefault())
                                                                .toInstant())))
                .filter(
                        event ->
                                event.getStart()
                                        .before(
                                                Date.from(
                                                        endOfWeek
                                                                .atZone(ZoneId.systemDefault())
                                                                .toInstant())))
                .collect(Collectors.toList());
    }

    /**
     * function to filter this month events
     *
     * @param events events list
     * @return list of event within this month
     */
    public List<StudyEvent> filterEventsForThisMonth(List<StudyEvent> events) {
        LocalDateTime startOfMonth =
                LocalDateTime.now()
                        .withDayOfMonth(1)
                        .withHour(0)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusDays(1);
        return events.stream()
                .filter(
                        event ->
                                event.getStart()
                                        .after(
                                                Date.from(
                                                        startOfMonth
                                                                .atZone(ZoneId.systemDefault())
                                                                .toInstant())))
                .filter(
                        event ->
                                event.getStart()
                                        .before(
                                                Date.from(
                                                        endOfMonth
                                                                .atZone(ZoneId.systemDefault())
                                                                .toInstant())))
                .collect(Collectors.toList());
    }
    /*
     * Gets Object of Studyevent based on the event id.
     *
     * @param eventId String id of the event, Map<String, Object> updates
     * @return True or false based on if event updated
     */
    public boolean updateEvent(@Nonnull String eventId, @Nonnull Map<String, Object> updates) {
        StudyEvent event = getEventById(eventId);
        if (event == null) {
            return false; // Handle event not found
        }

        updates.forEach(
                (field, value) -> {
                    switch (field) {
                        case "title":
                            event.setTitle((String) value);
                            break;
                        case "startTime":
                            event.setStart((Date) value);
                            break;
                        case "endTime":
                            event.setEnd((Date) value);
                            break;
                        case "location":
                            event.setLocation(StudyEventLocation.valueOf((String) value));
                            break;
                        default:
                            // Log unexpected field or throw an exception if necessary
                            log.warn(
                                    "Unexpected field ["
                                            + field
                                            + "] in updates map. This field was not updated.");
                            break;
                    }
                });

        StudyEvent updatedEvent = studyEventRepository.update(event);
        return updatedEvent != null;
    }
}
