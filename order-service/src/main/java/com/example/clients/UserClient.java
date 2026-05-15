package com.example.clients;

import com.example.component.UserClientFallbackFactory;
import com.example.dtos.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service", fallbackFactory = UserClientFallbackFactory.class)
public interface UserClient {
    @GetMapping("/api/user/{userId}")
    UserResponse getUserByUserId(
            @RequestHeader("Authorization") String token,
            @PathVariable("userId") String userId
    );
}
