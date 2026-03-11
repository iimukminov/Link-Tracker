package backend.academy.linktracker.bot.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;

public record LinkUpdate(
        @NotNull Long id,
        @NotNull URI url,
        String description,
        @NotEmpty List<Long> tgChatIds) {}
