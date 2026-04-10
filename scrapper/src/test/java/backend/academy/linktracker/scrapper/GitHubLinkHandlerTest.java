package backend.academy.linktracker.scrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.dto.GitHubIssueResponse;
import backend.academy.linktracker.scrapper.handler.impl.GitHubLinkHandler;
import backend.academy.linktracker.scrapper.model.LinkData;
import backend.academy.linktracker.scrapper.service.UpdateMessageFormatter;
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
    private UpdateMessageFormatter messageFormatter;

    @InjectMocks
    private GitHubLinkHandler gitHubLinkHandler;

    @Captor
    private ArgumentCaptor<LinkUpdate> updateCaptor;

    @Test
    @DisplayName("Должен корректно определить тип Pull Request и обновить lastUpdate по времени изменения")
    void handle_shouldDetectPullRequestAndSendUpdate() {
        List<Long> chatIds = List.of(12345L);
        URI url = URI.create("https://github.com/spring-projects/spring-boot");

        OffsetDateTime lastUpdateInDb = OffsetDateTime.now(ZoneOffset.UTC).minusHours(1);
        LinkData linkData = new LinkData(1L, url, lastUpdateInDb, List.of(), List.of());

        OffsetDateTime createdAt = lastUpdateInDb.minusMinutes(10);
        OffsetDateTime updatedAt = lastUpdateInDb.plusMinutes(30);

        GitHubIssueResponse mockPr = new GitHubIssueResponse(
                999L,
                "New Feature PR",
                "https://github.com/spring-projects/spring-boot/pull/999",
                createdAt,
                updatedAt,
                "PR Body",
                new GitHubIssueResponse.User("Author"));

        when(gitHubClient.fetchIssuesSince(eq("spring-projects"), eq("spring-boot"), eq(lastUpdateInDb)))
                .thenReturn(List.of(mockPr));

        String expectedDescription = "Formatted PR Message";
        when(messageFormatter.formatGitHubUpdate(eq(mockPr), eq("Pull Request")))
                .thenReturn(expectedDescription);

        gitHubLinkHandler.handle(chatIds, linkData);

        verify(messageSender).send(updateCaptor.capture());
        LinkUpdate sentUpdate = updateCaptor.getValue();

        assertThat(sentUpdate.getDescription()).isEqualTo(expectedDescription);

        assertThat(linkData.getLastUpdate()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("Не должен отправлять уведомление, если updatedAt не больше, чем в базе (защита от дублей)")
    void handle_shouldNotSendUpdateWhenTimesAreEqual() {
        URI url = URI.create("https://github.com/user/repo");
        OffsetDateTime lastUpdate = OffsetDateTime.now(ZoneOffset.UTC);
        LinkData linkData = new LinkData(1L, url, lastUpdate, List.of(), List.of());

        GitHubIssueResponse mockIssue = new GitHubIssueResponse(
                20L, "Title", "url", lastUpdate, lastUpdate, "body", new GitHubIssueResponse.User("u"));

        when(gitHubClient.fetchIssuesSince(any(), any(), any())).thenReturn(List.of(mockIssue));

        gitHubLinkHandler.handle(List.of(1L), linkData);

        verify(messageSender, never()).send(any());
    }

    @Test
    @DisplayName("Должен правильно выбирать максимальный updatedAt из списка")
    void handle_shouldTrackMaxUpdatedAt() {
        OffsetDateTime baseTime = OffsetDateTime.now(ZoneOffset.UTC);
        LinkData linkData = new LinkData(1L, URI.create("https://github.com/u/r"), baseTime, List.of(), List.of());

        OffsetDateTime time1 = baseTime.plusMinutes(10);
        OffsetDateTime time2 = baseTime.plusMinutes(5);

        GitHubIssueResponse res1 =
                new GitHubIssueResponse(1L, "T1", "url1", time1, time1, "b", new GitHubIssueResponse.User("u"));
        GitHubIssueResponse res2 =
                new GitHubIssueResponse(2L, "T2", "url2", time2, time2, "b", new GitHubIssueResponse.User("u"));

        when(gitHubClient.fetchIssuesSince(any(), any(), any())).thenReturn(List.of(res1, res2));
        when(messageFormatter.formatGitHubUpdate(any(), any())).thenReturn("desc");

        gitHubLinkHandler.handle(List.of(1L), linkData);

        assertThat(linkData.getLastUpdate()).isEqualTo(time1);
        verify(messageSender, times(2)).send(any());
    }
}
