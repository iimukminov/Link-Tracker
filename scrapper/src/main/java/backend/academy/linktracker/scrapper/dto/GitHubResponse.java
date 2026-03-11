package backend.academy.linktracker.scrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record GitHubResponse(
        @JsonProperty("pushed_at") OffsetDateTime updatedAt,
        @JsonProperty("full_name") String fullName) {}
