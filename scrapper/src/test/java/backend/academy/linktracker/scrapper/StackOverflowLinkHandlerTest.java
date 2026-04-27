package backend.academy.linktracker.scrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.dto.StackOverflowResponse;
import backend.academy.linktracker.scrapper.handler.impl.StackOverflowLinkHandler;
import backend.academy.linktracker.scrapper.model.LinkData;
import backend.academy.linktracker.scrapper.service.UpdateMessageFormatter;
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
    @DisplayName("Должен обработать новые ответы и обновить время по lastActivityDate")
    void handle_shouldProcessAnswers() {
        URI url = URI.create("https://stackoverflow.com/questions/123");

        OffsetDateTime lastUpdateInDb = OffsetDateTime.now(ZoneOffset.UTC).minusDays(1);
        LinkData linkData = new LinkData(1L, url, lastUpdateInDb, List.of(), List.of());

        StackOverflowResponse questionResp = new StackOverflowResponse(
                List.of(new StackOverflowResponse.Item(123L, 0L, 0L, null, null, "Question title")));
        when(stackOverflowClient.fetchQuestion(123L)).thenReturn(Optional.of(questionResp));

        long now = Instant.now().getEpochSecond();
        StackOverflowResponse.Item answer = new StackOverflowResponse.Item(
                1L, lastUpdateInDb.toEpochSecond(), now, new StackOverflowResponse.Owner("user"), "body", null);

        when(stackOverflowClient.fetchNewAnswers(eq(123L), eq(lastUpdateInDb)))
                .thenReturn(Optional.of(new StackOverflowResponse(List.of(answer))));
        when(stackOverflowClient.fetchNewComments(anyLong(), any())).thenReturn(Optional.empty());

        stackOverflowLinkHandler.handle(List.of(1L), linkData);

        verify(messageFormatter).formatStackOverflowUpdate(eq(answer), any(), eq("Ответ"), any());
        verify(messageSender).send(any());

        assertThat(linkData.getLastUpdate().toEpochSecond()).isEqualTo(now);
    }

    @Test
    @DisplayName("Должен обработать новые комментарии, используя creationDate (fallback)")
    void handle_shouldProcessCommentsUsingCreationDate() {
        URI url = URI.create("https://stackoverflow.com/questions/123");
        OffsetDateTime lastUpdateInDb = OffsetDateTime.now(ZoneOffset.UTC).minusDays(1);
        LinkData linkData = new LinkData(1L, url, lastUpdateInDb, List.of(), List.of());

        when(stackOverflowClient.fetchQuestion(anyLong()))
                .thenReturn(Optional.of(new StackOverflowResponse(
                        List.of(new StackOverflowResponse.Item(123L, 0L, 0L, null, null, "Title")))));

        long now = Instant.now().getEpochSecond();
        StackOverflowResponse.Item comment =
                new StackOverflowResponse.Item(2L, now, null, new StackOverflowResponse.Owner("user"), "body", null);

        when(stackOverflowClient.fetchNewAnswers(anyLong(), any())).thenReturn(Optional.empty());
        when(stackOverflowClient.fetchNewComments(eq(123L), eq(lastUpdateInDb)))
                .thenReturn(Optional.of(new StackOverflowResponse(List.of(comment))));

        stackOverflowLinkHandler.handle(List.of(1L), linkData);

        verify(messageFormatter).formatStackOverflowUpdate(eq(comment), any(), eq("Комментарий"), any());
        assertThat(linkData.getLastUpdate().toEpochSecond()).isEqualTo(now);
    }

    @Test
    @DisplayName("Не должен отправлять уведомление, если дата совпадает с базой")
    void handle_shouldNotSendUpdateWhenDateIsSame() {
        URI url = URI.create("https://stackoverflow.com/questions/123");
        OffsetDateTime lastUpdate =
                OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        LinkData linkData = new LinkData(1L, url, lastUpdate, List.of(), List.of());

        StackOverflowResponse.Item oldItem = new StackOverflowResponse.Item(
                1L, lastUpdate.toEpochSecond(), lastUpdate.toEpochSecond(), null, "body", null);

        when(stackOverflowClient.fetchQuestion(anyLong()))
                .thenReturn(Optional.of(new StackOverflowResponse(List.of(oldItem))));
        when(stackOverflowClient.fetchNewAnswers(anyLong(), any()))
                .thenReturn(Optional.of(new StackOverflowResponse(List.of(oldItem))));
        when(stackOverflowClient.fetchNewComments(anyLong(), any())).thenReturn(Optional.empty());

        stackOverflowLinkHandler.handle(List.of(1L), linkData);

        verify(messageSender, never()).send(any());
    }
}
