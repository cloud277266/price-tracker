package com.example.pricetracker.scheduler;

import com.example.pricetracker.entity.Product;
import com.example.pricetracker.entity.PriceHistory;
import com.example.pricetracker.repository.ProductRepository;
import com.example.pricetracker.repository.PriceHistoryRepository;
import com.example.pricetracker.service.NaverShoppingService;
import com.example.pricetracker.service.TelegramService; // 추가

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PriceScheduler {

    private final ProductRepository productRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final NaverShoppingService naverShoppingService;
    private final TelegramService telegramService; // 추가

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void updatePrices() {
        log.info("=== 정기 가격 업데이트 스케줄러 실행 ===");
        List<Product> productList = productRepository.findAll();

        for (Product product : productList) {
            try {
                int latestPrice = naverShoppingService.getLowestPrice(product.getProductName());

                product.updatePrice(latestPrice);
                PriceHistory history = new PriceHistory(product, latestPrice);
                priceHistoryRepository.save(history);

                log.info("상품명: {}, 최신가격: {}", product.getProductName(), latestPrice);

                checkTargetPriceAndNotify(product, latestPrice);

            } catch (Exception e) {
                log.error("상품명 [{}] 업데이트 중 에러: {}", product.getProductName(), e.getMessage());
            }
        }
    }

    private void checkTargetPriceAndNotify(Product product, int currentPrice) {
        if (product.isAlarmEnabled() && currentPrice <= product.getTargetPrice()) {
            String message = String.format(
                    "🔔 [가격 도달 알림]\n상품명: %s\n현재가: %,d원\n목표가: %,d원\n바로가기: %s",
                    product.getProductName(), currentPrice, product.getTargetPrice(), product.getProductUrl()
            );

            // 🔥 실제로 텔레그램 메시지 전송
            telegramService.sendMessage(message);
        }
    }
}