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
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {
    private final CourseRepository courseRepository;
    private final CourseCategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CourseCacheService courseCacheService;
    private final Map<Long, AtomicInteger> hits = new ConcurrentHashMap<>();

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

//    @Cacheable(value = "courses", key = "#courseId")
//    public CourseResponseDto getCourseById(Long currentUserId, Long courseId) {
//        // Проверяем, что пользователь существует (аутентификация)
//        userRepository.findById(currentUserId)
//                .orElseThrow(() -> new EntityNotFoundException("User not found"));
//
//        log.info("Fetching course {} from DB", courseId);
//
//        // Используем метод с JOIN FETCH
//        Course course = courseRepository.findByIdWithCategoryAndTags(courseId)
//                .orElseThrow(() -> new EntityNotFoundException("Course not found"));
//
//
//        return toDto(course); // Теперь toDto не вызывает LazyInit
//    }

    public CourseResponseDto getCourseById(Long currentUserId, Long courseId) {
        // Проверка пользователя
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (isTopCourse(courseId)) {
            return courseCacheService.getCourseCached(courseId); // ✅ proxy
        }

        // Не топ → всегда БД
        log.info("Fetching NON-TOP course {} from DB", courseId);

        Course course = courseRepository.findByIdWithCategoryAndTags(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        return toDto(course);
    }

    private boolean isTopCourse(Long courseId) {
        hits.computeIfAbsent(courseId, id -> new AtomicInteger())
                .incrementAndGet();

        return hits.get(courseId).get() >= 100;
    }

    public CourseResponseDto updateCourse(Long currentUserId, Long courseId, CourseUpdateDto updateDto) {
        // Проверка прав текущего пользователя (аналогично createCourse)
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (currentUser.getRole() != Role.OWNER &&
                currentUser.getRole() != Role.ADMIN &&
                currentUser.getRole() != Role.TEACHER) {
            throw new RuntimeException("Only OWNER, ADMIN or TEACHER can update courses");
        }

        Course course = courseRepository.findById(courseId) // Теперь используем courseId
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));

        // Обновление полей (осталось как есть)
        if (updateDto.getName() != null) course.setName(updateDto.getName());
        if (updateDto.getDescription() != null) course.setDescription(updateDto.getDescription());
        if (updateDto.getExpectedDuration() != null) course.setExpectedDuration(updateDto.getExpectedDuration());
        if (updateDto.getCategoryId() != null) {
            CourseCategory category = categoryRepository.findById(updateDto.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found"));
            course.setCategory(category);
        }
        if (updateDto.getTags() != null) course.setTags(updateDto.getTags());

        courseRepository.save(course);

        Course updatedCourse = courseRepository.findByIdWithCategoryAndTags(courseId) // <-- Изменено
                .orElseThrow(() -> new EntityNotFoundException("Course not found after update"));



        return mapToResponseDto(updatedCourse); // <-- Теперь вызов course.getTags() внутри mapToResponseDto безопасен
    }

    private CourseResponseDto toDto(Course course) {
        CourseResponseDto dto = new CourseResponseDto();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setDescription(course.getDescription());
        dto.setExpectedDuration(course.getExpectedDuration());
        dto.setCreatedAt(course.getCreatedAt());
        dto.setUpdatedAt(course.getUpdatedAt());
        // Проверяем, есть ли категория, перед тем как получать её ID
        if (course.getCategory() != null) {
            dto.setCategoryId(course.getCategory().getId());
        } else {
            dto.setCategoryId(null); // Или не устанавливать, если поле в DTO может быть null
        }
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
        dto.setCreatedAt(course.getCreatedAt());
        dto.setUpdatedAt(course.getUpdatedAt());
        // Проверяем, есть ли категория, перед тем как получать её ID и Name
        if (course.getCategory() != null) {
            dto.setCategoryId(course.getCategory().getId());
            dto.setCategoryName(course.getCategory().getName());
        } else {
            // Устанавливаем null, если категория отсутствует
            dto.setCategoryId(null); // Или не устанавливать, если поле в DTO может быть null
            dto.setCategoryName(null); // Или не устанавливать
        }
        dto.setTags(course.getTags());
        return dto;
    }


}
