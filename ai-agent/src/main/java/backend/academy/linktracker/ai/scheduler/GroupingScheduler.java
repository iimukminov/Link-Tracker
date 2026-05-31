package backend.academy.linktracker.ai.scheduler;

import backend.academy.linktracker.ai.service.GroupingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupingScheduler {

    private final GroupingService groupingService;

    @Scheduled(fixedDelayString = "${ai-agent.grouping.window-ms:30000}")
    public void scheduleFlush() {
        log.atDebug().addKeyValue("action", "scheduledFlush").log("Triggering scheduled flush of grouped updates");
        groupingService.flushGroupedUpdates();
    }
}
