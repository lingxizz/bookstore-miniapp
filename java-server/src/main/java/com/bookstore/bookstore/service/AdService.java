package com.bookstore.bookstore.service;

import com.bookstore.bookstore.entity.AdDailyLimit;
import com.bookstore.bookstore.entity.AdToken;
import com.bookstore.bookstore.entity.UnlockedChapter;
import com.bookstore.bookstore.repository.AdDailyLimitRepository;
import com.bookstore.bookstore.repository.AdTokenRepository;
import com.bookstore.bookstore.repository.UnlockedChapterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AdService {

    private final AdTokenRepository adTokenRepository;
    private final AdDailyLimitRepository adDailyLimitRepository;
    private final UnlockedChapterRepository unlockedChapterRepository;

    public AdService(AdTokenRepository adTokenRepository,
                     AdDailyLimitRepository adDailyLimitRepository,
                     UnlockedChapterRepository unlockedChapterRepository) {
        this.adTokenRepository = adTokenRepository;
        this.adDailyLimitRepository = adDailyLimitRepository;
        this.unlockedChapterRepository = unlockedChapterRepository;
    }

    @Transactional
    public String generateAdToken(Integer userId) {
        String today = LocalDateTime.now().toLocalDate().toString();
        AdDailyLimit limit = adDailyLimitRepository.findByUserIdAndDate(userId, today).orElse(null);
        if (limit != null && limit.getCount() >= 10) {
            return null;
        }

        String token = UUID.randomUUID().toString();
        AdToken adToken = new AdToken();
        adToken.setToken(token);
        adToken.setUserId(userId);
        adToken.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        adTokenRepository.save(adToken);
        return token;
    }

    @Transactional
    public boolean verifyAdToken(Integer userId, Integer chapterId, String tokenStr) {
        AdToken token = adTokenRepository.findByToken(tokenStr).orElse(null);
        if (token == null || token.getUsed() || !token.getUserId().equals(userId) || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        String today = LocalDateTime.now().toLocalDate().toString();
        AdDailyLimit limit = adDailyLimitRepository.findByUserIdAndDate(userId, today).orElse(null);
        if (limit == null) {
            limit = new AdDailyLimit();
            limit.setUserId(userId);
            limit.setDate(today);
            limit.setCount(1);
        } else {
            limit.setCount(limit.getCount() + 1);
        }
        adDailyLimitRepository.save(limit);

        token.setUsed(true);
        adTokenRepository.save(token);

        if (unlockedChapterRepository.findByUserIdAndChapterId(userId, chapterId).isEmpty()) {
            UnlockedChapter uc = new UnlockedChapter();
            uc.setUserId(userId);
            uc.setChapterId(chapterId);
            uc.setUnlockType("ad");
            unlockedChapterRepository.save(uc);
        }
        return true;
    }
}
