package com.example.clients;

import com.example.component.ProductClientFallbackFactory;
import com.example.dtos.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "product-service", fallbackFactory = ProductClientFallbackFactory.class)
public interface ProductClient {
    @GetMapping("/api/product/skucode/{skuCode}")
    ProductResponse getProductBySkuCode(
            @RequestHeader("Authorization") String token,
            @PathVariable("skuCode") String skuCode
    );

    @GetMapping("/api/product/matching/skucodes")
    List<ProductResponse> getAllProductBySkuCodeIn(
            @RequestHeader("Authorization") String token,
            @RequestBody List<String> skuCodes
    );
}
