package backend.academy.linktracker.bot.command.impl;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.client.TelegramSender;
import backend.academy.linktracker.bot.command.Command;
import backend.academy.linktracker.bot.constants.BotCommandType;
import backend.academy.linktracker.bot.constants.UserState;
import backend.academy.linktracker.bot.properties.BotMessages;
import backend.academy.linktracker.bot.service.UserStateService;
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
    private final UserStateService userStateService;

    @Override
    public void execute(Message message) {
        long chatId = message.chat().id();

        userStateService.setState(chatId, UserState.IDLE);
        userStateService.clearTempData(chatId);

        try {
            scrapperClient.registerChat(chatId);
        } catch (Exception e) {
            log.atWarn()
                    .setMessage("Chat already registered or error")
                    .addKeyValue("chatId", chatId)
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
