package backend.academy.linktracker.bot.controller.interceptor;

import backend.academy.linktracker.bot.exceptions.RateLimitExceededException;
import backend.academy.linktracker.bot.properties.RateLimitProperties;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitingInterceptor implements HandlerInterceptor {

    private final RateLimitProperties properties;
    private final Cache<String, Bucket> cache;

    public RateLimitingInterceptor(RateLimitProperties properties) {
        this.properties = properties;
        cache = Caffeine.newBuilder()
                .maximumSize(properties.getCacheMaxSize())
                .expireAfterAccess(properties.getCacheExpireAfterAccess())
                .build();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String ip = getClientIp(request);
        Bucket bucket = cache.get(ip, k -> createNewBucket());

        if (!bucket.tryConsume(1)) {
            throw new RateLimitExceededException("Rate limit exceeded for IP: " + ip);
        }
        return true;
    }

    private Bucket createNewBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.simple(properties.getCapacity(), properties.getDuration()))
                .build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
