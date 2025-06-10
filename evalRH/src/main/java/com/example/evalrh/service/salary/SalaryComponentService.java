package com.example.evalrh.service.salary;

import com.example.evalrh.service.FrappeService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service

public class SalaryComponentService {
    private final FrappeService frappeService;

    public SalaryComponentService(FrappeService frappeService) {
        this.frappeService = frappeService;
    }
    private final String SALARY_COMPONENT_ENDPOINT = "/resource/Salary Component";
    public List<Map<String, Object>> getAllSalaryComponents() {
        Map<String,String> params = new HashMap<>();
        params.put("fields", "[\"name\",\"type\"]");
        ResponseEntity<Map> responseEntity = frappeService.get(SALARY_COMPONENT_ENDPOINT, params);
        return (List<Map<String, Object>>) responseEntity.getBody().get("data");
    }
}
