package com.example.pricetracker.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Member {
    @Id
    private String chatId; // 텔레그램 ID가 곧 회원 PK

    private boolean dndEnabled = false; // 방해금지 모드 켜짐 여부
    private int dndStartHour = 23;      // 시작 시간 (기본 밤 11시)
    private int dndEndHour = 8;         // 종료 시간 (기본 아침 8시)
}