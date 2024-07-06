package edu.northeastern.cs5500.starterbot.command;

import edu.northeastern.cs5500.starterbot.controller.StudyEventController;

/**
 * Concrete command for UnRSVP operation.
 *
 * @author ambikakabra
 */
public class UnRSVPCommand {
    private static final String EVENT_ALREADY_STARTED_MESSAGE =
            "Event has already started, and it's too late to un-RSVP now.";
    private final StudyEventController studyEventController;
    private final String userId;
    private final String eventId;

    /**
     * Constructs an UnRSVPCommand object.
     *
     * @param studyEventController The StudyEventController instance.
     * @param userId The ID of the user.
     * @param eventId The ID of the event.
     */
    public UnRSVPCommand(StudyEventController studyEventController, String userId, String eventId) {
        this.studyEventController = studyEventController;
        this.userId = userId;
        this.eventId = eventId;
    }

    /**
     * Executes the UnRSVP command.
     *
     * @return The response message.
     */
    public String execute() {
        if (studyEventController.isEventStarted(eventId)) {
            return EVENT_ALREADY_STARTED_MESSAGE;
        }

        return studyEventController.removeAttendee(userId, eventId);
    }
}
