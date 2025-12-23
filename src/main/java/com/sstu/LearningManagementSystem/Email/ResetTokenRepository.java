package com.sstu.LearningManagementSystem.Email;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResetTokenRepository extends JpaRepository<ResetToken, Long> {

    // Базовый для verify
    Optional<ResetToken> findByToken(String token);

    // С JOIN FETCH для user (fix lazy in verify/reset)
    @Query("SELECT rt FROM ResetToken rt JOIN FETCH rt.user WHERE rt.token = :token")
    Optional<ResetToken> findByTokenWithUser(@Param("token") String token);

    // Для scheduler cleanup
    @Query("SELECT rt FROM ResetToken rt JOIN FETCH rt.user WHERE rt.expiryDate < :now")
    List<ResetToken> findAllByExpiryDateBeforeWithUser(@Param("now") LocalDateTime now);
}
