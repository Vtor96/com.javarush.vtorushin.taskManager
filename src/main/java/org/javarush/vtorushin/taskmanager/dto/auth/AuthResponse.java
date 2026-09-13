package org.javarush.vtorushin.taskmanager.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String tokenType;

    public AuthResponse(String token) {
        this.token = token;
        this.tokenType = "Bearer";
    }
}
