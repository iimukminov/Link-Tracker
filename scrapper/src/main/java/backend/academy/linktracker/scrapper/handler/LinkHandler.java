package backend.academy.linktracker.scrapper.handler;

import backend.academy.linktracker.scrapper.model.LinkData;
import java.util.List;

public interface LinkHandler {
    boolean supports(String host);

    void handle(List<Long> chatIds, LinkData linkData);
}
