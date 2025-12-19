package com.sstu.LearningManagementSystem.model.dto.topicDto;

import com.sstu.LearningManagementSystem.model.enumType.ContentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TopicUpdateDto {
    private String title;
    private String description;
    private ContentType contentType;
    private String content;
}