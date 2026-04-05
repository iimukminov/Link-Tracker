package backend.academy.linktracker.scrapper.service.sender;

import backend.academy.linktracker.bot.dto.LinkUpdate;

public interface MessageSender {
    void send(LinkUpdate update);
}
