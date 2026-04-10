package backend.academy.linktracker.scrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    @DisplayName("Должен корректно определить тип Pull Request и отправить уведомление")
    void handle_shouldDetectPullRequestAndSendUpdate() {
        List<Long> chatIds = List.of(12345L);
        URI url = URI.create("https://github.com/spring-projects/spring-boot");
        OffsetDateTime lastUpdate = OffsetDateTime.now(ZoneOffset.UTC).minusDays(1);
        LinkData linkData = new LinkData(1L, url, lastUpdate, List.of(), List.of());

        GitHubIssueResponse mockPr = new GitHubIssueResponse(
                999L,
                "New Feature PR",
                "https://github.com/spring-projects/spring-boot/pull/999",
                OffsetDateTime.now(ZoneOffset.UTC),
                "PR Body",
                new GitHubIssueResponse.User("Author"));

        when(gitHubClient.fetchIssuesSince(eq("spring-projects"), eq("spring-boot"), eq(lastUpdate)))
                .thenReturn(List.of(mockPr));

        String expectedDescription = "Formatted PR Message";
        when(messageFormatter.formatGitHubUpdate(eq(mockPr), eq("Pull Request")))
                .thenReturn(expectedDescription);

        gitHubLinkHandler.handle(chatIds, linkData);

        verify(messageSender).send(updateCaptor.capture());
        LinkUpdate sentUpdate = updateCaptor.getValue();

        assertThat(sentUpdate.getDescription()).isEqualTo(expectedDescription);
        assertThat(linkData.getLastUpdate()).isEqualTo(mockPr.createdAt());
    }

    @Test
    @DisplayName("Должен корректно определить тип Issue")
    void handle_shouldDetectIssue() {
        URI url = URI.create("https://github.com/user/repo");
        OffsetDateTime lastUpdate = OffsetDateTime.now(ZoneOffset.UTC).minusDays(1);
        LinkData linkData = new LinkData(1L, url, lastUpdate, List.of(), List.of());

        GitHubIssueResponse mockIssue = new GitHubIssueResponse(
                20L,
                "Issue Title",
                "https://github.com/user/repo/issues/20",
                OffsetDateTime.now(ZoneOffset.UTC),
                "Issue Body",
                new GitHubIssueResponse.User("author"));

        when(gitHubClient.fetchIssuesSince(eq("user"), eq("repo"), eq(lastUpdate)))
                .thenReturn(List.of(mockIssue));
        when(messageFormatter.formatGitHubUpdate(eq(mockIssue), eq("Issue"))).thenReturn("Formatted Issue");

        gitHubLinkHandler.handle(List.of(1L), linkData);

        verify(messageFormatter).formatGitHubUpdate(any(), eq("Issue"));
        verify(messageSender).send(any());
    }
}
