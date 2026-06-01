package backend.academy.linktracker.scrapper.metrics;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class ControllerMetricsAspect {

    private final ScrapperMetrics metrics;

    @Before("execution(* backend.academy.linktracker.scrapper.controller..*(..))")
    public void measureApiControllerRequest(JoinPoint joinPoint) {
        String controllerName = joinPoint.getSignature().getDeclaringType().getSimpleName();

        metrics.recordApiRequest(controllerName);
    }
}
