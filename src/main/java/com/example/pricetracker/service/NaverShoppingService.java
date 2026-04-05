package com.example.pricetracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class NaverShoppingService {

    // 설정 파일(환경변수)에서 값을 주입받습니다.
    @Value("${naver.client.id}")
    private String clientId;

    @Value("${naver.client.secret}")
    private String clientSecret;

    public String searchProducts(String keyword, int display) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://openapi.naver.com/v1/search/shop.json?query=" + keyword + "&display=" + display;

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        return response.getBody();
    }

    public int getLowestPrice(String productName) {
        try {
            String jsonResponse = searchProducts(productName, 1);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode items = root.path("items");

            if (items.isArray() && items.size() > 0) {
                String lpriceStr = items.get(0).path("lprice").asText();
                return Integer.parseInt(lpriceStr);
            }
        } catch (Exception e) {
            log.error("네이버 가격 파싱 중 에러 발생: {}", e.getMessage());
        }

        return 0;
    }
}