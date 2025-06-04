package com.example.evalrh.service.employee;

import com.example.evalrh.service.FrappeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmployeeService {

    private final FrappeService frappeService;

    private static final String EMPLOYEE_ENDPOINT = "/resource/Employee";
    private static final String EMPLOYEE_SEARCH_ENDPOINT = "/method/erpnext.api.employee.search";

    @Autowired
    public EmployeeService(FrappeService frappeService) {
        this.frappeService = frappeService;
    }

    public List<Map<String, Object>> getAllEmployees() {
        Map<String, String> params = new HashMap<>();
        params.put("fields", "[\"name\", \"employee_name\", \"department\", \"date_of_joining\",\"gender\", \"status\"]");

        ResponseEntity<Map> responseEntity = frappeService.get(EMPLOYEE_ENDPOINT, params);

        return (List<Map<String, Object>>) responseEntity.getBody().get("data");
    }
    public List<Map<String, Object>> getAllEmployeesFiltered(String id, String employee_name, String department,
                                                             String gender, String status, String date_of_joining_min, String date_of_joining_max) {

        Map<String, String> params = new HashMap<>();

        if (id != null) {
            params.put("name", id);
        }
        if (employee_name != null) {
            params.put("employee_name", employee_name);
        }
        if (department != null) {
            params.put("department", department);
        }
        if (gender != null) {
            params.put("gender", gender);
        }
        if (status != null) {
            params.put("status", status);
        }
        if (date_of_joining_min != null) {
            params.put("date_of_joining_min", date_of_joining_min);
        }
        if (date_of_joining_max != null) {
            params.put("date_of_joining_max", date_of_joining_max);
        }

        ResponseEntity<Map> responseEntity = frappeService.get(EMPLOYEE_SEARCH_ENDPOINT, params);

        return (List<Map<String, Object>>) responseEntity.getBody().get("message");
    }
}
