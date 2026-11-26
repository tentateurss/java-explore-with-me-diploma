package ru.practicum.main.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import ru.practicum.main.client.SubscriptionAuthorsClient;
import ru.practicum.main.client.RequestCountClient;
import ru.practicum.main.client.UserClient;

@Configuration
@EnableFeignClients(clients = {UserClient.class, SubscriptionAuthorsClient.class, RequestCountClient.class})
public class EventFeignConfiguration {
}