package backend.academy.linktracker.scrapper.controller.interceptor;

import backend.academy.linktracker.scrapper.exceptions.RateLimitExceededException;
import backend.academy.linktracker.scrapper.properties.RateLimitProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class RateLimitingInterceptor implements HandlerInterceptor {

    private final RateLimitProperties properties;
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String ip = getClientIp(request);
        Bucket bucket = cache.computeIfAbsent(ip, k -> createNewBucket());

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
