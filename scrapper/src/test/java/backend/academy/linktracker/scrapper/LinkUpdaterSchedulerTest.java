package backend.academy.linktracker.scrapper;

import static org.mockito.Mockito.*;

import backend.academy.linktracker.scrapper.handler.LinkHandler;
import backend.academy.linktracker.scrapper.model.LinkData;
import backend.academy.linktracker.scrapper.repository.InMemoryScrapperRepository;
import backend.academy.linktracker.scrapper.scheduler.LinkUpdaterScheduler;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LinkUpdaterSchedulerTest {

    @Mock
    private InMemoryScrapperRepository repository;

    @Mock
    private LinkHandler githubHandler;

    private LinkUpdaterScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new LinkUpdaterScheduler(repository, List.of(githubHandler));
    }

    @Test
    void update_shouldGroupChatIdsByUrlAndNotifyOnlySubscribers() {
        URI targetUrl = URI.create("https://github.com/user/repo");
        URI otherUrl = URI.create("https://stackoverflow.com/questions/123");

        LinkData linkForChat1 = new LinkData(1L, targetUrl, OffsetDateTime.now(), List.of(), List.of());
        LinkData linkForChat2 = new LinkData(2L, targetUrl, OffsetDateTime.now(), List.of(), List.of());
        LinkData linkForChat3 = new LinkData(3L, otherUrl, OffsetDateTime.now(), List.of(), List.of());

        when(repository.findAll())
                .thenReturn(Map.of(
                        100L, Set.of(linkForChat1),
                        200L, Set.of(linkForChat2),
                        300L, Set.of(linkForChat3)));

        when(githubHandler.supports("github.com")).thenReturn(true);
        when(githubHandler.supports("stackoverflow.com")).thenReturn(false);

        scheduler.update();

        verify(githubHandler, times(1))
                .handle(
                        argThat(chatIds -> chatIds.size() == 2 && chatIds.containsAll(List.of(100L, 200L))),
                        eq(linkForChat1));
    }
}
