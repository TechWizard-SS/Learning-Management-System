package com.sstu.LearningManagementSystem.model.dto.topicDto;

import com.sstu.LearningManagementSystem.model.enumType.ContentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TopicCreateDto {
    @NotBlank
    private String title;
    private String description;
    @NotNull
    private ContentType contentType;
    private String content;
    @NotNull
    private Long moduleId; // Link to Module
}