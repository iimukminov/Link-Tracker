package backend.academy.linktracker.bot;

import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.command.impl.*;
import backend.academy.linktracker.bot.sender.TelegramSender;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommandTest {

    @Mock
    TelegramSender sender;

    @Mock
    Update update;

    @Test
    void startCommand_positive() {
        StartCommand command = new StartCommand(sender);
        long chatId = 123456L;
        when(update.message()).thenReturn(mock(Message.class));
        when(update.message().chat()).thenReturn(mock(Chat.class));
        when(update.message().chat().id()).thenReturn(chatId);

        command.execute(update);

        verify(sender).sendMessage(eq(chatId), eq("Добро пожаловать! Используйте /help для списка команд"));
    }

    @Test
    void helpCommand_positive() {
        HelpCommand command = new HelpCommand(sender);
        long chatId = 123456L;
        when(update.message()).thenReturn(mock(Message.class));
        when(update.message().chat()).thenReturn(mock(Chat.class));
        when(update.message().chat().id()).thenReturn(chatId);

        command.execute(update);

        verify(sender).sendMessage(eq(chatId), contains("Список доступных команд"));
    }

    @Test
    void wrongCommand_negative() {
        WrongCommand command = new WrongCommand(sender);
        long chatId = 123456L;
        when(update.message()).thenReturn(mock(Message.class));
        when(update.message().chat()).thenReturn(mock(Chat.class));
        when(update.message().chat().id()).thenReturn(chatId);

        command.execute(update);

        verify(sender).sendMessage(eq(chatId), eq("Неизвестная команда. Воспользуйтесь /help"));
    }
}
