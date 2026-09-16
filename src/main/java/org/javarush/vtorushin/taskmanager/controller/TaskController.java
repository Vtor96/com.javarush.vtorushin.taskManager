package org.javarush.vtorushin.taskmanager.controller;

import jakarta.validation.Valid;

import org.javarush.vtorushin.taskmanager.dto.task.TaskCreateRequest;
import org.javarush.vtorushin.taskmanager.dto.task.TaskResponse;
import org.javarush.vtorushin.taskmanager.dto.task.TaskUpdateRequest;
import org.javarush.vtorushin.taskmanager.model.entity.TaskStatus;
import org.javarush.vtorushin.taskmanager.service.TaskService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody TaskCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        TaskResponse response = taskService.createTask(request, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<TaskResponse>> getAllTasks(
            @RequestParam(name = "status", required = false) TaskStatus status,
            @RequestParam(name = "deadlineBefore", required = false) LocalDateTime deadlineBefore,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (status != null) {
            return ResponseEntity.ok(taskService.getTasksByStatus(userDetails, status));
        }
        if (deadlineBefore != null) {
            return ResponseEntity.ok(taskService.getTasksWithDeadlineBefore(userDetails, deadlineBefore));
        }
        return ResponseEntity.ok(taskService.getAllTasksForUser(userDetails));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<TaskResponse> getTaskById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(taskService.getTaskById(id, userDetails));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(taskService.updateTask(id, request, userDetails));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        taskService.deleteTask(id, userDetails);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> restoreTask(@PathVariable Long id) {
        taskService.restoreTask(id);
        return ResponseEntity.noContent().build();
    }
}
