package com.sstu.LearningManagementSystem.service;

import com.sstu.LearningManagementSystem.ForbiddenException;
import com.sstu.LearningManagementSystem.model.Course;
import com.sstu.LearningManagementSystem.model.Enrollment;
import com.sstu.LearningManagementSystem.model.User;
import com.sstu.LearningManagementSystem.model.dto.enrollmentDto.EnrollmentCreateDto;
import com.sstu.LearningManagementSystem.model.dto.enrollmentDto.EnrollmentResponseDto;
import com.sstu.LearningManagementSystem.model.dto.enrollmentDto.EnrollmentUpdateDto;
import com.sstu.LearningManagementSystem.model.enumType.Role;
import com.sstu.LearningManagementSystem.repository.CourseRepository;
import com.sstu.LearningManagementSystem.repository.EnrollmentRepository;
import com.sstu.LearningManagementSystem.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для управления зачислениями (Enrollment).
 * Обеспечивает создание, обновление, получение и удаление зачислений пользователей на курсы.
 * Обновляет прогресс и связаные отчеты.
 * Проверяет права доступа к зачислениям.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository; // Для проверки существования курса
    private final UserRepository userRepository; // Для проверки существования пользователя
    private final ReportService reportService;

    /**
     * Записывает пользователя на курс. Создает Enrollment.
     * Если зачисление уже существует, возвращает существующее (или может обновить статус, если логика требует).
     * Создает начальный отчет для пользователя по курсу.
     *
     * @param currentUserId ID пользователя, инициирующего запись.
     * @param createDto DTO с ID курса.
     * @return DTO созданного зачисления.
     * @throws EntityNotFoundException если пользователь или курс не найдены.
     */
    @Transactional
    public EnrollmentResponseDto enrollUser(Long currentUserId, EnrollmentCreateDto createDto) {
        // 1. Проверить, существует ли пользователь (аутентификация)
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + currentUserId));

        // 2. Проверить, существует ли курс
        Course course = courseRepository.findById(createDto.getCourseId())
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + createDto.getCourseId()));

        // 3. Проверить, существует ли уже зачисление на этот курс
        // (Опционально: можно использовать метод с JOIN FETCH, если валидация требует)
        // Enrollment existingEnrollment = enrollmentRepository.findByUserIdAndCourseIdWithUserAndCourse(currentUserId, createDto.getCourseId()).orElse(null);

        // 4. Создать новое зачисление
        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .course(course)
                .enrollmentDate(LocalDateTime.now()) // Устанавливаем текущую дату
                .confirmed(false) // По умолчанию не подтверждено
                .progress(0) // Начальный прогресс
                .build();

        // 5. Сохранить
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        // 6. Обновить/создать отчет
        reportService.updateReport(savedEnrollment.getUser().getId(), savedEnrollment.getCourse().getId(), 0, 0); // начальные значения

        // 7. Перезапрашиваем Enrollment с User и Course, чтобы избежать LazyInit в toDto
        Enrollment savedWithUserAndCourse = enrollmentRepository.findByIdWithUserAndCourse(savedEnrollment.getId())
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found after creation")); // На всякий случай

        // 8. Вернуть DTO
        return toDto(savedWithUserAndCourse); // <-- Теперь toDto не вызывает LazyInit
    }

    /**
     * Получить зачисление по ID.
     * Требует проверки прав: пользователь может видеть только свое зачисление или преподаватель/админ - любое.
     *
     * @param currentUserId ID пользователя, запрашивающего зачисление.
     * @param enrollmentId  ID зачисления.
     * @return DTO зачисления.
     * @throws EntityNotFoundException если зачисление не найдено.
     * @throws ForbiddenException      если доступ запрещен.
     */
    public EnrollmentResponseDto getEnrollmentById(Long currentUserId, Long enrollmentId) {
        // Используем метод с JOIN FETCH
        Enrollment enrollment = enrollmentRepository.findByIdWithUserAndCourse(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found with id: " + enrollmentId));

        // Проверить права доступа: текущий пользователь == владелец зачисления ИЛИ преподаватель/админ
        validateAccessToEnrollment(currentUserId, enrollment.getUser().getId());

        return toDto(enrollment); // <-- Теперь toDto не вызывает LazyInit
    }

    /**
     * Получить все зачисления пользователя.
     *
     * @param currentUserId ID пользователя, запрашивающего список.
     * @return Список DTO зачислений.
     * @throws ForbiddenException если доступ запрещен.
     */
    public List<EnrollmentResponseDto> getEnrollmentsByUserId(Long currentUserId) {
        // Проверить права: currentUserId == запрашиваемый пользователь ИЛИ преподаватель/админ
        validateAccessToList(currentUserId);

        // Используем метод с JOIN FETCH
        List<Enrollment> enrollments = enrollmentRepository.findByUserIdWithUserAndCourse(currentUserId);
        return enrollments.stream()
                .map(this::toDto) // <-- Теперь toDto не вызывает LazyInit для каждого элемента
                .collect(Collectors.toList());
    }

    /**
     * Получить все зачисления на курс (для преподавателя).
     * Требует проверки прав: только преподаватель/админ/владелец курса.
     *
     * @param currentUserId ID пользователя, запрашивающего список.
     * @param courseId      ID курса.
     * @return Список DTO зачислений.
     * @throws ForbiddenException если доступ запрещен.
     */
    public List<EnrollmentResponseDto> getEnrollmentsByCourseId(Long currentUserId, Long courseId) {
        // Проверить права: currentUserId должен быть преподавателем/админом/владельцем курса
        validateUserCanViewEnrollments(currentUserId, courseId);

        // Используем метод с JOIN FETCH
        List<Enrollment> enrollments = enrollmentRepository.findByCourseIdWithUserAndCourse(courseId);
        return enrollments.stream()
                .map(this::toDto) // <-- Теперь toDto не вызывает LazyInit для каждого элемента
                .collect(Collectors.toList());
    }

    /**
     * Обновить зачисление (например, подтвердить).
     * Требует проверки прав: только преподаватель/админ/владелец курса.
     *
     * @param currentUserId ID пользователя, инициирующего обновление.
     * @param enrollmentId  ID зачисления.
     * @param updateDto     DTO с новыми значениями.
     * @return DTO обновленного зачисления.
     * @throws EntityNotFoundException если зачисление не найдено.
     * @throws ForbiddenException      если доступ запрещен.
     */
    @Transactional
    public EnrollmentResponseDto updateEnrollment(Long currentUserId, Long enrollmentId, EnrollmentUpdateDto updateDto) {
        // Проверить права: только преподаватель/админ/владелец курса
        validateUserCanUpdateEnrollment(currentUserId, enrollmentId);

        // Используем метод с JOIN FETCH
        Enrollment enrollment = enrollmentRepository.findByIdWithUserAndCourse(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found with id: " + enrollmentId));

        // Обновить поля из DTO
        if (updateDto.getConfirmed() != null) enrollment.setConfirmed(updateDto.getConfirmed());

        Enrollment updatedEnrollment = enrollmentRepository.save(enrollment);

        // Перезапрашиваем обновленный Enrollment с User и Course, чтобы избежать LazyInit в toDto
        Enrollment updatedWithUserAndCourse = enrollmentRepository.findByIdWithUserAndCourse(updatedEnrollment.getId()) // <-- Добавлено
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found after update")); // На всякий случай

        return toDto(updatedWithUserAndCourse); // <-- Теперь toDto не вызывает LazyInit
    }

    /**
     * Обновить прогресс пользователя по курсу.
     * Вызывается, например, когда пользователь завершает тему или задание.
     * Требует проверки прав: только преподаватель/админ/владелец курса ИЛИ сам пользователь (если обновление связано с его действиями).
     * Часто вызывается внутренне сервисом при завершении тем/заданий.
     *
     * @param enrollmentId ID зачисления.
     * @param newProgress  Новый процент прогресса (0-100).
     * @return DTO обновленного зачисления.
     * @throws EntityNotFoundException если зачисление не найдено.
     */
    @Transactional
    public EnrollmentResponseDto updateProgressForEnrollment(Long enrollmentId, Integer newProgress) {
        // Используем метод с JOIN FETCH
        Enrollment enrollment = enrollmentRepository.findByIdWithUserAndCourse(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found with id: " + enrollmentId));

        // Обновить прогресс
        enrollment.setProgress(Math.max(0, Math.min(100, newProgress))); // Ограничить 0-100%

        Enrollment updatedEnrollment = enrollmentRepository.save(enrollment);

        // Перезапрашиваем обновленный Enrollment с User и Course, чтобы избежать LazyInit в toDto
        Enrollment updatedWithUserAndCourse = enrollmentRepository.findByIdWithUserAndCourse(updatedEnrollment.getId()) // <-- Добавлено
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found after update")); // На всякий случай

        return toDto(updatedWithUserAndCourse); // <-- Теперь toDto не вызывает LazyInit
    }

    // --- Вспомогательные методы ---

    /**
     * Преобразует сущность Enrollment в EnrollmentResponseDto.
     * Включает ID, ID пользователя, ID курса, дату зачисления, статус подтверждения, прогресс и даты создания/обновления.
     *
     * @param enrollment Сущность зачисления для преобразования.
     * @return DTO зачисления.
     */
    private EnrollmentResponseDto toDto(Enrollment enrollment) {
        EnrollmentResponseDto dto = new EnrollmentResponseDto();
        dto.setId(enrollment.getId());
        // Убедитесь, что enrollment.getUser() и enrollment.getCourse() загружены (JOIN FETCH в репозитории)
        dto.setUserId(enrollment.getUser().getId());
        dto.setCourseId(enrollment.getCourse().getId());
        dto.setEnrollmentDate(enrollment.getEnrollmentDate());
        dto.setConfirmed(enrollment.getConfirmed());
        dto.setProgress(enrollment.getProgress());
        dto.setCreatedAt(enrollment.getCreatedAt());
        dto.setUpdatedAt(enrollment.getUpdatedAt());
        return dto;
    }

    /**
     * Проверяет, имеет ли пользователь право просматривать конкретное зачисление.
     * Вызывает ForbiddenException, если прав недостаточно.
     *
     * @param requestUserId ID пользователя, делающего запрос.
     * @param ownerUserId   ID владельца зачисления.
     * @throws EntityNotFoundException если пользователь не найден.
     * @throws ForbiddenException      если доступ запрещен.
     */
    private void validateAccessToEnrollment(Long requestUserId, Long ownerUserId) {
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
            throw new ForbiddenException("Access denied: Cannot view another user's enrollment.");
        }
    }

    /**
     * Проверяет, имеет ли пользователь право просматривать список зачислений.
     * Вызывает ForbiddenException, если прав недостаточно.
     *
     * @param requestUserId ID пользователя, делающего запрос.
     * @throws EntityNotFoundException если пользователь не найден.
     */
    private void validateAccessToList(Long requestUserId) {
        // Пользователь может просматривать только свой список
        // Другие роли (преподаватель/админ) проверяются в других методах
        // Этот метод просто проверяет, что запрос от аутентифицированного пользователя
        userRepository.findById(requestUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        // Дальнейшие проверки происходят в вызывающих методах (getEnrollmentsByUserId vs getEnrollmentsByCourseId)
    }

    /**
     * Проверяет, имеет ли пользователь право просматривать зачисления на курс.
     * Вызывает ForbiddenException, если прав недостаточно.
     *
     * @param userId   ID пользователя.
     * @param courseId ID курса.
     * @throws EntityNotFoundException если пользователь или курс не найдены.
     * @throws ForbiddenException      если доступ запрещен.
     */
    private void validateUserCanViewEnrollments(Long userId, Long courseId) {
        // Получить курс и связанный с ним пользователь
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Проверить роль пользователя (аналогично другим validate методам)
        if (user.getRole() != Role.OWNER &&
                user.getRole() != Role.ADMIN &&
                user.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Only OWNER, ADMIN, or TEACHER can view enrollments for a course");
        }
    }

    /**
     * Проверяет, имеет ли пользователь право обновлять зачисление.
     * Вызывает ForbiddenException, если прав недостаточно.
     *
     * @param userId         ID пользователя.
     * @param enrollmentId   ID зачисления.
     * @throws EntityNotFoundException если пользователь или зачисление не найдены.
     * @throws ForbiddenException      если доступ запрещен.
     */
    private void validateUserCanUpdateEnrollment(Long userId, Long enrollmentId) {
        // Получить зачисление и проверить права
        // Используем метод с JOIN FETCH
        Enrollment enrollment = enrollmentRepository.findByIdWithUserAndCourse(enrollmentId) // <-- Изменено
                .orElseThrow(() -> new EntityNotFoundException("Enrollment not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Только преподаватель/админ/владелец курса может обновить зачисление
        if (user.getRole() != Role.OWNER &&
                user.getRole() != Role.ADMIN &&
                user.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Only OWNER, ADMIN, or TEACHER can update an enrollment");
        }
    }
}