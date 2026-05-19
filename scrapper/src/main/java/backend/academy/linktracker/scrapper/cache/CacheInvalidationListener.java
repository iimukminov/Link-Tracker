package backend.academy.linktracker.scrapper.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

@Slf4j
@RequiredArgsConstructor
public class CacheInvalidationListener implements MessageListener {

    private final CaffeineCacheManager caffeineCacheManager;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String key = new String(message.getBody());

        log.atDebug()
                .addKeyValue("redisKey", key)
                .addKeyValue("topicPattern", new String(pattern))
                .log("Получено pub/sub уведомление от Valkey об удалении/истечении ключа");

        int separatorIndex = key.indexOf(':');
        if (separatorIndex > 0) {
            String cacheName = key.substring(0, separatorIndex);
            String localKey = key.substring(separatorIndex + 1);

            Cache localCache = caffeineCacheManager.getCache(cacheName);
            if (localCache != null) {
                localCache.evict(localKey);
                log.atDebug()
                        .addKeyValue("cacheName", cacheName)
                        .addKeyValue("localKey", localKey)
                        .log("Ключ успешно инвалидирован в локальном кэше L1");
            } else {
                log.atWarn()
                        .addKeyValue("cacheName", cacheName)
                        .addKeyValue("localKey", localKey)
                        .log("Локальный кэш не найден при попытке инвалидации");
            }
        } else {
            log.atWarn()
                    .addKeyValue("redisKey", key)
                    .log("Получен неверный формат ключа для инвалидации кэша (отсутствует разделитель ':')");
        }
    }
}
