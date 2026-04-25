package com.bookstore.bookstore.repository;

import com.bookstore.bookstore.entity.ReadingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReadingRecordRepository extends JpaRepository<ReadingRecord, Integer> {
    List<ReadingRecord> findByUserIdOrderByLastReadAtDesc(Integer userId);
    Optional<ReadingRecord> findByUserIdAndBookId(Integer userId, Integer bookId);
}
