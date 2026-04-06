package com.example.pricetracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
public class NaverShoppingService {

    // 설정 파일(환경변수)에서 값을 주입받습니다.
    @Value("${naver.client.id}")
    private String clientId;

    @Value("${naver.client.secret}")
    private String clientSecret;



    public String searchProducts(String keyword, int display, int start, String sort) {
        RestTemplate restTemplate = new RestTemplate();

        // URL을 안전하게 만들어주는 UriComponentsBuilder 사용
        String url = UriComponentsBuilder.fromHttpUrl("https://openapi.naver.com/v1/search/shop.json")
                .queryParam("query", keyword)
                .queryParam("display", display)
                .queryParam("start", start)
                .queryParam("sort", sort)
                .encode()
                .toUriString();

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
            // 스케줄러가 최저가를 찾을 때는 1페이지(start=1), 저가순(sort="asc")으로 검색해야 가장 정확합니다.
            String jsonResponse = searchProducts(productName, 1, 1, "asc");

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