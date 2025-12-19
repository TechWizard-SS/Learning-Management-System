package com.sstu.LearningManagementSystem.controller;

import com.sstu.LearningManagementSystem.model.dto.userDto.UserCreateDto;
import com.sstu.LearningManagementSystem.model.dto.userDto.UserResponseDto;
import com.sstu.LearningManagementSystem.model.dto.userDto.UserUpdateDto;
import com.sstu.LearningManagementSystem.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        UserResponseDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDto updateDto) {
        UserResponseDto user = userService.updateUser(id, updateDto);
        return ResponseEntity.ok(user);
    }
}