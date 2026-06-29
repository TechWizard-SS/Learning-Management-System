package com.sstu.LearningManagementSystem.controller;

import com.sstu.LearningManagementSystem.model.User;
import com.sstu.LearningManagementSystem.model.dto.courseRatingDto.CourseRatingCreateDto;
import com.sstu.LearningManagementSystem.model.dto.courseRatingDto.CourseRatingResponseDto;
import com.sstu.LearningManagementSystem.model.dto.courseRatingDto.CourseRatingUpdateDto;
import com.sstu.LearningManagementSystem.service.CourseRatingService;
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
@RequestMapping("/api/course-ratings")
@RequiredArgsConstructor
public class CourseRatingController {

    private final CourseRatingService courseRatingService;
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
     * Оценить курс (создать/обновить Rating).
     * POST /api/course-ratings/rate
     * Требует currentUserId (из аутентификации)
     */
    @PostMapping("/rate")
    public ResponseEntity<CourseRatingResponseDto> rateCourse(@Valid @RequestBody CourseRatingCreateDto createDto) {
        Long currentUserId = getCurrentUserId();
        CourseRatingResponseDto rating = courseRatingService.rateCourse(currentUserId, createDto);
        return new ResponseEntity<>(rating, HttpStatus.CREATED); // 201 Created или 200 OK, если обновление
    }

    /**
     * Обновить свою оценку курса.
     * PUT /api/course-ratings/{id}
     * Требует currentUserId (из аутентификации) - владелец оценки
     */
    @PutMapping("/{id}")
    public ResponseEntity<CourseRatingResponseDto> updateRating(
            @PathVariable Long id,
            @Valid @RequestBody CourseRatingUpdateDto updateDto) {
        Long currentUserId = getCurrentUserId();
        CourseRatingResponseDto rating = courseRatingService.updateRating(currentUserId, id, updateDto);
        return ResponseEntity.ok(rating);
    }

    /**
     * Удалить свою оценку курса.
     * DELETE /api/course-ratings/{id}
     * Требует currentUserId (из аутентификации) - владелец оценки
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRating(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        courseRatingService.deleteRating(currentUserId, id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    /**
     * Получить оценку по ID.
     * GET /api/course-ratings/{id}
     * Требует currentUserId (из аутентификации)
     */
    @GetMapping("/{id}")
    public ResponseEntity<CourseRatingResponseDto> getRatingById(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        CourseRatingResponseDto rating = courseRatingService.getRatingById(currentUserId, id);
        return ResponseEntity.ok(rating);
    }

    /**
     * Получить оценку пользователя по ID курса.
     * GET /api/course-ratings/by-user-and-course?courseId={courseId}
     * Требует currentUserId (из аутентификации)
     */
    @GetMapping("/by-user-and-course")
    public ResponseEntity<CourseRatingResponseDto> getRatingByUserAndCourseId(@RequestParam Long courseId) {
        Long currentUserId = getCurrentUserId();
        CourseRatingResponseDto rating = courseRatingService.getRatingByUserAndCourseId(currentUserId, courseId);
        return ResponseEntity.ok(rating);
    }

    /**
     * Получить все оценки пользователя.
     * GET /api/course-ratings/by-user
     * Требует currentUserId (из аутентификации)
     */
    @GetMapping("/by-user")
    public ResponseEntity<List<CourseRatingResponseDto>> getRatingsByUserId() {
        Long currentUserId = getCurrentUserId();
        List<CourseRatingResponseDto> ratings = courseRatingService.getRatingsByUserId(currentUserId);
        return ResponseEntity.ok(ratings);
    }

    /**
     * Получить все оценки по ID курса (для преподавателя).
     * GET /api/course-ratings/by-course/{courseId}
     * Требует currentUserId (из аутентификации)
     */
    @GetMapping("/by-course/{courseId}")
    public ResponseEntity<List<CourseRatingResponseDto>> getRatingsByCourseId(@PathVariable Long courseId) {
        Long currentUserId = getCurrentUserId();
        List<CourseRatingResponseDto> ratings = courseRatingService.getRatingsByCourseId(currentUserId, courseId);
        return ResponseEntity.ok(ratings);
    }
}