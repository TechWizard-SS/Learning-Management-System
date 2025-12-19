package com.sstu.LearningManagementSystem.controller;

import com.sstu.LearningManagementSystem.model.User;
import com.sstu.LearningManagementSystem.model.dto.newsDto.NewsCreateDto;
import com.sstu.LearningManagementSystem.model.dto.newsDto.NewsResponseDto;
import com.sstu.LearningManagementSystem.model.dto.newsDto.NewsUpdateDto;
import com.sstu.LearningManagementSystem.service.NewsService;
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
@RequestMapping("/api/news") // Базовый URL
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;
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
     * Создать новость.
     * POST /api/news
     * Требует currentUserId (из аутентификации) - только для преподавателя/админа
     */
    @PostMapping
    public ResponseEntity<NewsResponseDto> createNews(@Valid @RequestBody NewsCreateDto createDto) {
        Long currentUserId = getCurrentUserId();
        NewsResponseDto news = newsService.createNews(currentUserId, createDto);
        return new ResponseEntity<>(news, HttpStatus.CREATED); // 201 Created
    }

    /**
     * Обновить новость.
     * PUT /api/news/{id}
     * Требует currentUserId (из аутентификации) - только для преподавателя/админа
     */
    @PutMapping("/{id}")
    public ResponseEntity<NewsResponseDto> updateNews(
            @PathVariable Long id,
            @Valid @RequestBody NewsUpdateDto updateDto) {
        Long currentUserId = getCurrentUserId();
        NewsResponseDto news = newsService.updateNews(currentUserId, id, updateDto);
        return ResponseEntity.ok(news);
    }

    /**
     * Удалить новость.
     * DELETE /api/news/{id}
     * Требует currentUserId (из аутентификации) - только для преподавателя/админа
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNews(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        newsService.deleteNews(currentUserId, id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    /**
     * Получить новость по ID.
     * GET /api/news/{id}
     * Требует currentUserId (из аутентификации)
     */
    @GetMapping("/{id}")
    public ResponseEntity<NewsResponseDto> getNewsById(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        NewsResponseDto news = newsService.getNewsById(currentUserId, id);
        return ResponseEntity.ok(news);
    }

    /**
     * Получить все новости по ID курса.
     * GET /api/news/by-course/{courseId}
     * Требует currentUserId (из аутентификации)
     */
    @GetMapping("/by-course/{courseId}")
    public ResponseEntity<List<NewsResponseDto>> getNewsByCourseId(@PathVariable Long courseId) {
        Long currentUserId = getCurrentUserId();
        List<NewsResponseDto> newsList = newsService.getNewsByCourseId(currentUserId, courseId);
        return ResponseEntity.ok(newsList);
    }
}