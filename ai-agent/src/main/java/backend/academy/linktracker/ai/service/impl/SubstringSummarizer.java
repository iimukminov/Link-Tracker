package backend.academy.linktracker.ai.service.impl;

import backend.academy.linktracker.ai.service.TextSummarizer;
import org.springframework.stereotype.Component;

@Component
public class SubstringSummarizer implements TextSummarizer {

    private static final String ELLIPSIS = "...";

    @Override
    public String summarize(String text, int threshold) {
        if (text == null || text.length() <= threshold) {
            return text;
        }
        return text.substring(0, threshold) + ELLIPSIS;
    }
}
