package com.example.pricetracker.repository;

import com.example.pricetracker.entity.PriceHistory;
import com.example.pricetracker.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    List<PriceHistory> findByProductOrderByCreatedAtDesc(Product product);

    // 🔥 차트 렌더링을 위해 과거부터 최신순으로 정렬해서 가져오는 기능 추가
    List<PriceHistory> findByProductOrderByCreatedAtAsc(Product product);
}