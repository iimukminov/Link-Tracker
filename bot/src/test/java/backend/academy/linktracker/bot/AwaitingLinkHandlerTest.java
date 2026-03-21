package backend.academy.linktracker.bot;

import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.client.TelegramSender;
import backend.academy.linktracker.bot.handlers.impl.AwaitingLinkHandler;
import backend.academy.linktracker.bot.properties.BotMessages;
import backend.academy.linktracker.bot.service.UserStateService;
import backend.academy.linktracker.bot.service.validator.LinkValidator;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AwaitingLinkHandlerTest {

    @Mock
    TelegramSender sender;

    @Mock
    BotMessages messages;

    @Mock
    UserStateService userStateService;

    @Mock
    LinkValidator validator;

    @Mock
    Message message;

    @Mock
    Chat chat;

    private AwaitingLinkHandler handler;

    @BeforeEach
    void setUp() {
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(123L);
        handler = new AwaitingLinkHandler(userStateService, sender, messages, List.of(validator));
    }

    @Test
    void handle_validLink_shouldSaveToTempDataAndAskForTags() {
        String validLink = "https://github.com/user/repo";
        when(message.text()).thenReturn(validLink);

        when(validator.supports("github.com")).thenReturn(true);

        BotMessages.Track trackMessages = mock(BotMessages.Track.class);
        when(messages.getTrack()).thenReturn(trackMessages);
        when(trackMessages.getTagRequest()).thenReturn("Введите теги:");

        handler.handle(message);

        verify(userStateService).setTempData(123L, "link", validLink);
        verify(sender).sendMessage(123L, "Введите теги:");
        verify(userStateService).setState(123L, backend.academy.linktracker.bot.constants.UserState.AWAITING_TAGS);
    }

    @Test
    void handle_invalidLink_shouldSendErrorMessage() {
        when(message.text()).thenReturn("tbank://github.com/user/repo");
        BotMessages.Track trackMessages = mock(BotMessages.Track.class);
        when(messages.getTrack()).thenReturn(trackMessages);
        when(trackMessages.getInvalidLink()).thenReturn("mocked_invalid_link");

        handler.handle(message);

        verify(sender).sendMessage(123L, "mocked_invalid_link");
        verify(userStateService, never()).setState(anyLong(), any());
    }
}
