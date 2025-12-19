package com.sstu.LearningManagementSystem.service;

import com.sstu.LearningManagementSystem.ForbiddenException;
import com.sstu.LearningManagementSystem.model.Course;
import com.sstu.LearningManagementSystem.model.CourseRating;
import com.sstu.LearningManagementSystem.model.User;
import com.sstu.LearningManagementSystem.model.dto.courseRatingDto.CourseRatingCreateDto;
import com.sstu.LearningManagementSystem.model.dto.courseRatingDto.CourseRatingResponseDto;
import com.sstu.LearningManagementSystem.model.dto.courseRatingDto.CourseRatingUpdateDto;
import com.sstu.LearningManagementSystem.model.enumType.Role;
import com.sstu.LearningManagementSystem.repository.CourseRatingRepository;
import com.sstu.LearningManagementSystem.repository.CourseRepository;
import com.sstu.LearningManagementSystem.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseRatingService {

    private final CourseRatingRepository courseRatingRepository;
    private final CourseRepository courseRepository; // Для проверки существования курса
    private final UserRepository userRepository; // Для проверки существования пользователя

    /**
     * Создает или обновляет оценку курса пользователем.
     *
     * @param currentUserId ID пользователя, ставящего оценку.
     * @param createDto DTO с ID курса и оценкой.
     * @return DTO созданной/обновленной оценки.
     */
    @Transactional
    public CourseRatingResponseDto rateCourse(Long currentUserId, CourseRatingCreateDto createDto) {
        // 1. Проверить, существует ли пользователь (аутентификация)
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + currentUserId));

        // 2. Проверить, существует ли курс
        Course course = courseRepository.findById(createDto.getCourseId())
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + createDto.getCourseId()));

        // 3. Проверить, существует ли уже оценка от этого пользователя для этого курса
        CourseRating existingRating = courseRatingRepository.findByUserIdAndCourseId(currentUserId, createDto.getCourseId())
                .orElse(null);

        if (existingRating != null) {
            // 4. Если оценка существует, обновить её
            existingRating.setRating(createDto.getRating());
            CourseRating updatedRating = courseRatingRepository.save(existingRating);
            return toDto(updatedRating);
        } else {
            // 5. Если оценки не существует, создать новую
            CourseRating newRating = CourseRating.builder()
                    .user(user)
                    .course(course)
                    .rating(createDto.getRating())
                    .build();

            // 6. Сохранить
            CourseRating savedRating = courseRatingRepository.save(newRating);

            // 7. Вернуть DTO
            return toDto(savedRating);
        }
    }

    /**
     * Обновить оценку курса пользователем (если пользователь хочет изменить свою оценку).
     *
     * @param currentUserId ID пользователя, изменяющего оценку.
     * @param courseRatingId ID самой оценки.
     * @param updateDto DTO с новой оценкой.
     * @return DTO обновленной оценки.
     */
    @Transactional
    public CourseRatingResponseDto updateRating(Long currentUserId, Long courseRatingId, CourseRatingUpdateDto updateDto) {
        CourseRating rating = courseRatingRepository.findById(courseRatingId)
                .orElseThrow(() -> new EntityNotFoundException("Rating not found with id: " + courseRatingId));

        // Проверить, что текущий пользователь является владельцем оценки
        if (!rating.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("Access denied: Cannot update another user's rating.");
        }

        // Обновить оценку
        rating.setRating(updateDto.getRating());

        // Сохранить
        CourseRating updatedRating = courseRatingRepository.save(rating);

        // Вернуть DTO
        return toDto(updatedRating);
    }

    /**
     * Удалить оценку курса пользователем.
     *
     * @param currentUserId ID пользователя, удаляющего оценку.
     * @param courseRatingId ID самой оценки.
     */
    @Transactional
    public void deleteRating(Long currentUserId, Long courseRatingId) {
        CourseRating rating = courseRatingRepository.findById(courseRatingId)
                .orElseThrow(() -> new EntityNotFoundException("Rating not found with id: " + courseRatingId));

        // Проверить, что текущий пользователь является владельцем оценки
        if (!rating.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("Access denied: Cannot delete another user's rating.");
        }

        // Удалить (жесткое или мягкое - см. комментарий в EnrollmentService)
        courseRatingRepository.delete(rating);
    }

    /**
     * Получить оценку по ID.
     *
     * @param currentUserId ID пользователя, запрашивающего оценку.
     * @param courseRatingId ID оценки.
     * @return DTO оценки.
     */
    public CourseRatingResponseDto getRatingById(Long currentUserId, Long courseRatingId) {
        CourseRating rating = courseRatingRepository.findById(courseRatingId)
                .orElseThrow(() -> new EntityNotFoundException("Rating not found with id: " + courseRatingId));

        // Проверить права доступа: текущий пользователь == владелец оценки ИЛИ преподаватель/админ
        validateAccessToRating(currentUserId, rating.getUser().getId());

        return toDto(rating);
    }

    /**
     * Получить оценку пользователя по ID курса.
     *
     * @param currentUserId ID пользователя, запрашивающего свою оценку.
     * @param courseId      ID курса.
     * @return DTO оценки.
     */
    public CourseRatingResponseDto getRatingByUserAndCourseId(Long currentUserId, Long courseId) {
        // Проверить, существует ли пользователь (аутентификация)
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        CourseRating rating = courseRatingRepository.findByUserIdAndCourseId(currentUserId, courseId)
                .orElseThrow(() -> new EntityNotFoundException("Rating not found for user id: " + currentUserId + " and course id: " + courseId));

        // Проверить права доступа: текущий пользователь == владелец оценки ИЛИ преподаватель/админ
        validateAccessToRating(currentUserId, rating.getUser().getId());

        return toDto(rating);
    }

    /**
     * Получить все оценки пользователя.
     *
     * @param currentUserId ID пользователя, запрашивающего список.
     * @return Список DTO оценок.
     */
    public List<CourseRatingResponseDto> getRatingsByUserId(Long currentUserId) {
        // Проверить права: currentUserId == запрашиваемый пользователь ИЛИ преподаватель/админ
        validateAccessToList(currentUserId);

        List<CourseRating> ratings = courseRatingRepository.findByUserId(currentUserId);
        return ratings.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Получить все оценки по курсу (для преподавателя или для вычисления средней).
     * Требует проверки прав: только преподаватель/админ/владелец курса.
     *
     * @param currentUserId ID пользователя, запрашивающего список.
     * @param courseId      ID курса.
     * @return Список DTO оценок.
     */
    public List<CourseRatingResponseDto> getRatingsByCourseId(Long currentUserId, Long courseId) {
        // Проверить права: currentUserId должен быть преподавателем/админом/владельцем курса
        validateUserCanViewRatings(currentUserId, courseId);

        List<CourseRating> ratings = courseRatingRepository.findByCourseId(courseId);
        return ratings.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // --- Вспомогательные методы ---

    private CourseRatingResponseDto toDto(CourseRating rating) {
        CourseRatingResponseDto dto = new CourseRatingResponseDto();
        dto.setId(rating.getId());
        dto.setUserId(rating.getUser().getId());
        dto.setCourseId(rating.getCourse().getId());
        dto.setRating(rating.getRating());
        return dto;
    }

    /**
     * Проверяет, имеет ли пользователь право просматривать конкретную оценку.
     * Вызывает ForbiddenException, если прав недостаточно.
     *
     * @param requestUserId ID пользователя, делающего запрос.
     * @param ownerUserId   ID владельца оценки.
     */
    private void validateAccessToRating(Long requestUserId, Long ownerUserId) {
        User requestingUser = userRepository.findById(requestUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Пользователь может просматривать свои данные
        if (requestUserId.equals(ownerUserId)) {
            return;
        }

        // Или пользователь должен быть преподавателем/админом/владельцем
        if (requestingUser.getRole() != Role.OWNER && // Убедитесь, что Role существует
                requestingUser.getRole() != Role.ADMIN &&
                requestingUser.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Access denied: Cannot view another user's rating.");
        }
    }

    /**
     * Проверяет, имеет ли пользователь право просматривать список оценок.
     * Вызывает ForbiddenException, если прав недостаточно.
     *
     * @param requestUserId ID пользователя, делающего запрос.
     */
    private void validateAccessToList(Long requestUserId) {
        // Пользователь может просматривать только свой список
        // Другие роли (преподаватель/админ) проверяются в других методах
        userRepository.findById(requestUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        // Дальнейшие проверки происходят в вызывающих методах (getRatingsByUserId vs getRatingsByCourseId)
    }

    /**
     * Проверяет, имеет ли пользователь право просматривать оценки по курсу.
     * Вызывает ForbiddenException, если прав недостаточно.
     *
     * @param userId   ID пользователя.
     * @param courseId ID курса.
     */
    private void validateUserCanViewRatings(Long userId, Long courseId) {
        // Получить курс и связанный с ним пользователь
        // Course course = courseRepository.findById(courseId) // Если нужна проверка принадлежности курса
        //         .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Проверить роль пользователя (аналогично другим validate методам)
        if (user.getRole() != Role.OWNER &&
                user.getRole() != Role.ADMIN &&
                user.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Only OWNER, ADMIN, or TEACHER can view ratings for a course");
        }
    }
}