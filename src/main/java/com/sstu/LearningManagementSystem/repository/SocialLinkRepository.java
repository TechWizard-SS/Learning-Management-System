package com.sstu.LearningManagementSystem.repository;

import com.sstu.LearningManagementSystem.model.SocialLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocialLinkRepository extends JpaRepository<SocialLink, Long> {

    List<SocialLink> findByUserId(Long userId);

    @Query("SELECT sl FROM SocialLink sl JOIN FETCH sl.user WHERE sl.id = :id")
    Optional<SocialLink> findByIdWithUser(@Param("id") Long id);

    @Query("SELECT sl FROM SocialLink sl JOIN FETCH sl.user WHERE sl.user.id = :userId")
    List<SocialLink> findByUserIdWithUser(@Param("userId") Long userId);
}