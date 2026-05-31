package backend.academy.linktracker.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;

import backend.academy.linktracker.ai.service.UpdateAggregator;
import backend.academy.linktracker.avro.LinkUpdateAvro;
import java.util.List;
import org.junit.jupiter.api.Test;

class UpdateAggregatorTest {

    private final UpdateAggregator aggregator = new UpdateAggregator();

    @Test
    void shouldAggregateMultipleUpdatesAndFindMaxPriority() {
        LinkUpdateAvro u1 = createUpdate(1L, "First update", "LOW");
        LinkUpdateAvro u2 = createUpdate(2L, "Second update", "HIGH");
        LinkUpdateAvro u3 = createUpdate(3L, "Third update", "MEDIUM");

        LinkUpdateAvro result = aggregator.aggregate(100L, List.of(u1, u2, u3));

        String expectedDesc = "1. First update\n2. Second update\n3. Third update";
        assertEquals(expectedDesc, result.getDescription().toString());

        assertEquals("HIGH", result.getPriority().toString());

        assertEquals(List.of(100L), result.getTgChatIds());
        assertEquals(1L, result.getId());
    }

    private LinkUpdateAvro createUpdate(Long id, String desc, String priority) {
        return LinkUpdateAvro.newBuilder()
                .setId(id)
                .setUrl("https://example.com")
                .setDescription(desc)
                .setAuthor("user")
                .setTgChatIds(List.of(100L))
                .setPriority(priority)
                .build();
    }
}
