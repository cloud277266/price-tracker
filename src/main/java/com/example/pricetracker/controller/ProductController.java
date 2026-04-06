package com.example.pricetracker.controller;

import com.example.pricetracker.dto.ProductRequestDto;
import com.example.pricetracker.entity.Product;
import com.example.pricetracker.service.NaverShoppingService;
import com.example.pricetracker.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final NaverShoppingService naverShoppingService;

    // 상품 등록 API
    @PostMapping
    public ResponseEntity<Product> addProduct(@RequestBody ProductRequestDto dto) {
        Product saved = productService.addProduct(dto);
        return ResponseEntity.ok(saved);
    }

    // 상품 전체 조회 API
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    // 네이버 쇼핑 가격 조회 테스트
    @GetMapping("/price-test")
    public ResponseEntity<String> priceTest(@RequestParam String productName) {
        int price = naverShoppingService.getLowestPrice(productName);
        return ResponseEntity.ok("최저가: " + price + "원");
    }
    // 카테고리별 상품 검색
    @GetMapping("/search")
    public ResponseEntity<String> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int display,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "asc") String sort) {

        // 네이버 API 페이징 공식: 1페이지는 1, 2페이지는 11, 3페이지는 21
        int start = (page - 1) * display + 1;

        // 서비스 호출 시 계산된 start와 sort를 넘겨줍니다
        String result = naverShoppingService.searchProducts(keyword, display, start, sort);
        return ResponseEntity.ok(result);
    }


}
