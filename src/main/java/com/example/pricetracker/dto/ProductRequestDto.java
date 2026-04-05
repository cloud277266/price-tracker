package com.example.pricetracker.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRequestDto {
    private String productName;  // 상품명
    private String productUrl;   // 쿠팡 URL
    private int targetPrice;     // 목표 가격
}