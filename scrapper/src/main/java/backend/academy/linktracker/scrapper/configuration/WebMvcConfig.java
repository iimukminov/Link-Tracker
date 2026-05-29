package backend.academy.linktracker.scrapper.configuration;

import backend.academy.linktracker.common.interceptor.RateLimitingInterceptor;
import backend.academy.linktracker.common.properties.RateLimitProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(RateLimitProperties.class)
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimitProperties rateLimitProperties;

    @Bean
    public RateLimitingInterceptor rateLimitingInterceptor() {
        return new RateLimitingInterceptor(rateLimitProperties, req -> {
            String chatId = req.getHeader("tg-chat-id");
            return chatId != null ? chatId : req.getRemoteAddr();
        });
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitingInterceptor()).addPathPatterns("/tg-chat/**", "/links/**");
    }
}
