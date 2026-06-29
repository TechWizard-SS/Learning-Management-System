package com.sstu.LearningManagementSystem.model.dto.newsDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewsCreateDto {
    private String title; // Название новости
    private String text;  // Текст новости
    private List<String> tags; // Теги
    private Long courseId; // ID курса, к которому относится новость
}
