package org.javarush.vtorushin.taskmanager.dto.auth;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Имя пользователя обязательно для заполнения")
    private String username;

    @NotBlank(message = "Пароль обязателен для заполнения")
    private String password;
}
