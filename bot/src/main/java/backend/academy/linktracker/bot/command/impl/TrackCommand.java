package backend.academy.linktracker.bot.command.impl;

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

@Slf4j
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

        if (userState == UserState.IDLE) {
            startTracking(chatId);
        } else {
            log.atWarn()
                    .addKeyValue("chatId", chatId)
                    .addKeyValue("state", userState)
                    .log("Unexpected user state during /track command");

            sender.sendMessage(chatId, messages.getTrack().getAlreadyInProcess());
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
