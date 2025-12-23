package com.sstu.LearningManagementSystem.ATest;

import com.sstu.LearningManagementSystem.model.*;
import com.sstu.LearningManagementSystem.model.Module;
import com.sstu.LearningManagementSystem.model.enumType.AssignmentType;
import com.sstu.LearningManagementSystem.model.enumType.ContentType;
import com.sstu.LearningManagementSystem.model.enumType.Role;
import com.sstu.LearningManagementSystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//@Component
//@RequiredArgsConstructor
//public class DataGenerator implements CommandLineRunner {
//
//    private final UserRepository userRepository;
//    private final CourseRepository courseRepository;
//    private final ModuleRepository moduleRepository;
//    private final TopicRepository topicRepository;
//    private final AssignmentRepository assignmentRepository;
//    private final EnrollmentRepository enrollmentRepository;
//    private final AchievementRepository achievementRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    @Override
//    @Transactional
//    public void run(String... args) throws Exception {
//        if (userRepository.count() > 0) {
//            System.out.println("Data already exists, skipping generation.");
//            return;
//        }
//
//        System.out.println("Starting to generate test data...");
//
//        long startTime, endTime;
//
//        // --- 1. Сначала генерируем пользователей ---
//        startTime = System.currentTimeMillis();
//        List<User> users = new ArrayList<>(10000);
//        String hashedPassword = passwordEncoder.encode("defaultPassword123");
//
//        for (int i = 1; i <= 10000; i++) {
//            User user = User.builder()
//                    .username("generated_user_" + i)
//                    .password(hashedPassword)
//                    .firstName("FirstName" + i)
//                    .lastName("LastName" + i)
//                    .email("generated_user_" + i + "@example.com")
//                    .phone("+799912345" + String.format("%02d", i % 100))
//                    .avatarUrl("http://example.com/avatar" + i + ".jpg")
//                    .registrationDate(LocalDateTime.now())
//                    .role(Role.STUDENT) // Предполагается, что enum Role существует
//                    .ratingPosition(0)
//                    .verified(true) // <-- Устанавливаем как true
//                    .build();
//            users.add(user);
//        }
//        userRepository.saveAll(users);
//
//        // генерируем преподов
//
//        startTime = System.currentTimeMillis();
//        List<User> teachers = new ArrayList<>(10);
//        String hashedPassword1 = passwordEncoder.encode("defaultPassword1234");
//
//        for (int i = 1; i <= 10; i++) {
//            User user = User.builder()
//                    .username("generated_teacher_" + i)
//                    .password(hashedPassword1)
//                    .firstName("FirstName1_" + i)
//                    .lastName("LastName1_" + i)
//                    .email("generated_teacher_" + i + "@example.com")
//                    .phone("+799912345" + String.format("%02d", i % 100))
//                    .avatarUrl("http://example.com/avatar" + i + ".jpg")
//                    .registrationDate(LocalDateTime.now())
//                    .role(Role.TEACHER) // Предполагается, что enum Role существует
//                    .ratingPosition(0)
//                    .verified(true) // <-- Устанавливаем как true
//                    .build();
//            users.add(user);
//        }
//        userRepository.saveAll(users);
//
//
//        endTime = System.currentTimeMillis();
//        System.out.println("Successfully generated and saved 10,000 users in " + (endTime - startTime) + " ms.");
//
//        // --- 2. Создаем курсы ---
//        startTime = System.currentTimeMillis();
//        List<Course> courses = new ArrayList<>();
//        String[] courseTitles = {
//                "Введение в Java", "Продвинутая Java", "Spring Framework", "Spring Boot", "Hibernate",
//                "Микросервисы", "Docker & Kubernetes", "Git", "Алгоритмы и Структуры Данных", "Системное Дизайн",
//                "Курс 11", "Курс 12", "Курс 13", "Курс 14", "Курс 15",
//                "Курс 16", "Курс 17", "Курс 18", "Курс 19", "Курс 20",
//                "Курс 21", "Курс 22", "Курс 23", "Курс 24", "Курс 25",
//                "Курс 26", "Курс 27", "Курс 28", "Курс 29", "Курс 30",
//                "Курс 31", "Курс 32", "Курс 33", "Курс 34", "Курс 35",
//                "Курс 36", "Курс 37", "Курс 38", "Курс 39", "Курс 40",
//                "Курс 41", "Курс 42", "Курс 43", "Курс 44", "Курс 45",
//                "Курс 46", "Курс 47", "Курс 48", "Курс 49", "Курс 50"
//        };
//
//        for (int i = 0; i < courseTitles.length; i++) {
//            Course course = Course.builder()
//                    .name(courseTitles[i]) // <-- Поле name, а не title
//                    .description("Описание курса " + courseTitles[i])
//                    .expectedDuration(40 + (i % 10)) // Пример: от 40 до 49 часов
//                    .rating(0.0) // Начальный рейтинг
//                    .category(null) // Оставляем null или создайте хотя бы одну категорию
//                    .tags(List.of("tag1", "tag2", "tag" + (i+1))) // Пример заполнения тегов
//                    .build();
//            courses.add(course);
//        }
//        courseRepository.saveAll(courses);
//        endTime = System.currentTimeMillis();
//        System.out.println("Successfully generated and saved " + courses.size() + " courses in " + (endTime - startTime) + " ms.");
//
//        // --- 3. Создаем модули для курсов ---
//        startTime = System.currentTimeMillis();
//        List<Module> modules = new ArrayList<>();
//        for (Course course : courses) {
//            for (int j = 1; j <= 5; j++) { // 5 модулей на курс
//                Module module = Module.builder()
//                        .title("Модуль " + j + " - " + course.getName()) // <-- Поле title
//                        .description("Описание модуля " + j + " для " + course.getName())
//                        .course(course) // <-- Устанавливаем связь с курсом
//                        .build();
//                modules.add(module);
//            }
//        }
//        moduleRepository.saveAll(modules);
//        endTime = System.currentTimeMillis();
//        System.out.println("Successfully generated and saved " + modules.size() + " modules in " + (endTime - startTime) + " ms.");
//
//        // --- 4. Создаем темы для модулей ---
//        startTime = System.currentTimeMillis();
//        List<Topic> topics = new ArrayList<>();
//        for (Module module : modules) {
//            for (int k = 1; k <= 10; k++) { // 10 тем на модуль
//                Topic topic = Topic.builder()
//                        .title("Тема " + k + " - " + module.getTitle())
//                        .description("Описание темы " + k + " для " + module.getTitle())
//                        .content("Содержимое темы " + k + "...")
//                        .contentType(ContentType.TEXT) // Предполагается, что enum ContentType существует
//                        .module(module) // <-- Устанавливаем связь с модулем
//                        .build();
//                topics.add(topic);
//            }
//        }
//        topicRepository.saveAll(topics);
//        endTime = System.currentTimeMillis();
//        System.out.println("Successfully generated and saved " + topics.size() + " topics in " + (endTime - startTime) + " ms.");
//
//        // --- 5. Создаем задания для тем ---
//        startTime = System.currentTimeMillis();
//        List<Assignment> assignments = new ArrayList<>();
//        for (Topic topic : topics) {
//            Assignment assignment = Assignment.builder()
//                    .title("Задание для " + topic.getTitle())
//                    .description("Описание задания для " + topic.getTitle())
//                    .content("Текст задания...")
//                    .type(AssignmentType.OPEN_ANSWER) // Предполагается, что enum AssignmentType существует
//                    .contentType(ContentType.TEXT)
//                    .topic(topic) // <-- Устанавливаем связь с темой
//                    .build();
//            assignments.add(assignment);
//        }
//        assignmentRepository.saveAll(assignments);
//        endTime = System.currentTimeMillis();
//        System.out.println("Successfully generated and saved " + assignments.size() + " assignments in " + (endTime - startTime) + " ms.");
//
//        // --- 6. Создаем зачисления (например, первые 200 пользователей на каждый курс) ---
//        startTime = System.currentTimeMillis();
//        List<Enrollment> enrollments = new ArrayList<>();
//        for (Course course : courses) {
//            for (int l = 0; l < 200 && l < users.size(); l++) { // 200 пользователей на курс
//                User user = users.get(l);
//                Enrollment enrollment = Enrollment.builder()
//                        .user(user) // <-- Устанавливаем пользователя
//                        .course(course) // <-- Устанавливаем курс
//                        .enrollmentDate(LocalDateTime.now())
//                        .confirmed(true) // Предположим, подтверждены
//                        .progress(0) // Начальный прогресс
//                        .build();
//                enrollments.add(enrollment);
//            }
//        }
//        enrollmentRepository.saveAll(enrollments);
//        endTime = System.currentTimeMillis();
//        System.out.println("Successfully generated and saved " + enrollments.size() + " enrollments in " + (endTime - startTime) + " ms.");
//
//        // --- 7. (Опционально) Создаем достижения ---
//        // startTime = System.currentTimeMillis();
//        // List<Achievement> achievements = new ArrayList<>();
//        // for (int i = 1; i <= 10; i++) {
//        //     Achievement achievement = Achievement.builder()
//        //             .title("Достижение " + i)
//        //             .description("Описание достижения " + i)
//        //             .iconUrl("http://example.com/achievement" + i + ".png")
//        //             .build();
//        //     achievements.add(achievement);
//        // }
//        // achievementRepository.saveAll(achievements);
//        // endTime = System.currentTimeMillis();
//        // System.out.println("Successfully generated and saved " + achievements.size() + " achievements in " + (endTime - startTime) + " ms.");
//
//        System.out.println("Data generation completed.");
//    }
//}