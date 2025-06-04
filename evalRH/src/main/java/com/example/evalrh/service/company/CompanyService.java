package com.example.evalrh.service.company;

import com.example.evalrh.service.FrappeService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service

public class CompanyService {
    private final FrappeService frappeService;
    private final String COMPANY_ENDPOINT = "/resource/Company";

    public CompanyService(FrappeService frappeService) {
        this.frappeService = frappeService;
    }
    public Map<String, Object> getCompanyDetails(String companyName) {
        ResponseEntity<Map> responseEntity = frappeService.get(COMPANY_ENDPOINT+"/"+companyName, null);
        return (Map<String, Object>) responseEntity.getBody().get("data");
    }
}
