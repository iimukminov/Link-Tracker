package backend.academy.linktracker.scrapper.handler.impl;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.dto.GitHubIssueResponse;
import backend.academy.linktracker.scrapper.handler.LinkHandler;
import backend.academy.linktracker.scrapper.model.LinkData;
import backend.academy.linktracker.scrapper.properties.ScrapperMessages;
import backend.academy.linktracker.scrapper.service.UpdateMessageFormatter;
import backend.academy.linktracker.scrapper.service.sender.MessageSender;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubLinkHandler implements LinkHandler {

    private final GitHubClient gitHubClient;
    private final MessageSender messageSender;
    private final UpdateMessageFormatter messageFormatter;

    @Override
    public boolean supports(String host) {
        return host != null && host.endsWith("github.com");
    }

    @Override
    public void handle(List<Long> chatIds, LinkData linkData) {
        String[] pathParts = parsePath(linkData.getUrl().getPath());
        if (pathParts == null) return;

        String owner = pathParts[0];
        String repo = pathParts[1];

        List<GitHubIssueResponse> newIssues = gitHubClient.fetchIssuesSince(owner, repo, linkData.getLastUpdate());
        if (newIssues.isEmpty()) return;

        OffsetDateTime maxUpdate = linkData.getLastUpdate();
        for (GitHubIssueResponse issue : newIssues) {
            String type = (issue.htmlUrl() != null && issue.htmlUrl().contains("/pull/"))
                ? "Pull Request" : "Issue";

            String description = messageFormatter.formatGitHubUpdate(issue, type);

            sendUpdate(chatIds, linkData, description);

            if (issue.createdAt() != null && issue.createdAt().isAfter(maxUpdate)) {
                maxUpdate = issue.createdAt();
            }
        }
        linkData.setLastUpdate(maxUpdate);
    }

    private void sendUpdate(List<Long> chatIds, LinkData linkData, String description) {
        messageSender.send(new LinkUpdate()
            .id(linkData.getId())
            .url(linkData.getUrl())
            .description(description)
            .tgChatIds(chatIds));
    }

    private String[] parsePath(String path) {
        if (path == null) return null;
        String[] parts = path.split("/");
        if (parts.length < 3) return null;
        return new String[]{parts[1], parts[2]};
    }
}
