package com.sstu.LearningManagementSystem.service;

import com.sstu.LearningManagementSystem.ForbiddenException;
import com.sstu.LearningManagementSystem.model.SocialLink;
import com.sstu.LearningManagementSystem.model.User;
import com.sstu.LearningManagementSystem.model.dto.socialLinkDto.SocialLinkCreateDto;
import com.sstu.LearningManagementSystem.model.dto.socialLinkDto.SocialLinkResponseDto;
import com.sstu.LearningManagementSystem.model.dto.socialLinkDto.SocialLinkUpdateDto;
import com.sstu.LearningManagementSystem.model.enumType.Role;
import com.sstu.LearningManagementSystem.repository.SocialLinkRepository;
import com.sstu.LearningManagementSystem.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SocialLinkService {

    private final SocialLinkRepository socialLinkRepository;
    private final UserRepository userRepository; // Для проверки существования пользователя

    /**
     * Создает новую социальную ссылку для текущего пользователя.
     *
     * @param currentUserId ID пользователя, инициирующего создание.
     * @param createDto DTO с данными для создания ссылки.
     * @return DTO созданной ссылки.
     */
    @Transactional
    public SocialLinkResponseDto createSocialLink(Long currentUserId, SocialLinkCreateDto createDto) {
        // Проверить, что пользователь, создающий ссылку, является владельцем
        validateAccessToSocialLink(currentUserId, currentUserId); // requestUserId == ownerUserId

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + currentUserId));

        SocialLink socialLink = SocialLink.builder()
                .platform(createDto.getPlatform())
                .url(createDto.getUrl())
                .user(user)
                .build();

        SocialLink savedSocialLink = socialLinkRepository.save(socialLink);
        return toDto(savedSocialLink);
    }

    /**
     * Обновляет существующую социальную ссылку.
     * Требует проверки прав: только владелец ссылки может обновить.
     *
     * @param currentUserId ID пользователя, инициирующего обновление.
     * @param linkId        ID ссылки для обновления.
     * @param updateDto     DTO с новыми данными.
     * @return DTO обновленной ссылки.
     */
    @Transactional
    public SocialLinkResponseDto updateSocialLink(Long currentUserId, Long linkId, SocialLinkUpdateDto updateDto) {
        SocialLink socialLink = socialLinkRepository.findById(linkId)
                .orElseThrow(() -> new EntityNotFoundException("SocialLink not found with id: " + linkId));

        // Проверить, что текущий пользователь является владельцем ссылки
        validateAccessToSocialLink(currentUserId, socialLink.getUser().getId());

        // Частичное обновление: обновляем только ненулевые поля из DTO
        if (updateDto.getPlatform() != null) socialLink.setPlatform(updateDto.getPlatform());
        if (updateDto.getUrl() != null) socialLink.setUrl(updateDto.getUrl());

        SocialLink updatedSocialLink = socialLinkRepository.save(socialLink);
        return toDto(updatedSocialLink);
    }

    /**
     * Удаляет социальную ссылку по ID.
     * Требует проверки прав: только владелец ссылки может удалить.
     *
     * @param currentUserId ID пользователя, инициирующего удаление.
     * @param linkId        ID ссылки для удаления.
     */
    @Transactional
    public void deleteSocialLink(Long currentUserId, Long linkId) {
        SocialLink socialLink = socialLinkRepository.findById(linkId)
                .orElseThrow(() -> new EntityNotFoundException("SocialLink not found with id: " + linkId));

        // Проверить, что текущий пользователь является владельцем ссылки
        validateAccessToSocialLink(currentUserId, socialLink.getUser().getId());

        // Если используется мягкое удаление (soft-delete) через Auditable,
        // вместо repository.delete() установите deletedAt и сохраните.
        // Иначе, выполняем жесткое удаление:
        socialLinkRepository.deleteById(linkId);
    }

    /**
     * Получает социальную ссылку по ID.
     * Требует проверки прав: только владелец ссылки или преподаватель/админ могут получить.
     *
     * @param currentUserId ID пользователя, инициирующего получение.
     * @param linkId        ID ссылки для получения.
     * @return DTO запрашиваемой ссылки.
     */
    public SocialLinkResponseDto getSocialLinkById(Long currentUserId, Long linkId) {
        SocialLink socialLink = socialLinkRepository.findById(linkId)
                .orElseThrow(() -> new EntityNotFoundException("SocialLink not found with id: " + linkId));

        // Проверить права доступа: текущий пользователь == владелец ссылки ИЛИ преподаватель/админ
        validateAccessToSocialLink(currentUserId, socialLink.getUser().getId());

        return toDto(socialLink);
    }

    /**
     * Получает все социальные ссылки пользователя.
     * Требует проверки прав: только владелец списка или преподаватель/админ могут получить.
     *
     * @param currentUserId ID пользователя, инициирующего получение.
     * @param targetUserId  ID пользователя, чьи ссылки запрашиваются.
     * @return Список DTO ссылок.
     */
    public List<SocialLinkResponseDto> getSocialLinksByUserId(Long currentUserId, Long targetUserId) {
        // Проверить права доступа: текущий пользователь == владелец списка ИЛИ преподаватель/админ
        validateAccessToList(currentUserId, targetUserId);

        List<SocialLink> socialLinks = socialLinkRepository.findByUserId(targetUserId);
        return socialLinks.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // --- Вспомогательные методы ---

    /**
     * Преобразует сущность SocialLink в SocialLinkResponseDto.
     *
     * @param socialLink Сущность социальной ссылки.
     * @return DTO социальной ссылки.
     */
    private SocialLinkResponseDto toDto(SocialLink socialLink) {
        SocialLinkResponseDto dto = new SocialLinkResponseDto();
        dto.setId(socialLink.getId());
        dto.setPlatform(socialLink.getPlatform());
        dto.setUrl(socialLink.getUrl());
        dto.setUserId(socialLink.getUser().getId());
        return dto;
    }

    /**
     * Проверяет, имеет ли пользователь право просматривать конкретную социальную ссылку.
     * Вызывает ForbiddenException, если прав недостаточно.
     *
     * @param requestUserId ID пользователя, делающего запрос.
     * @param ownerUserId   ID владельца ссылки.
     */
    private void validateAccessToSocialLink(Long requestUserId, Long ownerUserId) {
        User requestingUser = userRepository.findById(requestUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Пользователь может просматривать свои данные
        if (requestUserId.equals(ownerUserId)) {
            return;
        }

        // Или пользователь должен быть преподавателем/админом/владельцем
        if (requestingUser.getRole() != Role.OWNER && // Убедитесь, что Role существует
                requestingUser.getRole() != Role.ADMIN &&
                requestingUser.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Access denied: Cannot view another user's social link.");
        }
    }

    /**
     * Проверяет, имеет ли пользователь право просматривать список социальных ссылок.
     * Вызывает ForbiddenException, если прав недостаточно.
     *
     * @param requestUserId ID пользователя, делающего запрос.
     * @param targetUserId  ID пользователя, чей список запрашивается.
     */
    private void validateAccessToList(Long requestUserId, Long targetUserId) {
        User requestingUser = userRepository.findById(requestUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Пользователь может просматривать свой список
        if (requestUserId.equals(targetUserId)) {
            return;
        }

        // Или пользователь должен быть преподавателем/админом/владельцем
        if (requestingUser.getRole() != Role.OWNER && // Убедитесь, что Role существует
                requestingUser.getRole() != Role.ADMIN &&
                requestingUser.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Access denied: Cannot view another user's social links list.");
        }
    }
}
