package edu.northeastern.cs5500.starterbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

/**
 * Represents a user participating in events, with associated details such as name, role, email, and
 * calendar integration tokens.
 *
 * @kabraambika
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventUser implements Model {
    /** The unique identifier of the user. */
    ObjectId id;

    /** The Snowflake id of the user on the Discord server. */
    String discordId;

    /** The name of the user. */
    String name;

    /** The role of the user, which can either be Student or Staff. */
    EventUserRole role;

    /** The email address of the user. */
    String email;

    /** The calendar access token of the user for integrating with Outlook calendar. */
    String userCalendarAccessToken;

    /** The calendar refresh token of the user for integrating with Outlook calendar. */
    String userCalendarRefreshToken;
}
