package com.pocketnews.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class TranslationService {

    private final RestTemplate restTemplate = new RestTemplate();

    public String translate(String text, String targetLanguage) {

        // English — no translation needed
        if ("en".equals(targetLanguage)) return text;

        String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
        String url = "https://api.mymemory.translated.net/get?q="
                + encodedText
                + "&langpair=en|" + targetLanguage;

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        Map responseData = (Map) response.getBody().get("responseData");
        return (String) responseData.get("translatedText");
    }
}
