package backend.academy.linktracker.ai.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UpdatePriority {
    LOW(1),
    MEDIUM(2),
    HIGH(3);

    private final int weight;

    public static UpdatePriority fromString(String priorityStr) {
        if (priorityStr == null) return MEDIUM;
        try {
            return UpdatePriority.valueOf(priorityStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return MEDIUM;
        }
    }

    public static UpdatePriority max(UpdatePriority p1, UpdatePriority p2) {
        return p1.getWeight() > p2.getWeight() ? p1 : p2;
    }
}
