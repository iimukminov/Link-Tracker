package backend.academy.linktracker.bot.command.impl;

import backend.academy.linktracker.bot.command.Command;
import backend.academy.linktracker.bot.constants.BotCommandType;
import backend.academy.linktracker.bot.constants.UserState;
import backend.academy.linktracker.bot.properties.BotMessages;
import backend.academy.linktracker.bot.sender.TelegramSender;
import backend.academy.linktracker.bot.service.UserStateService;
import com.pengrad.telegrambot.model.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrackCommand implements Command {
    private final TelegramSender sender;
    private final BotMessages messages;
    private final UserStateService userStateService;

    @Override
    public void execute(Message message) {
        long chatId = message.chat().id();
        UserState userState = userStateService.getState(chatId);

        switch (userState) {
            case IDLE:
                startTracking(chatId);
                break;
            default:
                break;
        }
    }

    private void startTracking(long chatId) {
        userStateService.setState(chatId, UserState.AWAITING_LINK);
        sender.sendMessage(chatId, messages.getTrack().getLinkRequest());
    }

    @Override
    public String getName() {
        return BotCommandType.TRACK.getName();
    }

    @Override
    public String getDescription() {
        return BotCommandType.TRACK.getDescription();
    }
}
