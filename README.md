# Bank REST backend

## Описание
Bank REST - backend-проект, реализующий REST API для управления банковскими картами и переводами. Приложение построено на Spring Boot и включает регистрацию пользователей, выпуск и обслуживание карт, перевод средств, проверку лимитов и полноценную систему безопасности на основе JWT. Все данные хранятся в PostgreSQL, схемы создаются Liquibase миграциями, а документация по API поддерживается в формате OpenAPI.

Основные возможности:
- регистрация и управление пользователями, ролями и статусами;
- выпуск банковских карт, изменение их статусов, получение баланса и истории переводов;
- выполнение переводов между картами с учётом лимитов и бизнес-правил;
- аутентификация и авторизация по JWT токенам;
- централизованная обработка ошибок с единым форматом ответа.

## Используемые технологии
- Java 17, Spring Boot 3.2.x;
- Spring Web, Spring Data JPA, Spring Security, Bean Validation;
- PostgreSQL и Liquibase для миграций БД;
- JWT для токенов доступа;
- Maven и Docker Compose для сборки и инфраструктуры;
- springdoc-openapi для интерактивной документации.

## Требования к окружению
- JDK 17;
- Maven 3.9+ (при необходимости можно добавить Maven Wrapper и использовать `./mvnw`);
- Docker и Docker Compose (для быстрого запуска инфраструктуры) либо установленный PostgreSQL;
- Git для получения исходного кода.

## Подготовка окружения
### 1. Клонирование репозитория
```bash
git clone https://github.com/NBx03/Bank-REST-backend.git
cd bank_rest
```

### 2. Настройка переменных окружения
Приложение поддерживает переопределение настроек через переменные окружения. Ниже приведены ключевые параметры:

| Переменная | Назначение | Значение по умолчанию |
| --- | --- | --- |
| `BANK_REST_DB_URL` | JDBC URL PostgreSQL | `jdbc:postgresql://localhost:5433/bank_app` |
| `BANK_REST_DB_USERNAME` | Пользователь БД | `bank_user` |
| `BANK_REST_DB_PASSWORD` | Пароль пользователя БД | `bank_pass` |
| `BANK_REST_JWT_SECRET` | Секрет для подписи JWT | `change-me` |
| `BANK_REST_ENCRYPTION_KEY` | Ключ AES для шифрования номеров карт | `change-me-too` |
| `BANK_REST_DAILY_LIMIT` | Суточный лимит переводов | `50000.00` |

Для локального запуска достаточно создать файл `.env` (используется `docker-compose`) или экспортировать переменные в окружении shell:
```bash
export BANK_REST_DB_URL="jdbc:postgresql://localhost:5433/bank_app"
export BANK_REST_JWT_SECRET="long-secret"
export BANK_REST_ENCRYPTION_KEY="strong-secret"
```

### 3. Настройки приложения
Файл `src/main/resources/application.yml` содержит базовую конфигурацию Spring Boot. При необходимости измените порт сервера (`server.port`), суточный лимит переводов (`transfer.limit.daily`) или другие параметры.

### 4. База данных
#### Вариант A. Docker Compose
Самый быстрый способ поднять инфраструктуру - запустить контейнер PostgreSQL:
```bash
docker compose up -d
```
Контейнер создаст базу `bank_app` с пользователем `bank_user/bank_pass` и пробросит порт `5433` на хост. Остановить и очистить окружение можно командой:
```bash
docker compose down -v
```

#### Вариант B. Локальный PostgreSQL
Если база установлена локально, создайте пользователя и БД вручную:
```sql
create role bank_user login password 'bank_pass';
create database bank_app owner bank_user;
```
Убедитесь, что порт и креды совпадают с параметрами приложения.

### 5. Миграции Liquibase
При запуске Spring Boot автоматически выполняет Liquibase миграции из каталога `src/main/resources/db/changelog/migrations`. Никаких дополнительных действий не требуется. Для ручного применения можно использовать Maven:
```bash
mvn liquibase:update
```

## Сборка и запуск
### Сборка
```bash
mvn clean package
```
Готовый jar-файл появится в `target/bank-rest-0.0.1-SNAPSHOT.jar`. Если в проект будет добавлен Maven Wrapper, используйте `./mvnw` в тех же командах.

### Запуск из IDE
Импортируйте проект как Maven-проект и запустите класс `com.example.bankcards.BankRestApplication`.

### Запуск из командной строки
```bash
mvn spring-boot:run
```
или
```bash
java -jar target/bank-rest-0.0.1-SNAPSHOT.jar
```

Сервис по умолчанию слушает порт `8080`.

## Проверка работоспособности
- Проверьте состояние приложения: `GET http://localhost:8080/actuator/health`.
- Основная документация доступна по адресу `http://localhost:8080/swagger-ui/index.html`.
- Статическая спецификация лежит в `docs/openapi.yaml` и может быть импортирована в Swagger UI, Postman или Insomnia.
- Типовой сценарий авторизации:
   1. Выполнить запрос `POST /api/auth/login` с `username/password`.
   2. Полученный `accessToken` передавать в заголовке `Authorization: Bearer <token>`.
   3. Для операций с картами и переводами обязательно добавлять заголовок `X-Operator-Id` с идентификатором оператора.

Примеры запросов:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"my.name","password":"Secret123"}'

curl http://localhost:8080/api/users/1 \
  -H 'Authorization: Bearer <accessToken>'
```

## Дополнительные материалы
- OpenAPI спецификация: `docs/openapi.yaml`.
- Настройки безопасности находятся в `src/main/java/com/example/bankcards/config/SecurityConfig.java`.
- Liquibase миграции - в `src/main/resources/db/changelog/migrations`.

Документ обновляется по мере развития проекта.