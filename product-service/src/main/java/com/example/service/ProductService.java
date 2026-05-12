package com.example.service;

import com.example.component.MapperComponent;
import com.example.dtos.ProductRequest;
import com.example.dtos.ProductResponse;
import com.example.exceptions.CategoryNotFoundException;
import com.example.exceptions.ProductAlreadyExistsException;
import com.example.exceptions.ProductNotFoundException;
import com.example.model.Category;
import com.example.model.Product;
import com.example.repo.CategoryRepo;
import com.example.repo.ProductRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepo productRepo;
    private final CategoryRepo categoryRepo;
    private final MapperComponent mapper;

    @Transactional
    @Caching(put = {
            @CachePut(value = "PRODUCT_CACHE", key = "#result.productId"),
            @CachePut(value = "PRODUCT_SKUCODE_CACHE", key = "#result.skuCode"),
            @CachePut(value = "PRODUCT_NAME_CACHE", key = "#result.name")
    }, evict = {
            @CacheEvict(value = "PRODUCT_CACHE_LIST", allEntries = true),
            @CacheEvict(value = "PRODUCT_ACTIVE_LIST", allEntries = true)
    })
    public ProductResponse addProduct(ProductRequest request){
        boolean exists = productRepo.existsBySkuCode(request.getSkuCode());
        Category category = categoryRepo.findById(request.getCategoryId())
                .orElseThrow(() -> {
                    log.error("CATEGORY NOT FOUND FOR CATEGORYID: {}", request.getCategoryId());
                    return new CategoryNotFoundException("Category not found for CategoryId: " + request.getCategoryId());
                });
        if (exists){
            throw new ProductAlreadyExistsException("Product Already exists for Product Skucode: " + request.getSkuCode());
        }
        Product beforeSaving = mapper.toProductEntity(request, category);
        log.info("PRODUCT {} IS SUCCESSFULLY SAVED IN DB", request.getName());
        Product savedProduct = productRepo.save(beforeSaving);
        return mapper.toProductResponse(savedProduct);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "PRODUCT_CACHE", key = "#id")
    public ProductResponse getProductById(int id){
        Product product = productRepo.findById(id)
                .orElseThrow(() -> {
                    log.error("PRODUCT NOT FOUND FOR PRODUCTID: {}", id);
                    return new ProductNotFoundException("Product not found for ProductId: " + id);
                });
        log.info("PRODUCT FOUND FOR PRODUCTID: {}", id);
        return mapper.toProductResponse(product);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "PRODUCT_SKUCODE_CACHE", key = "#skuCode")
    public ProductResponse getProductBySkuCode(String skuCode){
        Product product = productRepo.findBySkuCode(skuCode)
                .orElseThrow(() -> {
                    log.error("PRODUCT NOT FOUND FOR SKUCODE: {}", skuCode);
                    return new ProductNotFoundException("Product not found for ProductSkuCode: " + skuCode);
                });
        log.info("PRODUCT FOUND FOR PRODUCT SKUCODE: {}", skuCode);
        return mapper.toProductResponse(product);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "PRODUCT_NAME_CACHE", key = "#name")
    public ProductResponse getProductByName(String name){
        Product product = productRepo.findByName(name)
                .orElseThrow(() -> {
                    log.error("PRODUCT NOT FOUND FOR PRODUCTNAME: {}", name);
                    return new ProductNotFoundException("Product not found for ProductName: " + name);
                });
        log.info("PRODUCT FOUND FOR PRODUCTNAME: {}", name);
        return mapper.toProductResponse(product);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "PRODUCT_INACTIVE_CACHE_LIST", allEntries = true),
            @CacheEvict(value = "PRODUCT_ACTIVE_CACHE_LIST", allEntries = true)
    })
    public ProductResponse softDeleteProduct(String skuCode){
        Product product = productRepo.findBySkuCode(skuCode)
                .orElseThrow(() -> {
                    log.error("PRODUCT NOT FOUND FOR SKUCODE: {}", skuCode);
                    return new ProductNotFoundException("Product not found for SkuCode: " + skuCode);
                });
        product.setIsActive(false);
        log.info("PRODUCT DEACTIVATED FOR PRODUCT SKUCODE: {}", skuCode);
        return mapper.toProductResponse(product);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "PRODUCT_CACHE_LIST", key = "'ALL'")
    public List<ProductResponse> getAllProducts(){
        List<Product> products = productRepo.findAll();
        log.info("ALL PRODUCT FOUND IN DB");
        return products
                .stream()
                .map(product -> mapper.toProductResponse(product))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "PRODUCT_ACTIVE_CACHE_LIST", key = "'ACTIVE'")
    public List<ProductResponse> getAllActiveProducts(){
        List<Product> products = productRepo.findAllByIsActiveTrue();
        log.info("FOUND ALL ACTIVE PRODUCTS IN DB");
        return products
                .stream()
                .map(mapper::toProductResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "PRODUCT_INACTIVE_CACHE_LIST", key = "'INACTIVE'")
    public List<ProductResponse> getAllInActiveProducts(){
        List<Product> products = productRepo.findAllByIsActiveFalse();
        log.info("FOUND ALL INACTIVE PRODUCTS IN DB");
        return products
                .stream()
                .map(mapper::toProductResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "PRODUCT_INACTIVE_CACHE_LIST", allEntries = true),
            @CacheEvict(value = "PRODUCT_ACTIVE_CACHE_LIST", allEntries = true)
    })
    public ProductResponse softAddProduct(String skuCode){
        Product product = productRepo.findBySkuCode(skuCode)
                .orElseThrow(() -> {
                    log.error("PRODUCT NOT FOUND FOR PRODUCT SKUCODE: {}", skuCode);
                    return new ProductNotFoundException("Product not found for product skuCode: " + skuCode);
                });
        log.info("PRODUCT FOUND FOR SKUCODE: {}", skuCode);
        product.setIsActive(true);
        return mapper.toProductResponse(product);
    }
}
