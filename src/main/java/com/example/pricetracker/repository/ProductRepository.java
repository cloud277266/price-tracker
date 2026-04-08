package com.example.pricetracker.repository;

import com.example.pricetracker.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // 상품명으로 검색하는 기능 추가
    Optional<Product> findByProductName(String productName);
}