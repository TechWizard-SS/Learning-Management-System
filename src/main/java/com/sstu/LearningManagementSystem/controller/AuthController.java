package com.sstu.LearningManagementSystem.controller;

import com.sstu.LearningManagementSystem.config.AuthRequest;
import com.sstu.LearningManagementSystem.config.JwtUtil;
import com.sstu.LearningManagementSystem.config.MyUserDetailsService;
import com.sstu.LearningManagementSystem.model.dto.userDto.UserCreateDto;
import com.sstu.LearningManagementSystem.model.dto.userDto.UserResponseDto;
import com.sstu.LearningManagementSystem.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final MyUserDetailsService userDetailsService;
    private final UserService userService;

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            String token = jwtUtil.generateToken(userDetails);
            return ResponseEntity.ok(token);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Неверный username или пароль");
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Ошибка аутентификации: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserCreateDto createDto) {
        // Вызов userService.createUser, затем отправка verification email
        UserResponseDto user = userService.createUser(createDto);
        // Добавьте email-service.sendVerification(user.getEmail(), token);
        return ResponseEntity.ok(user);
    }
}