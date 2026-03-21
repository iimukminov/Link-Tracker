package backend.academy.linktracker.bot.command.impl;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.client.TelegramSender;
import backend.academy.linktracker.bot.command.Command;
import backend.academy.linktracker.bot.constants.BotCommandType;
import backend.academy.linktracker.bot.constants.UserState;
import backend.academy.linktracker.bot.properties.BotMessages;
import backend.academy.linktracker.bot.service.UserStateService;
import backend.academy.linktracker.scrapper.dto.LinkResponse; // Правильный импорт из контракта скраппера
import backend.academy.linktracker.scrapper.dto.ListLinksResponse; // Правильный импорт из контракта скраппера
import com.pengrad.telegrambot.model.Message;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ListCommand implements Command {
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
        String filterTag = parts.length > 1 ? parts[1] : null;

        try {
            ListLinksResponse response = scrapperClient.getLinks(chatId);
            List<LinkResponse> links = response.getLinks();

            if (filterTag != null && links != null) {
                links = links.stream()
                        .filter(link -> link.getTags() != null && link.getTags().contains(filterTag))
                        .toList();
            }

            if (links == null || links.isEmpty()) {
                sender.sendMessage(chatId, messages.getListMsg().getEmpty());
                return;
            }

            StringBuilder sb = new StringBuilder(messages.getListMsg().getTitle()).append("\n\n");
            int index = 1;
            for (LinkResponse link : links) {
                sb.append(index++).append(". ").append(link.getUrl());

                if (link.getTags() != null && !link.getTags().isEmpty()) {
                    sb.append(" [").append(String.join(", ", link.getTags())).append("]");
                }
                sb.append("\n");
            }

            sender.sendMessage(chatId, sb.toString());

        } catch (Exception e) {
            log.atError()
                    .setMessage("Error fetching links")
                    .addKeyValue("chatId", chatId)
                    .setCause(e)
                    .log();
            sender.sendMessage(chatId, messages.getListMsg().getError());
        }
    }

    @Override
    public String getName() {
        return BotCommandType.LIST.getName();
    }

    @Override
    public String getDescription() {
        return BotCommandType.LIST.getDescription();
    }
}
