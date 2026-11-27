# ExploreWithMe

## Архитектура

Приложение разделено на независимые Spring Boot сервисы:

| Сервис | Ответственность |
| --- | --- |
| `user-service` | Пользователи |
| `event-service` | Мероприятия, категории и подборки |
| `request-service` | Заявки на участие |
| `subscription-service` | Подписки на организаторов |
| `stats-server` | Сбор просмотров и статистика |

У четырех бизнес-сервисов отдельные базы PostgreSQL. Внешние запросы принимает Spring Cloud Gateway (`localhost:8080`), адреса сервисов определяются через Eureka. Конфигурацию предоставляет Spring Cloud Config Server. Общие DTO и контракты вынесены в `core/common`.

Конфигурации находятся в:

- `infra/config-server/src/main/resources/config-repo/` - централизованные настройки и маршруты Gateway;
- `infra/config-server/src/main/resources/application.properties` - настройки Config Server;
- `infra/gateway-server/src/main/resources/` и `infra/discovery-server/src/main/resources/` - настройки Gateway и Eureka;
- `docker-compose.yml` - сервисы, базы данных, переменные окружения и порты.

## Внутренний API

Сервисы взаимодействуют по HTTP через OpenFeign, используя имена из Eureka. Межсервисные маршруты `/internal/**` не доступны через внешний Gateway.

| Откуда - куда                                             | Назначение вызовов |
|-----------------------------------------------------------| --- |
| `event-service` - `user-service`                          | Получение и проверка данных организаторов мероприятий |
| `request-service` - `user-service`                        | Проверка пользователей при обработке заявок |
| `request-service` - `event-service`                       | Получение сведений о мероприятии: организатор, статус, лимит участников, необходимость модерации |
| `event-service` - `request-service`                       | Получение количества подтвержденных заявок по мероприятиям; при недоступности сервиса заявок используется резервное значение `0` |
| `subscription-service` - `user-service` / `event-service` | Проверка пользователей и получение мероприятий авторов для подписок |

HTTP-контракты реализованы внутренними контроллерами и Feign-клиентами соответствующих модулей `core/*-service`. Сервис статистики предоставляет операции `POST /hit` и `GET /stats`.

## Внешний API

Все внешние запросы к основным сервисам проходят через Gateway на `http://localhost:8080`.

Спецификации API из группового проекта:

- [Основной API](https://github.com/tentateurss/java-explore-with-me-plus/blob/main/ewm-main-service-spec.json)
- [API сервиса статистики](https://github.com/tentateurss/java-explore-with-me-plus/blob/main/ewm-stats-service-spec.json)
