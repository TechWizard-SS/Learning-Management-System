[![Quality gate status](https://sonarcloud.io/api/project_badges/measure?project=TechWizard-SS_hexlet-jdbc&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=TechWizard-SS_hexlet-jdbc)

[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=TechWizard-SS_hexlet-jdbc&metric=coverage)](https://sonarcloud.io/summary/new_code?id=TechWizard-SS_hexlet-jdbc)

[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=TechWizard-SS_hexlet-jdbc&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=TechWizard-SS_hexlet-jdbc)

[![Lines of code](https://sonarcloud.io/api/project_badges/measure?project=TechWizard-SS_hexlet-jdbc&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=TechWizard-SS_hexlet-jdbc)

[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=TechWizard-SS_hexlet-jdbc&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=TechWizard-SS_hexlet-jdbc)

[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=TechWizard-SS_hexlet-jdbc&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=TechWizard-SS_hexlet-jdbc)

[![Maintainability issues](https://sonarcloud.io/api/project_badges/measure?project=TechWizard-SS_hexlet-jdbc&metric=software_quality_maintainability_issues)](https://sonarcloud.io/summary/new_code?id=TechWizard-SS_hexlet-jdbc)

[![Reliability issues](https://sonarcloud.io/api/project_badges/measure?project=TechWizard-SS_hexlet-jdbc&metric=software_quality_reliability_issues)](https://sonarcloud.io/summary/new_code?id=TechWizard-SS_hexlet-jdbc)

[![Security issues](https://sonarcloud.io/api/project_badges/measure?project=TechWizard-SS_hexlet-jdbc&metric=software_quality_security_issues)](https://sonarcloud.io/summary/new_code?id=TechWizard-SS_hexlet-jdbc)


# Learning Management System (LMS)

> **Learning Management System** — это монолитное приложение на базе Spring Boot, предназначенный для управления образовательным процессом. Проект разработан как учебная база знаний с акцентом на масштабируемость и современные методы бэкенд-разработки.

---

## 🏗️ Архитектурные особенности

* **Модульный монолит:** Четкое разделение ответственности (Controller-Service-Repository), упрощающее поддержку проекта.
* **Безопасность:** Реализована на Spring Security с JWT-аутентификацией. Доступ к защищенным ресурсам невозможен без валидного токена, а проверка статуса верификации пользователя происходит на этапе загрузки `UserDetails`.
* **Производительность:** Интеграция **Redis** (через Lettuce) для кэширования данных API, что существенно снижает нагрузку на основную базу данных.
* **Автоматизация:** Внедрен `TokenCleanupScheduler`, который в фоновом режиме (раз в час) очищает БД от просроченных токенов верификации и сброса пароля, предотвращая накопление «мусорных» данных.
* **Email-интеграция:** Поддержка SMTP (Mailtrap) для безопасной отправки писем подтверждения регистрации и восстановления доступа.

---

## 🛠️ Технологический стек

* **Язык:** Java 21
* **Фреймворк:** Spring Boot 3.5.7 (Gradle Kotlin DSL)
* **База данных:** PostgreSQL 17
* **Кэширование:** Redis 3.8
* **Безопасность:** Spring Security + JWT
* **ORM:** Spring Data JPA + Hibernate

---

## 🚀 Быстрый старт

### 1. Запуск инфраструктуры

Для работы приложения необходимы Redis. Используйте предоставленный `docker-compose.yml`:

```bash
docker-compose up -d

```

### 2. Конфигурация

Создайте `application.yml` на основе `application.yml.example` и укажите данные для подключения:

* `spring.datasource` — параметры вашей БД.
* `spring.mail` — данные для SMTP-сервера (Mailtrap).
* `spring.security.jwt.secret` — секретный ключ для подписи токенов.

### 3. Запуск приложения

```bash
./gradlew bootRun

```

---

## 📅 Roadmap (Планы развития)

* [ ] Покрытие кода Unit-тестами (JUnit 5 + Mockito).
* [ ] Интеграция Testcontainers для надежного тестирования репозиториев.
* [ ] Подключение OpenAPI (Swagger) для автоматизации документации API.
* [ ] Оптимизация получения `currentUserId` через `@ControllerAdvice` или `HandlerMethodArgumentResolver` для удаления дублирования кода.

---
