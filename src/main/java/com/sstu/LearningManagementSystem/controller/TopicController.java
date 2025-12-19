package com.sstu.LearningManagementSystem.controller;

import com.sstu.LearningManagementSystem.model.User;
import com.sstu.LearningManagementSystem.model.dto.topicDto.TopicCreateDto;
import com.sstu.LearningManagementSystem.model.dto.topicDto.TopicResponseDto;
import com.sstu.LearningManagementSystem.model.dto.topicDto.TopicUpdateDto;
import com.sstu.LearningManagementSystem.service.TopicService;
import com.sstu.LearningManagementSystem.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;
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

    @PostMapping
    public ResponseEntity<TopicResponseDto> createTopic(@Valid @RequestBody TopicCreateDto createDto) {
        Long currentUserId = getCurrentUserId(); // Получаем из SecurityContext
        TopicResponseDto createdTopic = topicService.createTopic(currentUserId, createDto);
        return ResponseEntity.ok(createdTopic);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TopicResponseDto> getTopicById(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId(); // Получаем из SecurityContext
        TopicResponseDto topic = topicService.getTopicById(currentUserId, id);
        return ResponseEntity.ok(topic);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TopicResponseDto> updateTopic(
            @PathVariable Long id,
            @Valid @RequestBody TopicUpdateDto updateDto) {
        Long currentUserId = getCurrentUserId(); // Получаем из SecurityContext
        TopicResponseDto updatedTopic = topicService.updateTopic(currentUserId, id, updateDto);
        return ResponseEntity.ok(updatedTopic);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTopic(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId(); // Получаем из SecurityContext
        topicService.deleteTopic(currentUserId, id);
        return ResponseEntity.noContent().build();
    }
}
