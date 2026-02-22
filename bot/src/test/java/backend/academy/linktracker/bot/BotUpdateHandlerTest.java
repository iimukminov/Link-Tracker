package backend.academy.linktracker.bot;

import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.command.Command;
import backend.academy.linktracker.bot.command.CommandRegistry;
import backend.academy.linktracker.bot.sender.TelegramSender;
import backend.academy.linktracker.bot.service.BotUpdateHandler;
import com.pengrad.telegrambot.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BotUpdateHandlerTest {

    @Mock
    CommandRegistry registry;

    @Mock
    TelegramSender sender;

    @InjectMocks
    BotUpdateHandler handler;

    @Test
    void testStartCommand_positive() {

        Update update = createUpdate("/start", 123456L);
        Command startCommand = mock(Command.class);
        when(registry.getCommand("/start")).thenReturn(startCommand);

        handler.processUpdate(update);

        verify(registry).getCommand("/start");
        verify(startCommand).execute(update);
    }

    @Test
    void testHelpCommand_positive() {
        Update update = createUpdate("/help", 123456L);
        Command helpCommand = mock(Command.class);
        when(registry.getCommand("/help")).thenReturn(helpCommand);

        handler.processUpdate(update);

        verify(helpCommand).execute(update);
    }

    @Test
    void testUnknownCommand_negative() {
        Update update = createUpdate("/unknown", 123456L);
        Command wrongCommand = mock(Command.class);
        when(registry.getCommand("/unknown")).thenReturn(wrongCommand);

        handler.processUpdate(update);

        verify(wrongCommand).execute(update);
    }

    @Test
    void testNoMessage() {
        Update update = mock(Update.class);
        when(update.message()).thenReturn(null);

        handler.processUpdate(update);

        verifyNoInteractions(registry);
    }

    private Update createUpdate(String text, long chatId) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn(text);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);

        return update;
    }
}
