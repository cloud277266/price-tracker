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
        Optional<Product> existing = productRepository.findByProductNameAndChatId(dto.getProductName(), dto.getChatId());
        if (existing.isPresent()) {
            Product product = existing.get();
            product.setTargetPrice(dto.getTargetPrice());
            product.setAlarmEnabled(true);
            return productRepository.save(product);
        }

        Product product = new Product();
        product.setChatId(dto.getChatId());
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

    // 🔥 1. 내 아이디(chatId)로 등록된 상품만 모두 가져오기
    public List<Product> getMyProducts(String chatId) {
        return productRepository.findByChatId(chatId);
    }

    // 🔥 2. 내가 등록한 상품 삭제하기 (보안을 위해 chatId도 확인)
    public void deleteProduct(Long id, String chatId) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent() && product.get().getChatId().equals(chatId)) {
            productRepository.delete(product.get());
        }
    }

    public List<Map<String, Object>> getPriceHistoryFormatted(String productName) {
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