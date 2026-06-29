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

    Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);

    List<Enrollment> findByUserId(Long userId);

    List<Enrollment> findByCourseId(Long courseId);

    @Query("SELECT e FROM Enrollment e JOIN FETCH e.user JOIN FETCH e.course WHERE e.id = :id")
    Optional<Enrollment> findByIdWithUserAndCourse(@Param("id") Long id);

    @Query("SELECT e FROM Enrollment e JOIN FETCH e.user JOIN FETCH e.course WHERE e.user.id = :userId AND e.course.id = :courseId")
    Optional<Enrollment> findByUserIdAndCourseIdWithUserAndCourse(@Param("userId") Long userId, @Param("courseId") Long courseId);

    @Query("SELECT e FROM Enrollment e JOIN FETCH e.user JOIN FETCH e.course WHERE e.user.id = :userId")
    List<Enrollment> findByUserIdWithUserAndCourse(@Param("userId") Long userId);

    @Query("SELECT e FROM Enrollment e JOIN FETCH e.user JOIN FETCH e.course WHERE e.course.id = :courseId")
    List<Enrollment> findByCourseIdWithUserAndCourse(@Param("courseId") Long courseId);
}