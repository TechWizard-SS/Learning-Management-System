package com.sstu.LearningManagementSystem.model.dto.assignmentDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class AssignmentResponseDto implements Serializable {
    private Long id; // Use Long for ID
    private String title;
    private String description;
    private String content;
    private String type; // Or receive as AssignmentType enum value
    private String contentType; // Or receive as ContentType enum value
    private LocalDateTime deadline; // Include if added to entity
    private Long topicId; // ID of the Topic this Assignment belongs to
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
