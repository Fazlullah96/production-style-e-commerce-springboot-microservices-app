package com.example.service;

import com.example.clients.UserClient;
import com.example.dtos.OrderRequest;
import com.example.dtos.OrderResponse;
import com.example.dtos.UserResponse;
import com.example.repo.OrderItemRepo;
import com.example.repo.OrderRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final UserClient userClient;
    private final OrderItemRepo orderItemRepo;
    private final OrderRepo orderRepo;

    public OrderResponse addOrder(String token, OrderRequest request){
        UserResponse user  = userClient.getUserByUserId(token, request.getUserId());
    }
}
