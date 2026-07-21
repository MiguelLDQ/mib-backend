package com.mib.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mib.backend.dto.request.LoginRequest;
import com.mib.backend.dto.request.RegisterRequest;
import com.mib.backend.dto.request.UpdateInterestsRequest;
import com.mib.backend.entity.ChatRoom;
import com.mib.backend.entity.ChatRoomType;
import com.mib.backend.entity.Interest;
import com.mib.backend.repository.ChatRoomRepository;
import com.mib.backend.repository.InterestRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InterestAndRecommendationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InterestRepository interestRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Test
    void deveSelecionarInteresseEEntrarNaSalaTematicaAutomaticamente() throws Exception {
        ChatRoom themeRoom = chatRoomRepository.save(new ChatRoom(ChatRoomType.THEME, "Sala Teste"));
        Interest interest = interestRepository.save(new Interest("Interesse Teste", null));
        interest.setThemeRoom(themeRoom);
        interestRepository.save(interest);

        String token = registerAndLogin("interesse_dev", "interesse_dev@example.com", "Interesse Dev");

        mockMvc.perform(put("/api/interests/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new UpdateInterestsRequest(List.of(interest.getId())))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + interest.getId() + "')].selectedByMe")
                        .value(org.hamcrest.Matchers.hasItem(true)));

        // Deve ter acesso de leitura a sala tematica (so participantes podem ler salas nao gerais)
        mockMvc.perform(get("/api/chat/rooms/" + themeRoom.getId() + "/messages")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void deveSugerirAmizadeComBaseEmInteresseEmComum() throws Exception {
        Interest interest = interestRepository.save(new Interest("Interesse Compartilhado", null));

        String tokenA = registerAndLogin("recA_dev", "recA_dev@example.com", "Rec A");
        String tokenB = registerAndLogin("recB_dev", "recB_dev@example.com", "Rec B");

        mockMvc.perform(put("/api/interests/me")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new UpdateInterestsRequest(List.of(interest.getId())))))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/interests/me")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new UpdateInterestsRequest(List.of(interest.getId())))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/recommendations/friends")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("recB_dev"))
                .andExpect(jsonPath("$[0].sharedInterestsCount").value(1));
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
