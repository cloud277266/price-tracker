package com.example.pricetracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NaverShoppingService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;


    public NaverShoppingService(@Value("${naver.client.id}") String clientId,
                                @Value("${naver.client.secret}") String clientSecret) {
        this.webClient = WebClient.builder() // 👈 여기가 핵심입니다. 직접 빌더를 생성합니다.
                .baseUrl("https://openapi.naver.com")
                .defaultHeader("X-Naver-Client-Id", clientId)
                .defaultHeader("X-Naver-Client-Secret", clientSecret)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    // 💡 기존의 동기식 검색 로직 (프론트엔드 호출용은 그대로 유지)
    public String searchProducts(String keyword, int display, int page, String sort) {
        // ... (기존 searchProducts 로직 그대로 유지)
        return "{\"items\":[]}"; // 에러 방지용 임시 처리 (실제 코드는 기존 로직을 유지하세요)
    }

    // 🔥 스케줄러를 위한 '비동기(Asynchronous)' 최저가 검색 메서드 추가!
    public CompletableFuture<Integer> getLowestPriceAsync(String productName) {
        URI uri = UriComponentsBuilder
                .fromPath("/v1/search/shop.json")
                .queryParam("query", productName)
                .queryParam("display", 1)
                .queryParam("start", 1)
                .queryParam("sort", "asc")
                .build().encode().toUri();

        // 대기하지 않고(Non-blocking) 네이버에 요청을 던진 후, 결과가 오면 Parsing 하도록 예약(Future)합니다.
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
                        log.error("가격 파싱 에러: {}", e.getMessage());
                    }
                    return 0; // 실패 시 0 반환
                })
                .toFuture(); // 💡 Java 표준 비동기 객체로 변환
    }
}