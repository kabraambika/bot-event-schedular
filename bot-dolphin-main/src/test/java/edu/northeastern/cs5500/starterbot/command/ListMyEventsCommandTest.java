package edu.northeastern.cs5500.starterbot.command;

import static com.google.common.truth.Truth.assertThat;

import edu.northeastern.cs5500.starterbot.controller.StudyEventController;
import edu.northeastern.cs5500.starterbot.controller.StudyEventControllerTest;
import edu.northeastern.cs5500.starterbot.model.StudyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListMyEventsCommandTest {
    StudyEventControllerTest sControllerTest;
    StudyEventController controller;

    @BeforeEach
    void setUp() {
        sControllerTest = new StudyEventControllerTest();
        controller = sControllerTest.getStudyEventController();
    }

    @Test
    void testGetName() {
        ListMyEventsCommand command = new ListMyEventsCommand();
        assertThat(command.getName()).isEqualTo("list-my-events");
    }

    @Test
    void testGetCommandData() {
        ListMyEventsCommand command = new ListMyEventsCommand();
        CommandData commandData = command.getCommandData();
        String name = "list-my-events";
        assertThat(commandData.getName()).isEqualTo(name);
    }

    @Test
    void testCreatMessageEmbeds() {
        ListMyEventsCommand command = new ListMyEventsCommand();

        List<StudyEvent> studyEvents = new ArrayList<>();
        studyEvents.add(sControllerTest.studyEvents.get(0));

        MessageEmbed aEmbed = command.creatEventMessageEmbed(studyEvents.get(0));

        MessageEmbed mockEmbed = createMockBuilder();

        Map<String, Object> actualMap = aEmbed.toData().toMap();
        Map<String, Object> expectedMap = mockEmbed.toData().toMap();

        assertThat(actualMap).isEqualTo(expectedMap);
    }

    private MessageEmbed createMockBuilder() {
        EmbedBuilder embedBuilder =
                new EmbedBuilder()
                        .setTitle("CS5500 project research")
                        .addField("Start", "Thu May 01 01:00:00 UTC 2025", true)
                        .addField("End", "Thu May 01 00:45:00 UTC 2025", true)
                        .addField("Location", "ONLINE", true)
                        .addField("Event Type", "PUBLIC_EVENT", true)
                        .addField("Description", "Research for bot", false)
                        .addField(
                                "Attachment Files",
                                "\n https://jda.wiki/using-jda/interactions/#slash-commands",
                                false)
                        .addField("Max Attendees", "2", true)
                        .addField("Max Waitlist Allowed", "5", true);

        return embedBuilder.build();
    }

    @Test
    void testCreateMessageData() {
        ListMyEventsCommand command = new ListMyEventsCommand();

        List<StudyEvent> studyEvents = new ArrayList<>();
        studyEvents.add(sControllerTest.studyEvents.get(0));

        MessageCreateData acutalMessageData = command.createMessageData(studyEvents.get(0));
        MessageCreateData mockCreateData = createMockMessageCreateData();

        Map<String, Object> actualMap = acutalMessageData.toData().toMap();
        Map<String, Object> expectedMap = mockCreateData.toData().toMap();

        assertThat(actualMap).isEqualTo(expectedMap);
    }

    private MessageCreateData createMockMessageCreateData() {
        MessageEmbed mockEmbed = createMockBuilder();
        MessageCreateBuilder messageBuilder = new MessageCreateBuilder().addEmbeds(mockEmbed);
        String eventId = sControllerTest.studyEvents.get(0).getId().toString();

        Button editButton = Button.danger("delete" + ":" + eventId, "Delete");
        Button editEventButton = Button.primary("edit_event:" + eventId, "Edit");

        messageBuilder.addActionRow(editEventButton, editButton);
        return messageBuilder.build();
    }
}
