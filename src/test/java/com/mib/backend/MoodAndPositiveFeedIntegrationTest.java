package com.mib.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mib.backend.dto.request.LoginRequest;
import com.mib.backend.dto.request.RecordMoodRequest;
import com.mib.backend.dto.request.RegisterRequest;
import com.mib.backend.entity.PositiveFeedItem;
import com.mib.backend.entity.PositiveFeedItemType;
import com.mib.backend.repository.PositiveFeedItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MoodAndPositiveFeedIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PositiveFeedItemRepository positiveFeedItemRepository;

    @Test
    void deveRegistrarHumorDoDiaEAtualizarAoInvesDeDuplicar() throws Exception {
        String token = registerAndLogin("humor_dev", "humor_dev@example.com", "Humor Dev");

        mockMvc.perform(post("/api/mood")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new RecordMoodRequest("GOOD", "Dia tranquilo"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moodLevel").value("GOOD"));

        // Registrar de novo no mesmo dia deve ATUALIZAR, nao duplicar
        mockMvc.perform(post("/api/mood")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new RecordMoodRequest("VERY_GOOD", "Melhorou ao longo do dia"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moodLevel").value("VERY_GOOD"));

        mockMvc.perform(get("/api/mood/today").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moodLevel").value("VERY_GOOD"));

        mockMvc.perform(get("/api/mood/history").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)); // upsert: continua sendo 1 registro
    }

    @Test
    void deveRejeitarNivelDeHumorInvalido() throws Exception {
        String token = registerAndLogin("humor_invalido_dev", "humor_invalido_dev@example.com", "Humor Invalido");

        mockMvc.perform(post("/api/mood")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new RecordMoodRequest("FELIZ_DEMAIS", null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornarFeedPositivoDoDia() throws Exception {
        positiveFeedItemRepository.save(new PositiveFeedItem(PositiveFeedItemType.QUOTE, "Frase de teste", null));
        positiveFeedItemRepository.save(new PositiveFeedItem(PositiveFeedItemType.FACT, "Curiosidade de teste", null));

        String token = registerAndLogin("feed_dev", "feed_dev@example.com", "Feed Dev");

        mockMvc.perform(get("/api/feed/positive").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    private String registerAndLogin(String username, String email, String displayName) throws Exception {
        RegisterRequest register = new RegisterRequest(username, email, "SenhaForte123", displayName);

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new LoginRequest(username, "SenhaForte123"))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }
}
