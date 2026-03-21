package backend.academy.linktracker.bot;

import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.client.TelegramSender;
import backend.academy.linktracker.bot.command.impl.ListCommand;
import backend.academy.linktracker.bot.properties.BotMessages;
import backend.academy.linktracker.bot.service.UserStateService;
import backend.academy.linktracker.scrapper.dto.LinkResponse;
import backend.academy.linktracker.scrapper.dto.ListLinksResponse;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListCommandTest {

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

    private ListCommand command;
    private BotMessages.List listMessages;

    @BeforeEach
    void setUp() {
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(123L);
        command = new ListCommand(sender, messages, scrapperClient, userStateService);

        listMessages = mock(BotMessages.List.class);
        when(messages.getListMsg()).thenReturn(listMessages);
    }

    @Test
    void execute_noSubscriptions_shouldSendEmptyMessage() {
        when(message.text()).thenReturn("/list");
        when(listMessages.getEmpty()).thenReturn("mocked_empty_list");
        when(scrapperClient.getLinks(123L)).thenReturn(new ListLinksResponse().links(List.of()));

        command.execute(message);

        verify(sender).sendMessage(123L, "mocked_empty_list");
    }

    @Test
    void execute_hasSubscriptions_shouldSendList() {
        when(message.text()).thenReturn("/list");
        when(listMessages.getTitle()).thenReturn("Links:");

        List<LinkResponse> links =
                List.of(new LinkResponse().url(URI.create("https://github.com")).tags(List.of("java")));
        when(scrapperClient.getLinks(123L)).thenReturn(new ListLinksResponse().links(links));

        command.execute(message);

        verify(sender).sendMessage(eq(123L), contains("https://github.com"));
    }

    @Test
    void execute_hasSubscriptionsWithFilter_shouldSendFilteredList() {
        when(message.text()).thenReturn("/list java");
        when(listMessages.getTitle()).thenReturn("Links:");
        when(listMessages.getEmpty()).thenReturn("mocked_empty_list");

        List<LinkResponse> links = List.of(
                new LinkResponse().url(URI.create("https://github.com")).tags(List.of("python")),
                new LinkResponse().url(URI.create("https://stackoverflow.com")).tags(List.of("java")));
        when(scrapperClient.getLinks(123L)).thenReturn(new ListLinksResponse().links(links));

        command.execute(message);

        verify(sender).sendMessage(eq(123L), contains("https://stackoverflow.com"));
        verify(sender, never()).sendMessage(eq(123L), contains("https://github.com"));
    }
}
