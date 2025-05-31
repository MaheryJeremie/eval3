package com.example.evalrh.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class FrappeService {
    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String apiKey;

    public FrappeService(
            RestTemplate restTemplate,
            @Value("${erpnext.api.url}") String apiUrl,
            @Value("${erpnext.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
    }
    public ResponseEntity<Map> get(String endpoint, Map<String, String> params) {
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        return restTemplate.exchange(
                buildUrl(endpoint, params),
                HttpMethod.GET,
                entity,
                Map.class
        );
    }

    public ResponseEntity<Map> send(String endpoint, Object body, HttpMethod method) {
        HttpEntity<Object> entity = new HttpEntity<>(body, createHeaders());
        return restTemplate.exchange(
                apiUrl + endpoint,
                method,
                entity,
                Map.class
        );
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String buildUrl(String endpoint, Map<String, String> params) {
        StringBuilder url = new StringBuilder(apiUrl + endpoint);

        if (params != null && !params.isEmpty()) {
            url.append("?");
            params.forEach((key, value) -> {
                String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8);
                String encodedValue = value;
                if (!isAlreadyEncoded(value)) {
                    encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8);
                }
                url.append(encodedKey).append("=").append(encodedValue).append("&");
            });
            url.setLength(url.length() - 1);
        }
        return url.toString();
    }

    private boolean isAlreadyEncoded(String value) {
        if (value.contains("%") && !value.matches(".*%[0-9A-Fa-f]{2}.*")) {
            return true;
        }
        String decoded = URLDecoder.decode(value, StandardCharsets.UTF_8);
        return decoded.equals(value);
    }
}