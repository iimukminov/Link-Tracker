package backend.academy.linktracker.scrapper.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class LinkUtils {
    public static String truncateBody(String body, String fallback) {
        if (body == null || body.isBlank()) {
            return fallback;
        }
        if (body.length() <= 200) {
            return body;
        }
        return body.substring(0, 197) + "...";
    }
}
