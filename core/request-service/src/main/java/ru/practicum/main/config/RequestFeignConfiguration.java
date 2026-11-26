package ru.practicum.main.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import ru.practicum.main.client.RequestEventClient;
import ru.practicum.main.client.RequestUserClient;

@Configuration
@EnableFeignClients(clients = {RequestUserClient.class, RequestEventClient.class})
public class RequestFeignConfiguration {
}