package org.javarush.vtorushin.taskmanager.service;

import org.javarush.vtorushin.taskmanager.dto.task.TaskCreateRequest;
import org.javarush.vtorushin.taskmanager.dto.task.TaskResponse;
import org.javarush.vtorushin.taskmanager.dto.task.TaskUpdateRequest;
import org.javarush.vtorushin.taskmanager.exception.ResourceAccessDeniedException;
import org.javarush.vtorushin.taskmanager.exception.ResourceNotFoundException;
import org.javarush.vtorushin.taskmanager.model.entity.Task;
import org.javarush.vtorushin.taskmanager.model.entity.TaskStatus;
import org.javarush.vtorushin.taskmanager.model.entity.User;
import org.javarush.vtorushin.taskmanager.model.repository.TaskRepository;
import org.javarush.vtorushin.taskmanager.model.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public TaskResponse createTask(TaskCreateRequest request, UserDetails userDetails) {
        User user = getUserByUsername(userDetails.getUsername());

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDeadline(request.getDeadline());
        task.setUser(user);

        Task savedTask = taskRepository.save(task);
        logger.info("Создана задача id={} для пользователя '{}'",
                savedTask.getId(),
                user.getUsername());
        return mapToResponse(savedTask);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasksForUser(UserDetails userDetails) {
        User user = getUserByUsername(userDetails.getUsername());
        return taskRepository.findAllByUserAndDeletedFalse(user).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByStatus(UserDetails userDetails, TaskStatus status) {
        User user = getUserByUsername(userDetails.getUsername());
        return taskRepository.findAllByUserAndStatusAndDeletedFalse(user, status).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksWithDeadlineBefore(UserDetails userDetails, LocalDateTime deadline) {
        User user = getUserByUsername(userDetails.getUsername());
        return taskRepository.findAllByUserAndDeadlineBeforeAndDeletedFalse(user, deadline).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long taskId, UserDetails userDetails) {
        Task task = findTaskAndCheckOwnership(taskId, userDetails);
        return mapToResponse(task);
    }

    public TaskResponse updateTask(Long taskId, TaskUpdateRequest request, UserDetails userDetails) {
        Task task = findTaskAndCheckOwnership(taskId, userDetails);

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getDeadline() != null) {
            task.setDeadline(request.getDeadline());
        }

        Task updatedTask = taskRepository.save(task);
        logger.info("Обновлена задача id={} пользователем '{}'",
                updatedTask.getId(),
                userDetails.getUsername());
        return mapToResponse(updatedTask);
    }

    public void deleteTask(Long taskId, UserDetails userDetails) {
        Task task = findTaskAndCheckOwnership(taskId, userDetails);
        task.setDeleted(true);
        taskRepository.save(task);
        logger.info("Удалена задача id={} пользователем '{}'", taskId, userDetails.getUsername());
    }

    private Task findTaskAndCheckOwnership(Long taskId, UserDetails userDetails) {
        Task task = taskRepository.findByIdAndDeletedFalse(taskId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Задача с id " + taskId + " не найдена"));

        if (!task.getUser().getUsername().equals(userDetails.getUsername())) {
            logger.warn("Попытка доступа к задаче id={} пользователем '{}'",
                    taskId,
                    userDetails.getUsername());
            throw new ResourceAccessDeniedException("У вас нет доступа к этой задаче");
        }
        return task;
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Пользователь '" + username + "' не найден"));
    }

    private TaskResponse mapToResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getDeadline(),
                task.getCreatedAt(),
                task.getUpdatedAt());
    }
}
