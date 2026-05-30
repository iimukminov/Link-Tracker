package backend.academy.linktracker.bot;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.linktracker.common.interceptor.RateLimitingInterceptor;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
        properties = {
            "SCRAPPER_VALKEY_CLUSTER_NODES=127.0.0.1:6379",
            "TELEGRAM_BOT_TOKEN=dummy-token",
            "app.use-queue=false",
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
    @DisplayName("TC-3.1: При превышении лимита (20 запросов) бот возвращает 429 Too Many Requests")
    void rateLimiter_ShouldReturn429_WhenLimitExceeded() throws Exception {
        String targetIp = "192.168.1.10";
        String payload =
                "{\"id\": 1, \"url\": \"http://example.com\", \"description\": \"test\", \"tgChatIds\": [123]}";

        int limit = 20;

        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(limit);

        for (int i = 0; i < limit; i++) {
            executor.submit(() -> {
                try {
                    mockMvc.perform(post("/updates")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(payload)
                                    .with(request -> {
                                        request.setRemoteAddr(targetIp);
                                        return request;
                                    }))
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

        mockMvc.perform(post("/updates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .with(request -> {
                            request.setRemoteAddr(targetIp);
                            return request;
                        }))
                .andExpect(status().isTooManyRequests());
    }
}
