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
    // private LocalDateTime deletedAt; // Если используется soft-delete (из Auditable)
    // private String createdBy; // Если нужно отображать автора (из Auditable)
    // private String updatedBy; // Если нужно отображать автора обновления (из Auditable)
}