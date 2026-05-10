package backend.academy.linktracker.scrapper.service;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.TestcontainersConfiguration;
import backend.academy.linktracker.scrapper.dto.AddLinkRequest;
import backend.academy.linktracker.scrapper.dto.ListLinksResponse;
import backend.academy.linktracker.scrapper.model.LinkData;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class LinkServiceCacheIT {

    @Autowired
    private LinkService linkService;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private LinkRepository linkRepository;

    @MockitoBean
    private ChatRepository chatRepository;

    @MockitoBean
    private ObjectMapper objectMapper;

    private final URI TEST_URI = URI.create("https://github.com/test");

    @BeforeEach
    void setUp() {
        if (cacheManager.getCache("links") != null) {
            cacheManager.getCache("links").clear();
        }
    }

    @Test
    @DisplayName("Должен возвращать данные из Valkey при повторных запросах без обращения к БД")
    void getLinks_shouldCacheResult() {
        Long chat1 = 111L;

        when(chatRepository.existsById(eq(chat1))).thenReturn(true);
        when(linkRepository.findAllByChatId(eq(chat1), anyInt(), anyInt()))
            .thenReturn(List.of(new LinkData(1L, TEST_URI, OffsetDateTime.now(), List.of(), List.of())));

        linkService.getLinks(chat1);
        linkService.getLinks(chat1);
        linkService.getLinks(chat1);

        verify(linkRepository, times(1)).findAllByChatId(eq(chat1), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Должен инвалидировать (удалять) кэш при добавлении новой ссылки")
    void addLink_shouldEvictCache() {
        Long chat2 = 222L;

        when(chatRepository.existsById(eq(chat2))).thenReturn(true);
        when(linkRepository.findAllByChatId(eq(chat2), anyInt(), anyInt())).thenReturn(List.of());
        when(linkRepository.isLinkedToChat(eq(chat2), eq(TEST_URI))).thenReturn(false);
        when(linkRepository.addLinkToChat(eq(chat2), eq(TEST_URI), anyList()))
            .thenReturn(new LinkData(1L, TEST_URI, OffsetDateTime.now(), List.of(), List.of()));

        linkService.getLinks(chat2);
        linkService.getLinks(chat2);

        linkService.addLink(chat2, new AddLinkRequest().link(TEST_URI).tags(List.of()));

        linkService.getLinks(chat2);

        verify(linkRepository, times(2)).findAllByChatId(eq(chat2), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Должен удалять данные из кэша при удалении ссылки")
    void removeLink_shouldEvictCache() {
        Long chat3 = 333L;

        when(chatRepository.existsById(eq(chat3))).thenReturn(true);
        when(linkRepository.isLinkedToChat(eq(chat3), eq(TEST_URI))).thenReturn(true);
        when(linkRepository.findAllByChatId(eq(chat3), anyInt(), anyInt())).thenReturn(List.of());

        linkService.getLinks(chat3);
        linkService.getLinks(chat3);

        linkService.removeLink(chat3, TEST_URI);

        linkService.getLinks(chat3);

        verify(linkRepository, times(2)).findAllByChatId(eq(chat3), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Данные в кэше должны соответствовать ожидаемому формату JSON")
    void getLinks_shouldStoreCorrectDataStructure() {
        Long chat4 = 444L;
        when(chatRepository.existsById(eq(chat4))).thenReturn(true);
        when(linkRepository.findAllByChatId(eq(chat4), anyInt(), anyInt())).thenReturn(List.of());

        linkService.getLinks(chat4);

        var cache = cacheManager.getCache("links");
        var cachedValue = cache.get(chat4).get();

        assertNotNull(cachedValue);
        assertInstanceOf(ListLinksResponse.class, cachedValue);
    }
}
