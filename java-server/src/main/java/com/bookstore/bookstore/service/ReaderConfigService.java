package com.bookstore.bookstore.service;

import com.bookstore.bookstore.dto.ReaderConfigRequest;
import com.bookstore.bookstore.entity.ReaderConfig;
import com.bookstore.bookstore.repository.ReaderConfigRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ReaderConfigService {

    private final ReaderConfigRepository readerConfigRepository;

    public ReaderConfigService(ReaderConfigRepository readerConfigRepository) {
        this.readerConfigRepository = readerConfigRepository;
    }

    public Optional<ReaderConfig> getConfig(Integer userId) {
        return readerConfigRepository.findByUserId(userId);
    }

    public ReaderConfig saveConfig(Integer userId, ReaderConfigRequest req) {
        ReaderConfig config = readerConfigRepository.findByUserId(userId).orElse(null);
        if (config == null) {
            config = new ReaderConfig();
            config.setUserId(userId);
        }
        if (req.getFontSize() != null) config.setFontSize(req.getFontSize());
        if (req.getLineHeight() != null) config.setLineHeight(req.getLineHeight());
        if (req.getTheme() != null) config.setTheme(req.getTheme());
        if (req.getBrightness() != null) config.setBrightness(req.getBrightness());
        if (req.getParagraphSpacing() != null) config.setParagraphSpacing(req.getParagraphSpacing());
        if (req.getFontFamily() != null) config.setFontFamily(req.getFontFamily());
        if (req.getPagingMode() != null) config.setPagingMode(req.getPagingMode());
        return readerConfigRepository.save(config);
    }
}
