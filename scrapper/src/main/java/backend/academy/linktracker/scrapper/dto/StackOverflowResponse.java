package backend.academy.linktracker.scrapper.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record StackOverflowResponse(List<Item> items) {

    public record Item(
        @JsonAlias({"answer_id", "comment_id"}) Long id,
        @JsonProperty("creation_date") Long creationDate,
        Owner owner,
        String body,
        String title
    ) {}

    public record Owner(
        @JsonProperty("display_name") String displayName
    ) {}
}
