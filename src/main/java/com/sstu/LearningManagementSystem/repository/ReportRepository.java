package com.sstu.LearningManagementSystem.repository;

import com.sstu.LearningManagementSystem.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    // Найти отчет по ID пользователя и ID курса (уникальная пара)
    Optional<Report> findByUserIdAndCourseId(Long userId, Long courseId);

    // Найти все отчеты конкретного пользователя
    List<Report> findByUserId(Long userId);

    // Найти все отчеты по конкретному курсу
    List<Report> findByCourseId(Long courseId);
}
