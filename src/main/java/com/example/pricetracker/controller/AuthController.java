package com.example.pricetracker.controller;

import com.example.pricetracker.service.TelegramAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/check")
    public ResponseEntity<?> checkAuth(@RequestParam String uuid) {
        // 임시 저장소에 이 UUID로 등록된 Chat ID가 있는지 확인
        String chatId = TelegramAuthService.authStorage.get(uuid);

        if (chatId != null) {
            // 인증 성공! Chat ID를 브라우저로 넘겨줍니다.
            return ResponseEntity.ok(Map.of("chatId", chatId));
        } else {
            // 아직 텔레그램에서 시작 버튼을 누르지 않음
            return ResponseEntity.status(401).body("대기 중...");
        }
    }
}