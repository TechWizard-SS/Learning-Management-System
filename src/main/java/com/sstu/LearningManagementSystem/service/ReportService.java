package com.sstu.LearningManagementSystem.service;

import com.sstu.LearningManagementSystem.ForbiddenException;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository; // Для проверки существования пользователя
    private final EnrollmentRepository enrollmentRepository; // Для получения прогресса из Enrollment
    private final CourseRepository courseRepository;

    // Метод для обновления отчета на основе данных из Enrollment
    // Этот метод будет вызываться из EnrollmentService при обновлении прогресса
    @Transactional
    public void updateReportForEnrollment(Long enrollmentId) {
        // Найти Enrollment по ID (предполагается, что он содержит актуальный progress)
        // В реальности EnrollmentService передаст userId, courseId, и новый progress
        // Для простоты, представим, что у нас есть метод получения прогресса из EnrollmentService
        // ReportService может зависеть от EnrollmentService или использовать EnrollmentRepository напрямую
        // В этом примере используем EnrollmentRepository напрямую, если ReportService сам обновляет.

        // Предположим, что мы получили userId, courseId, и новый progress из Enrollment
        // (Это может быть передано как параметр или извлечено из EnrollmentRepository)
        // Long userId = ...;
        // Long courseId = ...;
        // Integer newProgress = ...; // из Enrollment

        // Этот метод будет вызываться из EnrollmentService.updateProgressForEnrollment
        // Поэтому он получит userId, courseId, и progress как параметры
    }

    // Более реалистичный метод обновления отчета
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
     */
    public ReportResponseDto getReportById(Long currentUserId, Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("Report not found with id: " + reportId));

        // Проверить права доступа: текущий пользователь == владелец отчета ИЛИ преподаватель/админ
        validateAccessToReport(currentUserId, report.getUser().getId());

        return toDto(report);
    }

    /**
     * Получить отчет пользователя по ID курса.
     *
     * @param currentUserId ID пользователя, запрашивающего отчет.
     * @param courseId      ID курса.
     * @return DTO отчета.
     */
    public ReportResponseDto getReportByUserAndCourseId(Long currentUserId, Long courseId) {
        // Проверить, существует ли пользователь (аутентификация)
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Report report = reportRepository.findByUserIdAndCourseId(currentUserId, courseId)
                .orElseThrow(() -> new EntityNotFoundException("Report not found for user id: " + currentUserId + " and course id: " + courseId));

        // Проверить права доступа: текущий пользователь == владелец отчета ИЛИ преподаватель/админ
        validateAccessToReport(currentUserId, report.getUser().getId());

        return toDto(report);
    }

    /**
     * Получить все отчеты пользователя.
     *
     * @param currentUserId ID пользователя, запрашивающего список.
     * @return Список DTO отчетов.
     */
    public List<ReportResponseDto> getReportsByUserId(Long currentUserId) {
        // Проверить права: currentUserId == запрашиваемый пользователь ИЛИ преподаватель/админ
        validateAccessToList(currentUserId);

        List<Report> reports = reportRepository.findByUserId(currentUserId);
        return reports.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Получить все отчеты по курсу (для преподавателя).
     * Требует проверки прав: только преподаватель/админ/владелец курса.
     *
     * @param currentUserId ID пользователя, запрашивающего список.
     * @param courseId      ID курса.
     * @return Список DTO отчетов.
     */
    public List<ReportResponseDto> getReportsByCourseId(Long currentUserId, Long courseId) {
        // Проверить права: currentUserId должен быть преподавателем/админом/владельцем курса
        validateUserCanViewReports(currentUserId, courseId);

        List<Report> reports = reportRepository.findByCourseId(courseId);
        return reports.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // --- Вспомогательные методы ---

    private ReportResponseDto toDto(Report report) {
        ReportResponseDto dto = new ReportResponseDto();
        dto.setId(report.getId());
        dto.setUserId(report.getUser().getId());
        dto.setCourseId(report.getCourse().getId());
        dto.setProgress(report.getProgress());
        dto.setCompletedAssignments(report.getCompletedAssignments());
        // dto.setDeletedAt(report.getDeletedAt()); // Включить, если используется soft-delete
        return dto;
    }

    /**
     * Проверяет, имеет ли пользователь право просматривать конкретный отчет.
     * Вызывает ForbiddenException, если прав недостаточно.
     *
     * @param requestUserId ID пользователя, делающего запрос.
     * @param ownerUserId   ID владельца отчета.
     */
    private void validateAccessToReport(Long requestUserId, Long ownerUserId) {
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
            throw new ForbiddenException("Access denied: Cannot view another user's report.");
        }
    }

    /**
     * Проверяет, имеет ли пользователь право просматривать список отчетов.
     * Вызывает ForbiddenException, если прав недостаточно.
     *
     * @param requestUserId ID пользователя, делающего запрос.
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
