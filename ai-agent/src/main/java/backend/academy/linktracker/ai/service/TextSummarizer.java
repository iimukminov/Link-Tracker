package backend.academy.linktracker.ai.service;

public interface TextSummarizer {
    String summarize(String text, int threshold);
}
