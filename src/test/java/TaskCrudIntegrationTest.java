import org.javarush.vtorushin.taskmanager.dto.task.TaskCreateRequest;
import org.javarush.vtorushin.taskmanager.dto.task.TaskUpdateRequest;
import org.javarush.vtorushin.taskmanager.model.entity.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TaskCrudIntegrationTest extends AbstractIntegrationTest {

    // --- Создание ---

    @Test
    void createTaskSuccess() throws Exception {
        String token = registerAndGetToken();
        var req = new TaskCreateRequest("Тестовая задача", "Описание", null);

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Тестовая задача"))
                .andExpect(jsonPath("$.description").value("Описание"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    // --- Чтение ---

    @Test
    void getTaskByIdSuccess() throws Exception {
        String token = registerAndGetToken();
        Long taskId = createTask(token, "Задача для получения", "Описание");

        mockMvc.perform(get("/api/tasks/" + taskId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("Задача для получения"));
    }

    @Test
    void getTaskByIdNotFound() throws Exception {
        String token = registerAndGetToken();

        mockMvc.perform(get("/api/tasks/99999")
                        .header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllTasks() throws Exception {
        String token = registerAndGetToken();
        createTask(token, "Задача 1", "Описание 1");
        createTask(token, "Задача 2", "Описание 2");
        createTask(token, "Задача 3", "Описание 3");

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void getAllTasksEmptyList() throws Exception {
        String token = registerAndGetToken();

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // --- Обновление ---

    @Test
    void updateTaskSuccess() throws Exception {
        String token = registerAndGetToken();
        Long taskId = createTask(token, "Старый заголовок", "Старое описание");

        var updateReq = new TaskUpdateRequest();
        updateReq.setTitle("Новый заголовок");
        updateReq.setDescription("Новое описание");
        updateReq.setStatus(TaskStatus.IN_PROGRESS);

        mockMvc.perform(put("/api/tasks/" + taskId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Новый заголовок"))
                .andExpect(jsonPath("$.description").value("Новое описание"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void updateTaskNotFound() throws Exception {
        String token = registerAndGetToken();
        var updateReq = new TaskUpdateRequest();
        updateReq.setTitle("Обновление несуществующей");

        mockMvc.perform(put("/api/tasks/99999")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTaskPartial() throws Exception {
        String token = registerAndGetToken();
        Long taskId = createTask(token, "Оригинал", "Оригинальное описание");

        var updateReq = new TaskUpdateRequest();
        updateReq.setStatus(TaskStatus.DONE);

        mockMvc.perform(put("/api/tasks/" + taskId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Оригинал"))
                .andExpect(jsonPath("$.description").value("Оригинальное описание"))
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    // --- Удаление ---

    @Test
    void deleteTaskSuccess() throws Exception {
        String token = registerAndGetToken();
        Long taskId = createTask(token, "Задача для удаления", "Описание");

        mockMvc.perform(delete("/api/tasks/" + taskId)
                        .header("Authorization", token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks/" + taskId)
                        .header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTaskNotFound() throws Exception {
        String token = registerAndGetToken();

        mockMvc.perform(delete("/api/tasks/99999")
                        .header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    void taskNotVisibleAfterDeletion() throws Exception {
        String token = registerAndGetToken();
        Long taskId = createTask(token, "Видимая задача", "опис");

        mockMvc.perform(delete("/api/tasks/" + taskId)
                        .header("Authorization", token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
