package com.example.evalrh.service.salary;

import com.example.evalrh.service.FrappeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service

public class SalarySlipService {
    private final FrappeService frappeService;
    private final String SALARY_SLIP_ENDPOINT = "/resource/Salary Slip";
    private final String SALARY_SLIP_FILTER_MONTH_ENDPOINT = "/method/erpnext.api.salarySlip.search";
    private final String SALARY_SLIP_FILTER_YEAR_ENDPOINT = "/method/erpnext.api.salarySlip.get_yearly_salary_summary_per_month";
    @Autowired
    public SalarySlipService(FrappeService frappeService) {
        this.frappeService = frappeService;
    }
    public List<Map<String, Object>> getSalarySlipByEmployee(String employeeName) {
        Map<String,String> params = new HashMap<>();
        params.put("fields", "[\"name\",\"employee\",\"employee_name\",\"start_date\",\"end_date\",\"payroll_frequency\",\"gross_pay\",\"total_deduction\",\"net_pay\",\"currency\"]");
        params.put("filters", "[[\"employee\",\"=\",\"" + employeeName + "\"]]");
        ResponseEntity<Map> responseEntity = frappeService.get(SALARY_SLIP_ENDPOINT, params);

        return (List<Map<String, Object>>) responseEntity.getBody().get("data");
    }
    public Map<String, Object> getSalaryByName(String name) {
        var response = frappeService.get(SALARY_SLIP_ENDPOINT +"/"+ name, null);
        return (Map<String, Object>) response.getBody().get("data");
    }
    public List<Map<String, Object>> getSalarySlipByMonth(String month) {
        Map<String,String> params = new HashMap<>();
        params.put("month", month);
        ResponseEntity<Map> responseEntity = frappeService.get(SALARY_SLIP_FILTER_MONTH_ENDPOINT, params);

        return (List<Map<String, Object>>) responseEntity.getBody().get("message");
    }
    public List<Map<String, Object>> getAllSalarySlip() {
        Map<String,String> params = new HashMap<>();
        params.put("fields", "[\"name\",\"employee\",\"employee_name\",\"start_date\",\"end_date\",\"payroll_frequency\",\"gross_pay\",\"total_deduction\",\"net_pay\",\"currency\"]");
        ResponseEntity<Map> responseEntity = frappeService.get(SALARY_SLIP_ENDPOINT, params);
        return (List<Map<String, Object>>) responseEntity.getBody().get("data");
    }
    public List<Map<String, Object>> getAllSalarySlipDetailsPerMonthByYear(String year) {
        Map<String,String> params = new HashMap<>();
        params.put("year", year);
        ResponseEntity<Map> responseEntity = frappeService.get(SALARY_SLIP_FILTER_YEAR_ENDPOINT, params);
        return (List<Map<String, Object>>) responseEntity.getBody().get("message");
    }

}
