package backend.academy.linktracker.scrapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestcontainersConfiguration.class)
public class ScrapperControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String LINK_URL = "https://github.com/spring-projects/spring-boot";
    private static final String LINK_JSON = "{ \"link\": \"" + LINK_URL + "\" }";

    @Test
    @DisplayName("test3_1: Добавление и получение списка ссылок")
    void test3_1_AddAndGetLink() throws Exception {
        mockMvc.perform(post("/tg-chat/1")).andExpect(status().isOk());

        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LINK_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/links").header("Tg-Chat-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.links[0].url").value(LINK_URL));
    }

    @Test
    @DisplayName("test3_2: Добавление и последующее удаление ссылки")
    void test3_2_AddAndRemoveLink() throws Exception {
        mockMvc.perform(post("/tg-chat/1")).andExpect(status().isOk());

        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LINK_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/links")
                        .header("Tg-Chat-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LINK_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/links").header("Tg-Chat-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(0));
    }

    @Test
    @DisplayName("test3_3: Попытка удаления ссылки из несуществующего чата")
    void test3_3_RemoveLinkFromNonExistentChat() throws Exception {
        mockMvc.perform(post("/tg-chat/1")).andExpect(status().isOk());

        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LINK_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/links")
                        .header("Tg-Chat-Id", 999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LINK_JSON))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/links").header("Tg-Chat-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(1));
    }

    @Test
    @DisplayName("test3_4: Попытка добавления ссылки в незарегистрированный чат")
    void test3_4_AddLinkToNonExistentChat() throws Exception {
        mockMvc.perform(post("/tg-chat/1")).andExpect(status().isOk());

        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LINK_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("test3_5: Работа с ссылками после удаления чата")
    void test3_5_WorkWithDeletedChat() throws Exception {
        mockMvc.perform(post("/tg-chat/1")).andExpect(status().isOk());
        mockMvc.perform(delete("/tg-chat/1")).andExpect(status().isOk());

        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LINK_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("test3_6: Попытка удаления незарегистрированного чата")
    void test3_6_RemoveNonExistentChat() throws Exception {
        mockMvc.perform(delete("/tg-chat/999")).andExpect(status().isNotFound());
    }
}
