package backend.academy.linktracker.bot.command.impl;

import backend.academy.linktracker.bot.command.Command;
import backend.academy.linktracker.bot.constants.BotCommandType;
import backend.academy.linktracker.bot.properties.BotMessages;
import backend.academy.linktracker.bot.sender.TelegramSender;
import backend.academy.linktracker.bot.service.ScrapperClient;
import com.pengrad.telegrambot.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StartCommand implements Command {
    private final TelegramSender sender;
    private final BotMessages messages;
    private final ScrapperClient scrapperClient;

    @Override
    public void execute(Message message) {
        long chatId = message.chat().id();

        try {
            scrapperClient.registerChat(chatId);
        } catch (Exception e) {
            log.atWarn()
                    .setMessage("Chat already registered or error")
                    .setCause(e)
                    .log();
        }

        sender.sendMessage(chatId, messages.getStart());
    }

    @Override
    public String getName() {
        return BotCommandType.START.getName();
    }

    @Override
    public String getDescription() {
        return BotCommandType.START.getDescription();
    }
}
