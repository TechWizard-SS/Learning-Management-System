package com.sstu.LearningManagementSystem.model.dto.achievementDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AchievementCreateDto {
    private String title; // Название достижения
    private String description; // Описание
    private String iconUrl; // URL иконки

    // Не включаем users - они управляются отдельно через UserService
    // Не включаем createdBy, createdAt - устанавливаются сервисом/аудитом
}
