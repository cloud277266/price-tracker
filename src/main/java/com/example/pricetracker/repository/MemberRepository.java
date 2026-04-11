package com.example.pricetracker.repository;

import com.example.pricetracker.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, String> {
}