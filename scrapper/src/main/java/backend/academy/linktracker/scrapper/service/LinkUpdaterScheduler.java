package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.dto.StackOverflowResponse;
import backend.academy.linktracker.scrapper.model.LinkData;
import backend.academy.linktracker.scrapper.repository.InMemoryScrapperRepository;
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
    private final GitHubClient gitHubClient;
    private final StackOverflowClient stackOverflowClient;
    private final BotClient botClient;

    @Scheduled(fixedDelayString = "#{@schedulerProperties.interval.toMillis()}")
    public void update() {
        log.atInfo().log("Updating links");

        Map<Long, Set<LinkData>> links = repository.findAll();

        for (Map.Entry<Long, Set<LinkData>> entry : links.entrySet()) {
            Long chatId = entry.getKey();
            for (LinkData link : entry.getValue()) {
                processLink(chatId, link);
            }
        }
    }

    private void processLink(Long chatId, LinkData linkData) {
        String host = linkData.getUrl().getHost();
        if (host == null) {
            return;
        }

        if (host.endsWith("github.com")) {
            handleGithub(chatId, linkData);
        } else if (host.endsWith("stackoverflow.com")) {
            handleStackOverflow(chatId, linkData);
        }
    }

    private void handleGithub(Long chatId, LinkData linkData) {
        String path = linkData.getUrl().getPath();
        if (path == null) return;
        String[] parts = path.split("/");

        if (parts.length >= 3) {
            String owner = parts[1];
            String repo = parts[2];

            gitHubClient.fetchUpdate(owner, repo).ifPresent(response -> {
                if (response.updatedAt().isAfter(linkData.getLastUpdate())) {
                    log.atInfo()
                            .addKeyValue("repo", repo)
                            .addKeyValue("chat_id", chatId)
                            .log("Update found in Github");

                    linkData.setLastUpdate(response.updatedAt());

                    botClient.sendUpdate(new LinkUpdate(
                            linkData.getId(), linkData.getUrl(), "Обновление в репозитории " + repo, List.of(chatId)));
                }
            });
        }
    }

    private void handleStackOverflow(Long chatId, LinkData linkData) {
        String path = linkData.getUrl().getPath();
        if (path == null) return;
        String[] parts = path.split("/");

        if (parts.length >= 3 && parts[1].equals("questions")) {
            try {
                Long questionId = Long.parseLong(parts[2]);
                stackOverflowClient.fetchQuestion(questionId).ifPresent(response -> {
                    if (response.items() != null && !response.items().isEmpty()) {
                        StackOverflowResponse.Item item = response.items().get(0);
                        if (item.lastActivityDate().isAfter(linkData.getLastUpdate())) {
                            log.atInfo()
                                    .addKeyValue("question_id", questionId)
                                    .addKeyValue("chat_id", chatId)
                                    .log("Update found in StackOverflow");

                            linkData.setLastUpdate(item.lastActivityDate());

                            botClient.sendUpdate(new LinkUpdate(
                                    linkData.getId(),
                                    linkData.getUrl(),
                                    "Обновление в вопросе " + questionId,
                                    List.of(chatId)));
                        }
                    }
                });
            } catch (NumberFormatException e) {
                log.atError().addKeyValue("url", linkData.getUrl()).log("Failed to parse StackOverflow ID");
            }
        }
    }
}
