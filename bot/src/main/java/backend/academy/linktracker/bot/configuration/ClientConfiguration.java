package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.properties.ScrapperProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientConfiguration {

    @Bean
    public ScrapperClient scrapperClient(RestClient.Builder builder, ScrapperProperties properties) {

        RestClient restClient = builder.baseUrl(properties.getBaseUrl()).build();
        return new ScrapperClient(restClient);
    }
}
