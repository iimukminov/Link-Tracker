package backend.academy.linktracker.scrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record StackOverflowResponse(List<Answer> items) {

    public record Answer(
        @JsonProperty("answer_id") Long answerId,
        @JsonProperty("creation_date") Long creationDate, // здесь не OffsetDateTime, т.к. там Unix (считает секунды с 1970 года)
        Owner owner,
        String body
    ) {}

    public record Owner(
        @JsonProperty("display_name") String displayName
    ) {}
}
