package com.bookstore.bookstore.service;

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

    public ReaderConfig saveConfig(Integer userId, Integer fontSize, Integer lineHeight, String theme, Integer brightness) {
        ReaderConfig config = readerConfigRepository.findByUserId(userId).orElse(null);
        if (config == null) {
            config = new ReaderConfig();
            config.setUserId(userId);
        }
        if (fontSize != null) config.setFontSize(fontSize);
        if (lineHeight != null) config.setLineHeight(lineHeight);
        if (theme != null) config.setTheme(theme);
        if (brightness != null) config.setBrightness(brightness);
        return readerConfigRepository.save(config);
    }
}
