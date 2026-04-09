package com.example.pricetracker.scheduler;

import com.example.pricetracker.entity.Member;
import com.example.pricetracker.entity.Product;
import com.example.pricetracker.entity.PriceHistory;
import com.example.pricetracker.repository.MemberRepository; // 🔥 추가
import com.example.pricetracker.repository.ProductRepository;
import com.example.pricetracker.repository.PriceHistoryRepository;
import com.example.pricetracker.service.NaverShoppingService;
import com.example.pricetracker.service.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PriceScheduler {

    private final ProductRepository productRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final NaverShoppingService naverShoppingService;
    private final TelegramService telegramService;
    private final MemberRepository memberRepository; // 🔥 추가

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void updatePrices() {
        List<Product> productList = productRepository.findAll();
        for (Product product : productList) {
            try {
                int latestPrice = naverShoppingService.getLowestPrice(product.getProductName());
                product.updatePrice(latestPrice);
                priceHistoryRepository.save(new PriceHistory(product, latestPrice));
                checkTargetPriceAndNotify(product, latestPrice);
            } catch (Exception e) {}
        }
    }

    private void checkTargetPriceAndNotify(Product product, int currentPrice) {
        if (product.isAlarmEnabled() && currentPrice <= product.getTargetPrice()) {

            // 🔥 에티켓(방해금지) 모드 체크
            Member member = memberRepository.findById(product.getChatId()).orElse(null);
            if (member != null && member.isDndEnabled()) {
                int currentHour = LocalTime.now().getHour();
                boolean isDnd = false;
                if (member.getDndStartHour() > member.getDndEndHour()) {
                    isDnd = currentHour >= member.getDndStartHour() || currentHour < member.getDndEndHour();
                } else {
                    isDnd = currentHour >= member.getDndStartHour() && currentHour < member.getDndEndHour();
                }

                if (isDnd) {
                    log.info("🌙 에티켓 모드로 인해 알림 보류 (ChatID: {})", member.getChatId());
                    return; // 알림 쏘지 않고 종료 (다음 1분 뒤에 다시 체크함)
                }
            }

            String message = String.format("🔔 [가격 도달 알림]\n상품명: %s\n현재가: %,d원\n목표가: %,d원", product.getProductName(), currentPrice, product.getTargetPrice());
            telegramService.sendMessage(product.getChatId(), message);
            product.setAlarmEnabled(false);
        }
    }
}