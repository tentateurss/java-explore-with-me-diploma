package ru.practicum.main.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.main.dto.user.UserDto;

import java.util.List;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/admin/users")
    List<UserDto> getUsersByIds(@RequestParam("ids") List<Long> ids);
}