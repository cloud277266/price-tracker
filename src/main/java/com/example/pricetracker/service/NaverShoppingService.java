package com.example.pricetracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class NaverShoppingService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String clientId;
    private final String clientSecret;

    // 🔥 1. WebClient.Builder 에러 해결: 스프링에게 주입받지 않고 내부에서 직접 안전하게 생성
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

    // 💡 2. 동기식 단건 최저가 검색 (상품 상세 및 테스트용)
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

    // 🔥 3. 포트폴리오의 핵심! 자체 필터링 알고리즘 복구 (쓰레기 데이터 제거 및 재정렬)
    public String searchProducts(String keyword, int display, int page, String sort) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Naver-Client-Id", clientId);
            headers.set("X-Naver-Client-Secret", clientSecret);

            // [STEP 1] 네이버 API 호출: 무조건 인기순(sim)으로 50개를 넉넉히 가져옴 (케이스 등 필터링 목적)
            int fetchSize = 50;
            URI uri = UriComponentsBuilder
                    .fromUriString("https://openapi.naver.com/v1/search/shop.json")
                    .queryParam("query", keyword)
                    .queryParam("display", fetchSize)
                    .queryParam("start", 1)
                    .queryParam("sort", "sim") // 사용자의 선택과 무관하게 일단 인기순 호출!
                    .build().encode().toUri();

            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode itemsNode = rootNode.path("items");

            if (!itemsNode.isArray() || itemsNode.size() == 0) {
                return "{\"items\":[]}";
            }

            // [STEP 2] 쓰레기 데이터(1000원 이하) 1차 제거 및 평균가 계산
            List<ObjectNode> itemList = new ArrayList<>();
            long totalPrice = 0;
            int validCount = 0;

            for (JsonNode node : itemsNode) {
                ObjectNode obj = (ObjectNode) node;
                int price = obj.path("lprice").asInt();
                if (price > 1000) {
                    itemList.add(obj);
                    totalPrice += price;
                    validCount++;
                }
            }

            if (validCount == 0) return "{\"items\":[]}";

            int avgPrice = (int) (totalPrice / validCount);

            // [STEP 3] 평균가 대비 할인 금액 및 할인율 계산
            for (ObjectNode item : itemList) {
                int lprice = item.path("lprice").asInt();
                int discountRate = 0;
                int discountAmount = 0;

                if (lprice < avgPrice) { // 평균보다 쌀 때만 핫딜로 판정
                    discountAmount = avgPrice - lprice;
                    discountRate = (int) Math.round(((double) discountAmount / avgPrice) * 100);
                }

                item.put("discountRate", discountRate);
                item.put("discountAmount", discountAmount);
            }

            // [STEP 4] 프론트엔드가 요청한 정렬 방식에 맞춰 메모리 내에서 재정렬
            if ("asc".equals(sort)) {
                // 저점기준 선택 시: 가격이 저렴한 순서대로 오름차순 정렬
                itemList.sort(Comparator.comparingInt(a -> a.path("lprice").asInt()));
            }
            // 'sim(인기순)' 선택 시 이미 인기순으로 가져왔으므로 정렬 유지

            // [STEP 5] 페이징 처리: 정렬된 50개 중 화면에 보여줄 10개만 자르기
            int startIndex = (page - 1) * display;
            int endIndex = Math.min(startIndex + display, itemList.size());

            List<ObjectNode> pagedList = new ArrayList<>();
            if (startIndex < itemList.size()) {
                pagedList = itemList.subList(startIndex, endIndex);
            }

            // [STEP 6] 결과 반환
            ObjectNode resultNode = objectMapper.createObjectNode();
            ArrayNode jsonArray = objectMapper.createArrayNode();
            jsonArray.addAll(pagedList);
            resultNode.set("items", jsonArray);

            return objectMapper.writeValueAsString(resultNode);

        } catch (Exception e) {
            log.error("상품 검색 가공 에러: {}", e.getMessage());
            return "{\"items\":[]}";
        }
    }

    // 🚀 4. 스케줄러 성능 최적화: WebClient를 활용한 비동기 병렬 호출
    public CompletableFuture<Integer> getLowestPriceAsync(String productName) {

        // 🔥 URI 객체를 밖에서 만들지 않고, WebClient 내부의 uriBuilder를 사용해야 baseUrl이 유지됩니다!
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/search/shop.json")
                        .queryParam("query", productName)
                        .queryParam("display", 1)
                        .queryParam("start", 1)
                        .queryParam("sort", "asc")
                        .build())
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