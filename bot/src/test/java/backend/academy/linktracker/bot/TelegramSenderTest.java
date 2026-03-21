package backend.academy.linktracker.bot;

import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.client.TelegramSender;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.response.SendResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
