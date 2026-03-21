package backend.academy.linktracker.scrapper.configuration;

import backend.academy.linktracker.scrapper.client.BotClient;
import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.properties.BotProperties;
import backend.academy.linktracker.scrapper.properties.GithubProperties;
import backend.academy.linktracker.scrapper.properties.StackoverflowProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientConfiguration {

    @Bean
    public BotClient botClient(RestClient.Builder builder, BotProperties properties) {
        RestClient restClient = builder.baseUrl(properties.getBaseUrl()).build();
        return new BotClient(restClient);
    }

    @Bean
    public GitHubClient gitHubClient(RestClient.Builder builder, GithubProperties properties) {
        RestClient restClient = builder.baseUrl(properties.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getToken())
                .build();
        return new GitHubClient(restClient);
    }

    @Bean
    public StackOverflowClient stackOverflowClient(RestClient.Builder builder, StackoverflowProperties properties) {
        return new StackOverflowClient(builder.baseUrl(properties.getBaseUrl()).build());
    }
}
