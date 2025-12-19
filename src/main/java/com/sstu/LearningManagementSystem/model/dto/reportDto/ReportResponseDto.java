package com.sstu.LearningManagementSystem.model.dto.reportDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponseDto {
    private Long id;
    private Long userId; // ID пользователя, для которого сформирован отчет
    private Long courseId; // ID курса, по которому сформирован отчет
    private Double progress; // Процент прохождения курса
    private Integer completedAssignments; // Количество выполненных заданий
    private LocalDateTime createdAt; // Дата создания отчета (из Auditable)
    private LocalDateTime updatedAt; // Дата последнего обновления отчета (из Auditable)
    // private LocalDateTime deletedAt; // Если используется soft-delete (из Auditable)
}
