package ru.practicum.subscription;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.practicum.main.model.Subscription;
import ru.practicum.main.repository.SubscriptionRepository;

@SpringBootApplication(scanBasePackages = "ru.practicum.main")
@EntityScan(basePackageClasses = Subscription.class)
@EnableJpaRepositories(basePackageClasses = SubscriptionRepository.class)
public class SubscriptionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubscriptionServiceApplication.class, args);
    }
}
