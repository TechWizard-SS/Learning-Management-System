package com.sstu.LearningManagementSystem.service;

import com.sstu.LearningManagementSystem.ForbiddenException;
import com.sstu.LearningManagementSystem.model.Course;
import com.sstu.LearningManagementSystem.model.Report;
import com.sstu.LearningManagementSystem.model.User;
import com.sstu.LearningManagementSystem.model.dto.reportDto.ReportResponseDto;
import com.sstu.LearningManagementSystem.model.enumType.Role;
import com.sstu.LearningManagementSystem.repository.CourseRepository;
import com.sstu.LearningManagementSystem.repository.EnrollmentRepository;
import com.sstu.LearningManagementSystem.repository.ReportRepository;
import com.sstu.LearningManagementSystem.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для управления отчетами (Report).
 * Обеспечивает создание, обновление, получение и удаление отчетов о прогрессе пользователей по курсам.
 * Проверяет права доступа к отчетам.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository; // Для проверки существования пользователя
    private final EnrollmentRepository enrollmentRepository; // Для получения прогресса из Enrollment
    private final CourseRepository courseRepository;

    /**
     * Обновляет или создает отчет о прогрессе пользователя по курсу.
     * Если отчета нет, создает новый. Если есть - обновляет прогресс и количество выполненных заданий.
     *
     * @param userId                  ID пользователя, чей отчет обновляется.
     * @param courseId                ID курса, по которому обновляется отчет.
     * @param newProgress             Новый прогресс (в процентах или числовом значении).
     * @param newCompletedAssignments Новое количество выполненных заданий.
     */
    @Transactional
    public void updateReport(Long userId, Long courseId, Integer newProgress, Integer newCompletedAssignments) {
        // Найти существующий отчет
        Report report = reportRepository.findByUserIdAndCourseId(userId, courseId).orElse(null);

        if (report == null) {
            // Если отчета нет, создаем новый
            report = Report.builder()
                    .user(userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found")))
                    .course(courseRepository.findById(courseId).orElseThrow(() -> new EntityNotFoundException("Course not found")))
                    .progress(newProgress.doubleValue()) // Преобразуем Integer в Double
                    .completedAssignments(newCompletedAssignments)
                    .build();
        } else {
            // Если отчет существует, обновляем его
            report.setProgress(newProgress.doubleValue());
            report.setCompletedAssignments(newCompletedAssignments);
        }

        reportRepository.save(report);
    }

    /**
     * Получить отчет по ID.
     * Требует проверки прав: пользователь может видеть только свой отчет или преподаватель/админ - любой.
     *
     * @param currentUserId ID пользователя, запрашивающего отчет.
     * @param reportId      ID отчета.
     * @return DTO отчета.
     * @throws EntityNotFoundException если отчет не найден.
     * @throws ForbiddenException      если доступ запрещен.
     */
    public ReportResponseDto getReportById(Long currentUserId, Long reportId) {
        // Используем метод с JOIN FETCH
        Report report = reportRepository.findByIdWithUserAndCourse(reportId)
                .orElseThrow(() -> new EntityNotFoundException("Report not found with id: " + reportId));

        // Проверить права доступа: текущий пользователь == владелец отчета ИЛИ преподаватель/админ
        validateAccessToReport(currentUserId, report.getUser().getId());

        return toDto(report); // <-- Теперь toDto не вызывает LazyInit
    }

    /**
     * Получить отчет пользователя по ID курса.
     * Если отчета нет, создает новый с нулевым прогрессом и сохраняет его.
     *
     * @param currentUserId ID пользователя, запрашивающего отчет.
     * @param courseId      ID курса.
     * @return DTO отчета.
     * @throws EntityNotFoundException если пользователь или курс не найдены.
     * @throws ForbiddenException      если доступ запрещен.
     */
    @Transactional // @Transactional нужен, так как метод может создать и сохранить отчет
    public ReportResponseDto getReportByUserAndCourseId(Long currentUserId, Long courseId) {
        // Проверить, существует ли пользователь (аутентификация)
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        // Используем метод с JOIN FETCH, если отчет найден
        Report report = reportRepository.findByUserIdAndCourseIdWithUserAndCourse(currentUserId, courseId).orElse(null);

        if (report == null) {
            // Если отчета нет, создаем новый
            report = Report.builder()
                    .user(user)
                    .course(course)
                    .progress(0.0) // Начальный прогресс
                    .completedAssignments(0) // Начальное количество выполненных заданий
                    .build();
            // Сохраняем новый отчет в БД
            report = reportRepository.save(report);
            // report уже содержит User и Course, так как они были загружены при создании или через builder
        }
        // Если report != null, он уже содержит User и Course благодаря JOIN FETCH

        // Проверить права доступа: текущий пользователь == владелец отчета ИЛИ преподаватель/админ
        validateAccessToReport(currentUserId, report.getUser().getId());

        return toDto(report); // <-- Теперь toDto не вызывает LazyInit
    }

    /**
     * Получить все отчеты пользователя.
     *
     * @param currentUserId ID пользователя, запрашивающего список.
     * @return Список DTO отчетов.
     * @throws ForbiddenException если доступ запрещен.
     */
    public List<ReportResponseDto> getReportsByUserId(Long currentUserId) {
        // Проверить права: currentUserId == запрашиваемый пользователь ИЛИ преподаватель/админ
        validateAccessToList(currentUserId);

        // Используем метод с JOIN FETCH
        List<Report> reports = reportRepository.findByUserIdWithUserAndCourse(currentUserId);
        return reports.stream()
                .map(this::toDto) // <-- Теперь toDto не вызывает LazyInit для каждого элемента
                .collect(Collectors.toList());
    }

    /**
     * Получить все отчеты по курсу (для преподавателя).
     * Требует проверки прав: только преподаватель/админ/владелец курса.
     *
     * @param currentUserId ID пользователя, запрашивающего список.
     * @param courseId      ID курса.
     * @return Список DTO отчетов.
     * @throws ForbiddenException если доступ запрещен.
     */
    public List<ReportResponseDto> getReportsByCourseId(Long currentUserId, Long courseId) {
        // Проверить права: currentUserId должен быть преподавателем/админом/владельцем курса
        validateUserCanViewReports(currentUserId, courseId);

        // Используем метод с JOIN FETCH
        List<Report> reports = reportRepository.findByCourseIdWithUserAndCourse(courseId);
        return reports.stream()
                .map(this::toDto) // <-- Теперь toDto не вызывает LazyInit для каждого элемента
                .collect(Collectors.toList());
    }

    // --- Вспомогательные методы ---

    /**
     * Преобразует сущность Report в ReportResponseDto.
     *
     * @param report Сущность отчета для преобразования.
     * @return DTO отчета.
     */
    private ReportResponseDto toDto(Report report) {
        ReportResponseDto dto = new ReportResponseDto();
        dto.setId(report.getId());
        // Убедитесь, что report.getUser() и report.getCourse() загружены (JOIN FETCH в репозитории)
        dto.setUserId(report.getUser().getId());
        dto.setCourseId(report.getCourse().getId());
        dto.setProgress(report.getProgress());
        dto.setCompletedAssignments(report.getCompletedAssignments());
        dto.setCreatedAt(report.getCreatedAt());
        dto.setUpdatedAt(report.getUpdatedAt());
        return dto;
    }

    /**
     * Проверяет, имеет ли пользователь право просматривать конкретный отчет.
     * Вызывает ForbiddenException, если прав недостаточно.
     *
     * @param requestUserId ID пользователя, делающего запрос.
     * @param ownerUserId   ID владельца отчета.
     * @throws EntityNotFoundException если пользователь не найден.
     * @throws ForbiddenException      если доступ запрещен.
     */
    private void validateAccessToReport(Long requestUserId, Long ownerUserId) {
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
            throw new ForbiddenException("Access denied: Cannot view another user's report.");
        }
    }

    /**
     * Проверяет, имеет ли пользователь право просматривать список отчетов.
     * Вызывает ForbiddenException, если прав недостаточно.
     *
     * @param requestUserId ID пользователя, делающего запрос.
     * @throws EntityNotFoundException если пользователь не найден.
     */
    private void validateAccessToList(Long requestUserId) {
        // Пользователь может просматривать только свой список
        // Другие роли (преподаватель/админ) проверяются в других методах
        userRepository.findById(requestUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        // Дальнейшие проверки происходят в вызывающих методах (getReportsByUserId vs getReportsByCourseId)
    }

    /**
     * Проверяет, имеет ли пользователь право просматривать отчеты по курсу.
     * Вызывает ForbiddenException, если прав недостаточно.
     *
     * @param userId   ID пользователя.
     * @param courseId ID курса.
     * @throws EntityNotFoundException если пользователь не найден.
     * @throws ForbiddenException      если доступ запрещен.
     */
    private void validateUserCanViewReports(Long userId, Long courseId) {
        // Получить курс и связанный с ним пользователь
        // Course course = courseRepository.findById(courseId) // Если нужна проверка принадлежности курса
        //         .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Проверить роль пользователя (аналогично другим validate методам)
        if (user.getRole() != Role.OWNER &&
                user.getRole() != Role.ADMIN &&
                user.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Only OWNER, ADMIN, or TEACHER can view reports for a course");
        }
    }
}
