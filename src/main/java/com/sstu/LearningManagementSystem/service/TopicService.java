package com.sstu.LearningManagementSystem.service;

import com.sstu.LearningManagementSystem.ForbiddenException;
import com.sstu.LearningManagementSystem.model.Module;
import com.sstu.LearningManagementSystem.model.Topic;
import com.sstu.LearningManagementSystem.model.User;
import com.sstu.LearningManagementSystem.model.dto.topicDto.TopicCreateDto;
import com.sstu.LearningManagementSystem.model.dto.topicDto.TopicResponseDto;
import com.sstu.LearningManagementSystem.model.dto.topicDto.TopicUpdateDto;
import com.sstu.LearningManagementSystem.model.enumType.Role;
import com.sstu.LearningManagementSystem.repository.ModuleRepository;
import com.sstu.LearningManagementSystem.repository.TopicRepository;
import com.sstu.LearningManagementSystem.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;

    public TopicResponseDto createTopic(Long currentUserId, TopicCreateDto dto) {
        validateUserCanEditTopics(currentUserId);

        Module module = moduleRepository.findById(dto.getModuleId())
                .orElseThrow(() -> new EntityNotFoundException("Module not found"));

        Topic topic = Topic.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .contentType(dto.getContentType())
                .content(dto.getContent())
                .module(module)
                .build();

        Topic saved = topicRepository.save(topic);
        return toDto(saved);
    }

    public TopicResponseDto updateTopic(Long currentUserId, Long topicId, TopicUpdateDto dto) {
        validateUserCanEditTopics(currentUserId);

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new EntityNotFoundException("Topic not found"));

        if (dto.getTitle() != null) topic.setTitle(dto.getTitle());
        if (dto.getDescription() != null) topic.setDescription(dto.getDescription());
        if (dto.getContent() != null) topic.setContent(dto.getContent());
        if (dto.getContentType() != null) topic.setContentType(dto.getContentType());

        Topic updated = topicRepository.save(topic);
        return toDto(updated);
    }

    public TopicResponseDto getTopicById(Long currentUserId, Long topicId) {
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new EntityNotFoundException("Topic not found"));

        return toDto(topic);
    }

    public void deleteTopic(Long currentUserId, Long topicId) {
        validateUserCanEditTopics(currentUserId);
        if (!topicRepository.existsById(topicId)) {
            throw new EntityNotFoundException("Topic not found");
        }
        topicRepository.deleteById(topicId);
    }

    private TopicResponseDto toDto(Topic topic) {
        TopicResponseDto dto = new TopicResponseDto();
        dto.setId(topic.getId());
        dto.setTitle(topic.getTitle());
        dto.setDescription(topic.getDescription());
        dto.setContent(topic.getContent());
        dto.setContentType(topic.getContentType());
        dto.setModuleId(topic.getModule().getId());
        return dto;
    }

    private void validateUserCanEditTopics(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (user.getRole() != Role.OWNER &&
                user.getRole() != Role.ADMIN &&
                user.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Only OWNER, ADMIN or TEACHER can edit topics");
        }
    }
}
