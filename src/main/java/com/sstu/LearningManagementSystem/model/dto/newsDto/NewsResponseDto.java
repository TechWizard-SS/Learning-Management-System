package com.sstu.LearningManagementSystem.model.dto.newsDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewsResponseDto {
    private Long id;
    private String title; // Название
    private String text;  // Текст
    private Double rating; // Рейтинг
    private List<String> tags; // Теги
    private Long courseId; // ID курса, к которому относится новость
    private LocalDateTime createdAt; // Дата создания (из Auditable)
    private LocalDateTime updatedAt; // Дата последнего обновления (из Auditable)
}
