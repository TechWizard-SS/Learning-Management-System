package com.sstu.LearningManagementSystem.controller;

import com.sstu.LearningManagementSystem.model.User;
import com.sstu.LearningManagementSystem.model.dto.achievementDto.AchievementCreateDto;
import com.sstu.LearningManagementSystem.model.dto.achievementDto.AchievementResponseDto;
import com.sstu.LearningManagementSystem.model.dto.achievementDto.AchievementUpdateDto;
import com.sstu.LearningManagementSystem.service.AchievementService;
import com.sstu.LearningManagementSystem.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;
    private final UserService userService;

    // Вспомогательный метод для получения ID текущего пользователя
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new RuntimeException("User not authenticated");
        }
        String username = auth.getName();
        User user = userService.findByUsername(username);
        return user.getId();
    }

    /**
     * Создать достижение (тип).
     * POST /api/achievements
     * Требует currentUserId (из аутентификации) - только для преподавателя/админа
     */
    @PostMapping
    public ResponseEntity<AchievementResponseDto> createAchievement(@Valid @RequestBody AchievementCreateDto createDto) {
        Long currentUserId = getCurrentUserId();
        AchievementResponseDto achievement = achievementService.createAchievement(currentUserId, createDto);
        return new ResponseEntity<>(achievement, HttpStatus.CREATED); // 201 Created
    }

    /**
     * Обновить достижение (тип).
     * PUT /api/achievements/{id}
     * Требует currentUserId (из аутентификации) - только для преподавателя/админа
     */
    @PutMapping("/{id}")
    public ResponseEntity<AchievementResponseDto> updateAchievement(
            @PathVariable Long id,
            @Valid @RequestBody AchievementUpdateDto updateDto) {
        Long currentUserId = getCurrentUserId();
        AchievementResponseDto achievement = achievementService.updateAchievement(currentUserId, id, updateDto);
        return ResponseEntity.ok(achievement);
    }

    /**
     * Удалить достижение (тип).
     * DELETE /api/achievements/{id}
     * Требует currentUserId (из аутентификации) - только для преподавателя/админа
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAchievement(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        achievementService.deleteAchievement(currentUserId, id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    /**
     * Получить достижение по ID.
     * GET /api/achievements/{id}
     * Требует currentUserId (из аутентификации)
     */
    @GetMapping("/{id}")
    public ResponseEntity<AchievementResponseDto> getAchievementById(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        AchievementResponseDto achievement = achievementService.getAchievementById(currentUserId, id);
        return ResponseEntity.ok(achievement);
    }

    /**
     * Получить достижение по названию.
     * GET /api/achievements/by-title?title={title}
     * Требует currentUserId (из аутентификации)
     */
    @GetMapping("/by-title")
    public ResponseEntity<AchievementResponseDto> getAchievementByTitle(@RequestParam String title) {
        Long currentUserId = getCurrentUserId();
        AchievementResponseDto achievement = achievementService.getAchievementByTitle(currentUserId, title);
        return ResponseEntity.ok(achievement);
    }

    /**
     * Получить все достижения.
     * GET /api/achievements
     * Требует currentUserId (из аутентификации)
     */
    @GetMapping
    public ResponseEntity<List<AchievementResponseDto>> getAllAchievements() {
        Long currentUserId = getCurrentUserId();
        List<AchievementResponseDto> achievements = achievementService.getAllAchievements(currentUserId);
        return ResponseEntity.ok(achievements);
    }
}
