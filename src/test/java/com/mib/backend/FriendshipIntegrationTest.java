package com.mib.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mib.backend.dto.request.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FriendshipIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveEnviarAceitarERemoverAmizade() throws Exception {
        String tokenA = registerAndGetToken("ana_dev", "ana@example.com", "Ana");
        String userIdB = registerAndGetUserId("bruno_dev", "bruno@example.com", "SenhaForte123", "Bruno");
        String tokenB = login("bruno_dev", "SenhaForte123");

        // Ana envia solicitacao para Bruno
        mockMvc.perform(post("/api/friends/requests/" + userIdB)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isCreated());

        // Bruno ve a solicitacao recebida
        MvcResult incoming = mockMvc.perform(get("/api/friends/requests/incoming")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("ana_dev"))
                .andReturn();

        JsonNode incomingJson = objectMapper.readTree(incoming.getResponse().getContentAsString());
        String requestId = incomingJson.get(0).get("requestId").asText();

        // Bruno aceita
        mockMvc.perform(post("/api/friends/requests/" + requestId + "/accept")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNoContent());

        // Ambos aparecem na lista de amigos um do outro
        mockMvc.perform(get("/api/friends").header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("bruno_dev"));

        mockMvc.perform(get("/api/friends").header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("ana_dev"));
    }

    @Test
    void naoDevePermitirAdicionarASiMesmo() throws Exception {
        String userIdA = registerAndGetUserId("carla_dev", "carla@example.com", "SenhaForte123", "Carla");
        String tokenA = login("carla_dev", "SenhaForte123");

        mockMvc.perform(post("/api/friends/requests/" + userIdA)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isBadRequest());
    }

    private String registerAndGetToken(String username, String email, String displayName) throws Exception {
        return registerAndGetToken(username, email, "SenhaForte123", displayName);
    }

    private String registerAndGetToken(String username, String email, String password, String displayName) throws Exception {
        RegisterRequest register = new RegisterRequest(username, email, password, displayName);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("accessToken").asText();
    }

    private String registerAndGetUserId(String username, String email, String password, String displayName) throws Exception {
        RegisterRequest register = new RegisterRequest(username, email, password, displayName);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.get("user").get("id")).isNotNull();
        return json.get("user").get("id").asText();
    }

    private String login(String usernameOrEmail, String password) throws Exception {
        String body = objectMapper.writeValueAsString(
                new com.mib.backend.dto.request.LoginRequest(usernameOrEmail, password));

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("accessToken").asText();
    }
}
