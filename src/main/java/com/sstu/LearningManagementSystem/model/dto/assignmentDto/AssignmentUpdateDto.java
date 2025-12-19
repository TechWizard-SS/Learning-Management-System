package com.sstu.LearningManagementSystem.model.dto.assignmentDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class AssignmentUpdateDto {
    private String title; // Use Long for ID
    private String description;
    private String content;
    private String type; // Or send as AssignmentType enum value
    private String contentType; // Or send as ContentType enum value
    private LocalDateTime deadline; // Include if added to entity
}
