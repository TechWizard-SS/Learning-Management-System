package com.sstu.LearningManagementSystem.repository;

import com.sstu.LearningManagementSystem.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
    List<Topic> findByModuleId(Long moduleId);

    // Новый метод для загрузки Topic с Module по ID
    @Query("SELECT t FROM Topic t JOIN FETCH t.module WHERE t.id = :id")
    Optional<Topic> findByIdWithModule(@Param("id") Long id);
}
