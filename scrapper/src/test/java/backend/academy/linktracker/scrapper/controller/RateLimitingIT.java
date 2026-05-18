package backend.academy.linktracker.scrapper.controller;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.linktracker.scrapper.TestcontainersConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class RateLimitingIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("TC-3.1: При превышении лимита (20 запросов) возвращается 429 Too Many Requests")
    void rateLimiter_ShouldReturn429_WhenLimitExceeded() throws Exception {
        String targetIp = "192.168.7.7";

        for (int i = 0; i < 20; i++) {
            int finalI = i;
            mockMvc.perform(post("/tg-chat/100500").header("X-Forwarded-For", targetIp))
                    .andExpect(result -> assertNotEquals(
                            429,
                            result.getResponse().getStatus(),
                            "Запрос " + finalI + " не должен быть заблокирован"));
        }

        mockMvc.perform(post("/tg-chat/100500").header("X-Forwarded-For", targetIp))
                .andExpect(status().isTooManyRequests());

        String otherIp = "10.0.0.1";
        mockMvc.perform(post("/tg-chat/100500").header("X-Forwarded-For", otherIp))
                .andExpect(result -> assertNotEquals(429, result.getResponse().getStatus()));
    }
}
