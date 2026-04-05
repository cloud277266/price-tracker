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

    @Value("${telegram.chat.id}")
    private String chatId;

    public void sendMessage(String message) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // 텔레그램 API URL (메시지 내용을 URL 뒤에 붙이지 않습니다)
            String url = "https://api.telegram.org/bot" + token + "/sendMessage";

            // 메시지를 안전한 보따리(Map)에 담습니다.
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("chat_id", chatId);
            requestBody.put("text", message);

            // POST 방식으로 보따리를 통째로 전송합니다.
            restTemplate.postForObject(url, requestBody, String.class);

            log.info("텔레그램 알림 전송 완료 (한글 깨짐 해결!)");
        } catch (Exception e) {
            log.error("텔레그램 알림 전송 중 에러 발생: {}", e.getMessage());
        }
    }
}