package com.example.dtos;

import com.example.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {
    private String orderNumber;
    private OrderStatus status;
    private Double totalAmount;
    private List<OrderItemResponse> items;
}
