package com.bookstore.bookstore.repository;

import com.bookstore.bookstore.entity.AdToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdTokenRepository extends JpaRepository<AdToken, Integer> {
    Optional<AdToken> findByToken(String token);
}
