package com.sstu.LearningManagementSystem.controller;

import com.sstu.LearningManagementSystem.Email.*;
import com.sstu.LearningManagementSystem.config.AuthRequest;
import com.sstu.LearningManagementSystem.config.JwtUtil;
import com.sstu.LearningManagementSystem.config.MyUserDetailsService;
import com.sstu.LearningManagementSystem.model.User;
import com.sstu.LearningManagementSystem.model.dto.userDto.UserCreateDto;
import com.sstu.LearningManagementSystem.model.dto.userDto.UserResponseDto;
import com.sstu.LearningManagementSystem.repository.UserRepository;
import com.sstu.LearningManagementSystem.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final MyUserDetailsService userDetailsService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final ResetTokenRepository resetTokenRepository;
    private final PasswordEncoder passwordEncoder;

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
        UserResponseDto userDto = userService.createUser(createDto);
        User user = userService.findById(userDto.getId());

        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusHours(2);
        VerificationToken vt = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(expiry)
                .build();
        verificationTokenRepository.save(vt);

        emailService.sendVerificationEmail(user.getEmail(), token);

        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/verify")
    @Transactional
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        Optional<VerificationToken> optionalVt = verificationTokenRepository.findByTokenWithUser(token);

        if (optionalVt.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid or expired token. Please register again.");
        }

        VerificationToken vt = optionalVt.get();

        if (vt.getExpiryDate().isBefore(LocalDateTime.now())) {
            userRepository.delete(vt.getUser());
            verificationTokenRepository.delete(vt);
            return ResponseEntity.badRequest().body("Token expired. Please register again.");
        }

        User user = vt.getUser();
        user.setVerified(true);
        userRepository.save(user);
        verificationTokenRepository.delete(vt);

        return ResponseEntity.ok("Email verified! You can now login.");
    }

    @PostMapping("/forgot")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));

        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusHours(1);
        ResetToken rt = ResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(expiry)
                .build();
        resetTokenRepository.save(rt);

        emailService.sendResetEmail(email, token);
        return ResponseEntity.ok("Reset link sent to your email.");
    }

    @PostMapping("/reset")
    @Transactional
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestBody Map<String, String> body) {
        String newPassword = body.get("newPassword");

        Optional<ResetToken> optionalRt = resetTokenRepository.findByTokenWithUser(token);
        if (optionalRt.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid token.");
        }

        ResetToken rt = optionalRt.get();
        if (rt.getExpiryDate().isBefore(LocalDateTime.now())) {
            resetTokenRepository.delete(rt);
            return ResponseEntity.badRequest().body("Token expired.");
        }

        User user = rt.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        resetTokenRepository.delete(rt);

        return ResponseEntity.ok("Password reset successfully!");
    }

    @GetMapping("/reset")
    public ResponseEntity<?> showResetForm(@RequestParam(required = false) String token) {
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body("Token is required.");
        }

        Optional<ResetToken> optionalRt = resetTokenRepository.findByToken(token);
        if (optionalRt.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid token.");
        }

        ResetToken rt = optionalRt.get();
        if (rt.getExpiryDate().isBefore(LocalDateTime.now())) {
            resetTokenRepository.delete(rt);
            return ResponseEntity.badRequest().body("Token expired.");
        }
        return ResponseEntity.ok().body("Вы находитесь на странице сброса пароля. Используйте POST /api/auth/reset с токеном и новым паролем.");
    }
}