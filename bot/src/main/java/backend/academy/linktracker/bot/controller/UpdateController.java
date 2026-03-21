package backend.academy.linktracker.bot.controller;

import backend.academy.linktracker.bot.api.UpdatesApi;
import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.bot.service.BotUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UpdateController implements UpdatesApi {

    private final BotUpdateService botUpdateService;

    @Override
    public ResponseEntity<Void> updatesPost(LinkUpdate linkUpdate) {
        botUpdateService.processUpdate(linkUpdate);
        return ResponseEntity.ok().build();
    }
}
