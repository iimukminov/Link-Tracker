package backend.academy.linktracker.common.interceptor;

import backend.academy.linktracker.common.exceptions.RateLimitExceededException;
import backend.academy.linktracker.common.properties.RateLimitProperties;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.function.Function;

public class RateLimitingInterceptor implements HandlerInterceptor {

    private final RateLimitProperties properties;
    private final Cache<String, Bucket> cache;
    private final Function<HttpServletRequest, String> keyResolver;

    public RateLimitingInterceptor(RateLimitProperties properties, Function<HttpServletRequest, String> keyResolver) {
        this.properties = properties;
        this.keyResolver = keyResolver;
        this.cache = Caffeine.newBuilder()
                .maximumSize(properties.getCacheMaxSize())
                .expireAfterAccess(properties.getCacheExpireAfterAccess())
                .build();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String key = keyResolver.apply(request);
        Bucket bucket = cache.get(key, k -> createNewBucket());

        if (!bucket.tryConsume(1)) {
            throw new RateLimitExceededException("Rate limit exceeded for key: " + key);
        }
        return true;
    }

    private Bucket createNewBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(properties.getCapacity())
                        .refillGreedy(properties.getCapacity(), properties.getDuration())
                        .build())
                .build();
    }

    public void clearCache() {
        cache.invalidateAll();
    }
}
