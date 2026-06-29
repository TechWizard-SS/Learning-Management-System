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
public class NewsUpdateDto {
    private String title; // Новое название
    private String text;  // Новый текст
    private List<String> tags; // Новые теги
}