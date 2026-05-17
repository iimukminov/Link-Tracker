package backend.academy.linktracker.scrapper.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.dto.GitHubIssueResponse;
import backend.academy.linktracker.scrapper.handler.impl.GitHubLinkHandler;
import backend.academy.linktracker.scrapper.model.LinkData;
import backend.academy.linktracker.scrapper.service.LinkUpdateDbService;
import backend.academy.linktracker.scrapper.service.UpdateMessageFormatter;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GitHubLinkHandlerTest {

    @Mock
    private GitHubClient gitHubClient;

    @Mock
    private UpdateMessageFormatter messageFormatter;

    @Mock
    private LinkUpdateDbService dbService;

    @InjectMocks
    private GitHubLinkHandler gitHubLinkHandler;

    @Test
    @DisplayName("Должен корректно определить тип Pull Request и обновить lastUpdate")
    void handle_shouldDetectPullRequestAndSendUpdate() {
        List<Long> chatIds = List.of(12345L);
        URI url = URI.create("https://github.com/spring-projects/spring-boot");
        OffsetDateTime lastUpdateInDb = OffsetDateTime.now(ZoneOffset.UTC).minusHours(1);
        LinkData linkData = new LinkData(1L, url, lastUpdateInDb, List.of(), List.of());

        OffsetDateTime updatedAt = lastUpdateInDb.plusMinutes(30);
        GitHubIssueResponse mockPr = new GitHubIssueResponse(
                999L,
                "New PR",
                "https://github.com/../pull/999",
                updatedAt.minusMinutes(10),
                updatedAt,
                "Body",
                new GitHubIssueResponse.User("Author"));

        when(gitHubClient.fetchIssuesSince(any(), any(), any())).thenReturn(List.of(mockPr));
        when(messageFormatter.formatGitHubUpdate(any(), any())).thenReturn("Formatted Message");

        gitHubLinkHandler.handle(chatIds, linkData);

        verify(dbService).saveUpdates(anyList(), eq(linkData));
        assertThat(linkData.getLastUpdate()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("Не должен вызывать сохранение, если обновлений нет")
    void handle_shouldNotSendUpdateWhenTimesAreEqual() {
        URI url = URI.create("https://github.com/user/repo");
        OffsetDateTime lastUpdate = OffsetDateTime.now(ZoneOffset.UTC);
        LinkData linkData = new LinkData(1L, url, lastUpdate, List.of(), List.of());

        GitHubIssueResponse mockIssue = new GitHubIssueResponse(
                20L, "Title", "url", lastUpdate, lastUpdate, "body", new GitHubIssueResponse.User("u"));

        when(gitHubClient.fetchIssuesSince(any(), any(), any())).thenReturn(List.of(mockIssue));

        gitHubLinkHandler.handle(List.of(1L), linkData);

        verify(dbService, never()).saveUpdates(anyList(), any());
    }

    @Test
    @DisplayName("Должен правильно выбирать максимальный updatedAt из списка")
    void handle_shouldTrackMaxUpdatedAt() {
        OffsetDateTime baseTime = OffsetDateTime.now(ZoneOffset.UTC);
        LinkData linkData = new LinkData(1L, URI.create("https://github.com/u/r"), baseTime, List.of(), List.of());
        OffsetDateTime time1 = baseTime.plusMinutes(10);

        GitHubIssueResponse res1 =
                new GitHubIssueResponse(1L, "T1", "u1", time1, time1, "b", new GitHubIssueResponse.User("u"));
        when(gitHubClient.fetchIssuesSince(any(), any(), any())).thenReturn(List.of(res1));
        when(messageFormatter.formatGitHubUpdate(any(), any())).thenReturn("desc");

        gitHubLinkHandler.handle(List.of(1L), linkData);

        assertThat(linkData.getLastUpdate()).isEqualTo(time1);
        verify(dbService).saveUpdates(anyList(), eq(linkData));
    }
}
