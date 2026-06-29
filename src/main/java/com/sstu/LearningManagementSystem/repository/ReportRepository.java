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

    Optional<Report> findByUserIdAndCourseId(Long userId, Long courseId);

    List<Report> findByUserId(Long userId);

    List<Report> findByCourseId(Long courseId);

    @Query("SELECT r FROM Report r JOIN FETCH r.user JOIN FETCH r.course WHERE r.id = :id")
    Optional<Report> findByIdWithUserAndCourse(@Param("id") Long id);

    @Query("SELECT r FROM Report r JOIN FETCH r.user JOIN FETCH r.course WHERE r.user.id = :userId AND r.course.id = :courseId")
    Optional<Report> findByUserIdAndCourseIdWithUserAndCourse(@Param("userId") Long userId, @Param("courseId") Long courseId);

    @Query("SELECT r FROM Report r JOIN FETCH r.user JOIN FETCH r.course WHERE r.user.id = :userId")
    List<Report> findByUserIdWithUserAndCourse(@Param("userId") Long userId);

    @Query("SELECT r FROM Report r JOIN FETCH r.user JOIN FETCH r.course WHERE r.course.id = :courseId")
    List<Report> findByCourseIdWithUserAndCourse(@Param("courseId") Long courseId);
}