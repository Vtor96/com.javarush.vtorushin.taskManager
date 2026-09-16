import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TaskAdminIntegrationTest extends AbstractIntegrationTest {

    /** Админ видит все задачи всех пользователей, обычный юзер — только свои */
    @Test
    void adminSeesAllTasks() throws Exception {
        String tokenA = registerAndGetToken();
        String tokenB = registerAndGetToken();
        String tokenAdmin = registerAndGetAdminToken();

        Long taskA = createTask(tokenA, "Задача пользователя A", "опис");
        Long taskB = createTask(tokenB, "Задача пользователя B", "опис");

        // Юзер A видит только свою
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title", hasItem("Задача пользователя A")))
                .andExpect(jsonPath("$[*].title", not(hasItem("Задача пользователя B"))));

        // Админ видит обе
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title", hasItem("Задача пользователя A")))
                .andExpect(jsonPath("$[*].title", hasItem("Задача пользователя B")));
    }

    /** Админ может удалить чужую задачу — 204 */
    @Test
    void adminCanDeleteAnyTask() throws Exception {
        String tokenA = registerAndGetToken();
        String tokenAdmin = registerAndGetAdminToken();

        Long taskId = createTask(tokenA, "Чужая задача", "опис");

        mockMvc.perform(delete("/api/tasks/" + taskId)
                        .header("Authorization", tokenAdmin))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks/" + taskId)
                        .header("Authorization", tokenA))
                .andExpect(status().isNotFound());
    }

    /** Админ может восстановить удалённую задачу — 204, задача снова доступна */
    @Test
    void adminCanRestoreDeletedTask() throws Exception {
        String tokenA = registerAndGetToken();
        String tokenAdmin = registerAndGetAdminToken();

        Long taskId = createTask(tokenA, "Задача для восстановления", "опис");

        mockMvc.perform(delete("/api/tasks/" + taskId)
                        .header("Authorization", tokenAdmin))
                .andExpect(status().isNoContent());

        mockMvc.perform(patch("/api/tasks/" + taskId + "/restore")
                        .header("Authorization", tokenAdmin))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks/" + taskId)
                        .header("Authorization", tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Задача для восстановления"));
    }

    /** Обычный юзер не может восстановить задачу — 403 Forbidden */
    @Test
    void userCannotRestoreTask() throws Exception {
        String token = registerAndGetToken();
        Long taskId = createTask(token, "Задача", "опис");

        mockMvc.perform(delete("/api/tasks/" + taskId)
                        .header("Authorization", token))
                .andExpect(status().isNoContent());

        mockMvc.perform(patch("/api/tasks/" + taskId + "/restore")
                        .header("Authorization", token))
                .andExpect(status().isForbidden());
    }

    /** Обычный юзер не может удалить чужую задачу — 403 Forbidden */
    @Test
    void userCannotDeleteOtherUserTask() throws Exception {
        String tokenA = registerAndGetToken();
        String tokenB = registerAndGetToken();

        Long taskId = createTask(tokenA, "Задача A", "опис");

        mockMvc.perform(delete("/api/tasks/" + taskId)
                        .header("Authorization", tokenB))
                .andExpect(status().isForbidden());
    }

    /** Админ может получить чужую задачу — 200 */
    @Test
    void adminCanGetAnyTask() throws Exception {
        String tokenA = registerAndGetToken();
        String tokenAdmin = registerAndGetAdminToken();

        Long taskId = createTask(tokenA, "Чужая задача", "опис");

        mockMvc.perform(get("/api/tasks/" + taskId)
                        .header("Authorization", tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Чужая задача"));
    }

    /** Восстановление несуществующей задачи — 404 Not Found */
    @Test
    void restoreNonExistentTask() throws Exception {
        String tokenAdmin = registerAndGetAdminToken();

        mockMvc.perform(patch("/api/tasks/99999/restore")
                        .header("Authorization", tokenAdmin))
                .andExpect(status().isNotFound());
    }

    /** Восстановление уже активной задачи — 400 Bad Request */
    @Test
    void restoreAlreadyRestoredTask() throws Exception {
        String token = registerAndGetToken();
        String tokenAdmin = registerAndGetAdminToken();

        Long taskId = createTask(token, "Задача", "опис");

        mockMvc.perform(patch("/api/tasks/" + taskId + "/restore")
                        .header("Authorization", tokenAdmin))
                .andExpect(status().isBadRequest());
    }
}
