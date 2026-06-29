package com.sstu.LearningManagementSystem.controller;

import com.sstu.LearningManagementSystem.model.User;
import com.sstu.LearningManagementSystem.model.dto.reportDto.ReportResponseDto;
import com.sstu.LearningManagementSystem.service.ReportService;
import com.sstu.LearningManagementSystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
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
     * Получить отчет по ID.
     * GET /api/reports/{id}
     * Требует currentUserId (из аутентификации)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportResponseDto> getReportById(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        ReportResponseDto report = reportService.getReportById(currentUserId, id);
        return ResponseEntity.ok(report);
    }

    /**
     * Получить отчет пользователя по ID курса.
     * GET /api/reports/by-user-and-course?courseId={courseId}
     * Требует currentUserId (из аутентификации)
     */
    @GetMapping("/by-user-and-course")
    public ResponseEntity<ReportResponseDto> getReportByUserAndCourseId(@RequestParam Long courseId) {
        Long currentUserId = getCurrentUserId();
        ReportResponseDto report = reportService.getReportByUserAndCourseId(currentUserId, courseId);
        return ResponseEntity.ok(report);
    }

    /**
     * Получить все отчеты пользователя.
     * GET /api/reports/by-user
     * Требует currentUserId (из аутентификации)
     */
    @GetMapping("/by-user")
    public ResponseEntity<List<ReportResponseDto>> getReportsByUserId() {
        Long currentUserId = getCurrentUserId();
        List<ReportResponseDto> reports = reportService.getReportsByUserId(currentUserId);
        return ResponseEntity.ok(reports);
    }

    /**
     * Получить все отчеты по ID курса (для преподавателя).
     * GET /api/reports/by-course/{courseId}
     * Требует currentUserId (из аутентификации)
     */
    @GetMapping("/by-course/{courseId}")
    public ResponseEntity<List<ReportResponseDto>> getReportsByCourseId(@PathVariable Long courseId) {
        Long currentUserId = getCurrentUserId();
        List<ReportResponseDto> reports = reportService.getReportsByCourseId(currentUserId, courseId);
        return ResponseEntity.ok(reports);
    }
}
