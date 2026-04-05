package com.example.pricetracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling // 백그라운드 자동 실행(스케줄링) 기능을 켭니다.
@SpringBootApplication
public class PriceTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PriceTrackerApplication.class, args);
    }

}