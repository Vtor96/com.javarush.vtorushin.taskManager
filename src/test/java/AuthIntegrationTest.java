import org.javarush.vtorushin.taskmanager.dto.auth.LoginRequest;
import org.javarush.vtorushin.taskmanager.dto.auth.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthIntegrationTest extends AbstractIntegrationTest {

    // --- Регистрация ---

    @Test
    void registerSuccess() throws Exception {
        var req = new RegisterRequest(uniqueUsername(), "password123", uniqueEmail());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void registerDuplicateUsername() throws Exception {
        String username = uniqueUsername();
        var req1 = new RegisterRequest(username, "password123", uniqueEmail());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        var req2 = new RegisterRequest(username, "password456", uniqueEmail());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isConflict());
    }

    @Test
    void registerDuplicateEmail() throws Exception {
        String email = uniqueEmail();
        var req1 = new RegisterRequest(uniqueUsername(), "password123", email);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        var req2 = new RegisterRequest(uniqueUsername(), "password456", email);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isConflict());
    }

    // --- Логин ---

    @Test
    void loginSuccess() throws Exception {
        String username = uniqueUsername();
        String password = "password123";
        registerAndGetToken(username, password);

        var loginReq = new LoginRequest(username, password);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void loginWrongPassword() throws Exception {
        String username = uniqueUsername();
        registerAndGetToken(username, "password123");

        var loginReq = new LoginRequest(username, "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginNonExistentUser() throws Exception {
        var loginReq = new LoginRequest("ghost_user", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized());
    }
}
