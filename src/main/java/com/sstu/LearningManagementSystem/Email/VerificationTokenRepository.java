package com.sstu.LearningManagementSystem.Email;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    // Старый метод
    // Optional<VerificationToken> findByToken(String token);

    // Новый метод с JOIN FETCH для одного токена
    @Query("SELECT vt FROM VerificationToken vt JOIN FETCH vt.user WHERE vt.token = :token")
    Optional<VerificationToken> findByTokenWithUser(@Param("token") String token);

    // Метод для списка (для планировщика)
    @Query("SELECT vt FROM VerificationToken vt JOIN FETCH vt.user WHERE vt.expiryDate < :now")
    List<VerificationToken> findAllByExpiryDateBeforeWithUser(@Param("now") LocalDateTime now);

}