package com.bookstore.bookstore.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TtsService {

    public String generateAudioUrl(String text) {
        // Mock TTS: return a placeholder URL
        return "https://example.com/tts/" + UUID.randomUUID() + ".mp3";
    }
}
