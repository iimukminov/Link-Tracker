package backend.academy.linktracker.bot.handlers.impl;

import backend.academy.linktracker.bot.constants.UserState;
import backend.academy.linktracker.bot.dto.AddLinkRequest;
import backend.academy.linktracker.bot.handlers.StateHandler;
import backend.academy.linktracker.bot.properties.BotMessages;
import backend.academy.linktracker.bot.sender.TelegramSender;
import backend.academy.linktracker.bot.service.ScrapperClient;
import backend.academy.linktracker.bot.service.UserStateService;
import com.pengrad.telegrambot.model.Message;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

@Component
@RequiredArgsConstructor
@Slf4j
public class AwaitingTagsHandler implements StateHandler {

    private final UserStateService userStateService;
    private final TelegramSender telegramSender;
    private final BotMessages messages;
    private final ScrapperClient scrapperClient;

    @Override
    public void handle(Message message) {
        long chatId = message.chat().id();
        String text = message.text();

        String linkStr = userStateService.getTempData(chatId, "link");
        if (linkStr == null) {
            telegramSender.sendMessage(chatId, "Произошла ошибка, ссылка потерялась. Начните сначала: /track");
            userStateService.setState(chatId, UserState.IDLE);
            return;
        }

        List<String> tags = List.of();
        if (text != null && !text.isBlank()) {
            tags = Arrays.stream(text.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }

        try {
            AddLinkRequest request = new AddLinkRequest(URI.create(linkStr), tags, List.of());
            scrapperClient.addLink(chatId, request);

            telegramSender.sendMessage(chatId, messages.getTrack().getSuccess());

        } catch (HttpClientErrorException.Conflict e) {
            telegramSender.sendMessage(chatId, messages.getTrack().getAlreadyTracked());
        } catch (Exception e) {
            log.atError().setMessage("Error adding link").setCause(e).log();
            telegramSender.sendMessage(chatId, "Произошла ошибка при сохранении ссылки на сервере.");
        } finally {
            userStateService.setState(chatId, UserState.IDLE);
            userStateService.clearTempData(chatId);
        }
    }

    @Override
    public UserState getSupportedState() {
        return UserState.AWAITING_TAGS;
    }
}
