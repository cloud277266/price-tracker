package com.example.pricetracker.repository;

import com.example.pricetracker.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // 🔥 특정 유저가 이 상품을 이미 추적 중인지 확인
    Optional<Product> findByProductNameAndChatId(String productName, String chatId);

    // 🔥 추후 '내 추적 목록' 화면에서 쓸 내 상품만 다 가져오기 기능
    List<Product> findByChatId(String chatId);
}