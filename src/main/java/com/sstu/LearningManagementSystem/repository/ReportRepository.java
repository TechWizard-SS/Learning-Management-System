package com.sstu.LearningManagementSystem.repository;

import com.sstu.LearningManagementSystem.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // --- Новые методы с JOIN FETCH ---

    // Найти отчет по ID с загрузкой User и Course
    @Query("SELECT r FROM Report r JOIN FETCH r.user JOIN FETCH r.course WHERE r.id = :id")
    Optional<Report> findByIdWithUserAndCourse(@Param("id") Long id);

    // Найти отчет по ID пользователя и ID курса с загрузкой User и Course
    @Query("SELECT r FROM Report r JOIN FETCH r.user JOIN FETCH r.course WHERE r.user.id = :userId AND r.course.id = :courseId")
    Optional<Report> findByUserIdAndCourseIdWithUserAndCourse(@Param("userId") Long userId, @Param("courseId") Long courseId);

    // Найти все отчеты конкретного пользователя с загрузкой User и Course
    @Query("SELECT r FROM Report r JOIN FETCH r.user JOIN FETCH r.course WHERE r.user.id = :userId")
    List<Report> findByUserIdWithUserAndCourse(@Param("userId") Long userId);

    // Найти все отчеты по конкретному курсу с загрузкой User и Course
    @Query("SELECT r FROM Report r JOIN FETCH r.user JOIN FETCH r.course WHERE r.course.id = :courseId")
    List<Report> findByCourseIdWithUserAndCourse(@Param("courseId") Long courseId);
}