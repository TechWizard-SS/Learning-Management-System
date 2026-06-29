package com.sstu.LearningManagementSystem.controller;

import com.sstu.LearningManagementSystem.model.User;
import com.sstu.LearningManagementSystem.model.dto.socialLinkDto.SocialLinkCreateDto;
import com.sstu.LearningManagementSystem.model.dto.socialLinkDto.SocialLinkResponseDto;
import com.sstu.LearningManagementSystem.model.dto.socialLinkDto.SocialLinkUpdateDto;
import com.sstu.LearningManagementSystem.service.SocialLinkService;
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
@RequestMapping("/api/social-links")
@RequiredArgsConstructor
public class SocialLinkController {

    private final SocialLinkService socialLinkService;
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
     * Создать социальную ссылку для текущего пользователя.
     * POST /api/social-links
     * Требует currentUserId (из аутентификации) - владелец ссылки
     */
    @PostMapping
    public ResponseEntity<SocialLinkResponseDto> createSocialLink(@Valid @RequestBody SocialLinkCreateDto createDto) {
        Long currentUserId = getCurrentUserId();
        SocialLinkResponseDto socialLink = socialLinkService.createSocialLink(currentUserId, createDto);
        return new ResponseEntity<>(socialLink, HttpStatus.CREATED); // 201 Created
    }

    /**
     * Обновить социальную ссылку.
     * PUT /api/social-links/{id}
     * Требует currentUserId (из аутентификации) - владелец ссылки
     */
    @PutMapping("/{id}")
    public ResponseEntity<SocialLinkResponseDto> updateSocialLink(
            @PathVariable Long id,
            @Valid @RequestBody SocialLinkUpdateDto updateDto) {
        Long currentUserId = getCurrentUserId();
        SocialLinkResponseDto socialLink = socialLinkService.updateSocialLink(currentUserId, id, updateDto);
        return ResponseEntity.ok(socialLink);
    }

    /**
     * Удалить социальную ссылку.
     * DELETE /api/social-links/{id}
     * Требует currentUserId (из аутентификации) - владелец ссылки
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSocialLink(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        socialLinkService.deleteSocialLink(currentUserId, id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    /**
     * Получить социальную ссылку по ID.
     * GET /api/social-links/{id}
     * Требует currentUserId (из аутентификации)
     */
    @GetMapping("/{id}")
    public ResponseEntity<SocialLinkResponseDto> getSocialLinkById(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        SocialLinkResponseDto socialLink = socialLinkService.getSocialLinkById(currentUserId, id);
        return ResponseEntity.ok(socialLink);
    }

    /**
     * Получить все социальные ссылки пользователя.
     * GET /api/social-links/by-user/{userId}
     * Требует currentUserId (из аутентификации) - владелец списка или преподаватель/админ
     */
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<List<SocialLinkResponseDto>> getSocialLinksByUserId(@PathVariable Long userId) {
        Long currentUserId = getCurrentUserId();
        List<SocialLinkResponseDto> socialLinks = socialLinkService.getSocialLinksByUserId(currentUserId, userId);
        return ResponseEntity.ok(socialLinks);
    }
}