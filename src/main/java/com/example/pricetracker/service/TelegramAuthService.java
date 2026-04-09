package com.example.pricetracker.service;

import com.example.pricetracker.entity.Product;
import com.example.pricetracker.repository.ProductRepository; // 🔥 추가
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor // 🔥 추가
public class TelegramAuthService {

    @Value("${telegram.bot.token}")
    private String token;

    private final ProductRepository productRepository; // 🔥 추가
    private final TelegramService telegramService;     // 🔥 추가

    public static final Map<String, String> authStorage = new ConcurrentHashMap<>();
    private long lastUpdateId = 0;

    @Scheduled(fixedDelay = 2000)
    public void checkTelegramMessages() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://api.telegram.org/bot" + token + "/getUpdates?offset=" + (lastUpdateId + 1);
            String response = restTemplate.getForObject(url, String.class);
            JsonNode result = new ObjectMapper().readTree(response).path("result");

            if (result.isArray()) {
                for (JsonNode node : result) {
                    lastUpdateId = node.path("update_id").asLong();
                    JsonNode message = node.path("message");
                    String text = message.path("text").asText("");
                    String chatId = message.path("chat").path("id").asText();

                    // 💡 로그인 딥링크 처리
                    if (text.startsWith("/start ")) {
                        String uuid = text.substring(7).trim();
                        authStorage.put(uuid, chatId);
                        telegramService.sendMessage(chatId, "✅ PriceTracker 연동이 완료되었습니다!\n\n명령어 안내:\n/list - 내 추적 목록 보기");
                    }
                    // 💡 내 추적 목록 불러오기 (양방향 명령어)
                    else if (text.equalsIgnoreCase("/list")) {
                        List<Product> myProducts = productRepository.findByChatId(chatId);
                        if (myProducts.isEmpty()) {
                            telegramService.sendMessage(chatId, "텅~ 🛒\n현재 추적 중인 상품이 없습니다.");
                        } else {
                            StringBuilder sb = new StringBuilder("📋 [내 추적 목록]\n\n");
                            for (Product p : myProducts) {
                                sb.append("🔹 ").append(p.getProductName()).append("\n")
                                        .append("   - 현재: ").append(String.format("%,d", p.getCurrentPrice())).append("원\n")
                                        .append("   - 목표: ").append(String.format("%,d", p.getTargetPrice())).append("원\n\n");
                            }
                            telegramService.sendMessage(chatId, sb.toString());
                        }
                    }
                }
            }
        } catch (Exception e) {}
    }
}