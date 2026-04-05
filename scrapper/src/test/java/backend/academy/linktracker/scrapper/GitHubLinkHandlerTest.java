package backend.academy.linktracker.scrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.dto.GitHubIssueResponse;
import backend.academy.linktracker.scrapper.handler.impl.GitHubLinkHandler;
import backend.academy.linktracker.scrapper.model.LinkData;
import backend.academy.linktracker.scrapper.properties.ScrapperMessages;
import backend.academy.linktracker.scrapper.service.sender.MessageSender;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GitHubLinkHandlerTest {

    @Mock
    private GitHubClient gitHubClient;

    @Mock
    private MessageSender messageSender;

    @Mock
    private ScrapperMessages scrapperMessages;

    @Mock
    private ScrapperMessages.Updates updatesConfig;

    @InjectMocks
    private GitHubLinkHandler gitHubLinkHandler;

    @Captor
    private ArgumentCaptor<LinkUpdate> updateCaptor;

    @Test
    @DisplayName("Должен формировать и отправлять сообщение с названием, автором и превью")
    void handle_ShouldFormatAndSendMessage_WhenNewIssuesFound() {
        List<Long> chatIds = List.of(12345L);
        URI url = URI.create("https://github.com/spring-projects/spring-boot");
        OffsetDateTime lastUpdate = OffsetDateTime.of(2023, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        LinkData linkData = new LinkData(1L, url, lastUpdate, List.of(), List.of());

        GitHubIssueResponse mockIssue = new GitHubIssueResponse(
                999L,
                "Как исправить баг в транзакциях?",
                "https://github.com/spring-projects/spring-boot/issues/999",
                OffsetDateTime.now(),
                "Это очень длинный текст проблемы, который должен быть обрезан или показан как превью.",
                new GitHubIssueResponse.User("TestAuthor"));

        when(gitHubClient.fetchIssuesSince(eq("spring-projects"), eq("spring-boot"), eq(lastUpdate)))
                .thenReturn(List.of(mockIssue));

        when(scrapperMessages.getUpdates()).thenReturn(updatesConfig);
        when(updatesConfig.getGithubUpdate()).thenReturn("Новый Issue: %s\nАвтор: %s\nДата: %s\nПревью: %s");

        gitHubLinkHandler.handle(chatIds, linkData);

        verify(messageSender).send(updateCaptor.capture());
        LinkUpdate sentUpdate = updateCaptor.getValue();

        assertThat(sentUpdate.getId()).isEqualTo(1L);
        assertThat(sentUpdate.getUrl()).isEqualTo(url);
        assertThat(sentUpdate.getTgChatIds()).containsExactly(12345L);

        String description = sentUpdate.getDescription();
        assertThat(description)
                .contains("Новый Issue: Как исправить баг в транзакциях?")
                .contains("Автор: TestAuthor")
                .contains("Превью: Это очень длинный текст проблемы");
    }
}
