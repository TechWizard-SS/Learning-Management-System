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
}
