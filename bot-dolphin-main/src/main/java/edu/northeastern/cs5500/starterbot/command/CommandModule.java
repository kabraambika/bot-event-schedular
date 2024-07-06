package edu.northeastern.cs5500.starterbot.command;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import edu.northeastern.cs5500.starterbot.ExcludeFromJacocoGeneratedReport;

@ExcludeFromJacocoGeneratedReport
@Module
public class CommandModule {
    @Provides
    @IntoMap
    @StringKey(DropdownCommand.NAME)
    public SlashCommandHandler provideDropdownCommand(DropdownCommand dropdownCommand) {
        return dropdownCommand;
    }

    @Provides
    @IntoMap
    @StringKey(CreateEventCommand.NAME)
    public SlashCommandHandler provideCreateEventCommand(CreateEventCommand createEventCommand) {
        return createEventCommand;
    }

    @Provides
    @IntoMap
    @StringKey(DropdownCommand.NAME)
    public StringSelectHandler provideDropdownCommandMenuHandler(DropdownCommand dropdownCommand) {
        return dropdownCommand;
    }

    @Provides
    @IntoMap
    @StringKey(ListUpcomingPublicEventsCommand.NAME)
    public SlashCommandHandler provideListUpcomingPublicEventsCommand(
            ListUpcomingPublicEventsCommand listUpcomingPublicEventsCommand) {
        return listUpcomingPublicEventsCommand;
    }

    @Provides
    @IntoMap
    @StringKey(RSVPButtonHandler.RSVP_NAME)
    public ButtonHandler provideRSVPButtonHandler(RSVPButtonHandler rsvpHandler) {
        return rsvpHandler;
    }

    @Provides
    @IntoMap
    @StringKey(RSVPButtonHandler.UNRSVP_NAME)
    public ButtonHandler provideUNRSVPButtonHandler(RSVPButtonHandler rsvpHandler) {
        return rsvpHandler;
    }

    @Provides
    @IntoMap
    @StringKey(RSVPButtonHandler.CONFIRM_UNRSVP_NAME)
    public ButtonHandler provideConfirmUNRSVPButtonHandler(RSVPButtonHandler rsvpHandler) {
        return rsvpHandler;
    }

    @Provides
    @IntoMap
    @StringKey(ListMyEventsCommand.NAME)
    public SlashCommandHandler provideListMyEventsCommand(ListMyEventsCommand listMyEventsCommand) {
        return listMyEventsCommand;
    }

    @Provides
    @IntoMap
    @StringKey(MyEventButtonHandler.DELETE_BUTTON_NAME)
    public ButtonHandler provideMyEventsDeleteButtonCommand(
            MyEventButtonHandler eventButtonHandler) {
        return eventButtonHandler;
    }

    @Provides
    @IntoMap
    @StringKey(MyEventButtonHandler.CONFIRM_DELETE_YES)
    public ButtonHandler provideMyEventsYesButtonCommand(MyEventButtonHandler eventButtonHandler) {
        return eventButtonHandler;
    }

    @Provides
    @IntoMap
    @StringKey(MyEventButtonHandler.CONFIRM_DELETE_NO)
    public ButtonHandler provideMyEventsNoButtonCommand(MyEventButtonHandler eventButtonHandler) {
        return eventButtonHandler;
    }

    @Provides
    @IntoMap
    @StringKey(VerifyCommand.NAME)
    public SlashCommandHandler provideVerifyCommand(VerifyCommand verifyCommand) {
        return verifyCommand;
    }

    @Provides
    @IntoMap
    @StringKey(EditEventButtonHandler.EDIT_EVENT_NAME)
    public ButtonHandler provideEditEventButtonHandler(
            EditEventButtonHandler editEventButtonHandler) {
        return editEventButtonHandler;
    }

    @Provides
    @IntoMap
    @StringKey(ListMyEventsCommand.SEND_INVITE)
    public ButtonHandler provideMyEventsSelectAttendeeCommand(
            MyEventButtonHandler eventButtonHandler) {
        return eventButtonHandler;
    }

    @Provides
    @IntoMap
    @StringKey(MyEventButtonHandler.INVITE_DROPDOWN)
    public StringSelectHandler provideInviteDropdownMenuHandler(
            MyEventButtonHandler myEventButtonHandler) {
        return myEventButtonHandler;
    }

    @Provides
    @IntoMap
    @StringKey(UpcomingPrivateEventsCommand.NAME)
    public SlashCommandHandler provideUpcomingPrivateEventsCommand(
            UpcomingPrivateEventsCommand upcomingPrivateEventsCommand) {
        return upcomingPrivateEventsCommand;
    }
}
