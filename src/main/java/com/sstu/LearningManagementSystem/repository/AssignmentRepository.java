package com.sstu.LearningManagementSystem.repository;

import com.sstu.LearningManagementSystem.model.Assignment;
import com.sstu.LearningManagementSystem.model.CourseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByTopicId(Long topicId);

    @Query("SELECT a FROM Assignment a JOIN FETCH a.topic WHERE a.id = :id")
    Optional<Assignment> findByIdWithTopic(@Param("id") Long id);

    @Query("SELECT a FROM Assignment a JOIN FETCH a.topic WHERE a.topic.id = :topicId")
    List<Assignment> findByTopicIdWithTopic(@Param("topicId") Long topicId);
}
