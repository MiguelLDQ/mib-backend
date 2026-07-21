package com.mib.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mib.backend.dto.request.LoginRequest;
import com.mib.backend.dto.request.RegisterRequest;
import com.mib.backend.dto.request.SendMessageRequest;
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
class ChatIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveEnviarEListarMensagensNaSalaGeral() throws Exception {
        String token = registerAndLogin("geral_dev", "geral@example.com", "Geral Dev");

        MvcResult roomResult = mockMvc.perform(get("/api/chat/general/room")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        String roomId = roomResult.getResponse().getContentAsString().replace("\"", "");

        mockMvc.perform(post("/api/chat/rooms/" + roomId + "/messages")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new SendMessageRequest("Ola, pessoal!"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Ola, pessoal!"));

        mockMvc.perform(get("/api/chat/rooms/" + roomId + "/messages")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].content").value("Ola, pessoal!"));
    }

    @Test
    void naoDevePermitirConversaPrivadaSemAmizade() throws Exception {
        String tokenA = registerAndLogin("privA_dev", "privA@example.com", "Priv A");
        String userIdB = registerAndGetUserId("privB_dev", "privB@example.com", "Priv B");

        mockMvc.perform(get("/api/chat/direct/" + userIdB)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isBadRequest());
    }

    @Test
    void devePermitirConversaPrivadaEntreAmigosEBloquearTerceiros() throws Exception {
        String tokenA = registerAndLogin("amigoA_dev", "amigoA@example.com", "Amigo A");
        String userIdB = registerAndGetUserId("amigoB_dev", "amigoB@example.com", "Amigo B");
        String tokenB = login("amigoB_dev", "SenhaForte123");
        String tokenC = registerAndLogin("estranho_dev", "estranho@example.com", "Estranho");

        mockMvc.perform(post("/api/friends/requests/" + userIdB)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isCreated());

        MvcResult incoming = mockMvc.perform(get("/api/friends/requests/incoming")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andReturn();
        String requestId = objectMapper.readTree(incoming.getResponse().getContentAsString())
                .get(0).get("requestId").asText();

        mockMvc.perform(post("/api/friends/requests/" + requestId + "/accept")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNoContent());

        MvcResult roomResult = mockMvc.perform(get("/api/chat/direct/" + userIdB)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode roomJson = objectMapper.readTree(roomResult.getResponse().getContentAsString());
        String roomId = roomJson.get("roomId").asText();

        mockMvc.perform(post("/api/chat/rooms/" + roomId + "/messages")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new SendMessageRequest("Oi, tudo bem?"))))
                .andExpect(status().isCreated());

        // Amigo consegue ler
        mockMvc.perform(get("/api/chat/rooms/" + roomId + "/messages")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].content").value("Oi, tudo bem?"));

        // Terceiro nao participante nao consegue ler
        mockMvc.perform(get("/api/chat/rooms/" + roomId + "/messages")
                        .header("Authorization", "Bearer " + tokenC))
                .andExpect(status().isForbidden());
    }

    private String registerAndLogin(String username, String email, String displayName) throws Exception {
        registerAndGetUserId(username, email, displayName);
        return login(username, "SenhaForte123");
    }

    private String registerAndGetUserId(String username, String email, String displayName) throws Exception {
        RegisterRequest register = new RegisterRequest(username, email, "SenhaForte123", displayName);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("user").get("id").asText();
    }

    private String login(String usernameOrEmail, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new LoginRequest(usernameOrEmail, password))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }
}
