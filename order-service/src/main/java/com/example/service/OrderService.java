package com.example.service;

import com.example.clients.ProductClient;
import com.example.clients.UserClient;
import com.example.dtos.*;
import com.example.exceptions.ProductNotFoundException;
import com.example.model.Order;
import com.example.model.OrderItem;
import com.example.model.OrderStatus;
import com.example.repo.OrderItemRepo;
import com.example.repo.OrderRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final UserClient userClient;
    private final OrderItemRepo orderItemRepo;
    private final OrderRepo orderRepo;
    private final ProductClient productClient;

    @Transactional
    @CachePut(value = "ORDER_NUMBER_CACHE", key = "#result.orderNumber")
    public OrderResponse placeOrder(String token, OrderRequest request){
        UserResponse user  = userClient.getUserByUserId(token, request.getUserId());
        List<String> requestedSkuCodes = request.getItems()
                .stream()
                .map(OrderItemRequest::getSkuCode)
                .collect(Collectors.toList());

        List<ProductResponse> requestedProducts = productClient.getAllProductBySkuCodeIn(token, requestedSkuCodes);

        if(requestedProducts.size() != requestedSkuCodes.size()){
            List<String> existingSkus = requestedProducts.stream()
                    .map(product -> product.getSkuCode())
                    .collect(Collectors.toList());

            List<String> missingSku = requestedSkuCodes.stream()
                    .filter(sku -> !existingSkus.contains(sku))
                    .collect(Collectors.toList());

            log.error("Order failed. Missing products for SKUs: {}", missingSku);
            throw new ProductNotFoundException("The following products do not exist or are inactive: " + missingSku);
        }
        log.info("All products validated successfully. Calculating totals...");

        Order order = Order.builder()
                .orderNumber(UUID.randomUUID().toString().substring(0, 8))
                .customerId(user.getUserId())
                .orderStatus(OrderStatus.PENDING)
                .build();

        Map<String, Integer> skuQuantityMap = request.getItems()
                .stream()
                .collect(Collectors.toMap(product -> product.getSkuCode(), product -> product.getQuantity()));

        List<OrderItem> orderItems = requestedProducts
                .stream()
                .map(product -> OrderItem
                        .builder()
                        .skuCode(product.getSkuCode())
                        .price(product.getPrice())
                        .quantity(skuQuantityMap.get(product.getSkuCode()))
                        .build())
                .collect(Collectors.toList());

        order.setOrderItems(orderItems);

        double totalAmount = orderItems
                .stream()
                .mapToDouble(orderItem -> orderItem.getPrice() * orderItem.getQuantity())
                .sum();

        order.setTotalPrice(totalAmount);

        Order savedOrder = orderRepo.save(order);
        return OrderResponse
                .builder()
                .orderNumber(savedOrder.getOrderNumber())
                .status(savedOrder.getOrderStatus())
                .totalAmount(savedOrder.getTotalPrice())
                .items(savedOrder.getOrderItems()
                        .stream()
                        .map(orderItem -> OrderItemResponse
                                .builder()
                                .id(orderItem.getId())
                                .skuCode(orderItem.getSkuCode())
                                .price(orderItem.getPrice())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "ORDER_NUMBER_CACHE", key = "#orderNumber")
    public OrderResponse getOrderByOrderNumber(String orderNumber){
        Order order = orderRepo.findByOrderNumber(orderNumber)
                .orElseThrow(() ->{
                    log.error("PRODUCT NOT FOUND FOR ORDERNUMBER: {}", orderNumber);
                    return new  ProductNotFoundException("Product not found for orderNumber: " + orderNumber);
                });

        return OrderResponse
                .builder()
                .orderNumber(order.getOrderNumber())
                .status(order.getOrderStatus())
                .totalAmount(order.getTotalPrice())
                .items(order.getOrderItems()
                        .stream()
                        .map(orderItem -> OrderItemResponse
                                .builder()
                                .id(orderItem.getId())
                                .skuCode(orderItem.getSkuCode())
                                .price(orderItem.getPrice())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
