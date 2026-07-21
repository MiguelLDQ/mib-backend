package com.mib.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mib.backend.dto.request.LoginRequest;
import com.mib.backend.dto.request.RegisterRequest;
import com.mib.backend.entity.DailyMission;
import com.mib.backend.entity.MissionCategory;
import com.mib.backend.entity.MissionDifficulty;
import com.mib.backend.entity.MissionTemplate;
import com.mib.backend.repository.DailyMissionRepository;
import com.mib.backend.repository.MissionTemplateRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MissionTemplateRepository missionTemplateRepository;

    @Autowired
    private DailyMissionRepository dailyMissionRepository;

    @Test
    void deveNotificarPedidoDeAmizadeEPermitirMarcarComoLida() throws Exception {
        String tokenA = registerAndLogin("notifA_dev", "notifA_dev@example.com", "Notif A");
        String userIdB = registerAndGetUserId("notifB_dev", "notifB_dev@example.com", "Notif B");
        String tokenB = login("notifB_dev", "SenhaForte123");

        mockMvc.perform(post("/api/friends/requests/" + userIdB)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1));

        MvcResult listResult = mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].type").value("FRIEND_REQUEST_RECEIVED"))
                .andReturn();

        JsonNode json = objectMapper.readTree(listResult.getResponse().getContentAsString());
        String notificationId = json.get("content").get(0).get("id").asText();

        mockMvc.perform(patch("/api/notifications/" + notificationId + "/read")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    void deveNotificarConclusaoDeMissaoEPermitirMarcarTodasComoLidas() throws Exception {
        MissionTemplate template = missionTemplateRepository.save(new MissionTemplate(
                "Missao de Teste", "Descricao de teste", MissionCategory.MINDFULNESS, MissionDifficulty.EASY, 15));
        DailyMission dailyMission = dailyMissionRepository.save(new DailyMission(template, LocalDate.now(), 15));

        String token = registerAndLogin("notifmissao_dev", "notifmissao_dev@example.com", "Notif Missao");

        mockMvc.perform(post("/api/missions/daily/" + dailyMission.getId() + "/complete")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/notifications").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.type == 'MISSION_COMPLETED')]").isNotEmpty());

        mockMvc.perform(patch("/api/notifications/read-all").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/notifications/unread-count").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));
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
