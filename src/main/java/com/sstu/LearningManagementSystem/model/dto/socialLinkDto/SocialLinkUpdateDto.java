package com.sstu.LearningManagementSystem.model.dto.socialLinkDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SocialLinkUpdateDto {
    private String platform; // Новая платформа
    private String url;      // Новый URL
}
