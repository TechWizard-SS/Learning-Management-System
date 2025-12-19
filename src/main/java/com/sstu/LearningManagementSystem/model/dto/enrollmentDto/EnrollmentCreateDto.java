package com.sstu.LearningManagementSystem.model.dto.enrollmentDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentCreateDto {
    // Поле courseId, которое пользователь хочет "запустить"
    private Long courseId;
    // Не включаем userId, он берется из аутентифицированного пользователя (currentUserId)
    // Не включаем enrollmentDate, confirmed, progress - они устанавливаются сервисом
}