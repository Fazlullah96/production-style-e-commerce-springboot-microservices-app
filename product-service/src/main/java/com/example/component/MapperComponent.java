package com.example.component;

import com.example.dtos.CategoryRequest;
import com.example.dtos.CategoryResponse;
import com.example.dtos.ProductRequest;
import com.example.dtos.ProductResponse;
import com.example.model.Category;
import com.example.model.Product;
import org.springframework.stereotype.Component;

@Component
public class MapperComponent {
    public Product toProductEntity(ProductRequest request, Category category){
        return Product
                .builder()
                .name(request.getName())
                .skuCode(request.getSkuCode())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(category)
                .build();
    }

    public ProductResponse toProductResponse(Product product){
        return ProductResponse
                .builder()
                .productId(product.getId())
                .name(product.getName())
                .skuCode(product.getSkuCode())
                .description(product.getDescription())
                .price(product.getPrice())
                .createdAt(product.getCreatedAt())
                .category(toCategoryResponse(product.getCategory()))
                .build();
    }

    public CategoryResponse toCategoryResponse(Category category){
        return CategoryResponse
                .builder()
                .categoryId(category.getId())
                .name(category.getName())
                .build();
    }

    public Category toCategoryEntity(CategoryRequest request){
        return Category
                .builder()
                .name(request.getName())
                .build();
    }
}
