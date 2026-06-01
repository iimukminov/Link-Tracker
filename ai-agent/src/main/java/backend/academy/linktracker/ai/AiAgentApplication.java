package backend.academy.linktracker.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"backend.academy.linktracker.ai", "backend.academy.linktracker.common"})
@ConfigurationPropertiesScan(basePackages = {"backend.academy.linktracker.ai", "backend.academy.linktracker.common"})
@EnableScheduling
public class AiAgentApplication {

    static void main(String[] args) {
        SpringApplication.run(AiAgentApplication.class, args);
    }
}
