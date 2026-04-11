package com.example.pricetracker.controller;

import com.example.pricetracker.entity.Member;
import com.example.pricetracker.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

    @GetMapping("/{chatId}")
    public ResponseEntity<Member> getSettings(@PathVariable String chatId) {
        Member member = memberRepository.findById(chatId).orElseGet(() -> {
            Member newMember = new Member();
            newMember.setChatId(chatId);
            return memberRepository.save(newMember); // 없으면 기본값으로 생성
        });
        return ResponseEntity.ok(member);
    }

    @PostMapping("/{chatId}")
    public ResponseEntity<Member> updateSettings(@PathVariable String chatId, @RequestBody Member updated) {
        Member member = memberRepository.findById(chatId).orElse(new Member());
        member.setChatId(chatId);
        member.setDndEnabled(updated.isDndEnabled());
        member.setDndStartHour(updated.getDndStartHour());
        member.setDndEndHour(updated.getDndEndHour());
        return ResponseEntity.ok(memberRepository.save(member));
    }
}