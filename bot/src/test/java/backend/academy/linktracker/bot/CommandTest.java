package backend.academy.linktracker.bot;

import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.command.impl.*;
import backend.academy.linktracker.bot.properties.BotMessages;
import backend.academy.linktracker.bot.sender.TelegramSender;
import backend.academy.linktracker.bot.service.ScrapperClient;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommandTest {

    @Mock
    TelegramSender sender;

    @Mock
    BotMessages messages;

    @Mock
    Update update;

    @Mock
    Message message;

    @Mock
    Chat chat;

    @Mock
    ScrapperClient scrapperClient;

    private final long chatId = 123456L;

    @BeforeEach
    void setUp() {
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
    }

    @Test
    void startCommand_positive() {
        String expectedText = "mocked_start_message";
        when(messages.getStart()).thenReturn(expectedText);

        StartCommand command = new StartCommand(sender, messages, scrapperClient);

        command.execute(update.message());

        verify(sender).sendMessage(eq(chatId), eq(expectedText));
        verify(scrapperClient).registerChat(eq(chatId));
    }

    @Test
    void helpCommand_positive() {
        String expectedText = "mocked_help_message";
        when(messages.getHelp()).thenReturn(expectedText);
        HelpCommand command = new HelpCommand(sender, messages);

        command.execute(update.message());

        verify(sender).sendMessage(eq(chatId), eq(expectedText));
    }

    @Test
    void wrongCommand_negative() {
        String expectedText = "mocked_wrong_message";
        when(messages.getWrong()).thenReturn(expectedText);
        WrongCommand command = new WrongCommand(sender, messages);

        command.execute(update.message());

        verify(sender).sendMessage(eq(chatId), eq(expectedText));
    }
}
