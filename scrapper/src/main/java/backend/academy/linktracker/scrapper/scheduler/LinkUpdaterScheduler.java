package backend.academy.linktracker.scrapper.scheduler;

import backend.academy.linktracker.scrapper.handler.LinkHandler;
import backend.academy.linktracker.scrapper.model.LinkData;
import backend.academy.linktracker.scrapper.repository.InMemoryScrapperRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkUpdaterScheduler {
    private final InMemoryScrapperRepository repository;
    private final List<LinkHandler> linkHandlers;

    @Scheduled(fixedDelayString = "#{@schedulerProperties.interval.toMillis()}")
    public void update() {
        log.atInfo().log("Updating links");
        Map<Long, Set<LinkData>> links = repository.findAll();

        Map<String, List<Map.Entry<Long, LinkData>>> groupedByUrl = new HashMap<>();

        for (Map.Entry<Long, Set<LinkData>> entry : links.entrySet()) {
            Long chatId = entry.getKey();
            for (LinkData link : entry.getValue()) {
                groupedByUrl
                        .computeIfAbsent(link.getUrl().toString(), k -> new ArrayList<>())
                        .add(Map.entry(chatId, link));
            }
        }

        for (List<Map.Entry<Long, LinkData>> entries : groupedByUrl.values()) {
            LinkData linkData = entries.get(0).getValue();
            List<Long> chatIds = entries.stream().map(Map.Entry::getKey).toList();
            processLink(chatIds, linkData);
        }
    }

    private void processLink(List<Long> chatIds, LinkData linkData) {
        String host = linkData.getUrl().getHost();
        if (host == null) {
            return;
        }

        for (LinkHandler linkHandler : linkHandlers) {
            if (linkHandler.supports(host)) {
                linkHandler.handle(chatIds, linkData);
                break;
            }
        }
    }
}
