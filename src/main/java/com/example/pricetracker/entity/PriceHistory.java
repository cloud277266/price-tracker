package com.example.pricetracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Product 엔티티와 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer price;
    private LocalDateTime createdAt;

    // 객체 생성을 위한 생성자
    public PriceHistory(Product product, Integer price) {
        this.product = product;
        this.price = price;
        this.createdAt = LocalDateTime.now();
    }
}