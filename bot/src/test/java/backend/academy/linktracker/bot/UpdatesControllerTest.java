package backend.academy.linktracker.bot;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class UpdatesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void test1_ValidUpdateRequest_ShouldReturn200() throws Exception {
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
    void test2_InvalidUpdateRequest_MissingUrl_ShouldReturn400() throws Exception {
        String invalidJson = """
            {
              "id": 1,
              "description": "Новый коммит"
            }
            """;

        mockMvc.perform(post("/updates").contentType(MediaType.APPLICATION_JSON).content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
