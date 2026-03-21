package backend.academy.linktracker.bot;

import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.client.TelegramSender;
import backend.academy.linktracker.bot.constants.UserState;
import backend.academy.linktracker.bot.handlers.impl.AwaitingTagsHandler;
import backend.academy.linktracker.bot.properties.BotMessages;
import backend.academy.linktracker.bot.service.UserStateService;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

@ExtendWith(MockitoExtension.class)
class AwaitingTagsHandlerTest {

    @Mock
    TelegramSender sender;

    @Mock
    BotMessages messages;

    @Mock
    ScrapperClient scrapperClient;

    @Mock
    UserStateService userStateService;

    @Mock
    Message message;

    @Mock
    Chat chat;

    private AwaitingTagsHandler handler;
    private BotMessages.Track trackMessages;

    @BeforeEach
    void setUp() {
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(123L);
        handler = new AwaitingTagsHandler(userStateService, sender, messages, scrapperClient);

        trackMessages = mock(BotMessages.Track.class);
        when(messages.getTrack()).thenReturn(trackMessages);
        when(userStateService.getTempData(123L, "link")).thenReturn("https://github.com/test");
    }

    @Test
    void handle_validLinkAndTags_shouldSaveCorrectly() {
        String savedLink = "https://github.com/user/repo";
        when(userStateService.getTempData(123L, "link")).thenReturn(savedLink);

        when(message.text()).thenReturn("java, spring");
        when(trackMessages.getSuccess()).thenReturn("Ссылка успешно добавлена!");

        handler.handle(message);

        verify(scrapperClient)
                .addLink(
                        eq(123L),
                        argThat(req -> req.getLink().toString().equals(savedLink)
                                && req.getTags().contains("java")
                                && req.getTags().contains("spring")));

        verify(sender).sendMessage(123L, "Ссылка успешно добавлена!");
        verify(userStateService).setState(123L, UserState.IDLE);
        verify(userStateService).clearTempData(123L);
    }

    @Test
    void handle_alreadyTrackedLink_shouldNotifyUser() {
        when(message.text()).thenReturn("");
        when(trackMessages.getAlreadyTracked()).thenReturn("mocked_already_tracked");

        doThrow(HttpClientErrorException.Conflict.create(HttpStatus.CONFLICT, "Conflict", null, null, null))
                .when(scrapperClient)
                .addLink(anyLong(), any());

        handler.handle(message);

        verify(sender).sendMessage(123L, "mocked_already_tracked");
        verify(userStateService).setState(123L, UserState.IDLE);
    }
}
