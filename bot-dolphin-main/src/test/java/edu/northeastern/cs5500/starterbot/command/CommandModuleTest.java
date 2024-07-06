package edu.northeastern.cs5500.starterbot.command;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CommandModuleTest {

    private CommandModule commandModule;

    @BeforeEach
    public void setUp() {
        commandModule = new CommandModule();
    }

    @Test
    public void testProvideCreateEventCommand() {
        // Create a stub instance of CreateEventCommand
        CreateEventCommand createEventCommand = new CreateEventCommand();
        SlashCommandHandler result = commandModule.provideCreateEventCommand(createEventCommand);

        // Assert that the result is not null and of the expected type
        assertNotNull(result);
        assertTrue(result instanceof CreateEventCommand);
    }
}
