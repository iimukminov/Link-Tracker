package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.dto.GitHubIssueResponse;
import backend.academy.linktracker.scrapper.dto.StackOverflowResponse;
import backend.academy.linktracker.scrapper.properties.ScrapperMessages;
import backend.academy.linktracker.scrapper.util.LinkUtils;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateMessageFormatter {

    private final ScrapperMessages scrapperMessages;

    public String formatGitHubUpdate(GitHubIssueResponse issue, String type) {
        return String.format(
            scrapperMessages.getUpdates().getGithubUpdate(),
            type,
            issue.title(),
            issue.user() != null ? issue.user().login() : "Unknown",
            issue.createdAt(),
            LinkUtils.truncateBody(issue.body(), scrapperMessages.getUpdates().getGithubNoDescription())
        );
    }

    public String formatStackOverflowUpdate(StackOverflowResponse.Item item, String questionTitle, String type, OffsetDateTime date) {
        String author = item.owner() != null ? item.owner().displayName() : "Unknown";
        return String.format(
            scrapperMessages.getUpdates().getStackoverflowUpdate(),
            type,
            questionTitle,
            author,
            date,
            LinkUtils.truncateBody(item.body(), scrapperMessages.getUpdates().getStackoverflowNoDescription())
        );
    }
}
