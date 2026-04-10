package backend.academy.linktracker.scrapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.dto.StackOverflowResponse;
import backend.academy.linktracker.scrapper.handler.impl.StackOverflowLinkHandler;
import backend.academy.linktracker.scrapper.model.LinkData;
import backend.academy.linktracker.scrapper.service.UpdateMessageFormatter;
import backend.academy.linktracker.scrapper.service.sender.MessageSender;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    private UpdateMessageFormatter messageFormatter;

    @InjectMocks
    private StackOverflowLinkHandler stackOverflowLinkHandler;

    @Test
    @DisplayName("Должен обработать новые ответы с типом 'Ответ'")
    void handle_shouldProcessAnswers() {
        URI url = URI.create("https://stackoverflow.com/questions/123");
        OffsetDateTime lastUpdate = OffsetDateTime.now(ZoneOffset.UTC).minusDays(1);
        LinkData linkData = new LinkData(1L, url, lastUpdate, List.of(), List.of());

        StackOverflowResponse questionResp = new StackOverflowResponse(
                List.of(new StackOverflowResponse.Item(123L, 0L, null, null, "Question title")));
        when(stackOverflowClient.fetchQuestion(123L)).thenReturn(Optional.of(questionResp));

        StackOverflowResponse.Item answer = new StackOverflowResponse.Item(
                1L, OffsetDateTime.now().toEpochSecond(), new StackOverflowResponse.Owner("user"), "body", null);

        when(stackOverflowClient.fetchNewAnswers(eq(123L), eq(lastUpdate)))
                .thenReturn(Optional.of(new StackOverflowResponse(List.of(answer))));
        when(stackOverflowClient.fetchNewComments(anyLong(), any())).thenReturn(Optional.empty());

        stackOverflowLinkHandler.handle(List.of(1L), linkData);

        verify(messageFormatter).formatStackOverflowUpdate(eq(answer), any(), eq("Ответ"), any());
    }

    @Test
    @DisplayName("Должен обработать новые комментарии с типом 'Комментарий'")
    void handle_shouldProcessComments() {
        URI url = URI.create("https://stackoverflow.com/questions/123");
        OffsetDateTime lastUpdate = OffsetDateTime.now(ZoneOffset.UTC).minusDays(1);
        LinkData linkData = new LinkData(1L, url, lastUpdate, List.of(), List.of());

        StackOverflowResponse questionResp = new StackOverflowResponse(
                List.of(new StackOverflowResponse.Item(123L, 0L, null, null, "Question title")));
        when(stackOverflowClient.fetchQuestion(123L)).thenReturn(Optional.of(questionResp));

        StackOverflowResponse.Item comment = new StackOverflowResponse.Item(
                2L, OffsetDateTime.now().toEpochSecond(), new StackOverflowResponse.Owner("user"), "body", null);

        when(stackOverflowClient.fetchNewAnswers(anyLong(), any())).thenReturn(Optional.empty());
        when(stackOverflowClient.fetchNewComments(eq(123L), eq(lastUpdate)))
                .thenReturn(Optional.of(new StackOverflowResponse(List.of(comment))));

        stackOverflowLinkHandler.handle(List.of(1L), linkData);

        verify(messageFormatter).formatStackOverflowUpdate(eq(comment), any(), eq("Комментарий"), any());
    }
}
