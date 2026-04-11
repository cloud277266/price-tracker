package com.example.pricetracker.repository;

import com.example.pricetracker.entity.NotificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, Long> {
    List<NotificationHistory> findTop20ByChatIdOrderByCreatedAtDesc(String chatId); // 최신 20개만
}