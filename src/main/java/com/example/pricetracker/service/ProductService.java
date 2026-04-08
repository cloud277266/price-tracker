package com.example.pricetracker.service;

import com.example.pricetracker.dto.ProductRequestDto;
import com.example.pricetracker.entity.PriceHistory;
import com.example.pricetracker.entity.Product;
import com.example.pricetracker.repository.PriceHistoryRepository;
import com.example.pricetracker.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    // 상품 등록 (이미 있으면 목표가만 업데이트)
    public Product addProduct(ProductRequestDto dto) {
        Optional<Product> existing = productRepository.findByProductName(dto.getProductName());
        if (existing.isPresent()) {
            Product product = existing.get();
            product.setTargetPrice(dto.getTargetPrice());
            product.setAlarmEnabled(true);
            return productRepository.save(product);
        }

        Product product = new Product();
        product.setProductName(dto.getProductName());
        product.setProductUrl(dto.getProductUrl() != null ? dto.getProductUrl() : "");
        product.setTargetPrice(dto.getTargetPrice());
        product.setCurrentPrice(dto.getTargetPrice());
        product.setAlarmEnabled(true);

        Product savedProduct = productRepository.save(product);

        // 🔥 최초 등록 시 현재가를 기준으로 이력을 하나 남김
        priceHistoryRepository.save(new PriceHistory(savedProduct, dto.getTargetPrice()));
        return savedProduct;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // 🔥 차트용 데이터 포맷팅 로직
    public List<Map<String, Object>> getPriceHistoryFormatted(String productName) {
        Optional<Product> productOpt = productRepository.findByProductName(productName);
        if (productOpt.isEmpty()) {
            return List.of(); // 아직 등록 안된 상품이면 빈 리스트 반환
        }

        List<PriceHistory> histories = priceHistoryRepository.findByProductOrderByCreatedAtAsc(productOpt.get());

        // 날짜를 "04.08 14:00" 형식으로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM.dd HH:mm");

        return histories.stream().map(h -> {
            Map<String, Object> map = new HashMap<>();
            map.put("date", h.getCreatedAt().format(formatter));
            map.put("price", h.getPrice());
            return map;
        }).collect(Collectors.toList());
    }
}