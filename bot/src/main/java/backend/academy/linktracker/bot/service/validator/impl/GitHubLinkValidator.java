package backend.academy.linktracker.bot.service.validator.impl;

import backend.academy.linktracker.bot.service.validator.LinkValidator;
import org.springframework.stereotype.Component;

@Component
public class GitHubLinkValidator implements LinkValidator {
    @Override
    public boolean supports(String host) {
        return host != null && host.endsWith("github.com");
    }
}
