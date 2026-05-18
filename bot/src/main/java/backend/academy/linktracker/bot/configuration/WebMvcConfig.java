package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.bot.controller.interceptor.RateLimitingInterceptor;
import backend.academy.linktracker.bot.properties.RateLimitProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(RateLimitProperties.class)
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimitingInterceptor rateLimitingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitingInterceptor);
    }
}
