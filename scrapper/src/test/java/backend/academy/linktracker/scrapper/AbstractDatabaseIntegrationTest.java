package backend.academy.linktracker.scrapper;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.linktracker.scrapper.dto.AddLinkRequest;
import backend.academy.linktracker.scrapper.exceptions.ChatNotFoundException;
import backend.academy.linktracker.scrapper.exceptions.LinkAlreadyTrackedException;
import backend.academy.linktracker.scrapper.exceptions.LinkNotFoundException;
import backend.academy.linktracker.scrapper.service.LinkService;
import backend.academy.linktracker.scrapper.service.TagService;
import backend.academy.linktracker.scrapper.service.TgChatService;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
public abstract class AbstractDatabaseIntegrationTest {

    @Autowired
    protected LinkService linkService;

    @Autowired
    protected TgChatService tgChatService;

    @Autowired
    protected TagService tagService;

    @Test
    void shouldRegisterChatAndAddLink() {
        Long chatId = 12345L;
        tgChatService.registerChat(chatId);
        AddLinkRequest request = new AddLinkRequest();
        request.setLink(URI.create("https://github.com/spring-projects/spring-boot"));
        request.setTags(List.of("java", "spring"));

        var response = linkService.addLink(chatId, request);

        assertNotNull(response.getId(), "ID ссылки не должен быть null");
        assertEquals(
                "https://github.com/spring-projects/spring-boot",
                response.getUrl().toString());

        var links = linkService.getLinks(chatId);
        assertEquals(1, links.getSize(), "Должна сохраниться одна ссылка");
    }

    @Test
    void shouldThrowExceptionWhenAddingDuplicateLink() {
        Long chatId = 10L;
        tgChatService.registerChat(chatId);
        URI url = URI.create("https://github.com/duplicate/repo");
        AddLinkRequest request = new AddLinkRequest();
        request.setLink(url);

        linkService.addLink(chatId, request);

        assertThrows(
                LinkAlreadyTrackedException.class,
                () -> linkService.addLink(chatId, request),
                "Ожидается ошибка при добавлении дублирующей ссылки");
    }

    @Test
    void shouldRemoveLinkSuccessfully() {
        Long chatId = 20L;
        tgChatService.registerChat(chatId);
        URI url = URI.create("https://stackoverflow.com/questions/12345");
        AddLinkRequest request = new AddLinkRequest();
        request.setLink(url);
        linkService.addLink(chatId, request);

        var response = linkService.removeLink(chatId, url);
        assertEquals(url, response.getUrl());

        var links = linkService.getLinks(chatId);
        assertEquals(0, links.getSize(), "Ссылка должна отсутствовать в БД");
    }

    @Test
    void shouldThrowExceptionWhenRemovingNonExistentLink() {
        Long chatId = 30L;
        tgChatService.registerChat(chatId);
        URI url = URI.create("https://github.com/not/found");

        assertThrows(
                LinkNotFoundException.class,
                () -> linkService.removeLink(chatId, url),
                "Удаление неотслеживаемой ссылки должно вызывать ошибку");
    }

    @Test
    void shouldThrowExceptionWhenAddingLinkToNonExistentChat() {
        Long unassociatedChatId = 999L;
        AddLinkRequest request = new AddLinkRequest();
        request.setLink(URI.create("https://github.com/test/repo"));

        assertThrows(
                ChatNotFoundException.class,
                () -> linkService.addLink(unassociatedChatId, request),
                "Добавление ссылки несуществующему чату должно вызывать ошибку");
    }

    @Test
    void shouldPerformCrudOperationsOnTags() {
        String tagName = "test-tag";

        tagService.create(tagName);
        List<String> tags = tagService.findAll();
        assertTrue(tags.contains(tagName), "Тег должен быть сохранен в БД");

        String newTagName = "renamed-test-tag";
        tagService.rename(tagName, newTagName);
        tags = tagService.findAll();
        assertFalse(tags.contains(tagName), "Старое имя тега должно исчезнуть");
        assertTrue(tags.contains(newTagName), "Новое имя тега должно появиться");

        tagService.deleteByName(newTagName);
        tags = tagService.findAll();
        assertFalse(tags.contains(newTagName), "Тег должен быть удален из БД");
    }
}
