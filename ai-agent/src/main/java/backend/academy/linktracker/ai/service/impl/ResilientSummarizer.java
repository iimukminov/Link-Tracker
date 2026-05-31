package backend.academy.linktracker.ai.service.impl;

import backend.academy.linktracker.ai.properties.AiAgentProperties;
import backend.academy.linktracker.ai.service.TextSummarizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class ResilientSummarizer implements TextSummarizer {

    private final AiAgentProperties properties;
    private final ObjectProvider<LlmContentSummarizer> llmSummarizerProvider;
    private final SubstringSummarizer substringSummarizer;

    @Override
    public String summarize(String text, int threshold) {
        if (properties.getSummarization().getProvider() == AiAgentProperties.Provider.STUB) {
            return substringSummarizer.summarize(text, threshold);
        }

        try {
            LlmContentSummarizer llmSummarizer = llmSummarizerProvider.getIfAvailable();
            if (llmSummarizer != null) {
                return llmSummarizer.summarize(text, threshold);
            }
        } catch (Exception e) {
            log.atWarn()
                    .setCause(e)
                    .addKeyValue("threshold", threshold)
                    .addKeyValue("provider", properties.getSummarization().getProvider())
                    .log("LLM Summarization failed. Falling back to simple substring truncation.");
        }

        return substringSummarizer.summarize(text, threshold);
    }
}
