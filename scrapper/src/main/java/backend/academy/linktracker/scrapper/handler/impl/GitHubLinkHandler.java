package backend.academy.linktracker.scrapper.handler.impl;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.client.BotClient;
import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.handler.LinkHandler;
import backend.academy.linktracker.scrapper.model.LinkData;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubLinkHandler implements LinkHandler {

    private final GitHubClient gitHubClient;
    private final BotClient botClient;

    @Override
    public boolean supports(String host) {
        return host != null && host.endsWith("github.com");
    }

    @Override
    public void handle(List<Long> chatIds, LinkData linkData) {
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
                            .addKeyValue("chats_count", chatIds.size())
                            .log("Update found in Github");

                    linkData.setLastUpdate(response.updatedAt());

                    botClient.sendUpdate(new LinkUpdate()
                            .id(linkData.getId())
                            .url(linkData.getUrl())
                            .description("Обновление в репозитории " + repo)
                            .tgChatIds(chatIds));
                }
            });
        }
    }
}
