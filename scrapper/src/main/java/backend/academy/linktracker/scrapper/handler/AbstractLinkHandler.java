package backend.academy.linktracker.scrapper.handler;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.model.LinkData;
import backend.academy.linktracker.scrapper.service.LinkUpdateDbService;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractLinkHandler implements LinkHandler {

    private final LinkUpdateDbService dbService;

    @Override
    public void handle(List<Long> chatIds, LinkData linkData) {
        List<LinkUpdate> updates = fetchUpdates(chatIds, linkData);

        if (updates.isEmpty()) return;

        dbService.saveUpdates(updates, linkData);
    }

    protected abstract List<LinkUpdate> fetchUpdates(List<Long> chatIds, LinkData linkData);
}
