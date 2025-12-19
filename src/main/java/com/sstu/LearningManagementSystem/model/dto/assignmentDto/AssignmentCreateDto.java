package com.sstu.LearningManagementSystem.model.dto.assignmentDto;

import com.sstu.LearningManagementSystem.model.enumType.ContentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class AssignmentCreateDto {
    private String title;
    private String content;
    private String type;
    private String contentType;
    private LocalDateTime deadline;
    private Long topicId;
    private String description;
}
