import org.javarush.vtorushin.taskmanager.dto.auth.RegisterRequest;
import org.javarush.vtorushin.taskmanager.dto.task.TaskCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TaskValidationIntegrationTest extends AbstractIntegrationTest {

    // --- Валидация задач ---

    @Test
    void taskEmptyTitle() throws Exception {
        String token = registerAndGetToken();
        var req = new TaskCreateRequest("", "опис", null);

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages").isNotEmpty());
    }

    @Test
    void taskTitleTooLong() throws Exception {
        String token = registerAndGetToken();
        var req = new TaskCreateRequest("A".repeat(201), "опис", null);

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages").isNotEmpty());
    }

    @Test
    void taskDescriptionTooLong() throws Exception {
        String token = registerAndGetToken();
        var req = new TaskCreateRequest("Заголовок", "D".repeat(2001), null);

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages").isNotEmpty());
    }

    // --- Валидация регистрации ---

    @Test
    void registerEmptyUsername() throws Exception {
        var req = new RegisterRequest("", "password123", uniqueEmail());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerShortPassword() throws Exception {
        var req = new RegisterRequest(uniqueUsername(), "123", uniqueEmail());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerInvalidEmail() throws Exception {
        var req = new RegisterRequest(uniqueUsername(), "password123", "not-an-email");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUsernameTooLong() throws Exception {
        var req = new RegisterRequest("A".repeat(51), "password123", uniqueEmail());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
