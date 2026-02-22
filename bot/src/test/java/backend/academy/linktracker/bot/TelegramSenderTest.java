package backend.academy.linktracker.bot;

import backend.academy.linktracker.bot.sender.TelegramSender;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelegramSenderTest {

    @Mock
    TelegramBot bot;

    @InjectMocks
    TelegramSender sender;

    @Test
    void testSendMessage_success() {

        long chatId = 123456L;
        String text = "test message";
        SendResponse response = mock(SendResponse.class);
        when(response.isOk()).thenReturn(true);


        when(bot.execute(any())).thenReturn(response);
        sender.sendMessage(chatId, text);


        verify(bot).execute(any());
    }

}
