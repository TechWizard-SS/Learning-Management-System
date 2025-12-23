package com.sstu.LearningManagementSystem.model.dto.achievementDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AchievementResponseDto {
    private Long id;
    private String title; // Название
    private String description; // Описание
    private String iconUrl; // URL иконки
    private LocalDateTime createdAt; // Дата создания (из Auditable)
    private LocalDateTime updatedAt; // Дата последнего обновления (из Auditable)
}