package backend.academy.linktracker.scrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.dto.StackOverflowResponse;
import backend.academy.linktracker.scrapper.handler.impl.StackOverflowLinkHandler;
import backend.academy.linktracker.scrapper.model.LinkData;
import backend.academy.linktracker.scrapper.properties.ScrapperMessages;
import backend.academy.linktracker.scrapper.service.sender.MessageSender;
import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StackOverflowLinkHandlerTest {

    @Mock
    private StackOverflowClient stackOverflowClient;

    @Mock
    private MessageSender messageSender;

    @Mock
    private ScrapperMessages scrapperMessages;

    @Mock
    private ScrapperMessages.Updates updatesConfig;

    @InjectMocks
    private StackOverflowLinkHandler stackOverflowLinkHandler;

    @Captor
    private ArgumentCaptor<LinkUpdate> updateCaptor;

    @Test
    @DisplayName("Должен формировать и отправлять сообщение с названием вопроса, автором и превью ответа")
    void handle_ShouldFormatAndSendMessage_WhenNewAnswersFound() {
        long questionId = 123456L;
        List<Long> chatIds = List.of(100L, 200L);
        URI url = URI.create("https://stackoverflow.com/questions/" + questionId + "/how-to-exit-vim");
        OffsetDateTime lastUpdate = OffsetDateTime.of(2023, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        LinkData linkData = new LinkData(1L, url, lastUpdate, List.of(), List.of());

        StackOverflowResponse.Item questionItem =
                new StackOverflowResponse.Item(questionId, 0L, null, null, "Как выйти из Vim?");
        when(stackOverflowClient.fetchQuestion(questionId))
                .thenReturn(Optional.of(new StackOverflowResponse(List.of(questionItem))));

        long newAnswerTime = Instant.now().getEpochSecond();
        StackOverflowResponse.Item answerItem = new StackOverflowResponse.Item(
                999L,
                newAnswerTime,
                new StackOverflowResponse.Owner("Jon Skeet"),
                "Просто введите :wq и нажмите Enter. Это очень просто, если знать.",
                null);
        when(stackOverflowClient.fetchNewAnswers(questionId, lastUpdate))
                .thenReturn(Optional.of(new StackOverflowResponse(List.of(answerItem))));

        when(stackOverflowClient.fetchNewComments(questionId, lastUpdate)).thenReturn(Optional.empty());

        when(scrapperMessages.getUpdates()).thenReturn(updatesConfig);
        when(updatesConfig.getStackoverflowUpdate()).thenReturn("Вопрос: %s\nАвтор: %s\nДата: %s\nПревью: %s");

        stackOverflowLinkHandler.handle(chatIds, linkData);

        verify(messageSender).send(updateCaptor.capture());
        LinkUpdate sentUpdate = updateCaptor.getValue();

        assertThat(sentUpdate.getId()).isEqualTo(1L);
        assertThat(sentUpdate.getUrl()).isEqualTo(url);
        assertThat(sentUpdate.getTgChatIds()).containsExactly(100L, 200L);

        String description = sentUpdate.getDescription();
        assertThat(description)
                .contains("Вопрос: Как выйти из Vim?")
                .contains("Автор: Jon Skeet")
                .contains("Превью: Просто введите :wq");

        OffsetDateTime updatedTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(newAnswerTime), ZoneOffset.UTC);
        assertThat(linkData.getLastUpdate()).isEqualTo(updatedTime);
    }
}
