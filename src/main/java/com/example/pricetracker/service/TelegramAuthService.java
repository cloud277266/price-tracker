package com.example.pricetracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class TelegramAuthService {

    @Value("${telegram.bot.token}")
    private String token;

    // 🔥 핵심: 브라우저가 생성한 UUID와 실제 텔레그램 Chat ID를 짝지어두는 임시 저장소
    public static final Map<String, String> authStorage = new ConcurrentHashMap<>();
    private long lastUpdateId = 0;

    @Scheduled(fixedDelay = 2000) // 2초마다 텔레그램 메시지 확인
    public void checkTelegramMessages() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            // 최신 메시지만 가져오기 위해 offset 사용
            String url = "https://api.telegram.org/bot" + token + "/getUpdates?offset=" + (lastUpdateId + 1);

            String response = restTemplate.getForObject(url, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            JsonNode result = root.path("result");

            if (result.isArray()) {
                for (JsonNode node : result) {
                    lastUpdateId = node.path("update_id").asLong();
                    JsonNode message = node.path("message");
                    String text = message.path("text").asText("");

                    // 💡 누군가 딥링크(예: /start 7a8b9c)를 클릭해서 봇방에 들어온 경우!
                    if (text.startsWith("/start ")) {
                        String uuid = text.substring(7).trim(); // "7a8b9c" 추출
                        String chatId = message.path("chat").path("id").asText();

                        // 저장소에 매핑 완료
                        authStorage.put(uuid, chatId);
                        log.info("🎉 텔레그램 인증 성공! UUID: {}, ChatID: {}", uuid, chatId);
                    }
                }
            }
        } catch (Exception e) {
            // 로깅 생략 (에러 시 무시하고 다음 주기에 재시도)
        }
    }
}