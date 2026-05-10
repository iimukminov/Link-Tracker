package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.model.LinkData;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.service.sender.MessageSender;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LinkUpdateDbService {

    private final MessageSender messageSender;
    private final LinkRepository linkRepository;

    @Transactional
    public void saveUpdates(List<LinkUpdate> linkUpdates, LinkData linkData) {
        for (LinkUpdate linkUpdate : linkUpdates) {
            messageSender.send(linkUpdate);
        }

        linkRepository.updateLastUpdateTime(linkData.getId(), linkData.getLastUpdate());
    }
}
