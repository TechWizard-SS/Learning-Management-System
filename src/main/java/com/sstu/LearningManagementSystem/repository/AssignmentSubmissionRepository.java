package com.sstu.LearningManagementSystem.repository;

import com.sstu.LearningManagementSystem.model.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {

    // Найти сабмит по ID задания и ID пользователя
    Optional<AssignmentSubmission> findByAssignmentIdAndUserId(Long assignmentId, Long userId);

    // Найти все сабмиты конкретного пользователя
    List<AssignmentSubmission> findByUserId(Long userId);

    // Найти все сабмиты для конкретного задания
    List<AssignmentSubmission> findByAssignmentId(Long assignmentId);

    // (Опционально) Найти сабмиты пользователя по ID курса (через join с Assignment и Topic)
    // List<AssignmentSubmission> findByUserIdAndAssignment_Topic_Module_CourseId(Long userId, Long courseId);

    // Найти ВСЕ сабмиты конкретного пользователя по ID задания - ЭТОТ МЕТОД НУЖНО ДОБАВИТЬ
    List<AssignmentSubmission> findByAssignmentIdAndUserIdOrderByCreatedAtAsc(Long assignmentId, Long userId);
    // Добавлен OrderBy для получения сабмитов в хронологическом порядке (по дате создания)
}