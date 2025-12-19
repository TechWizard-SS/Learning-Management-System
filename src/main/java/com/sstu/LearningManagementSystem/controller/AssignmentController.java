package com.sstu.LearningManagementSystem.controller;

import com.sstu.LearningManagementSystem.model.User;
import com.sstu.LearningManagementSystem.model.dto.assignmentDto.AssignmentCreateDto;
import com.sstu.LearningManagementSystem.model.dto.assignmentDto.AssignmentResponseDto;
import com.sstu.LearningManagementSystem.model.dto.assignmentDto.AssignmentUpdateDto;
import com.sstu.LearningManagementSystem.service.AssignmentService;
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
@RequestMapping("/api/assignments") // Базовый URL
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final UserService userService; // Для получения currentUserId по username

    // Вспомогательный метод для получения ID текущего пользователя
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new RuntimeException("User not authenticated");
        }
        String username = auth.getName();
        // Предполагается, что у вас есть метод в UserService, возвращающий User по username
        User user = userService.findByUsername(username);
        return user.getId();
    }

    /**
     * Создать новое задание.
     * POST /api/assignments
     * Требует currentUserId (из аутентификации)
     */
    @PostMapping
    public ResponseEntity<AssignmentResponseDto> createAssignment(@Valid @RequestBody AssignmentCreateDto createDto) {
        Long currentUserId = getCurrentUserId();
        AssignmentResponseDto createdAssignment = assignmentService.createAssignment(currentUserId, createDto);
        return new ResponseEntity<>(createdAssignment, HttpStatus.CREATED);
    }

    /**
     * Получить задание по ID.
     * GET /api/assignments/{id}
     * Требует currentUserId (из аутентификации)
     */
    @GetMapping("/{id}")
    public ResponseEntity<AssignmentResponseDto> getAssignmentById(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        AssignmentResponseDto assignment = assignmentService.getAssignmentById(currentUserId, id);
        return ResponseEntity.ok(assignment);
    }

    /**
     * Обновить задание.
     * PUT /api/assignments/{id}
     * Требует currentUserId (из аутентификации)
     */
    @PutMapping("/{id}")
    public ResponseEntity<AssignmentResponseDto> updateAssignment(
            @PathVariable Long id,
            @Valid @RequestBody AssignmentUpdateDto updateDto) {
        Long currentUserId = getCurrentUserId();
        AssignmentResponseDto updatedAssignment = assignmentService.updateAssignment(currentUserId, id, updateDto);
        return ResponseEntity.ok(updatedAssignment);
    }

    /**
     * Удалить задание.
     * DELETE /api/assignments/{id}
     * Требует currentUserId (из аутентификации)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        assignmentService.deleteAssignment(currentUserId, id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    /**
     * Получить все задания по ID темы.
     * GET /api/assignments/by-topic/{topicId}
     * Требует currentUserId (из аутентификации)
     */
    @GetMapping("/by-topic/{topicId}")
    public ResponseEntity<List<AssignmentResponseDto>> getAssignmentsByTopicId(@PathVariable Long topicId) {
        Long currentUserId = getCurrentUserId();
        List<AssignmentResponseDto> assignments = assignmentService.findAssignmentsByTopicId(currentUserId, topicId);
        return ResponseEntity.ok(assignments);
    }
}
