package backend.academy.linktracker.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.ai.properties.AiAgentProperties;
import backend.academy.linktracker.ai.service.AiAgentUpdateProcessor;
import backend.academy.linktracker.ai.service.FilterService;
import backend.academy.linktracker.ai.service.PrioritizationService;
import backend.academy.linktracker.ai.service.TextSummarizer;
import backend.academy.linktracker.avro.LinkUpdateAvro;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AiAgentUpdateProcessorTest {

    private FilterService filterService;
    private TextSummarizer textSummarizer;
    private PrioritizationService prioritizationService;
    private AiAgentProperties properties;
    private AiAgentUpdateProcessor processor;

    @BeforeEach
    void setUp() {
        filterService = Mockito.mock(FilterService.class);
        textSummarizer = Mockito.mock(TextSummarizer.class);
        prioritizationService = Mockito.mock(PrioritizationService.class);

        properties = new AiAgentProperties();
        properties.getSummarization().setThreshold(10);

        processor = new AiAgentUpdateProcessor(filterService, textSummarizer, properties, prioritizationService);
    }

    @Test
    void shouldReturnEmptyWhenFilterFails() {
        LinkUpdateAvro rawUpdate = update("test");
        when(filterService.isPass(rawUpdate)).thenReturn(false);

        Optional<LinkUpdateAvro> result = processor.process(rawUpdate);

        assertTrue(result.isEmpty());
        verifyNoInteractions(textSummarizer);
        verifyNoInteractions(prioritizationService);
    }

    @Test
    void shouldProcessAndSummarizeWhenFilterPasses() {
        LinkUpdateAvro rawUpdate = update("A very long text that needs to be shortened");
        String summarized = "A very lon...";

        when(filterService.isPass(rawUpdate)).thenReturn(true);
        when(textSummarizer.summarize(rawUpdate.getDescription(), 10)).thenReturn(summarized);
        when(prioritizationService.determinePriority(summarized)).thenReturn("HIGH");

        LinkUpdateAvro processed = processor.process(rawUpdate).orElseThrow();

        assertEquals(1L, processed.getId());
        assertEquals(summarized, processed.getDescription());
        assertEquals("HIGH", processed.getPriority());
    }

    @Test
    void shouldNotSummarizeShortText() {
        LinkUpdateAvro rawUpdate = update("Short text");
        when(filterService.isPass(rawUpdate)).thenReturn(true);
        when(textSummarizer.summarize("Short text", 10)).thenReturn("Short text");
        when(prioritizationService.determinePriority("Short text")).thenReturn("MEDIUM");

        LinkUpdateAvro processed = processor.process(rawUpdate).orElseThrow();

        assertEquals("Short text", processed.getDescription());
        assertEquals("MEDIUM", processed.getPriority());
    }

    private LinkUpdateAvro update(String description) {
        return LinkUpdateAvro.newBuilder()
                .setId(1L)
                .setUrl("https://example.com")
                .setDescription(description)
                .setAuthor("alice")
                .setTgChatIds(List.of(1L))
                .setPriority("LOW")
                .build();
    }
}
