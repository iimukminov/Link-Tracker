package backend.academy.linktracker.scrapper.metrics;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class RepositoryMetricsAspect {

    private final ScrapperMetrics metrics;

    @Around("execution(* backend.academy.linktracker.scrapper.repository..*(..))")
    public Object measureRepositoryMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        long startedAt = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            String methodName = joinPoint.getSignature().getName();
            metrics.recordRequestDuration("database", methodName, startedAt);
        }
    }
}
