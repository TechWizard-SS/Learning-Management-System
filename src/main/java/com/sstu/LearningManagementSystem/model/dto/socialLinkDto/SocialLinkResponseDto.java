package com.sstu.LearningManagementSystem.model.dto.socialLinkDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SocialLinkResponseDto {
    private Long id;
    private String platform; // Платформа
    private String url;      // URL
    private Long userId;     // ID пользователя, которому принадлежит ссылка
    private LocalDateTime createdAt; // Дата создания (из Auditable)
    private LocalDateTime updatedAt; // Дата последнего обновления (из Auditable)
}