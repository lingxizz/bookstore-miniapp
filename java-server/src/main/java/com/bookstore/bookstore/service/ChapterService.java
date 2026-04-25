package com.bookstore.bookstore.service;

import com.bookstore.bookstore.entity.Chapter;
import com.bookstore.bookstore.entity.ChapterRead;
import com.bookstore.bookstore.entity.UnlockedChapter;
import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.repository.ChapterReadRepository;
import com.bookstore.bookstore.repository.ChapterRepository;
import com.bookstore.bookstore.repository.UnlockedChapterRepository;
import com.bookstore.bookstore.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ChapterService {

    private final ChapterRepository chapterRepository;
    private final UnlockedChapterRepository unlockedChapterRepository;
    private final UserRepository userRepository;
    private final ChapterReadRepository chapterReadRepository;

    public ChapterService(ChapterRepository chapterRepository,
                          UnlockedChapterRepository unlockedChapterRepository,
                          UserRepository userRepository,
                          ChapterReadRepository chapterReadRepository) {
        this.chapterRepository = chapterRepository;
        this.unlockedChapterRepository = unlockedChapterRepository;
        this.userRepository = userRepository;
        this.chapterReadRepository = chapterReadRepository;
    }

    public List<Chapter> getChaptersByBook(Integer bookId) {
        return chapterRepository.findByBookIdOrderByOrderAsc(bookId);
    }

    public Optional<Chapter> getChapter(Integer id) {
        return chapterRepository.findById(id);
    }

    public boolean isUnlocked(Integer userId, Integer chapterId) {
        return unlockedChapterRepository.findByUserIdAndChapterId(userId, chapterId).isPresent();
    }

    @Transactional
    public boolean unlockChapter(Integer userId, Integer chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId).orElse(null);
        if (chapter == null) return false;
        if (chapter.getIsFree()) return true;

        if (unlockedChapterRepository.findByUserIdAndChapterId(userId, chapterId).isPresent()) {
            return true;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getCoins() < chapter.getPrice()) {
            return false;
        }

        user.setCoins(user.getCoins() - chapter.getPrice());
        userRepository.save(user);

        UnlockedChapter uc = new UnlockedChapter();
        uc.setUserId(userId);
        uc.setChapterId(chapterId);
        unlockedChapterRepository.save(uc);
        return true;
    }

    @Transactional
    public void markChapterRead(Integer userId, Integer chapterId) {
        if (chapterReadRepository.findByUserIdAndChapterId(userId, chapterId).isPresent()) {
            return;
        }
        ChapterRead cr = new ChapterRead();
        cr.setUserId(userId);
        cr.setChapterId(chapterId);
        chapterReadRepository.save(cr);
    }

    public List<Integer> getReadChapterIds(Integer userId, Integer bookId) {
        return chapterReadRepository.findChapterIdsByUserIdAndBookId(userId, bookId);
    }
}
