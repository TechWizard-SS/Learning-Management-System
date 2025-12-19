package com.sstu.LearningManagementSystem.model.dto.achievementDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AchievementUpdateDto {
    private String title; // Новое название (если разрешено изменение)
    private String description; // Новое описание
    private String iconUrl; // Новый URL иконки
    // Не включаем users - они управляются отдельно через UserService
}