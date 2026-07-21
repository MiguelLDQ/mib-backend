package com.mib.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mib.backend.dto.request.CreateTaskRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveCriarConcluirEGanharXpApenasUmaVez() throws Exception {
        String token = registerAndLogin("tarefa_dev", "tarefa_dev@example.com", "Tarefa Dev");

        MvcResult createResult = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new CreateTaskRequest("Estudar Spring", "Revisar JPA", "Estudos", null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        String taskId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(patch("/api/tasks/" + taskId + "/complete")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        MvcResult xpAfterFirstComplete = mockMvc.perform(get("/api/xp/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        long xpAfterComplete = objectMapper.readTree(xpAfterFirstComplete.getResponse().getContentAsString())
                .get("totalXp").asLong();

        // Reabre e conclui de novo: XP nao deve ser concedido uma segunda vez
        mockMvc.perform(patch("/api/tasks/" + taskId + "/reopen")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(patch("/api/tasks/" + taskId + "/complete")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        MvcResult xpAfterSecondComplete = mockMvc.perform(get("/api/xp/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        long xpFinal = objectMapper.readTree(xpAfterSecondComplete.getResponse().getContentAsString())
                .get("totalXp").asLong();

        org.assertj.core.api.Assertions.assertThat(xpFinal).isEqualTo(xpAfterComplete);
    }

    @Test
    void deveFiltrarTarefasPorCategoria() throws Exception {
        String token = registerAndLogin("filtro_dev", "filtro_dev@example.com", "Filtro Dev");

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new CreateTaskRequest("Correr 5km", null, "Saude", null))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new CreateTaskRequest("Ler um capitulo", null, "Estudos", null))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/tasks").param("category", "Saude")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Correr 5km"));
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
