package edu.northeastern.cs5500.starterbot.controller;

import edu.northeastern.cs5500.starterbot.model.EventUser;
import edu.northeastern.cs5500.starterbot.repository.GenericRepository;
import edu.northeastern.cs5500.starterbot.service.FakeOpenTelemetryService;
import edu.northeastern.cs5500.starterbot.service.OpenTelemetry;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

/** Controller class for managing event users */
@Slf4j
@Singleton
public class EventUserController {
    GenericRepository<EventUser> eventUserRepository;

    static final String EMAIL_INVALID_MESSAGE =
            "Email is invalid(< 20 characters) or doesn't end with @northeastern.edu";
    static final String WELCOME = "Welcome ";
    static final String EMAIL_VERIFICATION_SUCCESS_MESSAGE = "!, you are successfully verified.";
    static final String USER_ALREADY_VERIFIED = "You are already verified!";
    static final String ERROR = "Something went wrong, please try again later.";
    static final int MIN_EMAIL_LENGTH = 20;
    @Inject OpenTelemetry openTelemetry;

    /**
     * Constructs a new EventUserController
     *
     * @param eventUserRepository dbcollections of EventUser
     */
    @Inject
    public EventUserController(GenericRepository<EventUser> eventUserRepository) {
        this.eventUserRepository = eventUserRepository;
        openTelemetry = new FakeOpenTelemetryService();
    }

    /**
     * Checks if a user with the given Discord username is present in the database.
     *
     * @param discordUserName The Discord username of the user.
     * @return true if the user exists (is verified), false otherwise.
     */
    public boolean isUserVerified(String discordUserId) {
        // Iterate through all EventUser objects and check if any matches the discordUserId
        return eventUserRepository.getAll().stream()
                .anyMatch(user -> discordUserId.equals(user.getDiscordId()));
    }

    /**
     * Adds the eventUser to the database after successful email verification
     *
     * @param eventUser user data object
     * @return Reply according to the case like invalid email, user already verified or successful
     *     verification
     */
    public String addVerifiedEventUser(@Nonnull EventUser eventUser) {

        // Handle Invalid EventUSer Email format case
        if (!validateEmail(eventUser.getEmail())) {
            log.info(EMAIL_INVALID_MESSAGE);
            return EMAIL_INVALID_MESSAGE;
        }

        // Handle EventUser already verified case
        Collection<EventUser> allEventUsers = this.eventUserRepository.getAll();
        for (EventUser dbEventUser : allEventUsers) {
            if (dbEventUser.getDiscordId() != null
                    && dbEventUser.getDiscordId().equals(eventUser.getDiscordId())) {
                return USER_ALREADY_VERIFIED;
            }
        }

        // Handle new UserEvent Verification and adding to EventUser Table
        try {
            this.eventUserRepository.add(eventUser);
        } catch (Error e) {
            log.error("Something went wrong with the DB");
            return ERROR;
        }

        return WELCOME + eventUser.getName() + EMAIL_VERIFICATION_SUCCESS_MESSAGE;
    }

    private boolean validateEmail(@Nonnull String email) {
        return email.length() >= MIN_EMAIL_LENGTH
                && email.toLowerCase().endsWith("@northeastern.edu");
    }

    /**
     * gets all the users in EventUser from database
     *
     * @return list of EventUser
     */
    public List<EventUser> getAllUsers() {
        Collection<EventUser> allUsers = eventUserRepository.getAll();

        return allUsers.stream().collect(Collectors.toList());
    }
}
