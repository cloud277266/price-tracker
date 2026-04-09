package com.example.pricetracker.entity;

import com.fasterxml.jackson.annotation.JsonIgnore; // 🔥 무한루프 방지를 위한 필수 import
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

    private String chatId;

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

    // 🔥 핵심 해결책: 프론트엔드에 데이터를 보낼 때 가격 변동 내역(PriceHistory)은
    // JSON 변환 과정에서 제외하여 무한 루프(순환 참조) 에러를 원천 차단합니다!
    @JsonIgnore
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

    public void updatePrice(int newPrice) {
        this.currentPrice = newPrice;
    }
}