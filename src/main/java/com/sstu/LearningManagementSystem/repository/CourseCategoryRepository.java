package com.sstu.LearningManagementSystem.repository;

import com.sstu.LearningManagementSystem.model.CourseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseCategoryRepository extends JpaRepository<CourseCategory, Long> {
}
