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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = {"app.cache.l1-ttl=1s", "app.cache.l2-ttl=2s"})
class LinkServiceCacheIT {

    @Autowired
    private LinkService linkService;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private LinkRepository linkRepository;

    @MockitoBean
    private ChatRepository chatRepository;

    private final URI TEST_URI = URI.create("https://github.com/test");

    @BeforeEach
    void setUp() {
        if (cacheManager.getCache("links") != null) {
            cacheManager.getCache("links").clear();
        }
    }

    @Test
    @DisplayName("Должен возвращать данные из кэша при повторных запросах (без обращения к БД)")
    void getLinks_shouldCacheResult() {
        Long chatId = 111L;

        when(chatRepository.existsById(eq(chatId))).thenReturn(true);
        when(linkRepository.findAllByChatId(eq(chatId), anyInt(), anyInt()))
                .thenReturn(List.of(new LinkData(1L, TEST_URI, OffsetDateTime.now(), List.of(), List.of())));

        linkService.getLinks(chatId);
        linkService.getLinks(chatId);
        linkService.getLinks(chatId);

        verify(linkRepository, times(1)).findAllByChatId(eq(chatId), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Должен инвалидировать (удалять) кэш при добавлении новой ссылки")
    void addLink_shouldEvictCache() {
        Long chatId = 222L;

        when(chatRepository.existsById(eq(chatId))).thenReturn(true);
        when(linkRepository.findAllByChatId(eq(chatId), anyInt(), anyInt())).thenReturn(List.of());
        when(linkRepository.isLinkedToChat(eq(chatId), eq(TEST_URI))).thenReturn(false);
        when(linkRepository.addLinkToChat(eq(chatId), eq(TEST_URI), anyList()))
                .thenReturn(new LinkData(1L, TEST_URI, OffsetDateTime.now(), List.of(), List.of()));

        linkService.getLinks(chatId);
        linkService.getLinks(chatId);

        linkService.addLink(chatId, new AddLinkRequest().link(TEST_URI).tags(List.of()));

        linkService.getLinks(chatId);

        verify(linkRepository, times(2)).findAllByChatId(eq(chatId), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Должен удалять данные из кэша при удалении ссылки")
    void removeLink_shouldEvictCache() {
        Long chatId = 333L;

        when(chatRepository.existsById(eq(chatId))).thenReturn(true);
        when(linkRepository.isLinkedToChat(eq(chatId), eq(TEST_URI))).thenReturn(true);
        when(linkRepository.findAllByChatId(eq(chatId), anyInt(), anyInt())).thenReturn(List.of());

        linkService.getLinks(chatId);
        linkService.getLinks(chatId);

        linkService.removeLink(chatId, TEST_URI);

        linkService.getLinks(chatId);

        verify(linkRepository, times(2)).findAllByChatId(eq(chatId), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Кэши разных пользователей изолированы друг от друга")
    void cache_shouldBeIsolatedPerUser() {
        Long userA = 400L;
        Long userB = 500L;

        when(chatRepository.existsById(eq(userA))).thenReturn(true);
        when(chatRepository.existsById(eq(userB))).thenReturn(true);

        when(linkRepository.findAllByChatId(eq(userA), anyInt(), anyInt())).thenReturn(List.of());
        when(linkRepository.findAllByChatId(eq(userB), anyInt(), anyInt())).thenReturn(List.of());

        linkService.getLinks(userA);
        linkService.getLinks(userB);
        linkService.getLinks(userA);

        verify(linkRepository, times(1)).findAllByChatId(eq(userA), anyInt(), anyInt());
        verify(linkRepository, times(1)).findAllByChatId(eq(userB), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Инвалидация кэша одного пользователя не затрагивает кэш другого")
    void cacheEvict_shouldNotAffectOtherUsers() {
        Long userA = 600L;
        Long userB = 700L;

        when(chatRepository.existsById(eq(userA))).thenReturn(true);
        when(chatRepository.existsById(eq(userB))).thenReturn(true);

        when(linkRepository.findAllByChatId(eq(userA), anyInt(), anyInt())).thenReturn(List.of());
        when(linkRepository.findAllByChatId(eq(userB), anyInt(), anyInt())).thenReturn(List.of());

        when(linkRepository.isLinkedToChat(eq(userA), eq(TEST_URI))).thenReturn(false);
        when(linkRepository.addLinkToChat(eq(userA), eq(TEST_URI), anyList()))
                .thenReturn(new LinkData(1L, TEST_URI, OffsetDateTime.now(), List.of(), List.of()));

        linkService.getLinks(userA);
        linkService.getLinks(userB);

        linkService.addLink(userA, new AddLinkRequest().link(TEST_URI).tags(List.of()));

        linkService.getLinks(userA);
        linkService.getLinks(userB);

        verify(linkRepository, times(2)).findAllByChatId(eq(userA), anyInt(), anyInt());
        verify(linkRepository, times(1)).findAllByChatId(eq(userB), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Реальная сериализация: данные в кэше должны сохраняться и восстанавливаться в JSON (без RCE)")
    void getLinks_shouldStoreCorrectDataStructure() {
        Long chatId = 888L;

        when(chatRepository.existsById(eq(chatId))).thenReturn(true);
        when(linkRepository.findAllByChatId(eq(chatId), anyInt(), anyInt())).thenReturn(List.of());

        linkService.getLinks(chatId);

        var cache = cacheManager.getCache("links");
        var cachedValue = cache.get(chatId).get();

        assertNotNull(cachedValue, "Значение должно лежать в кэше");
        assertInstanceOf(ListLinksResponse.class, cachedValue, "Объект должен корректно десериализоваться из JSON");
    }

    @Test
    @DisplayName("После истечения TTL данные удаляются, и сервис снова идет в БД")
    void getLinks_shouldExpireAfterTtl() throws InterruptedException {
        Long chatId = 999L;

        when(chatRepository.existsById(eq(chatId))).thenReturn(true);
        when(linkRepository.findAllByChatId(eq(chatId), anyInt(), anyInt())).thenReturn(List.of());

        linkService.getLinks(chatId);

        Thread.sleep(2500);

        linkService.getLinks(chatId);

        verify(linkRepository, times(2)).findAllByChatId(eq(chatId), anyInt(), anyInt());
    }
}
