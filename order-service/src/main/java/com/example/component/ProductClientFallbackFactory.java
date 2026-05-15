package com.example.component;

import com.example.clients.ProductClient;
import com.example.dtos.ProductResponse;
import com.example.exceptions.ProductNotFoundException;
import com.example.exceptions.ServiceUnavailableException;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProductClientFallbackFactory implements FallbackFactory<ProductClient> {
    @Override
    public ProductClient create(Throwable cause) {
        return new ProductClient() {
            @Override
            public ProductResponse getProductBySkuCode(String token, String skuCode) {
                if (cause instanceof FeignException.NotFound){
                    log.error("PRODUCT NOT FOUND FOR SKUCODE: {}", skuCode);
                    throw new ProductNotFoundException("Product not found for skuCode: " + skuCode);
                }
                log.error("PRODUCT SERVICE UNAVAILABLE AT THE MOMENT");
                throw new ServiceUnavailableException("PRODUCT SERVICE UNAVAILABLE........");
            }
        };
    }
}
