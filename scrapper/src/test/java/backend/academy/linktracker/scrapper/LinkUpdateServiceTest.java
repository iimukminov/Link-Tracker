package backend.academy.linktracker.scrapper;

import static org.mockito.Mockito.*;

import backend.academy.linktracker.scrapper.handler.LinkHandler;
import backend.academy.linktracker.scrapper.model.LinkData;
import backend.academy.linktracker.scrapper.properties.SchedulerProperties;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.service.LinkUpdateService;
import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LinkUpdateServiceTest {

    @Mock private LinkRepository linkRepository;
    @Mock private ChatRepository chatRepository;
    @Mock private LinkHandler githubHandler;
    @Mock private SchedulerProperties schedulerProperties;

    private LinkUpdateService linkUpdateService;

    @BeforeEach
    void setUp() {
        linkUpdateService = new LinkUpdateService(linkRepository, chatRepository, List.of(githubHandler), schedulerProperties);
    }

    @Test
    void update_shouldGroupChatIdsByUrlAndNotifyOnlySubscribers() {
        when(schedulerProperties.getForceCheckDelay()).thenReturn(Duration.ofMinutes(10));
        when(schedulerProperties.getBatchSize()).thenReturn(50);

        URI githubUrl = URI.create("https://github.com/user/repo");
        URI stackoverflowUrl = URI.create("https://stackoverflow.com/questions/123");

        LinkData githubLink = new LinkData(1L, githubUrl, OffsetDateTime.now().minusMinutes(15), List.of(), List.of());
        LinkData soLink = new LinkData(2L, stackoverflowUrl, OffsetDateTime.now().minusMinutes(15), List.of(), List.of());

        when(linkRepository.findLinksToUpdate(any(OffsetDateTime.class), anyInt()))
            .thenReturn(List.of(githubLink, soLink));
        when(githubHandler.supports("github.com")).thenReturn(true);
        when(githubHandler.supports("stackoverflow.com")).thenReturn(false);
        when(chatRepository.findAllByLinkId(1L)).thenReturn(List.of(100L, 200L));

        linkUpdateService.updateLinks();

        verify(githubHandler, times(1)).handle(
            argThat(chatIds -> chatIds.size() == 2 && chatIds.containsAll(List.of(100L, 200L))),
            eq(githubLink));
        verify(githubHandler, never()).handle(argThat(ids -> ids.contains(300L)), any());
        verify(linkRepository, times(1)).updateLastUpdateTime(eq(1L), any());
        verify(linkRepository, times(1)).updateLastUpdateTime(eq(2L), any());
    }
}
