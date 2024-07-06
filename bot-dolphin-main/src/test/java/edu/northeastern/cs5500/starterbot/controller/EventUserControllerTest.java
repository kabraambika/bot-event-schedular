package edu.northeastern.cs5500.starterbot.controller;

import static com.google.common.truth.Truth.assertThat;

import edu.northeastern.cs5500.starterbot.model.EventUser;
import edu.northeastern.cs5500.starterbot.repository.InMemoryRepository;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventUserControllerTest {

    private InMemoryRepository<EventUser> inMemoryRepository;
    private EventUserController eventUserController;
    private EventUser eventUser;
    static final String USER_DISCORD_ID = "1234";
    static final String USER_EMAIL = "dolphin@northeastern.edu";
    static final String USER_NAME = "FlyingDolphin";
    static final String INVALID_USER_EMAIL_1 = "d@northeastern.edu";
    static final String INVALID_USER_EMAIL_2 = "dani.b@northwestern.edu";

    @BeforeEach
    void setUp() {
        inMemoryRepository = new InMemoryRepository<>();
        eventUserController = new EventUserController(inMemoryRepository);

        eventUser = new EventUser();
        eventUser.setDiscordId(USER_DISCORD_ID);
        eventUser.setEmail(USER_EMAIL);

        // Add a sample verified user to the repository
        EventUser verifiedUser = new EventUser();
        verifiedUser.setDiscordId("12345");
        inMemoryRepository.add(verifiedUser);
    }

    @Test
    void testUserIsVerified() {
        // Test a user that exists in the database
        boolean isVerified = eventUserController.isUserVerified("12345");
        assertThat(isVerified).isTrue();
    }

    @Test
    void testUserIsNotVerified() {
        // Test a user that does not exist in the database
        boolean isVerified = eventUserController.isUserVerified("6789");
        assertThat(isVerified).isFalse();
    }

    private List<String> getListOfDiscordIds(Collection<EventUser> eventUsers) {
        return eventUsers.stream().map(EventUser::getDiscordId).collect(Collectors.toList());
    }

    @Test
    void testAddVerifiedEventUserWithZeroUsers() {
        Collection<EventUser> listEventUsers1 = inMemoryRepository.getAll();
        assertThat(getListOfDiscordIds(listEventUsers1).contains(USER_DISCORD_ID)).isFalse();
    }

    @Test
    void testAddVerifiedEventUser() {

        eventUserController.addVerifiedEventUser(eventUser);
        Collection<EventUser> listEventUsers2 = inMemoryRepository.getAll();
        assertThat(getListOfDiscordIds(listEventUsers2).contains(USER_DISCORD_ID)).isTrue();
    }

    @Test
    void testAddSameVerifiedEventUser() {

        eventUserController.addVerifiedEventUser(eventUser);
        Collection<EventUser> listEventUsers2 = inMemoryRepository.getAll();
        eventUserController.addVerifiedEventUser(eventUser);
        Collection<EventUser> listEventUsers3 = inMemoryRepository.getAll();
        assertThat(getListOfDiscordIds(listEventUsers3).contains(USER_DISCORD_ID)).isTrue();
        assertThat(listEventUsers2).isEqualTo(listEventUsers3);
    }

    @Test
    void testAddVerifiedEventUserWithInvalidEmail() {

        // Invalid length
        eventUser.setEmail(INVALID_USER_EMAIL_1);
        eventUserController.addVerifiedEventUser(eventUser);
        Collection<EventUser> listEventUsers4 = inMemoryRepository.getAll();
        assertThat(getListOfDiscordIds(listEventUsers4).contains(USER_DISCORD_ID)).isFalse();

        // Invalid Domain or Uni
        eventUser.setEmail(INVALID_USER_EMAIL_2);
        eventUserController.addVerifiedEventUser(eventUser);
        Collection<EventUser> listEventUsers5 = inMemoryRepository.getAll();
        assertThat(getListOfDiscordIds(listEventUsers5).contains(USER_DISCORD_ID)).isFalse();
    }

    @Test
    void testGetAllUsers() {
        List<EventUser> expectedUsers =
                inMemoryRepository.getAll().stream().collect(Collectors.toList());
        List<EventUser> actualUsers = eventUserController.getAllUsers();
        assertThat(actualUsers).isEqualTo(expectedUsers);
    }
}
