package backend.academy.linktracker.scrapper.cache;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;

@RequiredArgsConstructor
public class TwoLevelCacheManager implements CacheManager {

    private final CaffeineCacheManager l1CacheManager;
    private final RedisCacheManager l2CacheManager;

    @Override
    public Cache getCache(String name) {
        Cache l1Cache = l1CacheManager.getCache(name);
        Cache l2Cache = l2CacheManager.getCache(name);
        return new TwoLevelCache(name, l1Cache, l2Cache);
    }

    @Override
    public Collection<String> getCacheNames() {
        return l2CacheManager.getCacheNames();
    }
}
