package backend.academy.linktracker.scrapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    private static final Network NETWORK = Network.newNetwork();

    private static Path findJar(String moduleName) {
        Path modulePath = Path.of(moduleName);
        String basePath = Files.exists(modulePath) ? "./" : "../";
        Path targetDir = Path.of(basePath, moduleName, "target");

        if (!Files.exists(targetDir) || !Files.isDirectory(targetDir)) {
            throw new RuntimeException(
                    "Директория target для " + moduleName + " не найдена! Выполни 'mvn clean package -DskipTests'");
        }

        try (Stream<Path> paths = Files.list(targetDir)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString();
                        return name.endsWith(".jar")
                                && !name.endsWith("-plain.jar")
                                && !name.endsWith("-javadoc.jar")
                                && !name.endsWith("-sources.jar");
                    })
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException(
                            "JAR файл для " + moduleName + " не найден! Выполни 'mvn clean package -DskipTests'"));
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при поиске JAR файла для " + moduleName, e);
        }
    }

    @Bean
    public GenericContainer<?> scrapperContainer() {
        return new GenericContainer<>(new ImageFromDockerfile()
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
    }

    @Bean
    public GenericContainer<?> botContainer() {
        return new GenericContainer<>(new ImageFromDockerfile()
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
    }

    @Bean
    @ServiceConnection
    public PostgreSQLContainer postgreSQLContainer() {
        return new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));
    }
}
