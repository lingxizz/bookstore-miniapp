package com.bookstore.bookstore.service;

import com.bookstore.bookstore.entity.ReadingRecord;
import com.bookstore.bookstore.repository.ReadingRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ReadingRecordService {

    private final ReadingRecordRepository readingRecordRepository;

    public ReadingRecordService(ReadingRecordRepository readingRecordRepository) {
        this.readingRecordRepository = readingRecordRepository;
    }

    public List<ReadingRecord> getRecords(Integer userId) {
        return readingRecordRepository.findByUserIdOrderByLastReadAtDesc(userId);
    }

    @Transactional
    public ReadingRecord saveRecord(Integer userId, Integer bookId, Integer chapterId, Double progress, Integer paragraphIndex) {
        ReadingRecord record = readingRecordRepository.findByUserIdAndBookId(userId, bookId).orElse(null);
        if (record == null) {
            record = new ReadingRecord();
            record.setUserId(userId);
            record.setBookId(bookId);
        }
        if (chapterId != null) {
            record.setChapterId(chapterId);
        }
        if (progress != null) {
            record.setProgress(progress);
        }
        if (paragraphIndex != null) {
            record.setParagraphIndex(paragraphIndex);
        }
        return readingRecordRepository.save(record);
    }

    public Optional<ReadingRecord> getProgress(Integer userId, Integer bookId) {
        return readingRecordRepository.findByUserIdAndBookId(userId, bookId);
    }
}
