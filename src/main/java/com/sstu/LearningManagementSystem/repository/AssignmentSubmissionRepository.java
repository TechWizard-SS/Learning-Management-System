package com.sstu.LearningManagementSystem.repository;

import com.sstu.LearningManagementSystem.model.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {

    Optional<AssignmentSubmission> findByAssignmentIdAndUserId(Long assignmentId, Long userId);

    List<AssignmentSubmission> findByUserId(Long userId);

    List<AssignmentSubmission> findByAssignmentId(Long assignmentId);

    List<AssignmentSubmission> findByAssignmentIdAndUserIdOrderByCreatedAtAsc(Long assignmentId, Long userId);

    @Query("SELECT submission FROM AssignmentSubmission submission JOIN FETCH submission.user JOIN FETCH submission.assignment WHERE submission.id = :id")
    Optional<AssignmentSubmission> findByIdWithUserAndAssignment(@Param("id") Long id);

    @Query("SELECT submission FROM AssignmentSubmission submission JOIN FETCH submission.user JOIN FETCH submission.assignment WHERE submission.assignment.id = :assignmentId AND submission.user.id = :userId")
    Optional<AssignmentSubmission> findByAssignmentIdAndUserIdWithUserAndAssignment(@Param("assignmentId") Long assignmentId, @Param("userId") Long userId);

    @Query("SELECT submission FROM AssignmentSubmission submission JOIN FETCH submission.user JOIN FETCH submission.assignment WHERE submission.assignment.id = :assignmentId")
    List<AssignmentSubmission> findByAssignmentIdWithUserAndAssignment(@Param("assignmentId") Long assignmentId);

    @Query("SELECT submission FROM AssignmentSubmission submission JOIN FETCH submission.user JOIN FETCH submission.assignment WHERE submission.assignment.id = :assignmentId AND submission.user.id = :userId ORDER BY submission.createdAt ASC")
    List<AssignmentSubmission> findByAssignmentIdAndUserIdWithUserAndAssignmentOrderByCreatedAtAsc(@Param("assignmentId") Long assignmentId, @Param("userId") Long userId);
}