package com.sstu.LearningManagementSystem.Email;

import com.sstu.LearningManagementSystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Планировщик для автоматической очистки просроченных токенов.
 * Выполняет очистку раз в час.
 * Удаляет просроченные VerificationToken и связанных с ними пользователей.
 * Удаляет просроченные ResetToken.
 */
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final VerificationTokenRepository vtRepo;
    private final UserRepository userRepo;
    private final ResetTokenRepository resetTokenRepository;

    /**
     * Метод, выполняющий очистку просроченных токенов.
     * Удаляет пользователей, если их токен подтверждения просрочен и не был использован.
     * Удаляет токены сброса пароля, срок действия которых истек.
     */
    @Scheduled(cron = "0 0 * * * ?")  // Hourly
    @Transactional  // Add for atomic deletes
    public void cleanupExpiredTokens() {
        // Verification
        List<VerificationToken> expiredVt = vtRepo.findAllByExpiryDateBeforeWithUser(LocalDateTime.now());
        for (VerificationToken vt : expiredVt) {
            userRepo.delete(vt.getUser());
            vtRepo.delete(vt);
        }

        // Reset (add this)
        List<ResetToken> expiredRt = resetTokenRepository.findAllByExpiryDateBeforeWithUser(LocalDateTime.now());
        for (ResetToken rt : expiredRt) {
            // No delete user for reset, just token
            resetTokenRepository.delete(rt);
        }
    }
}