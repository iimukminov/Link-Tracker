package backend.academy.linktracker.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;

import backend.academy.linktracker.ai.service.impl.SubstringSummarizer;
import org.junit.jupiter.api.Test;

class SubstringSummarizerTest {


    private final SubstringSummarizer summarizer = new SubstringSummarizer();

    @Test
    void shouldSummarizeLongText() {
        String text = "This is a very long text that exceeds the threshold";
        int threshold = 10;

        String result = summarizer.summarize(text, threshold);

        assertEquals("This is a ...", result);
    }

    @Test
    void shouldReturnOriginalTextWhenShort() {
        String text = "Short";
        int threshold = 10;

        String result = summarizer.summarize(text, threshold);

        assertEquals("Short", result);
    }
}
