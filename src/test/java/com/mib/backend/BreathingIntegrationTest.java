package com.mib.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mib.backend.dto.request.CompleteBreathingSessionRequest;
import com.mib.backend.dto.request.LoginRequest;
import com.mib.backend.dto.request.RegisterRequest;
import com.mib.backend.entity.BreathingTechnique;
import com.mib.backend.repository.AchievementRepository;
import com.mib.backend.repository.BreathingTechniqueRepository;
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
class BreathingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BreathingTechniqueRepository breathingTechniqueRepository;

    @Autowired
    private AchievementRepository achievementRepository;

    @Test
    void deveRegistrarSessaoLimitarXpDiarioEAcumularEstatisticas() throws Exception {
        BreathingTechnique technique = new BreathingTechnique();
        technique.setCode("TEST-TECHNIQUE");
        technique.setName("Tecnica de Teste");
        technique.setDescription("Descricao de teste");
        technique.setBenefits("Beneficios de teste");
        technique.setInhaleSeconds(4);
        technique.setHoldAfterInhaleSeconds(4);
        technique.setExhaleSeconds(4);
        technique.setHoldAfterExhaleSeconds(4);
        technique.setSuggestedCycles(4);
        technique = breathingTechniqueRepository.save(technique);

        achievementRepository.save(new com.mib.backend.entity.Achievement(
                "FIRST_BREATHING_EXERCISE", "Primeiro Respiro", "Concluiu a primeira sessao", 5, null));

        String token = registerAndLogin("respira_dev", "respira_dev@example.com", "Respira Dev");

        long xpAfterLogin = getWallet(token);

        // Completa 6 sessoes: apenas as 5 primeiras devem conceder XP no mesmo dia
        for (int i = 0; i < 6; i++) {
            mockMvc.perform(post("/api/breathing/techniques/" + technique.getId() + "/complete")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(new CompleteBreathingSessionRequest(60))))
                    .andExpect(status().isNoContent());
        }

        long xpAfterSixSessions = getWallet(token);
        // 5 sessoes com XP (10 cada) + 5 XP da conquista de primeira sessao, concedida uma unica vez
        org.assertj.core.api.Assertions.assertThat(xpAfterSixSessions - xpAfterLogin).isEqualTo(5 * 10 + 5);

        mockMvc.perform(get("/api/breathing/stats").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSessions").value(6))
                .andExpect(jsonPath("$.totalMinutes").value(6)); // 6 sessoes de 60s = 6 minutos

        mockMvc.perform(get("/api/achievements").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code == 'FIRST_BREATHING_EXERCISE')].unlocked")
                        .value(org.hamcrest.Matchers.hasItem(true)));
    }

    private long getWallet(String token) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/xp/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("xpWallet").asLong();
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
