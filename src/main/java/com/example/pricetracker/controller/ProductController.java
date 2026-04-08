package com.example.pricetracker.controller;

import com.example.pricetracker.dto.ProductRequestDto;
import com.example.pricetracker.entity.Product;
import com.example.pricetracker.service.NaverShoppingService;
import com.example.pricetracker.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final NaverShoppingService naverShoppingService;

    @PostMapping
    public ResponseEntity<Product> addProduct(@RequestBody ProductRequestDto dto) {
        return ResponseEntity.ok(productService.addProduct(dto));
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
        return ResponseEntity.ok(naverShoppingService.searchProducts(keyword, display, page, sort));
    }

    // 🔥 프론트엔드 차트용 데이터 제공 API
    @GetMapping("/history")
    public ResponseEntity<List<Map<String, Object>>> getProductHistory(@RequestParam String productName) {
        List<Map<String, Object>> historyData = productService.getPriceHistoryFormatted(productName);
        return ResponseEntity.ok(historyData);
    }
}