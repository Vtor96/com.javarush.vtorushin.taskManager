package org.javarush.vtorushin.taskmanager.dto.task;

import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.javarush.vtorushin.taskmanager.model.entity.TaskStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskUpdateRequest {

    @Size(min = 1, max = 200, message = "Заголовок задачи должен быть от 1 до 200 символов")
    private String title;

    @Size(max = 2000, message = "Описание задачи не должно превышать 2000 символов")
    private String description;

    private TaskStatus status;

    private LocalDateTime deadline;
}
