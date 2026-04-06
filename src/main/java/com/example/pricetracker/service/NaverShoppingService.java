package com.example.pricetracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NaverShoppingService {

    @Value("${naver.client.id}")
    private String clientId;

    @Value("${naver.client.secret}")
    private String clientSecret;

    public String searchProducts(String keyword, int display, int page, String sort) {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Naver-Client-Id", clientId);
            headers.set("X-Naver-Client-Secret", clientSecret);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            if ("asc".equals(sort)) {
                URI uri = UriComponentsBuilder
                        .fromUriString("https://openapi.naver.com/v1/search/shop.json")
                        .queryParam("query", keyword)
                        .queryParam("display", 50)
                        .queryParam("start", 1)
                        .queryParam("sort", "sim")
                        .build()
                        .encode()
                        .toUri();

                ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode itemsNode = rootNode.path("items");

                List<JsonNode> productList = new ArrayList<>();
                long totalPrice = 0;

                if (itemsNode.isArray()) {
                    for (JsonNode itemNode : itemsNode) {
                        productList.add(itemNode);
                        totalPrice += itemNode.path("lprice").asLong();
                    }
                }

                long averagePrice = productList.isEmpty() ? 0 : totalPrice / productList.size();
                long minPriceThreshold = (long) (averagePrice * 0.3);

                List<JsonNode> filteredList = productList.stream()
                        .filter(itemNode -> itemNode.path("lprice").asLong() >= minPriceThreshold)
                        .sorted(Comparator.comparingLong(itemNode -> itemNode.path("lprice").asLong()))
                        .collect(Collectors.toList());

                int fromIndex = (page - 1) * display;
                int toIndex = Math.min(fromIndex + display, filteredList.size());

                List<JsonNode> pagedList = new ArrayList<>();
                if (fromIndex < filteredList.size()) {
                    pagedList = filteredList.subList(fromIndex, toIndex);
                }

                // 🔥 핵심 로직: 프론트엔드로 전달할 할인율/할인액 주입
                for (JsonNode node : pagedList) {
                    ObjectNode objNode = (ObjectNode) node;
                    long lprice = objNode.path("lprice").asLong();
                    long discountAmount = averagePrice - lprice;

                    if (discountAmount > 0 && averagePrice > 0) {
                        // 평균가보다 저렴한 경우 할인율 계산
                        int discountRate = (int) Math.round((discountAmount / (double) averagePrice) * 100);
                        objNode.put("discountRate", discountRate);
                        objNode.put("discountAmount", discountAmount);
                    } else {
                        // 비싸거나 같으면 0 처리
                        objNode.put("discountRate", 0);
                        objNode.put("discountAmount", 0);
                    }
                }

                ObjectNode newRootNode = objectMapper.createObjectNode();
                newRootNode.put("total", filteredList.size());

                ArrayNode newItemsArray = objectMapper.createArrayNode();
                newItemsArray.addAll(pagedList);
                newRootNode.set("items", newItemsArray);

                return objectMapper.writeValueAsString(newRootNode);

            } else {
                int start = (page - 1) * display + 1;
                URI uri = UriComponentsBuilder
                        .fromUriString("https://openapi.naver.com/v1/search/shop.json")
                        .queryParam("query", keyword)
                        .queryParam("display", display)
                        .queryParam("start", start)
                        .queryParam("sort", sort)
                        .build()
                        .encode()
                        .toUri();

                ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
                return response.getBody();
            }

        } catch (Exception e) {
            log.error("네이버 검색 API 호출 중 에러 발생: {}", e.getMessage());
            return "{\"items\":[]}";
        }
    }

    public int getLowestPrice(String productName) {
        try {
            String jsonResponse = searchProducts(productName, 1, 1, "asc");

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode itemsNode = rootNode.path("items");

            if (itemsNode.isArray() && itemsNode.size() > 0) {
                String lpriceStr = itemsNode.get(0).path("lprice").asText();
                return Integer.parseInt(lpriceStr);
            }
        } catch (Exception e) {
            log.error("네이버 가격 파싱 중 에러 발생: {}", e.getMessage());
        }
        return 0;
    }
}