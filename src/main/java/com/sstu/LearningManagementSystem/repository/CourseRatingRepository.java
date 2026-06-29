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

    Optional<CourseRating> findByUserIdAndCourseId(Long userId, Long courseId);

    List<CourseRating> findByUserId(Long userId);

    List<CourseRating> findByCourseId(Long courseId);

    @Query("SELECT cr FROM CourseRating cr JOIN FETCH cr.user JOIN FETCH cr.course WHERE cr.id = :id")
    Optional<CourseRating> findByIdWithUserAndCourse(@Param("id") Long id);

    @Query("SELECT cr FROM CourseRating cr JOIN FETCH cr.user JOIN FETCH cr.course WHERE cr.user.id = :userId AND cr.course.id = :courseId")
    Optional<CourseRating> findByUserIdAndCourseIdWithUserAndCourse(@Param("userId") Long userId, @Param("courseId") Long courseId);

    @Query("SELECT cr FROM CourseRating cr JOIN FETCH cr.user JOIN FETCH cr.course WHERE cr.user.id = :userId")
    List<CourseRating> findByUserIdWithUserAndCourse(@Param("userId") Long userId);

    @Query("SELECT cr FROM CourseRating cr JOIN FETCH cr.user JOIN FETCH cr.course WHERE cr.course.id = :courseId")
    List<CourseRating> findByCourseIdWithUserAndCourse(@Param("courseId") Long courseId);
}