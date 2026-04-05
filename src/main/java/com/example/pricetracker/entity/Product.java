package com.example.pricetracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productName;   // 상품명
    private String category;      // 카테고리 (노트북/태블릿)
    private String brand;         // 브랜드
    private String imageUrl;      // 상품 이미지

    @Column(length = 500)
    private String productUrl;    // 네이버 쇼핑 링크

    private int targetPrice;      // 목표가격 (알람 기준)
    private int currentPrice;     // 현재 최저가
    private boolean alarmEnabled; // 알람 설정 여부

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 🔥 스케줄러와 이력을 위해 추가된 부분 1: PriceHistory와의 1:N 관계 설정
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<PriceHistory> priceHistories = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 🔥 스케줄러와 이력을 위해 추가된 부분 2: 가격 업데이트 편의 메서드
    public void updatePrice(int newPrice) {
        this.currentPrice = newPrice;
    }
}