package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.handler.LinkHandler;
import backend.academy.linktracker.scrapper.model.LinkData;
import backend.academy.linktracker.scrapper.properties.SchedulerProperties;
import backend.academy.linktracker.scrapper.properties.ScrapperMessages;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.service.sender.MessageSender;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkUpdateService {

    private final LinkRepository linkRepository;
    private final ChatRepository chatRepository;
    private final List<LinkHandler> linkHandlers;
    private final SchedulerProperties schedulerProperties;
    private final ExecutorService linkUpdateExecutor;
    private final MessageSender messageSender;
    private final ScrapperMessages scrapperMessages;

    public void updateLinks() {
        OffsetDateTime thresholdTime = OffsetDateTime.now().minus(schedulerProperties.getForceCheckDelay());
        List<LinkData> linksToUpdate =
                linkRepository.findLinksToUpdate(thresholdTime, schedulerProperties.getBatchSize());

        if (linksToUpdate.isEmpty()) return;

        int threads = schedulerProperties.getThreadsCount();
        int totalLinks = linksToUpdate.size();

        int chunkSize = (int) Math.ceil((double) totalLinks / threads);

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < totalLinks; i += chunkSize) {
            int end = Math.min(i + chunkSize, totalLinks);

            List<LinkData> batchPart = linksToUpdate.subList(i, end);

            CompletableFuture<Void> future = CompletableFuture.runAsync(
                    () -> {
                        for (LinkData link : batchPart) {
                            processLink(link);
                        }
                    },
                    linkUpdateExecutor);

            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private void processLink(LinkData linkData) {
        try {
            String host = linkData.getUrl().getHost();
            if (host == null) return;

            boolean isHandled = false;
            for (LinkHandler linkHandler : linkHandlers) {
                if (linkHandler.supports(host)) {
                    List<Long> chatIds = chatRepository.findAllByLinkId(linkData.getId());
                    if (!chatIds.isEmpty()) {
                        linkHandler.handle(chatIds, linkData);
                        linkRepository.updateLastUpdateTime(linkData.getId(), linkData.getLastUpdate());
                    }
                    isHandled = true;
                    break;
                }
            }

            if (!isHandled) {
                log.atWarn().addKeyValue("host", host).log("No handler found for host");
            }

        } catch (Exception e) {
            log.atError().addKeyValue("url", linkData.getUrl()).setCause(e).log("Failed to process link");
            reportError(linkData);
        } finally {
            linkRepository.updateLastCheckTime(linkData.getId(), OffsetDateTime.now());
        }
    }

    private void reportError(LinkData linkData) {
        List<Long> chatIds = chatRepository.findAllByLinkId(linkData.getId());
        if (!chatIds.isEmpty()) {
            messageSender.send(new LinkUpdate()
                    .id(linkData.getId())
                    .url(linkData.getUrl())
                    .description(scrapperMessages.getErrors().getProcessingError())
                    .tgChatIds(chatIds));
        }
    }
}
