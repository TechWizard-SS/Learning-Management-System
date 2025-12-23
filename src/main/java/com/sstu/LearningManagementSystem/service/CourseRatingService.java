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

/**
 * Сервис для управления оценками курсов (CourseRating).
 * Обеспечивает создание, обновление, получение и удаление оценок пользователей для курсов.
 * Проверяет права доступа к оценкам.
 */
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
     * @throws EntityNotFoundException если пользователь или курс не найдены.
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
        // Используем метод с JOIN FETCH, если валидация требует
        CourseRating existingRating = courseRatingRepository.findByUserIdAndCourseIdWithUserAndCourse(currentUserId, createDto.getCourseId()).orElse(null);

        if (existingRating != null) {
            // 4. Если оценка существует, обновить её
            existingRating.setRating(createDto.getRating());
            CourseRating updatedRating = courseRatingRepository.save(existingRating);

            // Перезапрашиваем обновленный CourseRating с User и Course, чтобы избежать LazyInit в toDto
            CourseRating updatedWithUserAndCourse = courseRatingRepository.findByIdWithUserAndCourse(updatedRating.getId()) // <-- Добавлено
                    .orElseThrow(() -> new EntityNotFoundException("Rating not found after update")); // На всякий случай

            return toDto(updatedWithUserAndCourse); // <-- Теперь toDto не вызывает LazyInit
        } else {
            // 5. Если оценки не существует, создать новую
            CourseRating newRating = CourseRating.builder()
                    .user(user)
                    .course(course)
                    .rating(createDto.getRating())
                    .build();

            // 6. Сохранить
            CourseRating savedRating = courseRatingRepository.save(newRating);

            // Перезапрашиваем созданный CourseRating с User и Course, чтобы избежать LazyInit в toDto
            CourseRating savedWithUserAndCourse = courseRatingRepository.findByIdWithUserAndCourse(savedRating.getId()) // <-- Добавлено
                    .orElseThrow(() -> new EntityNotFoundException("Rating not found after creation")); // На всякий случай

            // 7. Вернуть DTO
            return toDto(savedWithUserAndCourse); // <-- Теперь toDto не вызывает LazyInit
        }
    }

    /**
     * Обновить оценку курса пользователем (если пользователь хочет изменить свою оценку).
     *
     * @param currentUserId ID пользователя, изменяющего оценку.
     * @param courseRatingId ID самой оценки.
     * @param updateDto DTO с новой оценкой.
     * @return DTO обновленной оценки.
     * @throws EntityNotFoundException если оценка не найдена.
     * @throws ForbiddenException      если доступ запрещен (не владелец оценки).
     */
    @Transactional
    public CourseRatingResponseDto updateRating(Long currentUserId, Long courseRatingId, CourseRatingUpdateDto updateDto) {
        // Используем метод с JOIN FETCH
        CourseRating rating = courseRatingRepository.findByIdWithUserAndCourse(courseRatingId)
                .orElseThrow(() -> new EntityNotFoundException("Rating not found with id: " + courseRatingId));

        // Проверить, что текущий пользователь является владельцем оценки
        if (!rating.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("Access denied: Cannot update another user's rating.");
        }

        // Обновить оценку
        rating.setRating(updateDto.getRating());

        // Сохранить
        CourseRating updatedRating = courseRatingRepository.save(rating);

        // Перезапрашиваем обновленный CourseRating с User и Course, чтобы избежать LazyInit в toDto
        CourseRating updatedWithUserAndCourse = courseRatingRepository.findByIdWithUserAndCourse(updatedRating.getId()) // <-- Добавлено
                .orElseThrow(() -> new EntityNotFoundException("Rating not found after update")); // На всякий случай

        // Вернуть DTO
        return toDto(updatedWithUserAndCourse); // <-- Теперь toDto не вызывает LazyInit
    }

    /**
     * Удалить оценку курса пользователем.
     *
     * @param currentUserId ID пользователя, удаляющего оценку.
     * @param courseRatingId ID самой оценки.
     * @throws EntityNotFoundException если оценка не найдена.
     * @throws ForbiddenException      если доступ запрещен (не владелец оценки).
     */
    @Transactional
    public void deleteRating(Long currentUserId, Long courseRatingId) {
        CourseRating rating = courseRatingRepository.findByIdWithUserAndCourse(courseRatingId) // <-- Изменено: используем JOIN FETCH для валидации
                .orElseThrow(() -> new EntityNotFoundException("Rating not found with id: " + courseRatingId));

        // Проверить, что текущий пользователь является владельцем оценки
        if (!rating.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("Access denied: Cannot delete another user's rating.");
        }

        // Удалить
        courseRatingRepository.delete(rating);
    }

    /**
     * Получить оценку по ID.
     *
     * @param currentUserId ID пользователя, запрашивающего оценку.
     * @param courseRatingId ID оценки.
     * @return DTO оценки.
     * @throws EntityNotFoundException если оценка не найдена.
     * @throws ForbiddenException      если доступ запрещен.
     */
    public CourseRatingResponseDto getRatingById(Long currentUserId, Long courseRatingId) {
        // Используем метод с JOIN FETCH
        CourseRating rating = courseRatingRepository.findByIdWithUserAndCourse(courseRatingId)
                .orElseThrow(() -> new EntityNotFoundException("Rating not found with id: " + courseRatingId));

        // Проверить права доступа: текущий пользователь == владелец оценки ИЛИ преподаватель/админ
        validateAccessToRating(currentUserId, rating.getUser().getId());

        return toDto(rating); // <-- Теперь toDto не вызывает LazyInit
    }

    /**
     * Получить оценку пользователя по ID курса.
     *
     * @param currentUserId ID пользователя, запрашивающего свою оценку.
     * @param courseId      ID курса.
     * @return DTO оценки.
     * @throws EntityNotFoundException если пользователь или оценка не найдены.
     * @throws ForbiddenException      если доступ запрещен.
     */
    public CourseRatingResponseDto getRatingByUserAndCourseId(Long currentUserId, Long courseId) {
        // Проверить, существует ли пользователь (аутентификация)
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Используем метод с JOIN FETCH
        CourseRating rating = courseRatingRepository.findByUserIdAndCourseIdWithUserAndCourse(currentUserId, courseId)
                .orElseThrow(() -> new EntityNotFoundException("Rating not found for user id: " + currentUserId + " and course id: " + courseId));

        // Проверить права доступа: текущий пользователь == владелец оценки ИЛИ преподаватель/админ
        validateAccessToRating(currentUserId, rating.getUser().getId());

        return toDto(rating); // <-- Теперь toDto не вызывает LazyInit
    }

    /**
     * Получить все оценки пользователя.
     *
     * @param currentUserId ID пользователя, запрашивающего список.
     * @return Список DTO оценок.
     * @throws ForbiddenException если доступ запрещен.
     */
    public List<CourseRatingResponseDto> getRatingsByUserId(Long currentUserId) {
        // Проверить права: currentUserId == запрашиваемый пользователь ИЛИ преподаватель/админ
        validateAccessToList(currentUserId);

        // Используем метод с JOIN FETCH
        List<CourseRating> ratings = courseRatingRepository.findByUserIdWithUserAndCourse(currentUserId);
        return ratings.stream()
                .map(this::toDto) // <-- Теперь toDto не вызывает LazyInit для каждого элемента
                .collect(Collectors.toList());
    }

    /**
     * Получить все оценки по курсу (для преподавателя или для вычисления средней).
     * Требует проверки прав: только преподаватель/админ/владелец курса.
     *
     * @param currentUserId ID пользователя, запрашивающего список.
     * @param courseId      ID курса.
     * @return Список DTO оценок.
     * @throws ForbiddenException если доступ запрещен.
     */
    public List<CourseRatingResponseDto> getRatingsByCourseId(Long currentUserId, Long courseId) {
        // Проверить права: currentUserId должен быть преподавателем/админом/владельцем курса
        validateUserCanViewRatings(currentUserId, courseId);

        // Используем метод с JOIN FETCH
        List<CourseRating> ratings = courseRatingRepository.findByCourseIdWithUserAndCourse(courseId);
        return ratings.stream()
                .map(this::toDto) // <-- Теперь toDto не вызывает LazyInit для каждого элемента
                .collect(Collectors.toList());
    }

    // --- Вспомогательные методы ---

    /**
     * Преобразует сущность CourseRating в CourseRatingResponseDto.
     * Включает ID, ID пользователя, ID курса, оценку и даты создания/обновления.
     *
     * @param rating Сущность оценки для преобразования.
     * @return DTO оценки.
     */
    private CourseRatingResponseDto toDto(CourseRating rating) {
        CourseRatingResponseDto dto = new CourseRatingResponseDto();
        dto.setId(rating.getId());
        // Убедитесь, что rating.getUser() и rating.getCourse() загружены (JOIN FETCH в репозитории)
        dto.setUserId(rating.getUser().getId());
        dto.setCourseId(rating.getCourse().getId());
        dto.setRating(rating.getRating());
        dto.setCreatedAt(rating.getCreatedAt());
        dto.setUpdatedAt(rating.getUpdatedAt());
        return dto;
    }

    /**
     * Проверяет, имеет ли пользователь право просматривать конкретную оценку.
     * Вызывает ForbiddenException, если прав недостаточно.
     *
     * @param requestUserId ID пользователя, делающего запрос.
     * @param ownerUserId   ID владельца оценки.
     * @throws EntityNotFoundException если пользователь не найден.
     * @throws ForbiddenException      если доступ запрещен.
     */
    private void validateAccessToRating(Long requestUserId, Long ownerUserId) {
        User requestingUser = userRepository.findById(requestUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Пользователь может просматривать свои данные
        if (requestUserId.equals(ownerUserId)) {
            return;
        }

        // Или пользователь должен быть преподавателем/админом/владельцем
        if (requestingUser.getRole() != Role.OWNER &&
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
     * @throws EntityNotFoundException если пользователь не найден.
     */
    private void validateAccessToList(Long requestUserId) {
        // Пользователь может просматривать только свой список
        userRepository.findById(requestUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    /**
     * Проверяет, имеет ли пользователь право просматривать оценки по курсу.
     * Вызывает ForbiddenException, если прав недостаточно.
     *
     * @param userId   ID пользователя.
     * @param courseId ID курса.
     * @throws EntityNotFoundException если пользователь не найден.
     * @throws ForbiddenException      если доступ запрещен.
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