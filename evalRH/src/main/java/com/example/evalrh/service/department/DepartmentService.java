package com.example.evalrh.service.department;

import com.example.evalrh.service.FrappeService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service

public class DepartmentService {
    private final FrappeService frappeService;
    private final String DEPARTMENT_ENDPOINT = "/resource/Department";

    public DepartmentService(FrappeService frappeService) {
        this.frappeService = frappeService;
    }

    public List<Map<String, Object>> getAllDepartments() {
        ResponseEntity<Map> responseEntity = frappeService.get(DEPARTMENT_ENDPOINT, null);
        return (List<Map<String, Object>>) responseEntity.getBody().get("data");
    }
}
