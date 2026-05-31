package backend.academy.linktracker.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;

import backend.academy.linktracker.ai.properties.AiAgentProperties;
import backend.academy.linktracker.ai.service.PrioritizationService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PrioritizationServiceTest {

    private PrioritizationService prioritizationService;

    @BeforeEach
    void setUp() {
        AiAgentProperties properties = new AiAgentProperties();
        properties.getPrioritization().setHighKeywords(List.of("critical", "urgent", "security"));
        properties.getPrioritization().setLowKeywords(List.of("typo", "minor", "docs"));

        prioritizationService = new PrioritizationService(properties);
    }

    @Test
    void shouldReturnHighPriority_whenContainsHighKeyword() {
        String text = "We have a critical bug in the production system.";
        String priority = prioritizationService.determinePriority(text);
        assertEquals("HIGH", priority);
    }

    @Test
    void shouldReturnMediumPriority_whenNoKeywordsMatch() {
        String text = "Just a regular update about new features.";
        String priority = prioritizationService.determinePriority(text);
        assertEquals("MEDIUM", priority);
    }

    @Test
    void shouldReturnLowPriority_whenContainsLowKeyword() {
        String text = "Fix typo in the readme file.";
        String priority = prioritizationService.determinePriority(text);
        assertEquals("LOW", priority);
    }

    @Test
    void shouldReturnHighPriority_whenContainsBothKeywords() {
        String text = "Critical fix for a minor typo.";
        String priority = prioritizationService.determinePriority(text);
        assertEquals("HIGH", priority);
    }

    @Test
    void shouldReturnMediumPriority_whenDescriptionIsNull() {
        String priority = prioritizationService.determinePriority(null);
        assertEquals("MEDIUM", priority);
    }

    @Test
    void shouldReturnMediumPriority_whenDescriptionIsEmpty() {
        String priority = prioritizationService.determinePriority("   ");
        assertEquals("MEDIUM", priority);
    }
}
