package com.sstu.LearningManagementSystem.controller;

import com.sstu.LearningManagementSystem.model.User;
import com.sstu.LearningManagementSystem.model.dto.courseDto.CourseCreateDto;
import com.sstu.LearningManagementSystem.model.dto.courseDto.CourseResponseDto;
import com.sstu.LearningManagementSystem.model.dto.courseDto.CourseUpdateDto;
import com.sstu.LearningManagementSystem.service.CourseService;
import com.sstu.LearningManagementSystem.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;
    private final UserService userService;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.findByUsername(username);
        return user.getId();
    }

    @PostMapping
    public ResponseEntity<CourseResponseDto> createCourse(@Valid @RequestBody CourseCreateDto createDto) {
        Long currentUserId = getCurrentUserId();
        return ResponseEntity.ok(courseService.createCourse(currentUserId, createDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponseDto> getCourseById(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        return ResponseEntity.ok(courseService.getCourseById(currentUserId, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseResponseDto> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseUpdateDto updateDto) {
        Long currentUserId = getCurrentUserId();
        return ResponseEntity.ok(courseService.updateCourse(currentUserId, id, updateDto));
    }
}