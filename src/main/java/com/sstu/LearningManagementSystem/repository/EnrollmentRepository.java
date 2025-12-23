package com.sstu.LearningManagementSystem.repository;

import com.sstu.LearningManagementSystem.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // Найти зачисление по ID пользователя и ID курса (уникальная пара)
    Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);

    // Найти все зачисления конкретного пользователя
    List<Enrollment> findByUserId(Long userId);

    // Найти все зачисления на конкретный курс
    List<Enrollment> findByCourseId(Long courseId);

    // --- Новые методы с JOIN FETCH ---

    // Найти зачисление по ID с загрузкой User и Course
    @Query("SELECT e FROM Enrollment e JOIN FETCH e.user JOIN FETCH e.course WHERE e.id = :id")
    Optional<Enrollment> findByIdWithUserAndCourse(@Param("id") Long id);

    // Найти зачисление по ID пользователя и ID курса с загрузкой User и Course
    @Query("SELECT e FROM Enrollment e JOIN FETCH e.user JOIN FETCH e.course WHERE e.user.id = :userId AND e.course.id = :courseId")
    Optional<Enrollment> findByUserIdAndCourseIdWithUserAndCourse(@Param("userId") Long userId, @Param("courseId") Long courseId);

    // Найти все зачисления конкретного пользователя с загрузкой User и Course
    @Query("SELECT e FROM Enrollment e JOIN FETCH e.user JOIN FETCH e.course WHERE e.user.id = :userId")
    List<Enrollment> findByUserIdWithUserAndCourse(@Param("userId") Long userId);

    // Найти все зачисления на конкретный курс с загрузкой User и Course
    @Query("SELECT e FROM Enrollment e JOIN FETCH e.user JOIN FETCH e.course WHERE e.course.id = :courseId")
    List<Enrollment> findByCourseIdWithUserAndCourse(@Param("courseId") Long courseId);
}