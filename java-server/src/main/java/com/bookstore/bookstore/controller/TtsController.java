package com.bookstore.bookstore.controller;

import com.bookstore.bookstore.dto.TtsRequest;
import com.bookstore.bookstore.service.TtsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class TtsController {

    private final TtsService ttsService;

    public TtsController(TtsService ttsService) {
        this.ttsService = ttsService;
    }

    @PostMapping("/tts")
    public ResponseEntity<?> tts(@RequestBody TtsRequest request) {
        String text = request.getText();
        if (text == null || text.isEmpty()) {
            return ResponseEntity.ok(Map.of("error", "Text is required"));
        }
        String url = ttsService.generateAudioUrl(text);
        return ResponseEntity.ok(Map.of("url", url));
    }
}
