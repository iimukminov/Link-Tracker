package backend.academy.linktracker.ai;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.linktracker.ai.metrics.AiAgentMetrics;
import backend.academy.linktracker.ai.properties.AiAgentProperties;
import backend.academy.linktracker.ai.service.FilterService;
import backend.academy.linktracker.avro.LinkUpdateAvro;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class FilterServiceTest {

    private AiAgentProperties properties;
    private FilterService filterService;
    private AiAgentMetrics metrics;

    @BeforeEach
    void setUp() {
        properties = new AiAgentProperties();
        properties.getFiltering().setStopWords(List.of("spam", "promo"));
        properties.getFiltering().setExcludedAuthors(List.of("bot-user"));
        properties.getFiltering().setMinLength(20);

        metrics = Mockito.mock(AiAgentMetrics.class);
        filterService = new FilterService(properties, metrics);
    }

    @Test
    void shouldFilterByStopWord() {
        LinkUpdateAvro update = update("This update contains spam content", "alice");
        assertFalse(filterService.isPass(update));
    }

    @Test
    void shouldFilterByExcludedAuthor() {
        LinkUpdateAvro update = update("This update has enough useful content", "bot-user");
        assertFalse(filterService.isPass(update));
    }

    @Test
    void shouldFilterByMinimumLength() {
        LinkUpdateAvro update = update("Too short", "alice");
        assertFalse(filterService.isPass(update));
    }

    @Test
    void shouldPassValidUpdate() {
        LinkUpdateAvro update = update("This update has enough useful content and is long enough", "alice");
        assertTrue(filterService.isPass(update));
    }

    @Test
    void shouldFilterByStopWordCaseInsensitive() {
        LinkUpdateAvro update = update("This update discusses PROMO in enough detail", "alice");
        assertFalse(filterService.isPass(update));
    }

    private LinkUpdateAvro update(String description, String author) {
        return LinkUpdateAvro.newBuilder()
                .setId(1L)
                .setUrl("https://example.com")
                .setDescription(description)
                .setAuthor(author)
                .setTgChatIds(List.of(1L))
                .build();
    }
}
