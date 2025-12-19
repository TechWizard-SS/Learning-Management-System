package com.sstu.LearningManagementSystem.repository;

import com.sstu.LearningManagementSystem.model.CourseRating;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
