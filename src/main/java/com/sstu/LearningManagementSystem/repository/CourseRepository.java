package com.sstu.LearningManagementSystem.repository;

import com.sstu.LearningManagementSystem.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.category WHERE c.id = :id")
    Optional<Course> findByIdWithCategory(@Param("id") Long id);

    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.category")
    List<Course> findAllWithCategory();

    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.category LEFT JOIN FETCH c.tags WHERE c.id = :id")
    Optional<Course> findByIdWithCategoryAndTags(@Param("id") Long id);

    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.category LEFT JOIN FETCH c.tags")
    List<Course> findAllWithCategoryAndTags();
}