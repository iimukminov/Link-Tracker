package backend.academy.linktracker.scrapper.sender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.client.BotClient;
import backend.academy.linktracker.scrapper.entity.OutboxEvent;
import backend.academy.linktracker.scrapper.properties.KafkaProperties;
import backend.academy.linktracker.scrapper.repository.OutboxRepository;
import backend.academy.linktracker.scrapper.service.sender.impl.HttpBotMessageSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;

public class FallbackIT {

    @Test
    @DisplayName("TC-5.1: Падение основного транспорта. Сообщение сохраняется в Outbox")
    @SuppressWarnings("unchecked")
    void shouldSaveToOutboxWhenHttpFails() throws Exception {
        BotClient mockBotClient = Mockito.mock(BotClient.class);
        OutboxRepository mockOutboxRepository = Mockito.mock(OutboxRepository.class);
        ObjectMapper mockObjectMapper = Mockito.mock(ObjectMapper.class);

        KafkaProperties mockKafkaProps = new KafkaProperties();
        mockKafkaProps.setTopic("test-topic");
        ObjectProvider<KafkaProperties> propsProvider = Mockito.mock(ObjectProvider.class);
        when(propsProvider.getIfAvailable()).thenReturn(mockKafkaProps);

        CallNotPermittedException exception = Mockito.mock(CallNotPermittedException.class);
        Mockito.doThrow(exception).when(mockBotClient).sendUpdate(any());

        when(mockObjectMapper.writeValueAsString(any())).thenReturn("{\"test\":\"data\"}");

        HttpBotMessageSender sender =
                new HttpBotMessageSender(mockBotClient, mockOutboxRepository, mockObjectMapper, propsProvider);

        LinkUpdate update = new LinkUpdate()
                .id(1L)
                .url(URI.create("http://test"))
                .description("desc")
                .tgChatIds(List.of(123L));

        sender.send(update);

        verify(mockOutboxRepository).save(any(OutboxEvent.class));
    }
}
