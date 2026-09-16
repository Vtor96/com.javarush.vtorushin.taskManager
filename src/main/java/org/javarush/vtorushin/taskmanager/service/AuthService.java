package org.javarush.vtorushin.taskmanager.service;

import org.javarush.vtorushin.taskmanager.dto.auth.AuthResponse;
import org.javarush.vtorushin.taskmanager.dto.auth.LoginRequest;
import org.javarush.vtorushin.taskmanager.dto.auth.RegisterRequest;
import org.javarush.vtorushin.taskmanager.exception.UserAlreadyExistsException;
import org.javarush.vtorushin.taskmanager.model.entity.User;
import org.javarush.vtorushin.taskmanager.model.repository.UserRepository;
import org.javarush.vtorushin.taskmanager.security.JwtTokenProvider;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final TaskMetrics taskMetrics;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider,
                       TaskMetrics taskMetrics) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.taskMetrics = taskMetrics;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())
                || userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(
                    "Пользователь с таким именем или email уже существует");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            // Если между проверкой и сохранением кто-то успел создать пользователя
            throw new UserAlreadyExistsException(
                    "Пользователь с таким именем или email уже существует", e);
        }

        String token = jwtTokenProvider.generateToken(user.getUsername());
        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()));

            return new AuthResponse(jwtTokenProvider.generateToken(authentication.getName()));
        } catch (BadCredentialsException e) {
            taskMetrics.loginsFailed.increment();
            throw e;
        }
    }
}
