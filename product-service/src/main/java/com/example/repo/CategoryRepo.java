package com.example.repo;

import com.example.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepo extends JpaRepository<Category, Integer> {
    Optional<Category> findByName(String name);
    boolean existsById(int id);
    boolean existsByName(String name);
}
