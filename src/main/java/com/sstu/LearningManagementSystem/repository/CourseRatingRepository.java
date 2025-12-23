package com.sstu.LearningManagementSystem.repository;

import com.sstu.LearningManagementSystem.model.CourseRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRatingRepository extends JpaRepository<CourseRating, Long> {

    // Найти оценку по ID пользователя и ID курса (уникальная пара, пользователь может оценить курс только один раз)
    Optional<CourseRating> findByUserIdAndCourseId(Long userId, Long courseId);

    // Найти все оценки конкретного пользователя
    List<CourseRating> findByUserId(Long userId);

    // Найти все оценки по конкретному курсу (для вычисления средней оценки)
    List<CourseRating> findByCourseId(Long courseId);

    // --- Новые методы с JOIN FETCH ---

    // Найти оценку по ID с загрузкой User и Course
    @Query("SELECT cr FROM CourseRating cr JOIN FETCH cr.user JOIN FETCH cr.course WHERE cr.id = :id")
    Optional<CourseRating> findByIdWithUserAndCourse(@Param("id") Long id);

    // Найти оценку по ID пользователя и ID курса с загрузкой User и Course
    @Query("SELECT cr FROM CourseRating cr JOIN FETCH cr.user JOIN FETCH cr.course WHERE cr.user.id = :userId AND cr.course.id = :courseId")
    Optional<CourseRating> findByUserIdAndCourseIdWithUserAndCourse(@Param("userId") Long userId, @Param("courseId") Long courseId);

    // Найти все оценки конкретного пользователя с загрузкой User и Course
    @Query("SELECT cr FROM CourseRating cr JOIN FETCH cr.user JOIN FETCH cr.course WHERE cr.user.id = :userId")
    List<CourseRating> findByUserIdWithUserAndCourse(@Param("userId") Long userId);

    // Найти все оценки по конкретному курсу с загрузкой User и Course
    @Query("SELECT cr FROM CourseRating cr JOIN FETCH cr.user JOIN FETCH cr.course WHERE cr.course.id = :courseId")
    List<CourseRating> findByCourseIdWithUserAndCourse(@Param("courseId") Long courseId);
}