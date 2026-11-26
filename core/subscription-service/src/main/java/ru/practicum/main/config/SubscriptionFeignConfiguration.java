package ru.practicum.main.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import ru.practicum.main.client.SubscriptionEventClient;
import ru.practicum.main.client.SubscriptionUserClient;

@Configuration
@EnableFeignClients(clients = {SubscriptionUserClient.class, SubscriptionEventClient.class})
public class SubscriptionFeignConfiguration {
}