package com.mib.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mib.backend.dto.request.LoginRequest;
import com.mib.backend.dto.request.RegisterRequest;
import com.mib.backend.entity.Achievement;
import com.mib.backend.entity.ShopItem;
import com.mib.backend.entity.ShopItemType;
import com.mib.backend.repository.AchievementRepository;
import com.mib.backend.repository.ShopItemRepository;
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
class ShopAndAchievementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ShopItemRepository shopItemRepository;

    @Autowired
    private AchievementRepository achievementRepository;

    @Test
    void deveComprarEquiparEDesequiparItemDaLoja() throws Exception {
        ShopItem item = shopItemRepository.save(
                new ShopItem("Moldura de Teste", "Item de teste", ShopItemType.FRAME, 5, null, false));

        String token = registerAndLogin("loja_dev", "loja_dev@example.com", "Loja Dev");

        // Apos o primeiro login, o bonus diario (10 XP) ja cobre o preco do item (5 XP)
        mockMvc.perform(post("/api/shop/items/" + item.getId() + "/purchase")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.equipped").value(false));

        mockMvc.perform(get("/api/xp/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.xpWallet").value(5)); // 10 ganhos - 5 gastos

        mockMvc.perform(post("/api/inventory/" + item.getId() + "/equip")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.equipped").value(true));

        mockMvc.perform(get("/api/profile/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.equippedItems[0].name").value("Moldura de Teste"));

        mockMvc.perform(post("/api/inventory/" + item.getId() + "/unequip")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.equipped").value(false));

        // Nao pode comprar de novo o que ja possui
        mockMvc.perform(post("/api/shop/items/" + item.getId() + "/purchase")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }

    @Test
    void naoDevePermitirComprarSemXpSuficiente() throws Exception {
        ShopItem item = shopItemRepository.save(
                new ShopItem("Item Caro", "Item de teste caro", ShopItemType.THEME, 999999, null, false));

        String token = registerAndLogin("pobre_dev", "pobre_dev@example.com", "Pobre Dev");

        mockMvc.perform(post("/api/shop/items/" + item.getId() + "/purchase")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveConcederConquistaDePrimeiraAmizadeParaAmbosOsUsuarios() throws Exception {
        achievementRepository.save(new Achievement("FIRST_FRIEND", "Primeira Amizade", "Fez um amigo", 20, null));

        String tokenA = registerAndLogin("conquistaA_dev", "conquistaA@example.com", "Conquista A");
        String userIdB = registerAndGetUserId("conquistaB_dev", "conquistaB@example.com", "Conquista B");
        String tokenB = login("conquistaB_dev", "SenhaForte123");

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

        mockMvc.perform(get("/api/achievements").header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code == 'FIRST_FRIEND')].unlocked").value(org.hamcrest.Matchers.hasItem(true)));

        mockMvc.perform(get("/api/achievements").header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.code == 'FIRST_FRIEND')].unlocked").value(org.hamcrest.Matchers.hasItem(true)));
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
