package ru.practicum.request;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.practicum.main.model.ParticipationRequest;
import ru.practicum.main.repository.ParticipationRequestRepository;

@SpringBootApplication(scanBasePackages = "ru.practicum.main")
@EntityScan(basePackageClasses = ParticipationRequest.class)
@EnableJpaRepositories(basePackageClasses = ParticipationRequestRepository.class)
public class RequestServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RequestServiceApplication.class, args);
    }
}
