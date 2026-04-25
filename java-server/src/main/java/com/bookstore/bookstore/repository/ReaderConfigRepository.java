package com.bookstore.bookstore.repository;

import com.bookstore.bookstore.entity.ReaderConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReaderConfigRepository extends JpaRepository<ReaderConfig, Integer> {
    Optional<ReaderConfig> findByUserId(Integer userId);
}
