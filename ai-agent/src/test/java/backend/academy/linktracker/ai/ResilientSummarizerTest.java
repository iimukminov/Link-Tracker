package backend.academy.linktracker.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.ai.properties.AiAgentProperties;
import backend.academy.linktracker.ai.service.impl.LlmContentSummarizer;
import backend.academy.linktracker.ai.service.impl.ResilientSummarizer;
import backend.academy.linktracker.ai.service.impl.SubstringSummarizer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;

class ResilientSummarizerTest {

    @Test
    @SuppressWarnings("unchecked")
    void shouldFallbackToSubstringWhenLlmFails() {
        AiAgentProperties properties = new AiAgentProperties();
        properties.getSummarization().setProvider(AiAgentProperties.Provider.API);
        properties.getSummarization().setThreshold(10);

        LlmContentSummarizer llmSummarizer = Mockito.mock(LlmContentSummarizer.class);
        when(llmSummarizer.summarize("123456789012345", 10)).thenThrow(new IllegalStateException("API timeout"));

        ObjectProvider<LlmContentSummarizer> provider = Mockito.mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(llmSummarizer);

        ResilientSummarizer resilientSummarizer =
                new ResilientSummarizer(properties, provider, new SubstringSummarizer());

        String summary = resilientSummarizer.summarize("123456789012345", 10);

        assertEquals("1234567890...", summary);
    }
}
