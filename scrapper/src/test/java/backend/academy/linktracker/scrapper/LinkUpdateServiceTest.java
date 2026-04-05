package backend.academy.linktracker.scrapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.handler.LinkHandler;
import backend.academy.linktracker.scrapper.model.LinkData;
import backend.academy.linktracker.scrapper.properties.SchedulerProperties;
import backend.academy.linktracker.scrapper.properties.ScrapperMessages;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.service.LinkUpdateService;
import backend.academy.linktracker.scrapper.service.sender.MessageSender;
import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LinkUpdateServiceTest {

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private LinkHandler githubHandler;

    @Mock
    private SchedulerProperties schedulerProperties;

    @Mock
    private MessageSender messageSender;

    @Mock
    private ScrapperMessages scrapperMessages;

    private ExecutorService linkUpdateExecutor;

    private LinkUpdateService linkUpdateService;

    @BeforeEach
    void setUp() {
        linkUpdateExecutor = Executors.newFixedThreadPool(2);

        linkUpdateService = new LinkUpdateService(
            linkRepository,
            chatRepository,
            List.of(githubHandler),
            schedulerProperties,
            linkUpdateExecutor,
            messageSender,
            scrapperMessages
        );
    }

    @AfterEach
    void tearDown() {
        if (linkUpdateExecutor != null) {
            linkUpdateExecutor.shutdownNow();
        }
    }

    @Test
    void update_shouldGroupChatIdsByUrlAndNotifyOnlySubscribers() {
        when(schedulerProperties.getForceCheckDelay()).thenReturn(Duration.ofMinutes(10));
        when(schedulerProperties.getBatchSize()).thenReturn(50);

        URI githubUrl = URI.create("https://github.com/user/repo");
        URI stackoverflowUrl = URI.create("https://stackoverflow.com/questions/123");

        LinkData githubLink = new LinkData(1L, githubUrl, OffsetDateTime.now().minusMinutes(15), List.of(), List.of());
        LinkData soLink =
            new LinkData(2L, stackoverflowUrl, OffsetDateTime.now().minusMinutes(15), List.of(), List.of());

        when(linkRepository.findLinksToUpdate(any(OffsetDateTime.class), anyInt()))
            .thenReturn(List.of(githubLink, soLink));

        when(githubHandler.supports("github.com")).thenReturn(true);
        when(githubHandler.supports("stackoverflow.com")).thenReturn(false);

        when(chatRepository.findAllByLinkId(1L)).thenReturn(List.of(100L, 200L));

        linkUpdateService.updateLinks();

        verify(githubHandler, times(1))
            .handle(
                argThat(chatIds -> chatIds.size() == 2 && chatIds.containsAll(List.of(100L, 200L))),
                eq(githubLink));

        verify(githubHandler, never()).handle(argThat(ids -> ids.contains(300L)), any());


        verify(linkRepository, times(1)).updateLastUpdateTime(eq(1L), any());
        verify(linkRepository, times(1)).updateLastUpdateTime(eq(2L), any());
    }

    @Test
    void updateLinks_shouldIsolateErrorsAndNotifyUser() {
        LinkData badLink = new LinkData(1L, URI.create("https://github.com/bad/repo"), OffsetDateTime.now().minusMinutes(15), List.of(), List.of());
        LinkData goodLink = new LinkData(2L, URI.create("https://github.com/good/repo"), OffsetDateTime.now().minusMinutes(15), List.of(), List.of());

        when(schedulerProperties.getForceCheckDelay()).thenReturn(Duration.ofMinutes(10));
        when(schedulerProperties.getBatchSize()).thenReturn(50);
        when(linkRepository.findLinksToUpdate(any(), anyInt())).thenReturn(List.of(badLink, goodLink));

        when(githubHandler.supports(anyString())).thenReturn(true);
        when(chatRepository.findAllByLinkId(anyLong())).thenReturn(List.of(100L));

        when(scrapperMessages.getErrors()).thenReturn(new ScrapperMessages.Errors());
        scrapperMessages.getErrors().setProcessingError("Error msg");

        doThrow(new RuntimeException("API DOWN")).when(githubHandler).handle(anyList(), eq(badLink));

        linkUpdateService.updateLinks();

        verify(githubHandler).handle(anyList(), eq(goodLink));

        verify(linkRepository, times(2)).updateLastUpdateTime(anyLong(), any());

        verify(messageSender).send(argThat(update ->
            update.getId().equals(1L) && update.getDescription().equals("Error msg")
        ));
    }
}
