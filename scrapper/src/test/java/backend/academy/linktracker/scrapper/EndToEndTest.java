package backend.academy.linktracker.scrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class EndToEndTest {

    private static final Network NETWORK = Network.newNetwork();

    private static Path findJar(String moduleName) {
        String basePath = new File(moduleName).exists() ? "./" : "../";
        File targetDir = new File(basePath + moduleName + "/target/");

        File[] jars = targetDir.listFiles((dir, name) -> name.endsWith(".jar")
                && !name.endsWith("-plain.jar")
                && !name.endsWith("-javadoc.jar")
                && !name.endsWith("-sources.jar"));

        if (jars == null || jars.length == 0) {
            throw new RuntimeException(
                    "JAR файл для " + moduleName + " не найден! Выполни 'mvn clean package -DskipTests'");
        }
        return jars[0].toPath();
    }

    @Container
    public static GenericContainer<?> scrapperContainer = new GenericContainer<>(new ImageFromDockerfile()
                    .withFileFromPath("app.jar", findJar("scrapper"))
                    .withDockerfileFromBuilder(builder -> builder.from("openjdk:25-ea-slim")
                            .copy("app.jar", "/app.jar")
                            .entryPoint("java", "--enable-preview", "-jar", "/app.jar")
                            .build()))
            .withNetwork(NETWORK)
            .withNetworkAliases("scrapper")
            .withExposedPorts(8081)
            .withEnv("GITHUB_TOKEN", "mock")
            .withEnv("STACKOVERFLOW_KEY", "mock")
            .withEnv("STACKOVERFLOW_ACCESS_KEY", "mock");

    @Container
    public static GenericContainer<?> botContainer = new GenericContainer<>(new ImageFromDockerfile()
                    .withFileFromPath("app.jar", findJar("bot"))
                    .withDockerfileFromBuilder(builder -> builder.from("openjdk:25-ea-slim")
                            .copy("app.jar", "/app.jar")
                            .entryPoint("java", "--enable-preview", "-jar", "/app.jar")
                            .build()))
            .withNetwork(NETWORK)
            .withNetworkAliases("bot")
            .withExposedPorts(8080)
            .withEnv("APP_SCRAPPER_BASE_URL", "http://scrapper:8081")
            .withEnv("TELEGRAM_BOT_TOKEN", "mock_token");

    @Test
    void testContainersAreRunningAndCommunicating() {
        String scrapperUrl = "http://" + scrapperContainer.getHost() + ":" + scrapperContainer.getMappedPort(8081);
        RestClient scrapperClient = RestClient.create(scrapperUrl);

        var scrapperResponse =
                scrapperClient.post().uri("/tg-chat/1").retrieve().toBodilessEntity();

        assertEquals(200, scrapperResponse.getStatusCode().value(), "Scrapper должен вернуть 200 OK");

        String botUrl = "http://" + botContainer.getHost() + ":" + botContainer.getMappedPort(8080);
        RestClient botClient = RestClient.create(botUrl);

        String updateJson = """
                {
                  "id": 1,
                  "url": "https://github.com/test/repo",
                  "description": "E2E Test Update",
                  "tgChatIds": [1]
                }
                """;

        var botResponse = botClient
                .post()
                .uri("/updates")
                .header("Content-Type", "application/json")
                .body(updateJson)
                .retrieve()
                .toBodilessEntity();

        assertEquals(200, botResponse.getStatusCode().value(), "Bot должен принять апдейт и вернуть 200 OK");
    }
}
