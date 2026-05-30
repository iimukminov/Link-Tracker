package backend.academy.linktracker.scrapper.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.linktracker.common.interceptor.RateLimitingInterceptor;
import backend.academy.linktracker.scrapper.TestcontainersConfiguration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
        properties = {
            "app.scheduler.enable=false",
            "app.use-queue=false",
            "app.use-outbox=false",
            "app.rate-limit.capacity=20"
        })
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class RateLimitingIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RateLimitingInterceptor rateLimitingInterceptor;

    @AfterEach
    void tearDown() {
        rateLimitingInterceptor.clearCache();
    }

    @Test
    @DisplayName("TC-3.1: При превышении лимита (20 запросов) возвращается 429 Too Many Requests")
    void rateLimiter_ShouldReturn429_WhenLimitExceeded() throws Exception {
        String chatId = "100500";
        int limit = 20;

        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(limit);

        for (int i = 0; i < limit; i++) {
            executor.submit(() -> {
                try {
                    mockMvc.perform(post("/tg-chat/" + chatId).header("tg-chat-id", chatId))
                            .andExpect(status().isOk());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        mockMvc.perform(post("/tg-chat/" + chatId).header("tg-chat-id", chatId))
                .andExpect(status().isTooManyRequests());

        String otherChatId = "999999";
        mockMvc.perform(post("/tg-chat/" + otherChatId).header("tg-chat-id", otherChatId))
                .andExpect(status().isOk());
    }
}
