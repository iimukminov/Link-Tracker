package backend.academy.linktracker.scrapper.handler.impl;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.dto.GitHubIssueResponse;
import backend.academy.linktracker.scrapper.handler.LinkHandler;
import backend.academy.linktracker.scrapper.model.LinkData;
import java.time.OffsetDateTime;
import java.util.List;
import backend.academy.linktracker.scrapper.service.sender.MessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubLinkHandler implements LinkHandler {

    private final GitHubClient gitHubClient;
    private final MessageSender messageSender;

    @Override
    public boolean supports(String host) {
        return host != null && host.endsWith("github.com");
    }

    @Override
    public void handle(List<Long> chatIds, LinkData linkData) {
        String path = linkData.getUrl().getPath();
        if (path == null) return;
        String[] parts = path.split("/");

        if (parts.length >= 3) {
            String owner = parts[1];
            String repo = parts[2];

            List<GitHubIssueResponse> newIssues = gitHubClient.fetchIssuesSince(owner, repo, linkData.getLastUpdate());

            if (newIssues != null && !newIssues.isEmpty()) {
                OffsetDateTime maxUpdate = linkData.getLastUpdate();

                for (GitHubIssueResponse issue : newIssues) {
                    log.atInfo()
                        .addKeyValue("repo", repo)
                        .addKeyValue("issue_id", issue.id())
                        .log("New Issue/PR found in Github");

                    String preview = truncateBody(issue.body());

                    String description = String.format(
                        "Новый Issue/Pull Request: %s\nАвтор: %s\nСоздано: %s\n\n%s",
                        issue.title(),
                        issue.user() != null ? issue.user().login() : "Unknown",
                        issue.createdAt(),
                        preview
                    );

                    messageSender.send(new LinkUpdate()
                        .id(linkData.getId())
                        .url(linkData.getUrl())
                        .description(description)
                        .tgChatIds(chatIds));

                    if (issue.createdAt() != null && issue.createdAt().isAfter(maxUpdate)) {
                        maxUpdate = issue.createdAt();
                    }
                }

                linkData.setLastUpdate(maxUpdate);
            }
        }
    }

    private String truncateBody(String body) {
        if (body == null || body.isBlank()) return "*Нет описания*";
        if (body.length() <= 200) return body;
        return body.substring(0, 197) + "...";
    }
}
