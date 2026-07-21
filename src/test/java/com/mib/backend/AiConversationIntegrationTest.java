package com.mib.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mib.backend.dto.request.LoginRequest;
import com.mib.backend.dto.request.RegisterRequest;
import com.mib.backend.dto.request.SendAiMessageRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AiConversationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveRetornarServicoIndisponivelQuandoGroqNaoConfiguradoESemDeixarMensagemOrfa() throws Exception {
        String token = registerAndLogin("ia_dev", "ia_dev@example.com", "IA Dev");

        mockMvc.perform(post("/api/ai/conversation/messages")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new SendAiMessageRequest("Ola, tudo bem?"))))
                .andExpect(status().isServiceUnavailable());

        // A troca e atomica: se a chamada a IA falha, nada fica salvo pela metade.
        mockMvc.perform(get("/api/ai/conversation").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void deveLimparHistoricoMesmoQuandoVazio() throws Exception {
        String token = registerAndLogin("ia_limpa_dev", "ia_limpa_dev@example.com", "IA Limpa Dev");

        mockMvc.perform(delete("/api/ai/conversation").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
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
