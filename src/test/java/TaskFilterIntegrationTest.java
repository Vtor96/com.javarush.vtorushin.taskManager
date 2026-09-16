import org.javarush.vtorushin.taskmanager.dto.task.TaskUpdateRequest;
import org.javarush.vtorushin.taskmanager.model.entity.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TaskFilterIntegrationTest extends AbstractIntegrationTest {

    /** Меняет статус задачи через PUT-запрос */
    private void changeStatus(String token, Long taskId, TaskStatus status) throws Exception {
        var req = new TaskUpdateRequest();
        req.setStatus(status);

        mockMvc.perform(put("/api/tasks/" + taskId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    /** Фильтр по статусу TODO — возвращает только задачи со статусом TODO */
    @Test
    void filterByStatusTodo() throws Exception {
        String token = registerAndGetToken();
        Long t1 = createTask(token, "TODO задача", "опис");
        Long t2 = createTask(token, "Будет IN_PROGRESS", "опис");
        Long t3 = createTask(token, "Будет DONE", "опис");

        changeStatus(token, t2, TaskStatus.IN_PROGRESS);
        changeStatus(token, t3, TaskStatus.DONE);

        mockMvc.perform(get("/api/tasks?status=TODO")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("TODO задача"));
    }

    /** Фильтр по статусу IN_PROGRESS — возвращает только задачи в работе */
    @Test
    void filterByStatusInProgress() throws Exception {
        String token = registerAndGetToken();
        Long t1 = createTask(token, "TODO задача", "опис");
        Long t2 = createTask(token, "IN_PROGRESS задача", "опис");

        changeStatus(token, t2, TaskStatus.IN_PROGRESS);

        mockMvc.perform(get("/api/tasks?status=IN_PROGRESS")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("IN_PROGRESS задача"));
    }

    /** Фильтр по статусу DONE — возвращает только завершённые задачи */
    @Test
    void filterByStatusDone() throws Exception {
        String token = registerAndGetToken();
        Long t1 = createTask(token, "TODO задача", "опис");
        Long t2 = createTask(token, "DONE задача", "опис");

        changeStatus(token, t2, TaskStatus.DONE);

        mockMvc.perform(get("/api/tasks?status=DONE")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("DONE задача"));
    }

    /** Фильтр по дедлайну — возвращает задачи с дедлайном раньше указанной даты */
    @Test
    void filterByDeadlineBefore() throws Exception {
        String token = registerAndGetToken();

        LocalDateTime soon = LocalDateTime.now().plusDays(1);
        LocalDateTime later = LocalDateTime.now().plusDays(10);

        createTask(token, "Скоро", "опис", soon);
        createTask(token, "Позже", "опис", later);

        String deadlineParam = soon.plusHours(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        mockMvc.perform(get("/api/tasks?deadlineBefore=" + deadlineParam)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Скоро"));
    }

    /** Фильтр по дедлайну — возвращает все задачи, попадающие в диапазон */
    @Test
    void filterByDeadlineBeforeReturnsAllMatching() throws Exception {
        String token = registerAndGetToken();

        LocalDateTime d1 = LocalDateTime.now().plusDays(1);
        LocalDateTime d2 = LocalDateTime.now().plusDays(2);
        LocalDateTime d3 = LocalDateTime.now().plusDays(5);

        createTask(token, "День 1", "опис", d1);
        createTask(token, "День 2", "опис", d2);
        createTask(token, "День 5", "опис", d3);

        String deadlineParam = d2.plusHours(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        mockMvc.perform(get("/api/tasks?deadlineBefore=" + deadlineParam)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void filterNoResults() throws Exception {
        String token = registerAndGetToken();
        createTask(token, "Задача", "опис");

        mockMvc.perform(get("/api/tasks?status=DONE")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
