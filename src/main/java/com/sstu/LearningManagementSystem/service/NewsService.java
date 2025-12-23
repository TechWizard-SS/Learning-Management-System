package com.sstu.LearningManagementSystem.service;

import com.sstu.LearningManagementSystem.ForbiddenException;
import com.sstu.LearningManagementSystem.model.Course;
import com.sstu.LearningManagementSystem.model.News;
import com.sstu.LearningManagementSystem.model.User;
import com.sstu.LearningManagementSystem.model.dto.newsDto.NewsCreateDto;
import com.sstu.LearningManagementSystem.model.dto.newsDto.NewsResponseDto;
import com.sstu.LearningManagementSystem.model.dto.newsDto.NewsUpdateDto;
import com.sstu.LearningManagementSystem.model.enumType.Role;
import com.sstu.LearningManagementSystem.repository.CourseRepository;
import com.sstu.LearningManagementSystem.repository.NewsRepository;
import com.sstu.LearningManagementSystem.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для управления новостями (News).
 * Обеспечивает создание, обновление, получение и удаление новостей, связанных с курсами.
 * Проверяет права доступа к новостям (только преподаватель/админ/владелец могут редактировать).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsService {

    private final NewsRepository newsRepository;
    private final CourseRepository courseRepository; // Для проверки существования курса
    private final UserRepository userRepository; // Для проверки существования пользователя

    /**
     * Создает новую новость для курса.
     * Требует проверки прав (например, только TEACHER, ADMIN, OWNER).
     *
     * @param currentUserId ID пользователя, инициирующего создание.
     * @param createDto DTO с данными для создания новости.
     * @return DTO созданной новости.
     * @throws EntityNotFoundException если курс не найден.
     * @throws ForbiddenException      если у пользователя недостаточно прав.
     */
    @Transactional
    public NewsResponseDto createNews(Long currentUserId, NewsCreateDto createDto) {
        validateUserCanEditNews(currentUserId); // Проверка прав

        Course course = courseRepository.findById(createDto.getCourseId())
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + createDto.getCourseId()));

        News news = News.builder()
                .title(createDto.getTitle())
                .text(createDto.getText())
                .tags(createDto.getTags() != null ? createDto.getTags() : new ArrayList<>()) // Обработка null
                .course(course)
                .rating(0.0) // Начальный рейтинг
                .build();

        News savedNews = newsRepository.save(news);

        // Перезапрашиваем News с Course, чтобы избежать LazyInit в toDto
        News savedWithCourse = newsRepository.findByIdWithCourse(savedNews.getId())
                .orElseThrow(() -> new EntityNotFoundException("News not found after creation")); // На всякий случай

        return toDto(savedWithCourse); // <-- Теперь toDto не вызывает LazyInit
    }

    /**
     * Обновляет существующую новость.
     * Требует проверки прав (например, только TEACHER, ADMIN, OWNER).
     *
     * @param currentUserId ID пользователя, инициирующего обновление.
     * @param newsId        ID новости для обновления.
     * @param updateDto     DTO с новыми данными.
     * @return DTO обновленной новости.
     * @throws EntityNotFoundException если новость не найдена.
     * @throws ForbiddenException      если у пользователя недостаточно прав.
     */
    @Transactional
    public NewsResponseDto updateNews(Long currentUserId, Long newsId, NewsUpdateDto updateDto) {
        validateUserCanEditNews(currentUserId); // Проверка прав

        News news = newsRepository.findByIdWithCourse(newsId) // <-- Изменено: используем JOIN FETCH
                .orElseThrow(() -> new EntityNotFoundException("News not found with id: " + newsId));

        // Частичное обновление: обновляем только ненулевые поля из DTO
        if (updateDto.getTitle() != null) news.setTitle(updateDto.getTitle());
        if (updateDto.getText() != null) news.setText(updateDto.getText());
        if (updateDto.getTags() != null) news.setTags(updateDto.getTags()); // Полное обновление тегов

        News updatedNews = newsRepository.save(news);

        // Перезапрашиваем обновленный News с Course, чтобы избежать LazyInit в toDto
        News updatedWithCourse = newsRepository.findByIdWithCourse(updatedNews.getId()) // <-- Добавлено
                .orElseThrow(() -> new EntityNotFoundException("News not found after update")); // На всякий случай

        return toDto(updatedWithCourse); // <-- Теперь toDto не вызывает LazyInit
    }

    /**
     * Удаляет новость по ID.
     * Требует проверки прав (например, только TEACHER, ADMIN, OWNER).
     *
     * @param currentUserId ID пользователя, инициирующего удаление.
     * @param newsId        ID новости для удаления.
     * @throws EntityNotFoundException если новость не найдена.
     * @throws ForbiddenException      если у пользователя недостаточно прав.
     */
    @Transactional
    public void deleteNews(Long currentUserId, Long newsId) {
        validateUserCanEditNews(currentUserId); // Проверка прав

        if (!newsRepository.existsById(newsId)) {
            throw new EntityNotFoundException("News not found with id: " + newsId);
        }
        // Если используется мягкое удаление (soft-delete) через Auditable,
        // вместо repository.delete() установите deletedAt и сохраните.
        // Иначе, выполняем жесткое удаление:
        newsRepository.deleteById(newsId);
    }

    /**
     * Получает новость по ID.
     * Проверяет, что пользователь существует (аутентифицирован).
     *
     * @param currentUserId ID пользователя, инициирующего получение.
     * @param newsId        ID новости для получения.
     * @return DTO запрашиваемой новости.
     * @throws EntityNotFoundException если пользователь или новость не найдены.
     */
    public NewsResponseDto getNewsById(Long currentUserId, Long newsId) {
        // Проверяем, что пользователь существует (аутентификация)
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Используем метод с JOIN FETCH
        News news = newsRepository.findByIdWithCourse(newsId)
                .orElseThrow(() -> new EntityNotFoundException("News not found with id: " + newsId));

        return toDto(news); // <-- Теперь toDto не вызывает LazyInit
    }

    /**
     * Находит все новости, связанные с определенным курсом.
     *
     * @param currentUserId ID пользователя, инициирующего получение.
     * @param courseId      ID курса.
     * @return Список DTO новостей.
     * @throws EntityNotFoundException если пользователь или курс не найдены.
     */
    public List<NewsResponseDto> getNewsByCourseId(Long currentUserId, Long courseId) {
        // Проверяем, что пользователь существует (аутентификация)
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Проверяем, что курс существует
        courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));

        // Используем метод с JOIN FETCH
        List<News> newsList = newsRepository.findByCourseIdWithCourse(courseId);
        return newsList.stream()
                .map(this::toDto) // <-- Теперь toDto не вызывает LazyInit для каждого элемента
                .collect(Collectors.toList());
    }

    // --- Вспомогательные методы ---

    /**
     * Преобразует сущность News в NewsResponseDto.
     * Включает ID, заголовок, текст, рейтинг, теги, ID курса и даты создания/обновления.
     *
     * @param news Сущность новости для преобразования.
     * @return DTO новости.
     */
    private NewsResponseDto toDto(News news) {
        NewsResponseDto dto = new NewsResponseDto();
        dto.setId(news.getId());
        dto.setTitle(news.getTitle());
        dto.setText(news.getText());
        dto.setRating(news.getRating());
        dto.setTags(news.getTags());
        // Убедитесь, что news.getCourse() загружен (JOIN FETCH в репозитории)
        dto.setCourseId(news.getCourse().getId());
        dto.setCreatedAt(news.getCreatedAt());
        dto.setUpdatedAt(news.getUpdatedAt());
        return dto;
    }

    /**
     * Проверяет, имеет ли пользователь право редактировать новости (создание, обновление, удаление).
     * Выбрасывает ForbiddenException, если у пользователя недостаточно прав.
     *
     * @param userId ID пользователя для проверки.
     * @throws EntityNotFoundException если пользователь не найден.
     * @throws ForbiddenException      если доступ запрещен.
     */
    private void validateUserCanEditNews(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (user.getRole() != Role.OWNER &&
                user.getRole() != Role.ADMIN &&
                user.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Only OWNER, ADMIN, or TEACHER can edit news");
        }
    }
}