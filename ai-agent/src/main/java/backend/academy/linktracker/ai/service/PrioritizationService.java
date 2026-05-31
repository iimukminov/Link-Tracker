package backend.academy.linktracker.ai.service;

import backend.academy.linktracker.ai.properties.AiAgentProperties;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrioritizationService {

    private final AiAgentProperties properties;

    public String determinePriority(String description) {
        if (description == null || description.isBlank()) {
            return "MEDIUM";
        }

        String lowerDesc = description.toLowerCase();
        List<String> highKeywords = properties.getPrioritization().getHighKeywords();
        List<String> lowKeywords = properties.getPrioritization().getLowKeywords();

        boolean isHigh = highKeywords.stream().anyMatch(keyword -> lowerDesc.contains(keyword.toLowerCase()));
        if (isHigh) {
            return "HIGH";
        }

        boolean isLow = lowKeywords.stream().anyMatch(keyword -> lowerDesc.contains(keyword.toLowerCase()));
        if (isLow) {
            return "LOW";
        }

        return "MEDIUM";
    }
}
