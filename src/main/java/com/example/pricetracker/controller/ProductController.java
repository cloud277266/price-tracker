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

    // 🔥 1. 내 추적 목록 가져오기 API
    @GetMapping("/my")
    public ResponseEntity<List<Product>> getMyProducts(@RequestParam String chatId) {
        return ResponseEntity.ok(productService.getMyProducts(chatId));
    }

    // 🔥 2. 추적 상품 삭제 API
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id, @RequestParam String chatId) {
        productService.deleteProduct(id, chatId);
        return ResponseEntity.ok().build();
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

    @GetMapping("/history")
    public ResponseEntity<List<Map<String, Object>>> getProductHistory(@RequestParam String productName) {
        List<Map<String, Object>> historyData = productService.getPriceHistoryFormatted(productName);
        return ResponseEntity.ok(historyData);
    }
}