package com.sstu.LearningManagementSystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.sstu.LearningManagementSystem.model.Module;
import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    List<Module> findByCourseId(Long courseId);

    // --- Новый метод с JOIN FETCH ---
    @Query("SELECT m FROM Module m JOIN FETCH m.course WHERE m.id = :id")
    Optional<Module> findByIdWithCourse(@Param("id") Long id);
}