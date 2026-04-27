package backend.academy.linktracker.bot;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.linktracker.bot.controller.UpdateController;
import backend.academy.linktracker.bot.properties.BotMessages;
import backend.academy.linktracker.bot.service.BotUpdateService;
import com.pengrad.telegrambot.TelegramBot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UpdateController.class)
public class UpdateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TelegramBot telegramBot;

    @MockitoBean
    private BotMessages botMessages;

    @MockitoBean
    private BotUpdateService botUpdateService;

    @Test
    @DisplayName("При отправке корректного JSON должен возвращаться статус 200 OK")
    void shouldReturn200ForValidRequest() throws Exception {
        String validJson = """
            {
              "id": 1,
              "url": "https://github.com/user/repo",
              "description": "Новый коммит",
              "tgChatIds": [123456]
            }
            """;

        mockMvc.perform(post("/updates").contentType(MediaType.APPLICATION_JSON).content(validJson))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("При отсутствии URL в запросе должен возвращаться статус 400 Bad Request")
    void shouldReturn400ForMissingUrl() throws Exception {
        String invalidJson = """
            {
              "id": 1,
              "description": "Новый коммит",
              "tgChatIds": [123456]
            }
            """;

        mockMvc.perform(post("/updates").contentType(MediaType.APPLICATION_JSON).content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
