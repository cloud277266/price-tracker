package com.example.pricetracker.repository;

import com.example.pricetracker.entity.PriceHistory;
import com.example.pricetracker.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    // 에러의 원인이었던 CheckedAt을 CreatedAt으로 수정했습니다.
    // (이 메서드는 나중에 화면에 가격 변동 그래프를 그릴 때 최신순으로 데이터를 가져오기 위해 쓰입니다.)
    List<PriceHistory> findByProductOrderByCreatedAtDesc(Product product);

}