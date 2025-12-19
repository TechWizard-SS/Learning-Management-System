package com.sstu.LearningManagementSystem.repository;

import com.sstu.LearningManagementSystem.model.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {

    // Найти достижение по названию (уникально)
    Optional<Achievement> findByTitle(String title);

    // (Опционально) Найти все достижения по части названия
    // List<Achievement> findByTitleContainingIgnoreCase(String titleFragment);
}
