package edu.northeastern.cs5500.starterbot.model;

import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

/**
 * Represents the study event.
 *
 * @kabraambika
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyEvent implements Model {
    /** The unique identifier of the study event. */
    ObjectId id;

    /** The title of the study event. */
    String title;

    /** The start date and time of the study event */
    Date start;

    /** The end date and time of the study event */
    Date end;

    /** The "unique identifier" id of user */
    String organizer;

    /** The possible locations for study events */
    StudyEventLocation location;

    /** The type of study event can be public event or private event */
    StudyEventType eventType;

    /** The description about the study event */
    String description;

    /** The channelId for the study event */
    String channelId;

    /** The list of attachment files' url associated with the study event. */
    List<String> attachmentFiles;

    /** The maximum number of attendees in the study event */
    int maxAttendees;

    /** The list of attendees' id */
    List<String> attendeesList;

    /** The waitlisting allowed in the study event */
    int maxWaitlListAllowed;

    /** The list of waitlisting attendees' id */
    List<String> waitListAttendeesList;
}
