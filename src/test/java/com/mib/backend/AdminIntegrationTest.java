package com.mib.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mib.backend.dto.request.LoginRequest;
import com.mib.backend.dto.request.RegisterRequest;
import com.mib.backend.dto.request.ShopItemRequest;
import com.mib.backend.dto.request.SuspendUserRequest;
import com.mib.backend.entity.Role;
import com.mib.backend.entity.RoleName;
import com.mib.backend.entity.User;
import com.mib.backend.repository.RoleRepository;
import com.mib.backend.repository.UserRepository;
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
class AdminIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void adminDevePodeSuspenderUsuarioECriarItemNaLoja() throws Exception {
        String adminToken = registerLoginAndPromoteToAdmin("admin_dev", "admin_dev@example.com", "Admin Dev");
        String targetUserId = registerAndGetUserId("alvo_dev", "alvo_dev@example.com", "Alvo Dev");

        mockMvc.perform(post("/api/admin/users/" + targetUserId + "/suspend")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new SuspendUserRequest(24, "Teste de suspensao"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suspendedUntil").exists());

        mockMvc.perform(get("/api/admin/users/" + targetUserId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suspendedUntil").exists());

        mockMvc.perform(post("/api/admin/shop/items")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new ShopItemRequest("Item Admin Teste", "Descricao", "ICON", 50, null, false))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Item Admin Teste"));

        mockMvc.perform(get("/api/admin/stats").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").isNumber());

        mockMvc.perform(get("/api/admin/logs").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].action").exists());
    }

    @Test
    void usuarioComumNaoDeveAcessarEndpointsAdministrativos() throws Exception {
        String userToken = registerAndLogin("comum_dev", "comum_dev@example.com", "Comum Dev");

        mockMvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    private String registerLoginAndPromoteToAdmin(String username, String email, String displayName) throws Exception {
        String token = registerAndLogin(username, email, displayName);

        User user = userRepository.findByUsernameIgnoreCase(username).orElseThrow();
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_ADMIN)));
        user.getRoles().add(adminRole);
        userRepository.save(user);

        return token;
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
