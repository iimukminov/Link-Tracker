package backend.academy.linktracker.bot.handlers.impl;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.client.TelegramSender;
import backend.academy.linktracker.bot.constants.UserState;
import backend.academy.linktracker.bot.handlers.StateHandler;
import backend.academy.linktracker.bot.properties.BotMessages;
import backend.academy.linktracker.bot.service.UserStateService;
import backend.academy.linktracker.scrapper.dto.AddLinkRequest; // Поменяли импорт на контракты скраппера
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
            telegramSender.sendMessage(chatId, messages.getTrack().getLostLink());
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
            AddLinkRequest request =
                    new AddLinkRequest().link(URI.create(linkStr)).tags(tags).filters(List.of());

            scrapperClient.addLink(chatId, request);

            telegramSender.sendMessage(chatId, messages.getTrack().getSuccess());

        } catch (HttpClientErrorException.Conflict e) {
            log.atInfo()
                    .addKeyValue("chatId", chatId)
                    .addKeyValue("link", linkStr)
                    .log("User tried to track an already tracked link");
            telegramSender.sendMessage(chatId, messages.getTrack().getAlreadyTracked());
        } catch (Exception e) {
            log.atError().setMessage("Error adding link").setCause(e).log();
            telegramSender.sendMessage(chatId, messages.getTrack().getError());
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
