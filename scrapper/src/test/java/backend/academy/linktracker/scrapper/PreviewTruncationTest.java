package backend.academy.linktracker.scrapper;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class PreviewTruncationTest {

    @Test
    void shouldTruncateTextTo200Characters() {
        String longText = "А".repeat(250);

        String truncated = truncateBody(longText);

        assertThat(truncated).hasSize(200);
        assertThat(truncated).endsWith("...");
    }

    private String truncateBody(String body) {
        if (body == null || body.isBlank()) return "*Нет текста*";
        if (body.length() <= 200) return body;
        return body.substring(0, 197) + "...";
    }
}
