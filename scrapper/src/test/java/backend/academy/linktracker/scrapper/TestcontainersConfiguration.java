package backend.academy.linktracker.scrapper;

import java.io.File;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    private static final Network NETWORK = Network.newNetwork();

    private static String getBasePath() {
        return Path.of("scrapper").toFile().exists() ? "./" : "../";
    }

    private static Path findJar(String moduleName) {
        File targetDir = Path.of(getBasePath() + moduleName + "/target/").toFile();
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

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgreSQLContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
            .withNetwork(NETWORK)
            .withNetworkAliases("postgres")
            .withDatabaseName("scrapper")
            .withUsername("postgres")
            .withPassword("postgres");
    }

    @Bean
    @Profile("e2e")
    public GenericContainer<?> scrapperContainer(PostgreSQLContainer<?> postgres) {
        Path scrapperMessagesPath = Path.of(getBasePath() + "scrapper/config/messages.yml").toAbsolutePath();
        return new GenericContainer<>(new ImageFromDockerfile("scrapper-it", false)
            .withFileFromPath("app.jar", findJar("scrapper"))
            .withDockerfileFromBuilder(builder -> builder.from("openjdk:25-ea-slim")
                .copy("app.jar", "/app.jar")
                .entryPoint("java", "--enable-preview", "-jar", "/app.jar")
                .build()))
            .withNetwork(NETWORK)
            .withNetworkAliases("scrapper")
            .withExposedPorts(8081)
            .withEnv("SPRING_LIQUIBASE_ENABLED", "true")
            .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://postgres:5432/scrapper")
            .withEnv("SPRING_DATASOURCE_USERNAME", "postgres")
            .withEnv("SPRING_DATASOURCE_PASSWORD", "postgres")
            .withCopyFileToContainer(
                MountableFile.forHostPath(scrapperMessagesPath),
                "/config/messages.yml"
            )
            .withEnv("GITHUB_TOKEN", "mock")
            .withEnv("STACKOVERFLOW_KEY", "mock")
            .withEnv("STACKOVERFLOW_ACCESS_KEY", "mock")
            .dependsOn(postgres);
    }

    @Bean
    @Profile("e2e")
    public GenericContainer<?> botContainer(@Qualifier("scrapperContainer") GenericContainer<?> scrapper) {
        Path botMessagesPath = Path.of(getBasePath() + "bot/config/messages.yml").toAbsolutePath();
        return new GenericContainer<>(new ImageFromDockerfile("bot-it", false)
            .withFileFromPath("app.jar", findJar("bot"))
            .withDockerfileFromBuilder(builder -> builder.from("openjdk:25-ea-slim")
                .copy("app.jar", "/app.jar")
                .entryPoint("java", "--enable-preview", "-jar", "/app.jar")
                .build()))
            .withNetwork(NETWORK)
            .withCopyFileToContainer(
                MountableFile.forHostPath(botMessagesPath),
                "/config/messages.yml"
            )
            .withNetworkAliases("bot")
            .withExposedPorts(8080)
            .withEnv("TELEGRAM_BOT_TOKEN", "123:mock")
            .dependsOn(scrapper);
    }
}
