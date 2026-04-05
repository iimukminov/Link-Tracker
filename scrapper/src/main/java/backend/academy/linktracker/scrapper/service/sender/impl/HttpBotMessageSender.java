package backend.academy.linktracker.scrapper.service.sender.impl;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.client.BotClient;
import backend.academy.linktracker.scrapper.service.sender.MessageSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HttpBotMessageSender implements MessageSender {

    private final BotClient botClient;

    @Override
    public void send(LinkUpdate update) {
        botClient.sendUpdate(update);
    }
}
