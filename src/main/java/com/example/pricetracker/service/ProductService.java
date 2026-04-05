package com.example.pricetracker.service;

import com.example.pricetracker.dto.ProductRequestDto;
import com.example.pricetracker.entity.Product;
import com.example.pricetracker.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // 상품 등록
    public Product addProduct(ProductRequestDto dto) {
        Product product = new Product();
        product.setProductName(dto.getProductName());
        product.setProductUrl(dto.getProductUrl());
        product.setTargetPrice(dto.getTargetPrice());
        product.setCurrentPrice(0); // 처음엔 0으로 설정
        return productRepository.save(product);
    }

    // 전체 상품 조회
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
}