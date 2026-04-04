package backend.academy.linktracker.scrapper.scheduler;

import backend.academy.linktracker.scrapper.service.LinkUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LinkUpdaterScheduler {

    private final LinkUpdateService linkUpdateService;

    @Scheduled(fixedDelayString = "#{@schedulerProperties.interval.toMillis()}")
    public void update() {
        log.info("Starting scheduled link update process");
        linkUpdateService.updateLinks();
        log.info("Link update process finished");
    }
}
