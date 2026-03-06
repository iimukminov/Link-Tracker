package backend.academy.linktracker.bot;

import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.command.Command;
import backend.academy.linktracker.bot.command.CommandRegistry;
import backend.academy.linktracker.bot.handlers.MessageHandler;
import com.pengrad.telegrambot.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MessageHandlerTest {

    @Mock
    CommandRegistry registry;

    @InjectMocks
    MessageHandler handler;

    @Test
    void testStartCommand_positive() {
        Message message = createMessage("/start", 123456L);
        Command startCommand = mock(Command.class);
        when(registry.getCommand("/start")).thenReturn(startCommand);

        handler.handle(message);

        verify(registry).getCommand("/start");
        verify(startCommand).execute(message);
    }

    @Test
    void testHelpCommand_positive() {
        Message message = createMessage("/help", 123456L);
        Command helpCommand = mock(Command.class);
        when(registry.getCommand("/help")).thenReturn(helpCommand);

        handler.handle(message);

        verify(helpCommand).execute(message);
    }

    @Test
    void testUnknownCommand_negative() {
        Message message = createMessage("/unknown", 123456L);
        Command wrongCommand = mock(Command.class);
        when(registry.getCommand("/unknown")).thenReturn(wrongCommand);

        handler.handle(message);

        verify(wrongCommand).execute(message);
    }

    @Test
    void testNoText() {
        Message message = mock(Message.class);
        when(message.text()).thenReturn(null);

        handler.handle(message);

        verifyNoInteractions(registry);
    }

    private Message createMessage(String text, long chatId) {
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(message.text()).thenReturn(text);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);

        return message;
    }
}
