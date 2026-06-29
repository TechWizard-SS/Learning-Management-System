package com.sstu.LearningManagementSystem.model.dto.topicDto;

import com.sstu.LearningManagementSystem.model.enumType.ContentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TopicResponseDto implements Serializable {
    private Long id;
    private String title;
    private String description;
    private ContentType contentType;
    private String content;
    private Long moduleId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}