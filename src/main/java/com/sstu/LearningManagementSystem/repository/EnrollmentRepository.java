package com.sstu.LearningManagementSystem.repository;

import com.sstu.LearningManagementSystem.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
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
}