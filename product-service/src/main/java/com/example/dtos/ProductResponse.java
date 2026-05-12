package com.example.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponse {
    private Integer productId;
    private String skuCode;
    private String name;
    private String description;
    private Double price;
    private CategoryResponse category;
    private LocalDateTime createdAt;
}
