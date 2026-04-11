package com.example.pricetracker.controller;

import com.example.pricetracker.entity.NotificationHistory;
import com.example.pricetracker.repository.NotificationHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationHistoryRepository repo;

    @GetMapping
    public List<NotificationHistory> getHistory(@RequestParam String chatId) {
        return repo.findTop20ByChatIdOrderByCreatedAtDesc(chatId);
    }
}