package com.sstu.LearningManagementSystem.repository;

import com.sstu.LearningManagementSystem.model.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

    // Найти все новости по ID курса
    List<News> findByCourseId(Long courseId);

    // --- Новые методы с JOIN FETCH ---

    // Найти новость по ID с загрузкой Course
    @Query("SELECT n FROM News n JOIN FETCH n.course WHERE n.id = :id")
    Optional<News> findByIdWithCourse(@Param("id") Long id);

    // Найти все новости по ID курса с загрузкой Course
    @Query("SELECT n FROM News n JOIN FETCH n.course WHERE n.course.id = :courseId")
    List<News> findByCourseIdWithCourse(@Param("courseId") Long courseId);
}
