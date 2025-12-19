package com.sstu.LearningManagementSystem.repository;

import com.sstu.LearningManagementSystem.model.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

    // Найти все новости по ID курса
    List<News> findByCourseId(Long courseId);

    // (Опционально) Найти новости по тегу
    // List<News> findByTagsContaining(String tag);

    // (Опционально) Найти новости по курсу, отсортированные по рейтингу
    // List<News> findByCourseIdOrderByRatingDesc(Long courseId);
}
