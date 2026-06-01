package backend.academy.linktracker.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = {"backend.academy.linktracker.bot", "backend.academy.linktracker.common"})
@ConfigurationPropertiesScan(basePackages = {"backend.academy.linktracker.bot", "backend.academy.linktracker.common"})
public class BotApplication {

    public static void main(String[] args) {
        SpringApplication.run(BotApplication.class, args);
    }
}
