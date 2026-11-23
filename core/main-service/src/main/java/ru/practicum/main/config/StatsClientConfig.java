package ru.practicum.main.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import ru.practicum.stats.client.StatsClient;

@Configuration
public class StatsClientConfig {

    @Bean
    public StatsClient statsClient(
            DiscoveryClient discoveryClient,
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper) {
        return new StatsClient(discoveryClient, restClientBuilder, objectMapper);
    }
}