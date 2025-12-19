package com.sstu.LearningManagementSystem.model.dto.socialLinkDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SocialLinkCreateDto {
    private String platform; // Платформа (например, "Twitter", "LinkedIn")
    private String url;      // URL ссылки

    // Не включаем userId - он берется из аутентифицированного пользователя (currentUserId)
    // Не включаем createdBy, createdAt - устанавливаются сервисом/аудитом
}