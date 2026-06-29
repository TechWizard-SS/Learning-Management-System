package com.sstu.LearningManagementSystem.controller;

import com.sstu.LearningManagementSystem.model.User;
import com.sstu.LearningManagementSystem.model.dto.assignmentSubmissionDto.AssignmentSubmissionCreateDto;
import com.sstu.LearningManagementSystem.model.dto.assignmentSubmissionDto.AssignmentSubmissionResponseDto;
import com.sstu.LearningManagementSystem.service.AssignmentSubmissionService;
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
@RequestMapping("/api/assignment-submissions")
@RequiredArgsConstructor
public class AssignmentSubmissionController {

    private final AssignmentSubmissionService assignmentSubmissionService;
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
     * Отправить ответ на задание (создать/обновить Submission).
     * POST /api/assignment-submissions/submit
     * Требует currentUserId (из аутентификации)
     */
    @PostMapping("/submit")
    public ResponseEntity<AssignmentSubmissionResponseDto> submitAnswer(@Valid @RequestBody AssignmentSubmissionCreateDto createDto) {
        Long currentUserId = getCurrentUserId();
        AssignmentSubmissionResponseDto submission = assignmentSubmissionService.submitAnswer(currentUserId, createDto);
        return new ResponseEntity<>(submission, HttpStatus.CREATED); // 201 Created после создания
    }

    /**
     * Получить Submission по ID.
     * GET /api/assignment-submissions/{id}
     * Требует currentUserId (из аутентификации)
     */
    @GetMapping("/{id}")
    public ResponseEntity<AssignmentSubmissionResponseDto> getSubmissionById(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        AssignmentSubmissionResponseDto submission = assignmentSubmissionService.getSubmissionById(currentUserId, id);
        return ResponseEntity.ok(submission);
    }

    /**
     * Получить все Submission'ы пользователя по ID задания.
     * GET /api/assignment-submissions/by-user-and-assignment?assignmentId={assignmentId}
     * Требует currentUserId (из аутентификации)
     */
    @GetMapping("/by-user-and-assignment")
    public ResponseEntity<List<AssignmentSubmissionResponseDto>> getSubmissionsByUserAndAssignmentId(@RequestParam Long assignmentId) {
        Long currentUserId = getCurrentUserId();
        List<AssignmentSubmissionResponseDto> submissions = assignmentSubmissionService.getSubmissionsByUserAndAssignmentId(currentUserId, assignmentId);
        return ResponseEntity.ok(submissions);
    }

    /**
     * Получить все Submission'ы по ID задания (для преподавателя).
     * GET /api/assignment-submissions/by-assignment/{assignmentId}
     * Требует currentUserId (из аутентификации)
     */
    @GetMapping("/by-assignment/{assignmentId}")
    public ResponseEntity<List<AssignmentSubmissionResponseDto>> getSubmissionsByAssignmentId(@PathVariable Long assignmentId) {
        Long currentUserId = getCurrentUserId();
        List<AssignmentSubmissionResponseDto> submissions = assignmentSubmissionService.getSubmissionsByAssignmentId(currentUserId, assignmentId);
        return ResponseEntity.ok(submissions);
    }
    // Примечание: Методы обновления и удаления Submission через API обычно не предоставляются пользователю напрямую.
    // Обновление происходит при проверке ответа или повторной отправке.
    // Удаление может быть доступно только преподавателю/админу в особых случаях.
}