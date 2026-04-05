package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.handler.LinkHandler;
import backend.academy.linktracker.scrapper.model.LinkData;
import backend.academy.linktracker.scrapper.properties.SchedulerProperties;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkUpdateService {

    private final LinkRepository linkRepository;
    private final ChatRepository chatRepository;
    private final List<LinkHandler> linkHandlers;
    private final SchedulerProperties schedulerProperties;

    @Transactional
    public void updateLinks() {
        OffsetDateTime thresholdTime = OffsetDateTime.now().minus(schedulerProperties.getForceCheckDelay());
        List<LinkData> linksToUpdate =
                linkRepository.findLinksToUpdate(thresholdTime, schedulerProperties.getBatchSize());

        for (LinkData linkData : linksToUpdate) {
            processLink(linkData);
            linkRepository.updateLastUpdateTime(linkData.getId(), OffsetDateTime.now());
        }
    }

    private void processLink(LinkData linkData) {
        String host = linkData.getUrl().getHost();
        if (host == null) return;

        for (LinkHandler linkHandler : linkHandlers) {
            if (linkHandler.supports(host)) {
                List<Long> chatIds = chatRepository.findAllByLinkId(linkData.getId());
                if (!chatIds.isEmpty()) {
                    linkHandler.handle(chatIds, linkData);
                }
                return;
            }
        }
    }
}
