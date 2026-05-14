package com.example.service;

import com.example.config.KeycloakConfig;
import com.example.config.Template;
import com.example.dtos.LoginRequest;
import com.example.dtos.TokenResponse;
import com.example.dtos.UserRequest;
import com.example.dtos.UserResponse;
import com.example.exceptions.InvalidCredentialsException;
import com.example.exceptions.KeycloakRegistrationFailedException;
import com.example.exceptions.UserNotFoundException;
import com.example.model.User;
import com.example.repo.AddressRepo;
import com.example.repo.UserRepo;
import jakarta.ws.rs.core.Response;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepo userRepo;
    private final AddressRepo addressRepo;
    private final KeycloakConfig keycloakConfig;
    private final Template template;

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;


    @Transactional
    @CachePut(value = "USER_CACHE", key = "#result.userId")
    public UserResponse registerUser(UserRequest request){
        CredentialRepresentation credentials = new CredentialRepresentation();
        credentials.setType(CredentialRepresentation.PASSWORD);
        credentials.setValue(request.getPassword());
        credentials.setTemporary(false);

        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.getEmail());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setCredentials(Collections.singletonList(credentials));

        Response response = keycloakConfig.keycloak().realm(realm).users().create(user);

        if(response.getStatus() != 201){
            throw new KeycloakRegistrationFailedException("Failed to create User in Keycloak");
        }

        String userId = CreatedResponseUtil.getCreatedId(response);

        User localUser = User
                .builder()
                .userId(userId)
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();
        User savedUser = userRepo.save(localUser);
        return UserResponse
                .builder()
                .userId(savedUser.getUserId())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .build();
    }

    public TokenResponse login(LoginRequest request){
        String tokenEndpoint = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("grant_type", "password");
        map.add("username", request.getEmail());
        map.add("password", request.getPassword());

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(map, httpHeaders);

        try{
            ResponseEntity<KeycloakTokenResponse> response = template.restTemplate().postForEntity(
                    tokenEndpoint, httpEntity, KeycloakTokenResponse.class
            );

            if(response.getStatusCode().is2xxSuccessful() && response.getBody() != null){
                return TokenResponse
                        .builder()
                        .accessToken(response.getBody().getAccess_token())
                        .refreshToken(response.getBody().getRefresh_token())
                        .expiresIn(response.getBody().getExpires_in())
                        .build();
            }else{
                throw new InvalidCredentialsException("USERNAME OR PASSWORD IS INCORRECT");
            }
        } catch (HttpClientErrorException.Unauthorized e){
            System.out.println("KEYCLOAK REJECTION REASON : " + e.getResponseBodyAsString());
            throw new RuntimeException("Authentication failed " + e.getResponseBodyAsString());
        }
    }

    @Data
    public static class KeycloakTokenResponse{
        private String access_token;
        private String refresh_token;
        private Integer expires_in;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "USER_CACHE", key = "#userId")
    public UserResponse getUserById(String userId){
        User user = userRepo.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found for UserId: " + userId));
        return UserResponse
                .builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }
}
