package com.sstu.LearningManagementSystem.repository;

import com.sstu.LearningManagementSystem.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
}
