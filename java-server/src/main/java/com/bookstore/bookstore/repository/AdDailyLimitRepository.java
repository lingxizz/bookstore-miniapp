package com.bookstore.bookstore.repository;

import com.bookstore.bookstore.entity.AdDailyLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdDailyLimitRepository extends JpaRepository<AdDailyLimit, Integer> {
    Optional<AdDailyLimit> findByUserIdAndDate(Integer userId, String date);
}
