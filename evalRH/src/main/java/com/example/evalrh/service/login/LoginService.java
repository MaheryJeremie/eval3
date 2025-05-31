package com.example.evalrh.service.login;

import com.example.evalrh.service.FrappeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class LoginService {
    private final FrappeService apiService;
    private final HttpSession session;

    public LoginService(FrappeService apiService, HttpSession session) {
        this.apiService = apiService;
        this.session = session;
    }

    public String login(String username, String password) {
        String endpoint = "/method/login";
        Map<String, String> body = Map.of(
                "usr", username,
                "pwd", password
        );

        ResponseEntity<Map> response = apiService.send(endpoint, body, HttpMethod.POST);

        HttpHeaders headers = response.getHeaders();
        List<String> cookies = headers.get(HttpHeaders.SET_COOKIE);

        if (cookies != null) {
            for (String cookie : cookies) {
                if (cookie.startsWith("sid=")) {
                    String sid = cookie.split(";")[0].split("=")[1];
                    session.setAttribute("sid", sid);
                    break;
                }
            }
        }

        return response.getBody().get("message").toString();
    }

    public void logout() {
        try {
            String endpoint = "/method/logout";
            apiService.send(endpoint, null, HttpMethod.POST);
        } finally {
            session.invalidate();
        }
    }
}
