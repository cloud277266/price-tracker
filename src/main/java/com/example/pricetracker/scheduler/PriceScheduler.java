package com.example.pricetracker.scheduler;

import com.example.pricetracker.entity.Member;
import com.example.pricetracker.entity.PriceHistory;
import com.example.pricetracker.entity.Product;
import com.example.pricetracker.repository.MemberRepository;
import com.example.pricetracker.repository.PriceHistoryRepository;
import com.example.pricetracker.repository.ProductRepository;
import com.example.pricetracker.service.NaverShoppingService;
import com.example.pricetracker.service.TelegramService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PriceScheduler {

    private final ProductRepository productRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final NaverShoppingService naverShoppingService;
    private final TelegramService telegramService;
    private final MemberRepository memberRepository;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void updatePrices() {
        log.info("=== 🚀 비동기 정기 가격 업데이트 스케줄러 실행 ===");
        List<Product> productList = productRepository.findAll();

        // 🔥 1. 모든 상품에 대해 비동기 API 요청을 동시에 날립니다! (병렬 처리)
        List<CompletableFuture<Void>> futures = productList.stream()
                .map(product -> naverShoppingService.getLowestPriceAsync(product.getProductName())
                        .thenAccept(latestPrice -> {
                            if (latestPrice > 0) {
                                processProductUpdate(product, latestPrice);
                            }
                        })
                ).collect(Collectors.toList());

        // 🔥 2. 모든 병렬 요청이 끝날 때까지 기다립니다. (1,000개라도 수 초 안에 끝남)
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        log.info("=== ✅ 스케줄러 업데이트 완료 ===");
    }

    // 개별 상품 업데이트 로직 분리 (트랜잭션 및 알림 처리)
    private void processProductUpdate(Product product, int latestPrice) {
        product.updatePrice(latestPrice);
        priceHistoryRepository.save(new PriceHistory(product, latestPrice));
        productRepository.save(product); // DB 반영
        checkTargetPriceAndNotify(product, latestPrice);
    }

    private void checkTargetPriceAndNotify(Product product, int currentPrice) {
        if (product.isAlarmEnabled() && currentPrice <= product.getTargetPrice()) {
            Member member = memberRepository.findById(product.getChatId()).orElse(null);

            // 에티켓 모드 확인
            if (member != null && member.isDndEnabled()) {
                int currentHour = LocalTime.now().getHour();
                boolean isDnd = (member.getDndStartHour() > member.getDndEndHour())
                        ? (currentHour >= member.getDndStartHour() || currentHour < member.getDndEndHour())
                        : (currentHour >= member.getDndStartHour() && currentHour < member.getDndEndHour());

                if (isDnd) {
                    log.info("🌙 에티켓 모드로 알림 보류 (ChatID: {})", member.getChatId());
                    return;
                }
            }

            String message = String.format("🔔 [핫딜 알림]\n상품명: %s\n현재가: %,d원\n목표가: %,d원",
                    product.getProductName(), currentPrice, product.getTargetPrice());
            telegramService.sendMessage(product.getChatId(), message);
            product.setAlarmEnabled(false);
        }
    }
}