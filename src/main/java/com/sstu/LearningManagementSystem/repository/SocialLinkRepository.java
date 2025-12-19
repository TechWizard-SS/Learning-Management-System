package com.sstu.LearningManagementSystem.repository;

import com.sstu.LearningManagementSystem.model.SocialLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SocialLinkRepository extends JpaRepository<SocialLink, Long> {

    // Найти все ссылки пользователя
    List<SocialLink> findByUserId(Long userId);

    // (Опционально) Найти ссылки пользователя на конкретной платформе
    // List<SocialLink> findByUserIdAndPlatform(Long userId, String platform);
}