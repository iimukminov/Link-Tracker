package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.properties.ScrapperProperties;
import java.net.http.HttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientConfiguration {

    @Bean
    public ScrapperClient scrapperClient(RestClient.Builder builder, ScrapperProperties properties) {
        HttpClient httpClient =
                HttpClient.newBuilder().connectTimeout(properties.getTimeout()).build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(properties.getTimeout());

        RestClient restClient =
                builder.baseUrl(properties.getBaseUrl()).requestFactory(factory).build();

        return new ScrapperClient(restClient);
    }
}
