package com.example.component;

import com.example.clients.UserClient;
import com.example.dtos.UserResponse;
import com.example.exceptions.ServiceUnavailableException;
import com.example.exceptions.UserNotFoundException;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {
    @Override
    public UserClient create(Throwable cause) {
        return new UserClient() {
            @Override
            public UserResponse getUserByUserId(String token, int userId) {
                if(cause instanceof FeignException.NotFound){
                    log.error("USER FOR FOUND FOR USERID: {}", userId);
                    throw new UserNotFoundException("User Not Found for UserId: " + userId);
                }
                log.error("USER SERVICE UNAVAILABLE AT THE MOMENT");
                throw new ServiceUnavailableException("USER SERVICE UNAVAILABLE.......");
            }
        };
    }
}
