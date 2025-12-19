package com.sstu.LearningManagementSystem.repository;

import com.sstu.LearningManagementSystem.model.Assignment;
import com.sstu.LearningManagementSystem.model.CourseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByTopicId(Long topicId);
}
