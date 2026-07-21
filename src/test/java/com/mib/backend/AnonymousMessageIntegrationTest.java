package com.mib.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mib.backend.dto.request.CreateAnonymousMessageRequest;
import com.mib.backend.dto.request.LoginRequest;
import com.mib.backend.dto.request.RegisterRequest;
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
class AnonymousMessageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void devePublicarCurtirEListarNoFeedSemExporAutor() throws Exception {
        String tokenAuthor = registerAndLogin("estrela_autor", "estrela_autor@example.com", "Autor");
        String tokenLiker = registerAndLogin("estrela_liker", "estrela_liker@example.com", "Liker");

        MvcResult createResult = mockMvc.perform(post("/api/anonymous")
                        .header("Authorization", "Bearer " + tokenAuthor)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new CreateAnonymousMessageRequest("Hoje foi um dia dificil, mas vou superar.", null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Hoje foi um dia dificil, mas vou superar."))
                .andExpect(jsonPath("$.likeCount").value(0))
                .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        // Garante que nenhum campo de autoria e exposto na resposta.
        org.assertj.core.api.Assertions.assertThat(created.has("authorId")).isFalse();
        org.assertj.core.api.Assertions.assertThat(created.has("author")).isFalse();

        String messageId = created.get("id").asText();

        mockMvc.perform(get("/api/anonymous")
                        .header("Authorization", "Bearer " + tokenLiker))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].content").value("Hoje foi um dia dificil, mas vou superar."));

        mockMvc.perform(post("/api/anonymous/" + messageId + "/like")
                        .header("Authorization", "Bearer " + tokenLiker))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(true));

        // O proprio autor nao pode curtir a propria mensagem
        mockMvc.perform(post("/api/anonymous/" + messageId + "/like")
                        .header("Authorization", "Bearer " + tokenAuthor))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRemoverAutomaticamenteMensagemComTermoDeAssedio() throws Exception {
        String token = registerAndLogin("estrela_infrator", "estrela_infrator@example.com", "Infrator");

        mockMvc.perform(post("/api/anonymous")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new CreateAnonymousMessageRequest("Voce e um idiota", null))))
                .andExpect(status().isUnprocessableEntity());

        // A mensagem removida nao deve aparecer no feed
        mockMvc.perform(get("/api/anonymous").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
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
