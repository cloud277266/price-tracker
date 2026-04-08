package com.example.pricetracker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class TelegramService {

    @Value("${telegram.bot.token}")
    private String token;

    // 🔥 공통 chatId 변수는 완전히 삭제했습니다.

    // 🔥 알림을 받을 사람의 targetChatId를 파라미터로 직접 받아서 보냅니다.
    public void sendMessage(String targetChatId, String message) {
        if (targetChatId == null || targetChatId.trim().isEmpty()) {
            log.warn("수신자(chatId) 정보가 없어서 알림을 보낼 수 없습니다.");
            return;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://api.telegram.org/bot" + token + "/sendMessage";

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("chat_id", targetChatId); // 🔥 수신자 개별 지정
            requestBody.put("text", message);

            restTemplate.postForObject(url, requestBody, String.class);
            log.info("텔레그램 맞춤 알림 전송 완료 - 수신자: {}", targetChatId);
        } catch (Exception e) {
            log.error("텔레그램 전송 에러: {}", e.getMessage());
        }
    }
}