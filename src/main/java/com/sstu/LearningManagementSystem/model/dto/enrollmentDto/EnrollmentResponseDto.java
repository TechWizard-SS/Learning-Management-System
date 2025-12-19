package com.sstu.LearningManagementSystem.model.dto.enrollmentDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentResponseDto {
    private Long id;
    private Long userId; // ID пользователя, зачисленного на курс
    private Long courseId; // ID курса, на который зачислен пользователь
    private LocalDateTime enrollmentDate; // Дата зачисления
    private Boolean confirmed; // Подтверждено ли зачисление
    private Integer progress; // Процент прохождения
    private LocalDateTime createdAt; // Дата создания записи (из Auditable)
    private LocalDateTime updatedAt; // Дата последнего обновления (из Auditable)
    private LocalDateTime deletedAt;
}
