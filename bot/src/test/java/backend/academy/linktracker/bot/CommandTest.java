package backend.academy.linktracker.bot;

import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.client.TelegramSender;
import backend.academy.linktracker.bot.command.impl.*;
import backend.academy.linktracker.bot.constants.UserState;
import backend.academy.linktracker.bot.properties.BotMessages;
import backend.academy.linktracker.bot.service.UserStateService;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommandTest {

    @Mock
    TelegramSender sender;

    @Mock
    BotMessages messages;

    @Mock
    Update update;

    @Mock
    Message message;

    @Mock
    Chat chat;

    @Mock
    ScrapperClient scrapperClient;

    @Mock
    UserStateService userStateService;

    private final long chatId = 123456L;

    @BeforeEach
    void setUp() {
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
    }

    @Test
    void startCommand_positive() {
        String expectedText = "mocked_start_message";
        when(messages.getStart()).thenReturn(expectedText);

        StartCommand command = new StartCommand(sender, messages, scrapperClient, userStateService);

        command.execute(update.message());

        verify(userStateService).setState(chatId, UserState.IDLE);
        verify(userStateService).clearTempData(chatId);

        verify(sender).sendMessage(eq(chatId), eq(expectedText));
        verify(scrapperClient).registerChat(eq(chatId));
    }

    @Test
    void helpCommand_positive() {
        String expectedText = "mocked_help_message";
        when(messages.getHelp()).thenReturn(expectedText);
        HelpCommand command = new HelpCommand(sender, messages);

        command.execute(update.message());

        verify(sender).sendMessage(eq(chatId), eq(expectedText));
    }

    @Test
    void wrongCommand_negative() {
        String expectedText = "mocked_wrong_message";
        when(messages.getWrong()).thenReturn(expectedText);
        WrongCommand command = new WrongCommand(sender, messages);

        command.execute(update.message());

        verify(sender).sendMessage(eq(chatId), eq(expectedText));
    }

    @Test
    void trackCommand_positive_idle() {
        when(userStateService.getState(chatId)).thenReturn(UserState.IDLE);
        BotMessages.Track trackMessages = mock(BotMessages.Track.class);
        when(messages.getTrack()).thenReturn(trackMessages);
        when(trackMessages.getLinkRequest()).thenReturn("mocked_link_request");

        TrackCommand command = new TrackCommand(sender, messages, userStateService);
        command.execute(update.message());

        verify(userStateService).setState(chatId, UserState.AWAITING_LINK);
        verify(sender).sendMessage(eq(chatId), eq("mocked_link_request"));
    }

    @Test
    void cancelCommand_positive() {
        when(messages.getCancel()).thenReturn("mocked_cancel");

        CancelCommand command = new CancelCommand(sender, messages, userStateService);
        command.execute(update.message());

        verify(userStateService).setState(chatId, UserState.IDLE);
        verify(userStateService).clearTempData(chatId);
        verify(sender).sendMessage(eq(chatId), eq("mocked_cancel"));
    }

    @Test
    void untrackCommand_invalidFormat() {
        when(message.text()).thenReturn("/untrack");

        BotMessages.Untrack untrackMessages = mock(BotMessages.Untrack.class);
        when(messages.getUntrack()).thenReturn(untrackMessages);
        when(untrackMessages.getUsage()).thenReturn("mocked_usage");

        UntrackCommand command = new UntrackCommand(sender, messages, scrapperClient, userStateService);
        command.execute(update.message());

        verify(userStateService).setState(chatId, UserState.IDLE);
        verify(userStateService).clearTempData(chatId);
        verify(sender).sendMessage(eq(chatId), eq("mocked_usage"));
    }
}
