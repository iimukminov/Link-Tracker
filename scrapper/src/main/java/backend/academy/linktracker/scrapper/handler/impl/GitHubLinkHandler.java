package backend.academy.linktracker.scrapper.handler.impl;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.dto.GitHubIssueResponse;
import backend.academy.linktracker.scrapper.handler.AbstractLinkHandler;
import backend.academy.linktracker.scrapper.model.LinkData;
import backend.academy.linktracker.scrapper.service.LinkUpdateDbService;
import backend.academy.linktracker.scrapper.service.UpdateMessageFormatter;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GitHubLinkHandler extends AbstractLinkHandler {

    private final GitHubClient gitHubClient;
    private final UpdateMessageFormatter messageFormatter;

    public GitHubLinkHandler(
            GitHubClient gitHubClient,
            UpdateMessageFormatter messageFormatter,
            LinkUpdateDbService linkUpdateDbService) {
        super(linkUpdateDbService);
        this.gitHubClient = gitHubClient;
        this.messageFormatter = messageFormatter;
    }

    @Override
    public boolean supports(String host) {
        return host != null && host.endsWith("github.com");
    }

    @Override
    protected List<LinkUpdate> fetchUpdates(List<Long> chatIds, LinkData linkData) {
        String[] pathParts = parsePath(linkData.getUrl().getPath());
        if (pathParts == null) return List.of();

        String owner = pathParts[0];
        String repo = pathParts[1];

        List<GitHubIssueResponse> newIssues = gitHubClient.fetchIssuesSince(owner, repo, linkData.getLastUpdate());
        if (newIssues.isEmpty()) return List.of();

        List<LinkUpdate> updates = new ArrayList<>();
        OffsetDateTime lastUpdate = linkData.getLastUpdate();
        OffsetDateTime maxUpdate = lastUpdate;

        for (GitHubIssueResponse issue : newIssues) {
            if (issue.updatedAt() != null && issue.updatedAt().isAfter(lastUpdate)) {
                String type =
                        (issue.htmlUrl() != null && issue.htmlUrl().contains("/pull/")) ? "Pull Request" : "Issue";

                String description = messageFormatter.formatGitHubUpdate(issue, type);

                String author = issue.user() != null ? issue.user().login() : null;

                updates.add(new LinkUpdate()
                        .id(linkData.getId())
                        .url(linkData.getUrl())
                        .description(description)
                        .author(author)
                        .tgChatIds(chatIds));

                if (issue.updatedAt().isAfter(maxUpdate)) {
                    maxUpdate = issue.updatedAt();
                }
            }
        }
        linkData.setLastUpdate(maxUpdate);

        return updates;
    }

    private String[] parsePath(String path) {
        if (path == null) return null;
        String[] parts = path.split("/");
        if (parts.length < 3) return null;
        return new String[] {parts[1], parts[2]};
    }
}
