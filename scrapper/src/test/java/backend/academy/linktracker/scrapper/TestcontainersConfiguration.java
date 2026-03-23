package backend.academy.linktracker.scrapper;

import java.io.File;
import java.nio.file.Path;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.postgresql.PostgreSQLContainer;
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
    public PostgreSQLContainer postgreSQLContainer() {
        return new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))
                .withNetwork(NETWORK)
                .withNetworkAliases("postgres")
                .withDatabaseName("scrapper")
                .withUsername("postgres")
                .withPassword("postgres")
                .withCopyFileToContainer(
                        MountableFile.forHostPath(getBasePath() + "migrations/01-init-schema.sql"),
                        "/docker-entrypoint-initdb.d/01-init-schema.sql")
                .withCopyFileToContainer(
                        MountableFile.forHostPath(getBasePath() + "migrations/02-add-indexes.sql"),
                        "/docker-entrypoint-initdb.d/02-add-indexes.sql");
    }

    @Bean
    @Profile("e2e")
    public GenericContainer<?> scrapperContainer(PostgreSQLContainer postgres) {
        return new GenericContainer<>(new ImageFromDockerfile("scrapper-it", false)
                        .withFileFromPath("app.jar", findJar("scrapper"))
                        .withDockerfileFromBuilder(builder -> builder.from("openjdk:25-ea-slim")
                                .copy("app.jar", "/app.jar")
                                .entryPoint("java", "--enable-preview", "-jar", "/app.jar")
                                .build()))
                .withNetwork(NETWORK)
                .withNetworkAliases("scrapper")
                .withExposedPorts(8081)
                .waitingFor(Wait.forLogMessage(".*Started ScrapperApplication.*", 1))
                .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://postgres:5432/scrapper")
                .withEnv("SPRING_DATASOURCE_USERNAME", "postgres")
                .withEnv("SPRING_DATASOURCE_PASSWORD", "postgres")
                .withEnv("APP_DATABASE_ACCESS_TYPE", "JPA")
                .withEnv("SPRING_JPA_HIBERNATE_DDL_AUTO", "validate")
                .withEnv("SPRING_LIQUIBASE_ENABLED", "false")
                .withEnv("SERVER_PORT", "8081")
                .withEnv("APP_BOT_BASE_URL", "http://bot:8080")
                .withEnv("APP_SCHEDULER_ENABLE", "false")
                .withEnv("GITHUB_TOKEN", "mock")
                .withEnv("STACKOVERFLOW_KEY", "mock")
                .withEnv("STACKOVERFLOW_ACCESS_KEY", "mock")
                .dependsOn(postgres);
    }

    @Bean
    @Profile("e2e")
    public GenericContainer<?> botContainer() {
        return new GenericContainer<>(new ImageFromDockerfile("bot-it", false)
                        .withFileFromPath("app.jar", findJar("bot"))
                        .withDockerfileFromBuilder(builder -> builder.from("openjdk:25-ea-slim")
                                .copy("app.jar", "/app.jar")
                                .entryPoint("java", "--enable-preview", "-jar", "/app.jar")
                                .build()))
                .withNetwork(NETWORK)
                .withNetworkAliases("bot")
                .withExposedPorts(8080)
                .waitingFor(Wait.forLogMessage(".*Started BotApplication.*", 1))
                .withEnv("SERVER_PORT", "8080")
                .withEnv("APP_SCRAPPER_BASE_URL", "http://scrapper:8081")
                .withEnv("TELEGRAM_BOT_TOKEN", "123:mock")
                .withEnv("BOT_MESSAGES_HELP", "help message")
                .withEnv("BOT_MESSAGES_START", "start message")
                .withEnv("BOT_MESSAGES_BAD_REQUEST", "bad request")
                .withEnv("BOT_MESSAGES_UPDATE", "update")
                .withEnv("BOT_MESSAGES_CANCEL", "cancel")
                .withEnv("BOT_MESSAGES_WRONG", "wrong");
    }
}
