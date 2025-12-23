package com.sstu.LearningManagementSystem.Email;

import com.sstu.LearningManagementSystem.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Сущность токена сброса пароля.
 * Используется для сброса пароля пользователя.
 */
@Entity
@Table(name = "reset_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String token;  // UUID

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiryDate = LocalDateTime.now();  // now + 1h или 24h, по ТЗ не уточнено, сделай 1h
}