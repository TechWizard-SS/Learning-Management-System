package com.sstu.LearningManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "assignment_submission")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentSubmission extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @Column
    private int attempts = 0;

    @Column
    private Integer successfulAttempt;

    @Column
    private Boolean passed = false;

    @Column(columnDefinition = "TEXT")
    private String answer;
}