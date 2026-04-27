package backend.academy.linktracker.scrapper;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.scrapper.util.LinkUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

class PreviewTruncationTest {

    private static final String FALLBACK = "*Нет текста*";

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Должен возвращать заглушку, если текст null или пустой")
    void shouldReturnFallbackWhenNullOrEmpty(String input) {
        String result = LinkUtils.truncateBody(input, FALLBACK);
        assertThat(result).isEqualTo(FALLBACK);
    }

    @Test
    @DisplayName("Не должен обрезать текст короче 200 символов")
    void shouldNotTruncateShortText() {
        String shortText = "Короткий текст";
        String result = LinkUtils.truncateBody(shortText, FALLBACK);
        assertThat(result).isEqualTo(shortText);
    }

    @Test
    @DisplayName("Не должен обрезать текст длиной ровно 200 символов")
    void shouldNotTruncateExactLimitText() {
        String exactText = "A".repeat(200);
        String result = LinkUtils.truncateBody(exactText, FALLBACK);
        assertThat(result).hasSize(200).isEqualTo(exactText);
    }

    @Test
    @DisplayName("Должен обрезать текст длиннее 200 символов и добавлять многоточие")
    void shouldTruncateLongText() {
        String longText = "A".repeat(250);
        String result = LinkUtils.truncateBody(longText, FALLBACK);

        assertThat(result).hasSize(200);
        assertThat(result).endsWith("...");
        assertThat(result.substring(0, 197)).isEqualTo("A".repeat(197));
    }
}
