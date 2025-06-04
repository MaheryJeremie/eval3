package com.example.evalrh.service.gender;

import com.example.evalrh.service.FrappeService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service

public class GenderService {
    private final FrappeService frappeService;
    private final String GENDER_ENDPOINT = "/resource/Gender";
    public GenderService(FrappeService frappeService) {
        this.frappeService = frappeService;
    }
    public List<Map<String, Object>> getAllGenders() {
        ResponseEntity<Map> responseEntity = frappeService.get(GENDER_ENDPOINT, null);
        return (List<Map<String, Object>>) responseEntity.getBody().get("data");
    }
}
