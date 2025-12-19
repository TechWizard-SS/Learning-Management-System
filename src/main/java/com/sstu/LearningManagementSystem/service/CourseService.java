package com.sstu.LearningManagementSystem.service;

import com.sstu.LearningManagementSystem.model.Course;
import com.sstu.LearningManagementSystem.model.CourseCategory;
import com.sstu.LearningManagementSystem.model.User;
import com.sstu.LearningManagementSystem.model.dto.courseDto.CourseCreateDto;
import com.sstu.LearningManagementSystem.model.dto.courseDto.CourseResponseDto;
import com.sstu.LearningManagementSystem.model.dto.courseDto.CourseUpdateDto;
import com.sstu.LearningManagementSystem.model.enumType.Role;
import com.sstu.LearningManagementSystem.repository.CourseCategoryRepository;
import com.sstu.LearningManagementSystem.repository.CourseRepository;
import com.sstu.LearningManagementSystem.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
//import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final CourseCategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CourseResponseDto createCourse(Long currentUserId, CourseCreateDto createDto) {
        // Получаем текущего пользователя
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Проверка роли
        if (currentUser.getRole() != Role.OWNER &&
                currentUser.getRole() != Role.ADMIN &&
                currentUser.getRole() != Role.TEACHER) {
            throw new RuntimeException ("Only OWNER, ADMIN or TEACHER can create courses");
        }

        CourseCategory category = null;
        if (createDto.getCategoryId() != null) {
            category = categoryRepository.findById(createDto.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        }

        Course course = Course.builder()
                .name(createDto.getName())
                .description(createDto.getDescription())
                .expectedDuration(createDto.getExpectedDuration())
                .category(category)
                .tags(createDto.getTags() != null ? createDto.getTags() : new ArrayList<>())
                .build();

        Course savedCourse = courseRepository.save(course);
        return mapToResponseDto(savedCourse);
    }

    public CourseResponseDto getCourseById(Long currentUserId, Long courseId) {
        // Проверяем, что пользователь существует (аутентификация)
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        return toDto(course);
    }

    public CourseResponseDto updateCourse(Long id, Long aLong, CourseUpdateDto updateDto) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + id));

        if (updateDto.getName() != null) course.setName(updateDto.getName());
        if (updateDto.getDescription() != null) course.setDescription(updateDto.getDescription());
        if (updateDto.getExpectedDuration() != null) course.setExpectedDuration(updateDto.getExpectedDuration());
        if (updateDto.getCategoryId() != null) {
            CourseCategory category = categoryRepository.findById(updateDto.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found"));
            course.setCategory(category);
        }
        if (updateDto.getTags() != null) course.setTags(updateDto.getTags());

        Course updatedCourse = courseRepository.save(course);
        return mapToResponseDto(updatedCourse);
    }

    private CourseResponseDto toDto(Course course) {
        CourseResponseDto dto = new CourseResponseDto();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setDescription(course.getDescription());
        dto.setExpectedDuration(course.getExpectedDuration());
        dto.setCategoryId(course.getCategory().getId());
        dto.setTags(course.getTags());
        return dto;
    }

    private CourseResponseDto mapToResponseDto(Course course) {
        CourseResponseDto dto = new CourseResponseDto();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setDescription(course.getDescription());
        dto.setExpectedDuration(course.getExpectedDuration());
        dto.setRating(course.getRating());
        if (course.getCategory() != null) {
            dto.setCategoryId(course.getCategory().getId());
            dto.setCategoryName(course.getCategory().getName());
        }
        dto.setTags(course.getTags());

        return dto;
    }
}
