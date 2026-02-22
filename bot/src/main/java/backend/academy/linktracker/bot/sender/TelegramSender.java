package backend.academy.linktracker.bot.sender;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
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

        BaseResponse response = bot.execute(new SendMessage(chatId, text));

        log.atInfo()
            .setMessage("Message sent")
            .addKeyValue("chatId", chatId)
            .addKeyValue("text", text)
            .addKeyValue("success", response.isOk())
            .addKeyValue("errorCode", response.errorCode())
            .log();
    }
}
