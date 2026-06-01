package backend.academy.linktracker.scrapper.configuration;

import backend.academy.linktracker.scrapper.client.BotClient;
import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.metrics.ScrapperMetrics;
import backend.academy.linktracker.scrapper.properties.BotProperties;
import backend.academy.linktracker.scrapper.properties.GithubProperties;
import backend.academy.linktracker.scrapper.properties.StackoverflowProperties;
import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientConfiguration {

    private ClientHttpRequestFactory createRequestFactory(Duration timeout) {
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(timeout).build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(timeout);

        return factory;
    }

    @Bean
    public BotClient botClient(RestClient.Builder builder, BotProperties properties) {
        RestClient restClient = builder.baseUrl(properties.getBaseUrl())
                .requestFactory(createRequestFactory(properties.getTimeout()))
                .build();
        return new BotClient(restClient);
    }

    @Bean
    public GitHubClient gitHubClient(RestClient.Builder builder, GithubProperties properties, ScrapperMetrics metrics) {
        RestClient restClient = builder.baseUrl(properties.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getToken())
                .requestFactory(createRequestFactory(properties.getTimeout()))
                .build();
        return new GitHubClient(restClient, metrics);
    }

    @Bean
    public StackOverflowClient stackOverflowClient(
            RestClient.Builder builder, StackoverflowProperties properties, ScrapperMetrics metrics) {
        RestClient restClient = builder.baseUrl(properties.getBaseUrl())
                .requestFactory(createRequestFactory(properties.getTimeout()))
                .build();
        return new StackOverflowClient(restClient, metrics);
    }
}
