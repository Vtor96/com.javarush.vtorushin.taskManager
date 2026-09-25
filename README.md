# Task Manager - Spring Boot Application

Финальный проект для 5 Модуля «Spring» для курса JuvaRush.
Полноценное Spring-приложение с богатым набором функционала: REST API, безопасность, работа с базой данных, логирование, мониторинг, а также настройка CI/CD для деплоя.

## Что сделано

- **Аутентификация и авторизация**: JWT-токены (jjwt), Spring Security, ролевое разграничение (RBAC) - админ видит все задачи, пользователь - только свои.
- **CRUD задач**: REST-контроллеры, валидация (`@Valid`, Bean Validation), обработка ошибок через `@ControllerAdvice`.
- **Изоляция данных**: на уровне сервиса и запросов - пользователь не может получить/изменить чужие задачи; помеченные на удаление задачи не отображаются в списке, но доступны админу.
- **Миграции БД**: Liquibase - управление схемой БД, воспроизводимость среды.
- **Мониторинг и метрики**: Actuator + Micrometer + Prometheus + кастомные метрики (JVM, HTTP, HikariCP), экспорт для Grafana.
- **Логирование**: структурированные логи, трассировка запросов, уровни логирования под профили.
- **Тестирование**: JUnit 5, Mockito, MockMvc, тесты контроллеров и сервисов; H2 - для интеграционных тестов.
- **CI/CD-готовность**: конфигурация для сборки и базовой проверки в CI.

## Стек

- Java 21
- Spring Boot 3.5.11
- Spring Data JPA + Hibernate 6.6
- PostgreSQL (основная БД), H2 (тесты)
- Liquibase (миграции)
- Spring Security + jjwt (0.12.3) (JWT)
- Spring Boot Actuator + Micrometer + Prometheus
- Grafana (визуализация метрик)
- Lombok (boilerplate)
- Maven (сборка)
- JUnit 5 + Mockito + MockMvc (тестирование)

## Требования

- JDK 21+
- Maven 3.8+
- Docker (опционально для БД и Prometheus/Grafana)
- PostgreSQL 15+ (или Docker-контейнер)
- Свободные порты: 8080 (приложение), 9090 (Prometheus), 3000 (Grafana)

## Как запустить

### Вариант 1: Через Docker Compose (весь стек)

Создайте файл `.env` в корне проекта:

```env
POSTGRES_PASSWORD=your_password
JWT_SECRET=your_secret_key_at_least_64_chars_long_for_hmac_sha256
JWT_EXPIRATION_MS=86400000
```

Запустите все сервисы одной командой:
```docker-compose up -d --build```

Поднимутся:

PostgreSQL на порту 5432 (с миграциями Liquibase при старте приложения)
Task Manager на порту 8080
Prometheus на порту 9090 (сбор метрик с /actuator/prometheus)
Grafana на порту 3000 (логин admin / пароль admin, дашборды автопровижены)

Проверка состояния приложения:
```curl http://localhost:8080/actuator/health```

### Вариант 2: Локально (только приложение)
Запустите PostgreSQL (локально или через Docker):
```docker run -d --name taskmanager-db \
  -e POSTGRES_DB=taskdb \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=your_password \
  -p 5432:5432 \
  postgres:15-alpine
```

Соберите и запустите:
```mvn clean package
java -jar target/task-manager-1.0-SNAPSHOT.jar \
  --spring.datasource.password=your_password \
  --jwt.secret=your_secret_key_at_least_64_chars_long_for_hmac_sha256
```

Или импортируйте проект в IntelliJ IDEA и запустите TaskManagerApplication напрямую.

## Первый запуск: регистрация и логин
### Регистрация
```curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123","email":"test@test.com"}'
```

### Ответ: {"token":"eyJ...","tokenType":"Bearer"}

### Логин
```curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'
```

