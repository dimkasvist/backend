# Backend Deployment Guide

Этот репозиторий содержит бэкенд приложения на Spring Boot 4.0 с Java 25.

## 📋 Предварительные требования

### GitHub Secrets

В настройках репозитория (Settings → Secrets and variables → Actions) добавьте:

- **SSH_HOST** - IP адрес или домен вашего Ubuntu сервера
- **SSH_USERNAME** - имя пользователя на сервере (обычно root или ubuntu)
- **SSH_PRIVATE_KEY** - приватный SSH ключ для доступа к серверу
- **SSH_PORT** - порт SSH (по умолчанию 22, опционально)

## 🚀 Автоматический деплой (CI/CD)

Создан GitHub Actions workflow в `.github/workflows/deploy.yml`.

### Когда запускается:
- Автоматически при push в ветки `main` или `master`
- Вручную через GitHub Actions → Deploy Backend → Run workflow

### Процесс деплоя:
1. ✅ Собирает Docker образ бэкенда (Gradle + Java 25)
2. ✅ Публикует образ в GitHub Container Registry (ghcr.io)
3. ✅ Подключается к серверу по SSH
4. ✅ Скачивает новый образ
5. ✅ Перезапускает контейнер бэкенда
6. ✅ Очищает старые образы

## 🛠️ Локальная разработка

```bash
# Запуск приложения
./gradlew bootRun

# Сборка
./gradlew build

# Запуск тестов
./gradlew test

# Сборка без тестов
./gradlew build -x test
```

## 🐳 Docker сборка локально

```bash
# Сборка образа
docker build -t dimkasvist-backend .

# Запуск контейнера (требуется PostgreSQL и MinIO)
docker run -p 8080:8080 \
  -e POSTGRES_HOST=host.docker.internal \
  -e POSTGRES_PASSWORD=password \
  dimkasvist-backend
```

## 📦 Структура проекта

```
backend/
├── src/
│   ├── main/
│   │   ├── java/           # Java исходники
│   │   └── resources/      # Конфигурация
│   └── test/              # Тесты
├── gradle/                # Gradle wrapper
├── build.gradle.kts       # Gradle конфигурация
├── Dockerfile            # Docker конфигурация
├── .dockerignore         # Исключения для Docker
└── .github/workflows/    # CI/CD workflows
```

## 🔧 Переменные окружения

### Для разработки (.env)
```env
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=dimkasvist
POSTGRES_USER=dimkasvist
POSTGRES_PASSWORD=password

S3_ENDPOINT=http://localhost:9000
S3_BUCKET=dimkasvist
S3_ACCESS_KEY=minioadmin
S3_SECRET_KEY=minioadmin

GOOGLE_CLIENT_ID=your-client-id
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

### Для production (на сервере)
Все переменные передаются через docker-compose на сервере.

## 🗄️ База данных

Проект использует:
- **PostgreSQL 16** - основная БД
- **Liquibase** - миграции БД
- **JPA/Hibernate** - ORM

Миграции применяются автоматически при старте приложения.

## 📁 Хранилище файлов

Проект использует **MinIO** (S3-compatible storage) для хранения:
- Фотографий пользователей
- Аватаров
- Других медиа файлов

## 🔐 Аутентификация

- Google OAuth 2.0
- JWT токены
- Spring Security

## 📡 API Endpoints

- `/api/**` - REST API endpoints
- `/actuator/health` - Health check endpoint

## 🔍 Мониторинг на сервере

```bash
# Статус контейнера
docker compose ps backend

# Логи бэкенда
docker compose logs -f backend

# Проверка health
curl http://localhost:8080/actuator/health

# Перезапуск
docker compose restart backend
```

## 🆘 Troubleshooting

### Образ не обновляется
```bash
ssh user@server
cd /opt/dimkasvist
docker compose pull backend
docker compose up -d backend
```

### Ошибки подключения к БД
```bash
# Проверьте что PostgreSQL запущен
docker compose ps postgres
docker compose logs postgres

# Проверьте переменные окружения
docker compose config
```

### Ошибки Liquibase
Проверьте логи:
```bash
docker compose logs backend | grep liquibase
```

### Порт занят
```bash
# Проверьте что запущено на порту 8080
sudo lsof -i :8080
```

## 🧪 Тестирование

```bash
# Юнит-тесты
./gradlew test

# Интеграционные тесты
./gradlew integrationTest

# Все тесты с отчетом
./gradlew test jacocoTestReport
```

## 📚 Дополнительные ресурсы

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Gradle Documentation](https://docs.gradle.org/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [MinIO Documentation](https://min.io/docs/minio/linux/index.html)
