package backend.academy.linktracker.bot.command.impl;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.client.TelegramSender;
import backend.academy.linktracker.bot.command.Command;
import backend.academy.linktracker.bot.constants.BotCommandType;
import backend.academy.linktracker.bot.constants.UserState;
import backend.academy.linktracker.bot.properties.BotMessages;
import backend.academy.linktracker.bot.service.UserStateService;
import backend.academy.linktracker.scrapper.dto.RemoveLinkRequest;
import com.pengrad.telegrambot.model.Message;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

@Component
@RequiredArgsConstructor
@Slf4j
public class UntrackCommand implements Command {
    private final TelegramSender sender;
    private final BotMessages messages;
    private final ScrapperClient scrapperClient;
    private final UserStateService userStateService;

    @Override
    public void execute(Message message) {
        long chatId = message.chat().id();

        userStateService.setState(chatId, UserState.IDLE);
        userStateService.clearTempData(chatId);

        String text = message.text();
        String[] parts = text.split("\\s+");

        if (parts.length < 2) {
            sender.sendMessage(chatId, messages.getUntrack().getUsage());
            return;
        }

        String linkStr = parts[1];

        try {
            RemoveLinkRequest request = new RemoveLinkRequest().link(URI.create(linkStr));
            scrapperClient.removeLink(chatId, request);

            sender.sendMessage(chatId, messages.getUntrack().getSuccess());

        } catch (IllegalArgumentException e) {
            log.atWarn().addKeyValue("chatId", chatId).log("Invalid URI format provided for untrack");
            sender.sendMessage(chatId, messages.getUntrack().getInvalidFormat());

        } catch (HttpClientErrorException.NotFound e) {
            log.atInfo()
                    .addKeyValue("chatId", chatId)
                    .addKeyValue("link", linkStr)
                    .log("User tried to untrack a non-existent link");
            sender.sendMessage(chatId, messages.getUntrack().getNotFound());

        } catch (Exception e) {
            log.atError().setMessage("Error removing link").setCause(e).log();
            sender.sendMessage(chatId, messages.getUntrack().getError());
        }
    }

    @Override
    public String getName() {
        return BotCommandType.UNTRACK.getName();
    }

    @Override
    public String getDescription() {
        return BotCommandType.UNTRACK.getDescription();
    }
}
