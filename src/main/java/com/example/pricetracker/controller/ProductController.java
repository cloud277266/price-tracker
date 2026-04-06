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

    @PostMapping
    public ResponseEntity<Product> addProduct(@RequestBody ProductRequestDto dto) {
        Product saved = productService.addProduct(dto);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/price-test")
    public ResponseEntity<String> priceTest(@RequestParam String productName) {
        int price = naverShoppingService.getLowestPrice(productName);
        return ResponseEntity.ok("최저가: " + price + "원");
    }

    @GetMapping("/search")
    public ResponseEntity<String> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int display,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "asc") String sort) {

        // 변경점: start 파라미터 계산을 서비스 단으로 넘기고, page 번호를 그대로 전달합니다.
        String result = naverShoppingService.searchProducts(keyword, display, page, sort);
        return ResponseEntity.ok(result);
    }
}