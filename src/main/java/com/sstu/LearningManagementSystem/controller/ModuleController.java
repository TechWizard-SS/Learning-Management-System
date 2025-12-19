package com.sstu.LearningManagementSystem.controller;

import com.sstu.LearningManagementSystem.model.User;
import com.sstu.LearningManagementSystem.model.dto.moduleDto.ModuleCreateDto;
import com.sstu.LearningManagementSystem.model.dto.moduleDto.ModuleResponseDto;
import com.sstu.LearningManagementSystem.model.dto.moduleDto.ModuleUpdateDto;
import com.sstu.LearningManagementSystem.service.ModuleService;
import com.sstu.LearningManagementSystem.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;
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
    public ResponseEntity<ModuleResponseDto> createModule(@Valid @RequestBody ModuleCreateDto createDto) {
        Long currentUserId = getCurrentUserId(); // Получаем из SecurityContext
        ModuleResponseDto createdModule = moduleService.createModule(currentUserId, createDto);
        return ResponseEntity.ok(createdModule);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModuleResponseDto> getModuleById(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId(); // Получаем из SecurityContext
        ModuleResponseDto module = moduleService.getModuleById(currentUserId, id);
        return ResponseEntity.ok(module);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModuleResponseDto> updateModule(
            @PathVariable Long id,
            @Valid @RequestBody ModuleUpdateDto updateDto) {
        Long currentUserId = getCurrentUserId(); // Получаем из SecurityContext
        ModuleResponseDto updatedModule = moduleService.updateModule(currentUserId, id, updateDto);
        return ResponseEntity.ok(updatedModule);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModule(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId(); // Получаем из SecurityContext
        moduleService.deleteModule(currentUserId, id);
        return ResponseEntity.noContent().build();
    }
}