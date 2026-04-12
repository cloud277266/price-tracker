# 🎯 PriceTracker (핫딜 추적 & 텔레그램 알림 서비스)

![PriceTracker Logo](https://img.shields.io/badge/Price-Tracker-blue?style=for-the-badge&logo=spring)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_4.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Java 17](https://img.shields.io/badge/Java_17-007396?style=for-the-badge&logo=java&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)

## 📌 프로젝트 소개
**PriceTracker**는 네이버 쇼핑 API를 활용하여 관심 상품의 가격 변동을 추적하고, 사용자가 설정한 목표가에 도달하면 **텔레그램(Telegram)으로 실시간 푸시 알림**을 보내주는 웹 서비스입니다.
수많은 상품 데이터 속에서 의미 없는 낚시상품(보호필름, 케이스 등)를 걸러내는 **자체 필터링 알고리즘**과, 다수의 알림을 지연 없이 처리하기 위한 **비동기 스케줄러 최적화**에 집중하여 개발했습니다.

## ✨ 핵심 기능 (Key Features)
- **자체 데이터 필터링:** 무분별한 최저가 데이터 속에서 진짜 상품을 걸러내고 평균가 대비 '할인율'을 계산해 주는 알고리즘 적용
- **Passwordless 텔레그램 인증:** 번거로운 회원가입 없이 Telegram 딥링크를 활용한 1초 연동 및 양방향 봇 커뮤니케이션(`/list` 명령어)
- **에티켓(방해금지) 모드:** 심야 시간대 수면을 방해하지 않도록, 유저가 설정한 시간에는 목표가에 도달해도 알림 발송을 보류하는 개인화 설정 지원
- **알림 히스토리 보관함:** 웹사이트 내에서 과거의 핫딜 도달 알림 내역을 슬라이드 패널로 직관적으로 확인
- **데이터 시각화 및 스마트 구매 제안:** Chart.js를 활용한 과거 가격 추이 그래프 제공 및 데이터(평균가 vs 현재가) 기반의 '구매 타이밍(Wait or Buy)' 자동 추천 알고리즘 적용
---

## 🏗 시스템 아키텍처 (System Architecture)
```mermaid
graph TD
    A[Client Browser] -->|Search & Track| B(Spring Boot Controller)
    B --> C{Product Service}
    C -->|Async WebClient| D[Naver Shopping API]
    C --> E[(MySQL DB)]
    
    F[Price Scheduler] -->|Every 1 Min| D
    F -->|Target Price Hit| G[Telegram Auth Service]
    G -->|Push Notification| H[Telegram Bot / User]
