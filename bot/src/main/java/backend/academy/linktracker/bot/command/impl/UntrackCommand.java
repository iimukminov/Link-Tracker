package backend.academy.linktracker.bot.command.impl;

import backend.academy.linktracker.bot.command.Command;
import backend.academy.linktracker.bot.constants.BotCommandType;
import backend.academy.linktracker.bot.dto.RemoveLinkRequest;
import backend.academy.linktracker.bot.properties.BotMessages;
import backend.academy.linktracker.bot.sender.TelegramSender;
import backend.academy.linktracker.bot.service.ScrapperClient;
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

    @Override
    public void execute(Message message) {
        long chatId = message.chat().id();
        String text = message.text();

        String[] parts = text.split("\\s+");

        if (parts.length < 2) {
            sender.sendMessage(
                    chatId, "Пожалуйста, укажите ссылку для удаления. Пример:\n/untrack https://github.com/user/repo");
            return;
        }

        String linkStr = parts[1];

        try {
            RemoveLinkRequest request = new RemoveLinkRequest(URI.create(linkStr));
            scrapperClient.removeLink(chatId, request);

            sender.sendMessage(chatId, messages.getUntrack().getSuccess());

        } catch (HttpClientErrorException.NotFound e) {
            sender.sendMessage(chatId, messages.getUntrack().getNotFound());
        } catch (Exception e) {
            log.atError().setMessage("Error removing link").setCause(e).log();
            sender.sendMessage(chatId, "Произошла ошибка при удалении ссылки.");
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
