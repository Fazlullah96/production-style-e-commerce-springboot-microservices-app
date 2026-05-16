package com.example.repo;

import com.example.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepo extends JpaRepository<Product, Integer> {
    Optional<Product> findBySkuCode(String skuCode);
    Optional<Product> findByName(String name);
    List<Product> findAllByIsActiveTrue();
    List<Product> findAllByIsActiveFalse();
    boolean existsBySkuCode(String skuCode);
    List<Product> findBySkuCodeIn(List<String> skuCode);
}
