package backend.academy.linktracker.bot;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.utility.TestcontainersConfiguration;

@SpringBootTest(
        properties = {
            "SCRAPPER_VALKEY_CLUSTER_NODES=127.0.0.1:6379",
            "TELEGRAM_BOT_TOKEN=dummy-token",
            "app.use-queue=false"
        })
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class RateLimitingIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("TC-3.1: При превышении лимита (20 запросов) бот возвращает 429 Too Many Requests")
    void rateLimiter_ShouldReturn429_WhenLimitExceeded() throws Exception {
        String targetIp = "192.168.1.10";
        String payload =
                "{\"id\": 1, \"url\": \"http://example.com\", \"description\": \"test\", \"tgChatIds\": [123]}";

        for (int i = 0; i < 20; i++) {
            mockMvc.perform(post("/updates")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload)
                            .header("X-Forwarded-For", targetIp))
                    .andExpect(
                            result -> assertNotEquals(429, result.getResponse().getStatus()));
        }

        mockMvc.perform(post("/updates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("X-Forwarded-For", targetIp))
                .andExpect(status().isTooManyRequests());
    }
}
