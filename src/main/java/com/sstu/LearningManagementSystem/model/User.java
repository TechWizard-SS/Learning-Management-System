package com.sstu.LearningManagementSystem.model;

import com.sstu.LearningManagementSystem.model.enumType.Role;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "app_user", indexes = @Index(name = "idx_email", columnList = "email"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;
    private String avatarUrl;

    @Column(nullable = false)
    private LocalDateTime registrationDate;

    private Integer ratingPosition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SocialLink> socialLinks = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "user_achievements",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "achievement_id")
    )
    private Set<Achievement> achievements = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private List<CourseRating> courseRatings = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Report> reports = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Enrollment> enrollments = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<AssignmentSubmission> assignmentSubmissions = new ArrayList<>();
}
