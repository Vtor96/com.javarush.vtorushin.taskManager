import com.fasterxml.jackson.databind.ObjectMapper;

import org.javarush.vtorushin.taskmanager.TaskManagerApplication;
import org.javarush.vtorushin.taskmanager.dto.auth.AuthResponse;
import org.javarush.vtorushin.taskmanager.dto.auth.RegisterRequest;
import org.javarush.vtorushin.taskmanager.dto.task.TaskCreateRequest;
import org.javarush.vtorushin.taskmanager.dto.task.TaskResponse;
import org.javarush.vtorushin.taskmanager.model.entity.Role;
import org.javarush.vtorushin.taskmanager.model.entity.User;
import org.javarush.vtorushin.taskmanager.model.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Базовый класс для интеграционных тестов.
 * Поднимает полный контекст Spring Boot с профилем "test" (H2 вместо PostgreSQL).
 */

@SpringBootTest(classes = TaskManagerApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    protected final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private static final AtomicInteger counter = new AtomicInteger(0);

    protected String uniqueUsername() {
        return "user" + counter.incrementAndGet();
    }

    protected String uniqueEmail() {
        return "email" + counter.incrementAndGet() + "@test.com";
    }

    protected String registerAndGetToken() throws Exception {
        return registerAndGetToken(uniqueUsername(), "password123");
    }

    /** Регистрирует пользователя с заданными именем и паролем, возвращает Bearer-токен */
    protected String registerAndGetToken(String username, String password) throws Exception {
        var req = new RegisterRequest(username, password, uniqueEmail());
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(body, AuthResponse.class);
        return "Bearer " + authResponse.getToken();
    }

    /** Создаёт задачу без дедлайна, возвращает её id */
    protected Long createTask(String token, String title, String description) throws Exception {
        return createTask(token, title, description, null);
    }

    /** Создаёт задачу с дедлайном, возвращает её id */
    protected Long createTask(String token, String title, String description, LocalDateTime deadline) throws Exception {
        var req = new TaskCreateRequest(title, description, deadline);
        MvcResult result = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        return objectMapper.readValue(body, TaskResponse.class).getId();
    }

    @Autowired
    protected UserRepository userRepository;

    /** Регистрирует пользователя, выдаёт ему роль ADMIN, возвращает Bearer-токен */
    protected String registerAndGetAdminToken() throws Exception {
        String username = uniqueUsername();
        String token = registerAndGetToken(username, "password123");

        User user = userRepository.findByUsername(username).orElseThrow();
        user.setRole(Role.ROLE_ADMIN);
        userRepository.save(user);

        return token;
    }
}
