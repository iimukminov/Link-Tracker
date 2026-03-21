package backend.academy.linktracker.bot.service.validator.impl;

import backend.academy.linktracker.bot.service.validator.LinkValidator;
import org.springframework.stereotype.Component;

@Component
public class StackOverflowLinkValidator implements LinkValidator {
    @Override
    public boolean supports(String host) {
        return host != null && host.endsWith("stackoverflow.com");
    }
}
