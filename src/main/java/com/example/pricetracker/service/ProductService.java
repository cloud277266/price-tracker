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

    public Product addProduct(ProductRequestDto dto) {
        // 🔥 이름뿐만 아니라 내 아이디(chatId)까지 일치해야 내 기존 상품으로 인정
        Optional<Product> existing = productRepository.findByProductNameAndChatId(dto.getProductName(), dto.getChatId());
        if (existing.isPresent()) {
            Product product = existing.get();
            product.setTargetPrice(dto.getTargetPrice());
            product.setAlarmEnabled(true);
            return productRepository.save(product);
        }

        Product product = new Product();
        product.setChatId(dto.getChatId()); // 🔥 DB에 주인 명찰 기록!
        product.setProductName(dto.getProductName());
        product.setProductUrl(dto.getProductUrl() != null ? dto.getProductUrl() : "");
        product.setTargetPrice(dto.getTargetPrice());
        product.setCurrentPrice(dto.getTargetPrice());
        product.setAlarmEnabled(true);

        Product savedProduct = productRepository.save(product);

        priceHistoryRepository.save(new PriceHistory(savedProduct, dto.getTargetPrice()));
        return savedProduct;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Map<String, Object>> getPriceHistoryFormatted(String productName) {
        // 🔥 주의: 차트 표시는 임시로 첫 번째 상품 기준으로 띄웁니다 (추후 고도화 필요)
        Optional<Product> productOpt = productRepository.findByProductNameAndChatId(productName, "임시").or(() -> productRepository.findAll().stream().filter(p -> p.getProductName().equals(productName)).findFirst());

        if (productOpt.isEmpty()) {
            return List.of();
        }

        List<PriceHistory> histories = priceHistoryRepository.findByProductOrderByCreatedAtAsc(productOpt.get());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM.dd HH:mm");

        return histories.stream().map(h -> {
            Map<String, Object> map = new HashMap<>();
            map.put("date", h.getCreatedAt().format(formatter));
            map.put("price", h.getPrice());
            return map;
        }).collect(Collectors.toList());
    }
}