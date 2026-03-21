package backend.academy.linktracker.bot.client;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramSender {

    private final TelegramBot bot;

    public void sendMessage(long chatId, String text) {
        log.atInfo()
                .setMessage("Sending message")
                .addKeyValue("chatId", chatId)
                .addKeyValue("text", text)
                .log();

        BaseResponse response = bot.execute(new SendMessage(chatId, text).parseMode(ParseMode.HTML));

        if (!response.isOk()) {
            log.atError()
                    .setMessage("Failed to send message")
                    .addKeyValue("chatId", chatId)
                    .addKeyValue("errorCode", response.errorCode())
                    .addKeyValue("description", response.description())
                    .log();
        } else {
            log.atInfo()
                    .setMessage("Message sent")
                    .addKeyValue("chatId", chatId)
                    .addKeyValue("text", text)
                    .log();
        }
    }

    public void setMyCommands(BotCommand[] botCommands) {
        SetMyCommands setMyCommands = new SetMyCommands(botCommands);
        bot.execute(setMyCommands);
    }
}
