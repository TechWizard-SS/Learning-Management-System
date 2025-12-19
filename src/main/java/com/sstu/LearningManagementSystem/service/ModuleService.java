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


@Service
@RequiredArgsConstructor
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public ModuleResponseDto createModule(Long currentUserId, ModuleCreateDto dto) {
        validateModuleCreationPermissions(currentUserId, dto.getCourseId());
        Course course = courseRepository.findById(dto.getCourseId()).get();
        Module module = Module.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .course(course)
                .build();

        Module savedModule = moduleRepository.save(module);

        return toDto(savedModule);
    }



    public ModuleResponseDto updateModule(Long currentUserId, Long moduleId, ModuleUpdateDto dto) {
        // 1. Проверяем права пользователя
        validateUserCanEditModules(currentUserId);

        // 2. Проверяем, что модуль существует и принадлежит курсу
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new EntityNotFoundException("Module not found"));

        // 3. (Опционально) можно проверить, что пользователь — автор курса,
        //    но по ТЗ достаточно проверить роль.

        // 4. Обновляем поля
        if (dto.getTitle() != null) module.setTitle(dto.getTitle());
        if (dto.getDescription() != null) module.setDescription(dto.getDescription());

        // 5. Сохраняем
        Module saved = moduleRepository.save(module);
        return toDto(saved);
    }

    public void deleteModule(Long currentUserId, Long moduleId) {
        validateUserCanEditModules(currentUserId);
        if (!moduleRepository.existsById(moduleId)) {
            throw new EntityNotFoundException("Module not found");
        }
        moduleRepository.deleteById(moduleId);
    }


    public ModuleResponseDto getModuleById(Long currentUserId, Long moduleId) {
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new EntityNotFoundException("Module not found"));

        return toDto(module);
    }



    private void validateUserCanEditModules(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (user.getRole() != Role.OWNER &&
                user.getRole() != Role.ADMIN &&
                user.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Only OWNER, ADMIN or TEACHER can edit modules");
        }
    }

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

    private ModuleResponseDto toDto(Module module) {
        ModuleResponseDto dto = new ModuleResponseDto();
        dto.setId(module.getId());
        dto.setTitle(module.getTitle());
        dto.setDescription(module.getDescription());
        dto.setCourseId(module.getCourse().getId());
        return dto;
    }
}
