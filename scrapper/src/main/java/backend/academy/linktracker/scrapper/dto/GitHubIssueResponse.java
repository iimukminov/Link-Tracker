package backend.academy.linktracker.scrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record GitHubIssueResponse(
        Long id,
        String title,
        @JsonProperty("html_url") String htmlUrl,

        @JsonProperty("created_at")
        OffsetDateTime
                createdAt, // сейчас не используется, но потом может пригодиться для определения новое issue или это
        // апдейт (если не станет лень)

        @JsonProperty("updated_at") OffsetDateTime updatedAt,
        String body,
        User user) {
    public record User(String login) {}
}
