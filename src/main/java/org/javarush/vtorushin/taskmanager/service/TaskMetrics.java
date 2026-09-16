package org.javarush.vtorushin.taskmanager.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import org.javarush.vtorushin.taskmanager.model.repository.TaskRepository;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class TaskMetrics {

    public final Counter tasksCreated;
    public final Counter tasksDeleted;
    public final Counter tasksRestored;
    public final Counter loginsFailed;
    public final AtomicInteger tasksActive;

    public TaskMetrics(MeterRegistry registry, TaskRepository taskRepository) {
        this.tasksCreated = registry.counter("task.created.counter");
        this.tasksDeleted = registry.counter("task.deleted.counter");
        this.tasksRestored = registry.counter("task.restored.counter");
        this.loginsFailed = registry.counter("login.failed.counter");

        long activeCount = taskRepository.countByDeletedFalse();
        this.tasksActive = new AtomicInteger((int) activeCount);
        registry.gauge("task.active.gauge", tasksActive);
    }
}
