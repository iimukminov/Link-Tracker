package backend.academy.linktracker.scrapper.sender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.TestcontainersConfiguration;
import backend.academy.linktracker.scrapper.entity.OutboxEvent;
import backend.academy.linktracker.scrapper.repository.OutboxRepository;
import backend.academy.linktracker.scrapper.scheduler.OutboxScheduler;
import backend.academy.linktracker.scrapper.service.sender.MessageSender;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(
        properties = {
            "app.use-queue=true",
            "app.use-outbox=true",
            "app.scheduler.enable=false",
            "app.outbox.interval=100000"
        })
public class OutboxIT {

    @Autowired
    private MessageSender messageSender;

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private OutboxScheduler outboxScheduler;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @BeforeEach
    void cleanDb() {
        jdbcTemplate.update("TRUNCATE TABLE outbox_event");
    }

    @Test
    @DisplayName("Сценарий 1: Успешная отправка меняет статус на SENT")
    void shouldChangeStatusToSentWhenKafkaIsAvailable() {
        LinkUpdate update = new LinkUpdate()
                .id(101L)
                .url(URI.create("https://github.com/ok"))
                .description("Test");
        when(kafkaTemplate.send(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(new SendResult<>(null, null)));

        messageSender.send(update);

        List<OutboxEvent> pendingEvents = outboxRepository.findPending(10);
        assertThat(pendingEvents).hasSize(1);
        Long eventId = pendingEvents.get(0).getId();

        outboxScheduler.processOutbox();

        verify(kafkaTemplate, times(1)).send(any(), eq("101"), any());

        String status =
                jdbcTemplate.queryForObject("SELECT status FROM outbox_event WHERE id = ?", String.class, eventId);
        assertThat(status).isEqualTo("SENT");
    }

    @Test
    @DisplayName("Сценарий 2: Падение сети (Кафки) оставляет статус PENDING и увеличивает retry_count")
    void shouldKeepStatusPendingAndIncrementRetryCountWhenKafkaIsDown() {
        LinkUpdate update = new LinkUpdate()
                .id(202L)
                .url(URI.create("https://github.com/fail"))
                .description("Fail");

        CompletableFuture<SendResult<String, Object>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Network timeout simulation"));
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(failedFuture);

        messageSender.send(update);

        List<OutboxEvent> pendingEvents = outboxRepository.findPending(10);
        assertThat(pendingEvents).hasSize(1);
        Long eventId = pendingEvents.get(0).getId();

        Integer initialRetryCount = jdbcTemplate.queryForObject(
                "SELECT retry_count FROM outbox_event WHERE id = ?", Integer.class, eventId);
        assertThat(initialRetryCount).isEqualTo(0);

        outboxScheduler.processOutbox();

        String status =
                jdbcTemplate.queryForObject("SELECT status FROM outbox_event WHERE id = ?", String.class, eventId);
        assertThat(status).isEqualTo("PENDING");

        Integer retryCountAfterFail = jdbcTemplate.queryForObject(
                "SELECT retry_count FROM outbox_event WHERE id = ?", Integer.class, eventId);
        assertThat(retryCountAfterFail).isEqualTo(1);
    }
}
