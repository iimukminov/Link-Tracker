package backend.academy.linktracker.ai;

import static org.mockito.Mockito.*;

import backend.academy.linktracker.ai.service.GroupingService;
import backend.academy.linktracker.ai.service.KafkaProcessedUpdateSender;
import backend.academy.linktracker.ai.service.UpdateAggregator;
import backend.academy.linktracker.avro.LinkUpdateAvro;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GroupingServiceTest {

    private KafkaProcessedUpdateSender kafkaSender;
    private UpdateAggregator updateAggregator;
    private GroupingService groupingService;

    @BeforeEach
    void setUp() {
        kafkaSender = mock(KafkaProcessedUpdateSender.class);
        updateAggregator = mock(UpdateAggregator.class);

        groupingService = new GroupingService(kafkaSender, updateAggregator);
    }

    @Test
    void shouldNotGroupSingleUpdate() {
        LinkUpdateAvro update = createUpdate(1L, "Single update", "LOW");

        groupingService.addUpdate(update);
        groupingService.flushGroupedUpdates();

        verifyNoInteractions(updateAggregator);

        verify(kafkaSender, times(1)).send(any(LinkUpdateAvro.class));
    }

    @Test
    void shouldDelegateToAggregatorWhenMultipleUpdates() {
        LinkUpdateAvro u1 = createUpdate(1L, "U1", "LOW");
        LinkUpdateAvro u2 = createUpdate(2L, "U2", "HIGH");

        LinkUpdateAvro aggregatedMockResult = createUpdate(1L, "1. U1\n2. U2", "HIGH");

        when(updateAggregator.aggregate(eq(100L), anyList())).thenReturn(aggregatedMockResult);

        groupingService.addUpdate(u1);
        groupingService.addUpdate(u2);
        groupingService.flushGroupedUpdates();

        verify(updateAggregator, times(1)).aggregate(eq(100L), anyList());

        verify(kafkaSender, times(1)).send(aggregatedMockResult);
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
