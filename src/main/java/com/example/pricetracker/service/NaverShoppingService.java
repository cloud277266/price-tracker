package com.example.pricetracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class NaverShoppingService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String clientId;
    private final String clientSecret;

    // 🔥 1. 에러의 주범이었던 WebClient.Builder 주입을 완전히 제거하고, 내부에서 직접 생성합니다!
    public NaverShoppingService(@Value("${naver.client.id}") String clientId,
                                @Value("${naver.client.secret}") String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.objectMapper = new ObjectMapper();

        this.webClient = WebClient.builder()
                .baseUrl("https://openapi.naver.com")
                .defaultHeader("X-Naver-Client-Id", clientId)
                .defaultHeader("X-Naver-Client-Secret", clientSecret)
                .build();
    }

    // 💡 2. 삭제되어 컴파일 에러를 냈던 기존 '동기식' 최저가 검색 복구 (Controller용)
    public int getLowestPrice(String productName) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Naver-Client-Id", clientId);
            headers.set("X-Naver-Client-Secret", clientSecret);

            URI uri = UriComponentsBuilder
                    .fromUriString("https://openapi.naver.com/v1/search/shop.json")
                    .queryParam("query", productName)
                    .queryParam("display", 1)
                    .queryParam("sort", "asc")
                    .build().encode().toUri();

            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode items = root.path("items");
            if (items.isArray() && items.size() > 0) {
                return items.get(0).path("lprice").asInt();
            }
        } catch (Exception e) {
            log.error("동기 최저가 검색 에러: {}", e.getMessage());
        }
        return 0;
    }

    // 💡 3. 먹통이 될 뻔했던 기존 상품 목록 검색 로직 완벽 복구 (프론트엔드 검색창용)
    public String searchProducts(String keyword, int display, int page, String sort) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Naver-Client-Id", clientId);
            headers.set("X-Naver-Client-Secret", clientSecret);

            int start = ((page - 1) * display) + 1;
            URI uri = UriComponentsBuilder
                    .fromUriString("https://openapi.naver.com/v1/search/shop.json")
                    .queryParam("query", keyword)
                    .queryParam("display", display)
                    .queryParam("start", start)
                    .queryParam("sort", sort)
                    .build().encode().toUri();

            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("상품 검색 에러: {}", e.getMessage());
            return "{\"items\":[]}";
        }
    }

    // 🔥 4. 포트폴리오의 핵심! 스케줄러 성능을 끌어올릴 '비동기' 최저가 검색 (WebClient)
    public CompletableFuture<Integer> getLowestPriceAsync(String productName) {
        URI uri = UriComponentsBuilder
                .fromPath("/v1/search/shop.json")
                .queryParam("query", productName)
                .queryParam("display", 1)
                .queryParam("start", 1)
                .queryParam("sort", "asc")
                .build().encode().toUri();

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    try {
                        JsonNode rootNode = objectMapper.readTree(response);
                        JsonNode itemsNode = rootNode.path("items");
                        if (itemsNode.isArray() && itemsNode.size() > 0) {
                            return itemsNode.get(0).path("lprice").asInt();
                        }
                    } catch (Exception e) {
                        log.error("비동기 파싱 에러: {}", e.getMessage());
                    }
                    return 0;
                })
                .toFuture();
    }
}