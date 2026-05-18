package backend.academy.linktracker.scrapper.sender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.client.BotClient;
import backend.academy.linktracker.scrapper.properties.KafkaProperties;
import backend.academy.linktracker.scrapper.service.sender.impl.HttpBotMessageSender;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.kafka.core.KafkaTemplate;

public class FallbackIT {

    @Test
    @DisplayName("TC-5.1: Падение основного транспорта. Сообщение улетает в Kafka")
    @SuppressWarnings("unchecked")
    void shouldSendToKafkaWhenHttpFails() {
        BotClient mockBotClient = Mockito.mock(BotClient.class);
        KafkaTemplate<String, Object> mockKafkaTemplate = Mockito.mock(KafkaTemplate.class);
        KafkaProperties mockKafkaProps = new KafkaProperties();
        mockKafkaProps.setTopic("test-topic");

        ObjectProvider<KafkaTemplate<String, Object>> templateProvider = Mockito.mock(ObjectProvider.class);
        ObjectProvider<KafkaProperties> propsProvider = Mockito.mock(ObjectProvider.class);

        when(templateProvider.getIfAvailable()).thenReturn(mockKafkaTemplate);
        when(propsProvider.getIfAvailable()).thenReturn(mockKafkaProps);

        CallNotPermittedException exception = Mockito.mock(CallNotPermittedException.class);
        Mockito.doThrow(exception).when(mockBotClient).sendUpdate(any());

        HttpBotMessageSender sender = new HttpBotMessageSender(mockBotClient, templateProvider, propsProvider);

        LinkUpdate update = new LinkUpdate()
                .id(1L)
                .url(URI.create("http://test"))
                .description("desc")
                .tgChatIds(List.of(123L));

        sender.send(update);

        verify(mockKafkaTemplate).send(eq("test-topic"), eq("1"), any());
    }
}
