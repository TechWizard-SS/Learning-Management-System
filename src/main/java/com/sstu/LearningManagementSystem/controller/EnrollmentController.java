package com.sstu.LearningManagementSystem.controller;

import com.sstu.LearningManagementSystem.model.User;
import com.sstu.LearningManagementSystem.model.dto.enrollmentDto.EnrollmentCreateDto;
import com.sstu.LearningManagementSystem.model.dto.enrollmentDto.EnrollmentResponseDto;
import com.sstu.LearningManagementSystem.model.dto.enrollmentDto.EnrollmentUpdateDto;
import com.sstu.LearningManagementSystem.service.EnrollmentService;
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
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final UserService userService;

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
     * Записать пользователя на курс (создать Enrollment).
     * POST /api/enrollments/enroll
     * Требует currentUserId (из аутентификации)
     */
    @PostMapping("/enroll")
    public ResponseEntity<EnrollmentResponseDto> enrollUser(@Valid @RequestBody EnrollmentCreateDto createDto) {
        Long currentUserId = getCurrentUserId();
        EnrollmentResponseDto enrollment = enrollmentService.enrollUser(currentUserId, createDto);
        return new ResponseEntity<>(enrollment, HttpStatus.CREATED); // 201 Created после создания
    }

    /**
     * Получить зачисление по ID.
     * GET /api/enrollments/{id}
     * Требует currentUserId (из аутентификации)
     */
    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentResponseDto> getEnrollmentById(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        EnrollmentResponseDto enrollment = enrollmentService.getEnrollmentById(currentUserId, id);
        return ResponseEntity.ok(enrollment);
    }

    /**
     * Получить все зачисления пользователя.
     * GET /api/enrollments/by-user
     * Требует currentUserId (из аутентификации)
     */
    @GetMapping("/by-user")
    public ResponseEntity<List<EnrollmentResponseDto>> getEnrollmentsByUserId() {
        Long currentUserId = getCurrentUserId();
        List<EnrollmentResponseDto> enrollments = enrollmentService.getEnrollmentsByUserId(currentUserId);
        return ResponseEntity.ok(enrollments);
    }

    /**
     * Получить все зачисления по ID курса (для преподавателя).
     * GET /api/enrollments/by-course/{courseId}
     * Требует currentUserId (из аутентификации)
     */
    @GetMapping("/by-course/{courseId}")
    public ResponseEntity<List<EnrollmentResponseDto>> getEnrollmentsByCourseId(@PathVariable Long courseId) {
        Long currentUserId = getCurrentUserId();
        List<EnrollmentResponseDto> enrollments = enrollmentService.getEnrollmentsByCourseId(currentUserId, courseId);
        return ResponseEntity.ok(enrollments);
    }

    /**
     * Обновить зачисление (например, подтвердить).
     * PUT /api/enrollments/{id}
     * Требует currentUserId (из аутентификации) - только для преподавателя/админа
     */
    @PutMapping("/{id}")
    public ResponseEntity<EnrollmentResponseDto> updateEnrollment(
            @PathVariable Long id,
            @Valid @RequestBody EnrollmentUpdateDto updateDto) {
        Long currentUserId = getCurrentUserId();
        EnrollmentResponseDto enrollment = enrollmentService.updateEnrollment(currentUserId, id, updateDto);
        return ResponseEntity.ok(enrollment);
    }

    // Примечание: Метод удаления Enrollment через API обычно не предоставляется пользователю напрямую.
    // Удаление может быть доступно только преподавателю/админу в особых случаях или реализовано как "отписка" с логикой.
}