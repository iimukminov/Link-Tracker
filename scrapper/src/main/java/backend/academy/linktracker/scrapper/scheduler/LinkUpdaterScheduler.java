package backend.academy.linktracker.scrapper.scheduler;

import backend.academy.linktracker.scrapper.handler.LinkHandler;
import backend.academy.linktracker.scrapper.model.LinkData;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkUpdaterScheduler {

    private final LinkRepository linkRepository;
    private final ChatRepository chatRepository;
    private final List<LinkHandler> linkHandlers;

    @Scheduled(fixedDelayString = "#{@schedulerProperties.interval.toMillis()}")
    public void update() {
        log.atInfo().log("Updating links");

        OffsetDateTime thresholdTime = OffsetDateTime.now().minusMinutes(10);
        List<LinkData> linksToUpdate = linkRepository.findLinksToUpdate(thresholdTime, 50);

        for (LinkData linkData : linksToUpdate) {
            String host = linkData.getUrl().getHost();
            if (host == null) continue;

            for (LinkHandler linkHandler : linkHandlers) {
                if (linkHandler.supports(host)) {
                    List<Long> chatIds = chatRepository.findAllByLinkId(linkData.getId());

                    if (!chatIds.isEmpty()) {
                        linkHandler.handle(chatIds, linkData);
                    }
                    break;
                }
            }
            linkRepository.updateLastUpdateTime(linkData.getId(), OffsetDateTime.now());
        }
    }
}
