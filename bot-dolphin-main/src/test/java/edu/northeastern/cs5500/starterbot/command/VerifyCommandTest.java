package edu.northeastern.cs5500.starterbot.command;

import static com.google.common.truth.Truth.assertThat;

import edu.northeastern.cs5500.starterbot.model.EventUser;
import edu.northeastern.cs5500.starterbot.model.EventUserRole;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.junit.jupiter.api.Test;

class VerifyCommandTest {
    static final String USER_DISCORD_ID = "1234";
    static final String USER_EMAIL = "dolphin@northeastern.edu";
    static final String USER_NAME = "FlyingDoplhin";
    static final String INVALID_USER_EMAIL = "d@northwestern.edu";
    static final String EMAIL_INVALID_MESSAGE =
            "Email is invalid(< 20 characters) or doesn't end with @northeastern.edu";
    static final String WELCOME = "Welcome ";
    static final String EMAIL_VERIFICATION_SUCCESS_MESSAGE = "!, you are successfully verified.";
    static final String USER_ALREADY_VERIFIED = "You are already verified!";

    @Test
    void testGetCommandData() {
        VerifyCommand command = new VerifyCommand();
        CommandData commandData = command.getCommandData();
        String name = "verify";
        assertThat(commandData.getName()).isEqualTo(name);
    }

    @Test
    void testGetName() {
        VerifyCommand command = new VerifyCommand();
        assertThat(command.getName()).isEqualTo("verify");
    }

    @Test
    void testBuildEventUserStudent() {
        VerifyCommand command = new VerifyCommand();

        EventUser eventUser2 =
                command.buildEventUser(
                        USER_NAME, USER_DISCORD_ID, USER_EMAIL, EventUserRole.STUDENT.toString());

        assertThat(eventUser2.getDiscordId()).isEqualTo(USER_DISCORD_ID);
        assertThat(eventUser2.getName()).isEqualTo(USER_NAME);
        assertThat(eventUser2.getEmail()).isEqualTo(USER_EMAIL);
        assertThat(eventUser2.getRole()).isEqualTo(EventUserRole.STUDENT);
    }

    @Test
    void testBuildEventUserStaff() {
        VerifyCommand command = new VerifyCommand();
        EventUser eventUser1 =
                command.buildEventUser(
                        USER_NAME, USER_DISCORD_ID, USER_EMAIL, EventUserRole.STAFF.toString());

        assertThat(eventUser1.getDiscordId()).isEqualTo(USER_DISCORD_ID);
        assertThat(eventUser1.getName()).isEqualTo(USER_NAME);
        assertThat(eventUser1.getEmail()).isEqualTo(USER_EMAIL);
        assertThat(eventUser1.getRole()).isEqualTo(EventUserRole.STAFF);
    }
}
