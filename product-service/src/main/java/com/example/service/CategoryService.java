package com.example.service;

import com.example.component.MapperComponent;
import com.example.dtos.CategoryRequest;
import com.example.dtos.CategoryResponse;
import com.example.exceptions.CategoryAlreadyExistsException;
import com.example.exceptions.CategoryNotFoundException;
import com.example.model.Category;
import com.example.repo.CategoryRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {
    private final CategoryRepo categoryRepo;
    private final MapperComponent mapper;

    @Transactional
    @CachePut(value = "CATEGORY_CACHE", key = "#result.categoryId")
    public CategoryResponse addCategory(CategoryRequest request){
        boolean exists = categoryRepo.existsByName(request.getName());
        if(exists){
            log.error("CATEGORY ALREADY EXISTS FOR NAME {}", request.getName());
            throw new CategoryAlreadyExistsException("Category Already exists for Name: " + request.getName());
        }
        Category category = categoryRepo.save(mapper.toCategoryEntity(request));
        return mapper.toCategoryResponse(category);
    }
}