### Создание задачи
```curl -X POST http://localhost:8080/api/tasks \
  -H "Authorization: Bearer eyJ..." \
  -H "Content-Type: application/json" \
  -d '{"title":"Купить молоко","description":"2 литра","deadline":null}'
```

### Получение всех задач
```curl http://localhost:8080/api/tasks \
  -H "Authorization: Bearer eyJ..."
```

### Фильтр по статусу
```curl "http://localhost:8080/api/tasks?status=TODO" \
  -H "Authorization: Bearer eyJ..."
```

### Фильтр по дедлайну
```curl "http://localhost:8080/api/tasks?deadlineBefore=2026-10-01T00:00:00" \
  -H "Authorization: Bearer eyJ..."
```

### Удаление задачи
```curl -X DELETE http://localhost:8080/api/tasks/1 \
  -H "Authorization: Bearer eyJ..."
```

### Восстановление (только админ)
```curl -X PATCH http://localhost:8080/api/tasks/1/restore \
  -H "Authorization: Bearer eyJ_admin_token..."
```

## Тесты
Интеграционные тесты запускаются на встроенной H2 (профиль test), Liquibase отключён — схема создаётся Hibernate (ddl-auto = create-drop). Базовый класс AbstractIntegrationTest предоставляет хелперы для регистрации, получения токенов и создания задач.
- AuthIntegrationTest - Регистрация (успех, дубликат имени, дубликат email), логин (успех, неверный пароль, несуществующий пользователь).
- TaskCrudIntegrationTest - Создание, получение по id, список задач (пустой и заполненный), полное и частичное обновление, удаление, скрытие удалённой задачи из списка.
- TaskAdminIntegrationTest - Админ видит все задачи, удаляет и восстанавливает чужие задачи, юзер не может восстановить, восстановление несуществующей и уже активной задачи.
- TaskSecurityIntegrationTest - Доступ без токена (403), доступ с невалидным токеном (403), доступ к чужой задаче (403), изоляция данных между пользователями.
- TaskFilterIntegrationTest - Фильтр по статусу (TODO, IN_PROGRESS, DONE), фильтр по дедлайну (до даты, диапазон), пустой результат.
- TaskValidationIntegrationTest - Пустой заголовок, слишком длинный заголовок, слишком длинное описание, пустое имя, короткий пароль, некорректный email, слишком длинное имя.

Запуск:
```mvn test```

## Особенности
- Мягкое удаление: задачи не удаляются физически — флаг deleted = true, что позволяет админу восстанавливать их через PATCH /{id}/restore. Обычный запрос findByIdAndDeletedFalse скрывает удалённые задачи.
- Изоляция данных на уровне сервиса: findTaskAndCheckOwnership проверяет, что пользователь обращается к своей задаче, иначе ResourceAccessDeniedException (403). Админ обходит проверку.
- Частичное обновление: PUT /api/tasks/{id принимает TaskUpdateRequest с nullable-полями — передаются только изменённые поля, null-поля не затрагиваются.
- Метрики Micrometer: счётчики созданных, удалённых, восстановленных задач и неудачных логинов; gauge активных задач. Prometheus собирает метрики с /actuator/prometheus каждые 15 секунд.
- RequestLoggingFilter: логирует метод, URI, статус-код и время выполнения каждого запроса в миллисекундах — работает до UsernamePasswordAuthenticationFilter.
- JWT через jjwt 0.12.3: HMAC-SHA256, токен содержит subject (username), issuedAt, expiration. Срок жизни — 24 часа (настраиваемо через jwt.expiration-ms).
- Профили: application.yml — PostgreSQL для продакшена, application-test.yml — H2 для тестов с ddl-auto = create-drop и отключённым Liquibase.
- Docker multi-stage: сборка через maven:3.9-eclipse-temurin-21, рантайм — eclipse-temurin:21-jre-alpine с healthcheck на /actuator/health.
- Grafana автопровижн: дашборды и datasource (Prometheus) провижнятся автоматически при старте контейнера, без ручной настройки.
