package backend.academy.linktracker.scrapper.metrics;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class ControllerMetricsAspect {

    private final ScrapperMetrics metrics;

    @Before("execution(* backend.academy.linktracker.scrapper.controller..*(..))")
    public void measureApiControllerRequest(JoinPoint joinPoint) {
        String source = "unknown";

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            source = request.getHeader("User-Agent");

            if (source == null || source.isBlank()) {
                source = request.getRemoteAddr();
            }
        }

        metrics.recordApiRequest(source);
    }
}
