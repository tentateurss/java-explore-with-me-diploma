package ru.practicum.main.service.subscription;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.client.SubscriptionUserClient;
import ru.practicum.main.dto.event.EventSearchParams;
import ru.practicum.main.dto.event.EventShortDto;
import ru.practicum.main.dto.subscription.SubscriberDto;
import ru.practicum.main.dto.subscription.SubscriptionDto;
import ru.practicum.main.dto.user.UserDto;
import ru.practicum.main.dto.user.UserShortDto;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.mapper.SubscriptionMapper;
import ru.practicum.main.model.Subscription;
import ru.practicum.main.repository.SubscriptionRepository;
import ru.practicum.main.client.SubscriptionEventClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionServiceImpl implements SubscriptionService {

    private static final DateTimeFormatter EVENT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionEventClient subscriptionEventClient;
    private final SubscriptionUserClient subscriptionUserClient;

    @Override
    @Transactional
    public void subscribe(Long subscriberId, Long authorId) {
        if (subscriberId.equals(authorId)) {
            throw new ConflictException("Нельзя подписаться на самого себя.");
        }
        Map<Long, UserShortDto> existingUsers = getUserDtos(List.of(subscriberId, authorId));
        if (!existingUsers.containsKey(subscriberId)) {
            throw new NotFoundException("Подписчик " + subscriberId + " не найден.");
        }
        if (!existingUsers.containsKey(authorId)) {
            throw new NotFoundException("Автор " + authorId + " не найден.");
        }
        if (subscriptionRepository.existsBySubscriberIdAndAuthorId(subscriberId, authorId)) {
            throw new ConflictException("Вы уже подписаны на этого пользователя.");
        }

        Subscription subscription = new Subscription();
        subscription.setSubscriberId(subscriberId);
        subscription.setAuthorId(authorId);
        subscription.setCreatedAt(LocalDateTime.now());

        subscriptionRepository.save(subscription);
    }

    @Override
    @Transactional
    public void unsubscribe(Long subscriberId, Long authorId) {
        Map<Long, UserShortDto> existingUsers = getUserDtos(List.of(subscriberId, authorId));
        if (!existingUsers.containsKey(subscriberId)) {
            throw new NotFoundException("Подписчик " + subscriberId + " не найден.");
        }
        if (!existingUsers.containsKey(authorId)) {
            throw new NotFoundException("Автор " + authorId + " не найден.");
        }
        if (!subscriptionRepository.existsBySubscriberIdAndAuthorId(subscriberId, authorId)) {
            throw new NotFoundException("Подписка не найдена. Вы не были подписаны на этого пользователя.");
        }

        subscriptionRepository.deleteBySubscriberIdAndAuthorId(subscriberId, authorId);
    }

    @Override
    public List<SubscriptionDto> getSubscriptions(Long userId) {
        if (!getUserDtos(List.of(userId)).containsKey(userId)) {
            throw new NotFoundException("Пользователь " + userId + " не найден.");
        }
        List<Subscription> subscriptions = subscriptionRepository.findBySubscriberId(userId);
        Map<Long, UserShortDto> authors = getUserDtos(subscriptions.stream()
                .map(subscription -> subscription.getAuthorId())
                .toList());

        return subscriptions.stream()
                .map(subscription -> SubscriptionMapper.toSubscriptionDto(subscription,
                        authors.get(subscription.getAuthorId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<SubscriberDto> getSubscribers(Long userId) {
        if (!getUserDtos(List.of(userId)).containsKey(userId)) {
            throw new NotFoundException("Пользователь " + userId + " не найден.");
        }
        List<Subscription> subscriptions = subscriptionRepository.findByAuthorId(userId);
        Map<Long, UserShortDto> subscribers = getUserDtos(subscriptions.stream()
                .map(subscription -> subscription.getSubscriberId())
                .toList());

        return subscriptions.stream()
                .map(subscription -> SubscriptionMapper.toSubscriberDto(subscription,
                        subscribers.get(subscription.getSubscriberId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<EventShortDto> getEventsFromSubscriptions(Long userId, EventSearchParams params) {
        if (!getUserDtos(List.of(userId)).containsKey(userId)) {
            throw new NotFoundException("Пользователь " + userId + " не найден.");
        }
        List<Subscription> subscriptions = subscriptionRepository.findBySubscriberId(userId);
        if (subscriptions.isEmpty()) {
            return List.of();
        }

        List<Long> authorIds = subscriptions.stream()
                .map(sub -> sub.getAuthorId())
                .collect(Collectors.toList());

        return subscriptionEventClient.getEventsByAuthors(
                        authorIds,
                        params.getRangeStart() == null
                                ? null : params.getRangeStart().format(EVENT_DATE_FORMAT),
                        params.getRangeEnd() == null
                                ? null : params.getRangeEnd().format(EVENT_DATE_FORMAT),
                        params.getFrom(),
                        params.getSize());
    }

    @Override
    public List<Long> getSubscribedAuthorIds(Long userId) {
        if (!getUserDtos(List.of(userId)).containsKey(userId)) {
            throw new NotFoundException("Пользователь " + userId + " не найден.");
        }

        return subscriptionRepository.findBySubscriberId(userId)
                .stream()
                .map(Subscription::getAuthorId)
                .distinct()
                .toList();
    }
    private Map<Long, UserShortDto> getUserDtos(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }

        List<Long> uniqueIds = ids.stream()
                .distinct()
                .toList();

        List<UserDto> users = subscriptionUserClient.getUsersByIds(uniqueIds);

        if (users == null) {
            throw new IllegalStateException("user-service returned null users");
        }

        return users.stream()
                .collect(Collectors.toMap(
                        UserDto::getId,
                        user -> new UserShortDto(user.getId(), user.getName()),
                        (first, second) -> first
                ));
    }
}
