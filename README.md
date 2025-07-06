# Kufar Apartment Monitor Bot

Telegram бот для мониторинга новых объявлений об аренде квартир на Kufar.by. Бот отслеживает появление новых объявлений и автоматически отправляет уведомления подписанным пользователям.

## Функциональность

- 🏠 Мониторинг новых объявлений об аренде квартир
- 📨 Автоматические уведомления о новых объявлениях
- 📝 Система подписок для пользователей
- 📊 Просмотр последних объявлений по команде

### Команды бота

- `/start` - Начало работы с ботом
- `/latest` - Показать последние объявления
- `/subscribe` - Подписаться на уведомления
- `/unsubscribe` - Отписаться от уведомлений

## Технологии

- Java 17
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Telegram Bots API
- Docker & Docker Compose

## Запуск проекта

### Предварительные требования

- Java 21+
- Docker и Docker Compose
- PostgreSQL
- Telegram Bot Token (получить у [@BotFather](https://t.me/BotFather))

### Настройка окружения

1. Скопируйте файл `.env.example` в `.env`:
```bash
cp .env.example .env
```

2. Заполните необходимые переменные окружения в файле `.env`:
```properties
BOT_TOKEN=your_bot_token
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/your_db
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password
```

### Запуск с помощью Docker

1. Соберите и запустите контейнеры:
```bash
docker-compose up -d
```

### Запуск локально

1. Установите зависимости:
```bash
./mvnw install
```

2. Запустите приложение:
```bash
./mvnw spring-boot:run
```

## Мониторинг

Бот проверяет наличие новых объявлений каждые 3 минуты. При обнаружении новых объявлений, они сохраняются в базу данных и отправляются всем активным подписчикам.

## Структура проекта

```
ai-kufar-bot/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/innowise/aikufarbot/
│   │   │       ├── bot/
│   │   │       ├── config/
│   │   │       ├── model/
│   │   │       ├── repository/
│   │   │       └── service/
│   │   └── resources/
│   └── test/
├── .env.example
├── docker-compose.yaml
├── Dockerfile
└── pom.xml
```

## Лицензия

MIT 