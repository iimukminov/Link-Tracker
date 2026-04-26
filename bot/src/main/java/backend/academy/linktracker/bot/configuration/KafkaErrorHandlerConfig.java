package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.bot.properties.KafkaProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.util.backoff.FixedBackOff;
import jakarta.validation.ValidationException;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class KafkaErrorHandlerConfig {

    private final KafkaProperties kafkaProperties;

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> kafkaTemplate) {

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, exception) -> {
                    log.atError()
                       .setMessage("Sending message to DLQ")
                       .addKeyValue("topic", record.topic())
                       .addKeyValue("dlq_topic", kafkaProperties.getDlqTopic())
                       .setCause(exception)
                       .log();
                    return new TopicPartition(kafkaProperties.getDlqTopic(), record.partition());
                });

        FixedBackOff backOff = new FixedBackOff(
                kafkaProperties.getRetry().getBackoffIntervalMs(),
                kafkaProperties.getRetry().getMaxAttempts() - 1L
        );

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        errorHandler.addNotRetryableExceptions(
                DeserializationException.class,
                ValidationException.class,
                IllegalArgumentException.class
        );

        return errorHandler;
    }
}
