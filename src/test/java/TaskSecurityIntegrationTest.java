import org.javarush.vtorushin.taskmanager.dto.task.TaskCreateRequest;
import org.javarush.vtorushin.taskmanager.dto.task.TaskUpdateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TaskSecurityIntegrationTest extends AbstractIntegrationTest {

    /** Создание задачи без токена — 403 Forbidden */
    @Test
    void createTaskWithoutToken() throws Exception {
        var req = new TaskCreateRequest("Без токена", "опис", null);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    /** Получение задачи без токена — 403 Forbidden */
    @Test
    void getTaskWithoutToken() throws Exception {
        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isForbidden());
    }

    /** Получение чужой задачи — 403 Forbidden */
    @Test
    void getOtherUserTaskForbidden() throws Exception {
        String tokenA = registerAndGetToken();
        String tokenB = registerAndGetToken();

        Long taskA = createTask(tokenA, "Задача A", "опис");

        mockMvc.perform(get("/api/tasks/" + taskA)
                        .header("Authorization", tokenB))
                .andExpect(status().isForbidden());
    }

    /** Обновление чужой задачи — 403 Forbidden */
    @Test
    void updateOtherUserTaskForbidden() throws Exception {
        String tokenA = registerAndGetToken();
        String tokenB = registerAndGetToken();

        Long taskA = createTask(tokenA, "Задача A", "опис");

        var updateReq = new TaskUpdateRequest();
        updateReq.setTitle("Взлом");

        mockMvc.perform(put("/api/tasks/" + taskA)
                        .header("Authorization", tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden());
    }

    /** Удаление чужой задачи — 403 Forbidden */
    @Test
    void deleteOtherUserTaskForbidden() throws Exception {
        String tokenA = registerAndGetToken();
        String tokenB = registerAndGetToken();

        Long taskA = createTask(tokenA, "Задача A", "опис");

        mockMvc.perform(delete("/api/tasks/" + taskA)
                        .header("Authorization", tokenB))
                .andExpect(status().isForbidden());
    }

    /** Запрос с невалидным токеном — 403 Forbidden */
    @Test
    void invalidTokenRejected() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isForbidden());
    }

    /** Каждый пользователь видит только свои задачи */
    @Test
    void eachUserSeesOnlyOwnTasks() throws Exception {
        String tokenA = registerAndGetToken();
        String tokenB = registerAndGetToken();

        createTask(tokenA, "Задача A1", "опис");
        createTask(tokenA, "Задача A2", "опис");
        createTask(tokenB, "Задача B1", "опис");

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
