package com.sstu.LearningManagementSystem.model;


import com.sstu.LearningManagementSystem.model.enumType.AssignmentType;
import com.sstu.LearningManagementSystem.model.enumType.ContentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assignment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assignment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentType type;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type") // Optional: specify DB column name
    private ContentType contentType; // Ensure this enum exists (TEXT, VIDEO, DOCUMENT, AUDIO, etc.)

    @Column(name = "deadline") // Optional: specify DB column name
    private LocalDateTime deadline; // Optional: add deadline field

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssignmentSubmission> submissions = new ArrayList<>();
}
