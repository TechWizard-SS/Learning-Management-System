package com.sstu.LearningManagementSystem.service;

import com.sstu.LearningManagementSystem.model.User;
import com.sstu.LearningManagementSystem.model.dto.userDto.UserCreateDto;
import com.sstu.LearningManagementSystem.model.dto.userDto.UserResponseDto;
import com.sstu.LearningManagementSystem.model.dto.userDto.UserUpdateDto;
import com.sstu.LearningManagementSystem.model.enumType.Role;
import com.sstu.LearningManagementSystem.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDto createUser(UserCreateDto createDto) {
        if (userRepository.existsByEmail(createDto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .username(createDto.getUsername())
                .firstName(createDto.getFirstName())
                .lastName(createDto.getLastName())
                .email(createDto.getEmail())
                .phone(createDto.getPhone())
                .avatarUrl(createDto.getAvatarUrl())
                .registrationDate(LocalDateTime.now())
                .password(passwordEncoder.encode(createDto.getPassword()))
                .role(Role.STUDENT)
                .ratingPosition(0)
                .build();

        User savedUser = userRepository.save(user);
        return mapToResponseDto(savedUser);
    }

    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        return mapToResponseDto(user);
    }

    public UserResponseDto updateUser(Long id, UserUpdateDto updateDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        if (updateDto.getUsername() != null) user.setUsername(updateDto.getUsername());
        if (updateDto.getFirstName() != null) user.setFirstName(updateDto.getFirstName());
        if (updateDto.getLastName() != null) user.setLastName(updateDto.getLastName());
        if (updateDto.getPhone() != null) user.setPhone(updateDto.getPhone());
        if (updateDto.getAvatarUrl() != null) user.setAvatarUrl(updateDto.getAvatarUrl());

        User updatedUser = userRepository.save(user);
        return mapToResponseDto(updatedUser);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    private UserResponseDto mapToResponseDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setRegistrationDate(user.getRegistrationDate());
        dto.setRatingPosition(user.getRatingPosition());
        return dto;
    }
}
