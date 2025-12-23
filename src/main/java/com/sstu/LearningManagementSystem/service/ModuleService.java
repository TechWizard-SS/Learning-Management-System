package com.sstu.LearningManagementSystem.service;


import com.sstu.LearningManagementSystem.ForbiddenException;
import com.sstu.LearningManagementSystem.model.Course;
import com.sstu.LearningManagementSystem.model.User;
import com.sstu.LearningManagementSystem.model.dto.moduleDto.ModuleCreateDto;
import com.sstu.LearningManagementSystem.model.dto.moduleDto.ModuleResponseDto;
import com.sstu.LearningManagementSystem.model.dto.moduleDto.ModuleUpdateDto;
import com.sstu.LearningManagementSystem.model.enumType.Role;
import com.sstu.LearningManagementSystem.repository.CourseRepository;
import com.sstu.LearningManagementSystem.repository.ModuleRepository;
import com.sstu.LearningManagementSystem.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.sstu.LearningManagementSystem.model.Module;


/**
 * Сервис для управления модулями (Module).
 * Обеспечивает создание, обновление, получение и удаление модулей, связанных с курсами.
 * Проверяет права доступа к модулям (только преподаватель/админ/владелец могут редактировать).
 */
@Service
@RequiredArgsConstructor
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    /**
     * Создает новый модуль для курса.
     * Проверяет права текущего пользователя на создание модуля.
     *
     * @param currentUserId ID пользователя, инициирующего создание.
     * @param dto           DTO с данными для создания модуля.
     * @return DTO созданного модуля.
     * @throws EntityNotFoundException если курс не найден.
     * @throws ForbiddenException      если у пользователя недостаточно прав.
     */
    public ModuleResponseDto createModule(Long currentUserId, ModuleCreateDto dto) {
        validateModuleCreationPermissions(currentUserId, dto.getCourseId());
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new EntityNotFoundException("Course not found")); // Лучше использовать orElseThrow

        Module module = Module.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .course(course)
                .build();

        Module savedModule = moduleRepository.save(module);

        // Перезапрашиваем Module с Course, чтобы избежать LazyInit в toDto
        Module savedWithCourse = moduleRepository.findByIdWithCourse(savedModule.getId())
                .orElseThrow(() -> new EntityNotFoundException("Module not found after creation")); // На всякий случай

        return toDto(savedWithCourse); // <-- Теперь toDto не вызывает LazyInit
    }

    /**
     * Обновляет существующий модуль.
     * Проверяет права текущего пользователя на редактирование модуля.
     *
     * @param currentUserId ID пользователя, инициирующего обновление.
     * @param moduleId      ID модуля для обновления.
     * @param dto           DTO с новыми данными.
     * @return DTO обновленного модуля.
     * @throws EntityNotFoundException если модуль не найден.
     * @throws ForbiddenException      если у пользователя недостаточно прав.
     */
    public ModuleResponseDto updateModule(Long currentUserId, Long moduleId, ModuleUpdateDto dto) {
        // 1. Проверяем права пользователя
        validateUserCanEditModules(currentUserId);

        // 2. Проверяем, что модуль существует и принадлежит курсу
        // Используем метод с JOIN FETCH
        Module module = moduleRepository.findByIdWithCourse(moduleId)
                .orElseThrow(() -> new EntityNotFoundException("Module not found"));

        // 3. (Опционально) можно проверить, что пользователь — автор курса,
        //    но по ТЗ достаточно проверить роль.

        // 4. Обновляем поля
        if (dto.getTitle() != null) module.setTitle(dto.getTitle());
        if (dto.getDescription() != null) module.setDescription(dto.getDescription());

        // 5. Сохраняем
        Module saved = moduleRepository.save(module);

        // Перезапрашиваем обновленный Module с Course, чтобы избежать LazyInit в toDto
        Module updatedWithCourse = moduleRepository.findByIdWithCourse(saved.getId()) // <-- Добавлено
                .orElseThrow(() -> new EntityNotFoundException("Module not found after update")); // На всякий случай

        return toDto(updatedWithCourse); // <-- Теперь toDto не вызывает LazyInit
    }

    /**
     * Удаляет модуль по ID.
     * Проверяет права текущего пользователя на удаление модуля.
     *
     * @param currentUserId ID пользователя, инициирующего удаление.
     * @param moduleId      ID модуля для удаления.
     * @throws EntityNotFoundException если модуль не найден.
     * @throws ForbiddenException      если у пользователя недостаточно прав.
     */
    public void deleteModule(Long currentUserId, Long moduleId) {
        validateUserCanEditModules(currentUserId);
        if (!moduleRepository.existsById(moduleId)) {
            throw new EntityNotFoundException("Module not found");
        }
        moduleRepository.deleteById(moduleId);
    }

    /**
     * Получает модуль по ID.
     * Проверяет, что пользователь существует (аутентификация).
     *
     * @param currentUserId ID пользователя, инициирующего получение.
     * @param moduleId      ID модуля для получения.
     * @return DTO запрашиваемого модуля.
     * @throws EntityNotFoundException если пользователь или модуль не найдены.
     */
    public ModuleResponseDto getModuleById(Long currentUserId, Long moduleId) {
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Используем метод с JOIN FETCH
        Module module = moduleRepository.findByIdWithCourse(moduleId)
                .orElseThrow(() -> new EntityNotFoundException("Module not found"));

        return toDto(module); // <-- Теперь toDto не вызывает LazyInit
    }

    /**
     * Проверяет, имеет ли пользователь право редактировать модули (создание, обновление, удаление).
     * Выбрасывает ForbiddenException, если у пользователя недостаточно прав.
     *
     * @param userId ID пользователя для проверки.
     * @throws EntityNotFoundException если пользователь не найден.
     * @throws ForbiddenException      если доступ запрещен.
     */
    private void validateUserCanEditModules(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (user.getRole() != Role.OWNER &&
                user.getRole() != Role.ADMIN &&
                user.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Only OWNER, ADMIN or TEACHER can edit modules");
        }
    }

    /**
     * Проверяет права пользователя на создание модуля в курсе.
     * Проверяет роль пользователя и существование курса.
     *
     * @param currentUserId ID пользователя, инициирующего создание.
     * @param courseId      ID курса, в который создается модуль.
     * @throws EntityNotFoundException если курс не найден.
     * @throws ForbiddenException      если доступ запрещен.
     */
    private void validateModuleCreationPermissions(Long currentUserId, Long courseId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (currentUser.getRole() != Role.OWNER &&
                currentUser.getRole() != Role.ADMIN &&
                currentUser.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Only OWNER, ADMIN or TEACHER can create modules");
        }

        courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));
    }

    /**
     * Преобразует сущность Module в ModuleResponseDto.
     * Включает ID, заголовок, описание, ID курса и даты создания/обновления.
     *
     * @param module Сущность модуля для преобразования.
     * @return DTO модуля.
     */
    private ModuleResponseDto toDto(Module module) {
        ModuleResponseDto dto = new ModuleResponseDto();
        dto.setId(module.getId());
        dto.setTitle(module.getTitle());
        dto.setDescription(module.getDescription());
        // Убедитесь, что module.getCourse() загружен (JOIN FETCH в репозитории)
        dto.setCourseId(module.getCourse().getId());
        dto.setUpdatedAt(module.getUpdatedAt());
        dto.setCreatedAt(module.getCreatedAt());
        return dto;
    }
}
