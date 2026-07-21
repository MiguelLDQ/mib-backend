package com.mib.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mib.backend.dto.request.LoginRequest;
import com.mib.backend.dto.request.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveRegistrarELogarComSucesso() throws Exception {
        RegisterRequest register = new RegisterRequest("mig_dev", "mig@example.com", "SenhaForte123", "Miguel");

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.user.username").value("mig_dev"));

        LoginRequest login = new LoginRequest("mig_dev", "SenhaForte123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    void deveRejeitarLoginComSenhaInvalida() throws Exception {
        RegisterRequest register = new RegisterRequest("outro_user", "outro@example.com", "SenhaForte123", "Outro");

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        LoginRequest login = new LoginRequest("outro_user", "senhaErrada");

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRejeitarRegistroComSenhaFraca() throws Exception {
        RegisterRequest register = new RegisterRequest("fraco_user", "fraco@example.com", "12345678", "Fraco");

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isBadRequest());
    }
}
