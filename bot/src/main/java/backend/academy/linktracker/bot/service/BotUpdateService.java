package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.client.TelegramSender;
import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.bot.properties.BotMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotUpdateService {

    private final TelegramSender telegramSender;
    private final BotMessages messages;
    private final StringRedisTemplate redisTemplate;

    public void processUpdate(LinkUpdate linkUpdate) {
        if (linkUpdate.getTgChatIds() == null || linkUpdate.getTgChatIds().isEmpty()) {
            log.atWarn()
                .setMessage("Received update with no target chat IDs")
                .addKeyValue("link_id", linkUpdate.getId())
                .log();
            return;
        }

        log.atInfo()
            .setMessage("Received update from Scrapper")
            .addKeyValue("link_id", linkUpdate.getId())
            .addKeyValue("url", linkUpdate.getUrl())
            .addKeyValue("chats_count", linkUpdate.getTgChatIds().size())
            .log();

        boolean hasErrors = false;

        for (Long chatId : linkUpdate.getTgChatIds()) {
            String redisKey = String.format("processed:%d-%d", linkUpdate.getId(), chatId);

            try {
                Boolean isNew = redisTemplate.opsForValue().setIfAbsent(redisKey, "sent", Duration.ofHours(24));

                if (Boolean.TRUE.equals(isNew)) {
                    String messageText =
                        String.format(messages.getUpdate(), linkUpdate.getDescription(), linkUpdate.getUrl());
                    telegramSender.sendMessage(chatId, messageText);
                } else {
                    log.atInfo()
                        .setMessage("Duplicated update skipped")
                        .addKeyValue("chatId", chatId)
                        .addKeyValue("updateId", linkUpdate.getId())
                        .log();
                }

            } catch (Exception e) {
                log.atError()
                    .setMessage("Failed to send update to chat")
                    .addKeyValue("chatId", chatId)
                    .setCause(e)
                    .log();
                hasErrors = true;
            }
        }
        if (hasErrors) {
            throw new RuntimeException("Partial failure during update delivery");
        }
    }
}
