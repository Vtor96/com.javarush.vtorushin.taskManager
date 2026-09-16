package org.javarush.vtorushin.taskmanager.model.repository;

import org.javarush.vtorushin.taskmanager.model.entity.Task;
import org.javarush.vtorushin.taskmanager.model.entity.TaskStatus;
import org.javarush.vtorushin.taskmanager.model.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    long countByDeletedFalse();

    List<Task> findAllByUserAndDeletedFalse(User user);

    List<Task> findAllByUserAndStatusAndDeletedFalse(User user, TaskStatus status);

    List<Task> findAllByUserAndDeadlineBeforeAndDeletedFalse(User user, LocalDateTime deadline);

    List<Task> findAllByDeletedFalse();

    List<Task> findAllByDeletedFalseAndStatus(TaskStatus status);

    List<Task> findAllByDeletedFalseAndDeadlineBefore(LocalDateTime deadline);

    Optional<Task> findByIdAndDeletedFalse(Long id);
}
