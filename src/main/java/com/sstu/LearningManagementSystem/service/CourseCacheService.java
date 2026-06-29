package com.sstu.LearningManagementSystem.service;

import com.sstu.LearningManagementSystem.model.Course;
import com.sstu.LearningManagementSystem.model.dto.courseDto.CourseResponseDto;
import com.sstu.LearningManagementSystem.repository.CourseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseCacheService {

    private final CourseRepository courseRepository;

    @Cacheable(value = "courses", key = "#courseId")
    public CourseResponseDto getCourseCached(Long courseId) {
        log.info("Fetching course {} from DB", courseId);

        Course course = courseRepository.findByIdWithCategoryAndTags(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        return toDto(course);
    }

    private CourseResponseDto toDto(Course course) {
        CourseResponseDto dto = new CourseResponseDto();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setDescription(course.getDescription());
        dto.setExpectedDuration(course.getExpectedDuration());
        dto.setCreatedAt(course.getCreatedAt());
        dto.setUpdatedAt(course.getUpdatedAt());
        if (course.getCategory() != null) {
            dto.setCategoryId(course.getCategory().getId());
        }
        dto.setTags(course.getTags());
        return dto;
    }
}
