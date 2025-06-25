package com.example.evalrh.service.salary;

import com.example.evalrh.service.FrappeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
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
        Map<String, String> params = new HashMap<>();
        params.put("fields", "[\"name\",\"employee\",\"employee_name\",\"start_date\",\"end_date\",\"payroll_frequency\",\"gross_pay\",\"total_deduction\",\"net_pay\",\"currency\"]");
        params.put("filters", "[[\"employee\",\"=\",\"" + employeeName + "\"]]");
        ResponseEntity<Map> responseEntity = frappeService.get(SALARY_SLIP_ENDPOINT, params);

        return (List<Map<String, Object>>) responseEntity.getBody().get("data");
    }

    public Map<String, Object> getSalaryByName(String name) {
        var response = frappeService.get(SALARY_SLIP_ENDPOINT + "/" + name, null);
        return (Map<String, Object>) response.getBody().get("data");
    }

    public List<Map<String, Object>> getSalarySlipByMonth(String month) {
        Map<String, String> params = new HashMap<>();
        params.put("month", month);
        ResponseEntity<Map> responseEntity = frappeService.get(SALARY_SLIP_FILTER_MONTH_ENDPOINT, params);

        return (List<Map<String, Object>>) responseEntity.getBody().get("message");
    }

    public List<Map<String, Object>> getAllSalarySlip() {
        Map<String, String> params = new HashMap<>();
        params.put("fields", "[\"name\",\"employee\",\"employee_name\",\"start_date\",\"end_date\",\"payroll_frequency\",\"gross_pay\",\"total_deduction\",\"net_pay\",\"currency\"]");
        ResponseEntity<Map> responseEntity = frappeService.get(SALARY_SLIP_ENDPOINT, params);
        return (List<Map<String, Object>>) responseEntity.getBody().get("data");
    }

    public List<Map<String, Object>> getAllSalarySlipDetailsPerMonthByYear(String year) {
        Map<String, String> params = new HashMap<>();
        params.put("year", year);
        ResponseEntity<Map> responseEntity = frappeService.get(SALARY_SLIP_FILTER_YEAR_ENDPOINT, params);
        return (List<Map<String, Object>>) responseEntity.getBody().get("message");
    }
    public void insert(String employee,String employeeName,String salary_structure,String start_date,String end_date)throws Exception{
        Map<String,Object> param = new HashMap<>();
        param.put("employee",employee);
        param.put("employee_name",employeeName);
        param.put("salary_structure",salary_structure);
        param.put("start_date",start_date);
        param.put("end_date",end_date);
        param.put("posting_date",start_date);
        param.put("payroll_frequency","Monthly");
        ResponseEntity<Map> map = frappeService.send(SALARY_SLIP_ENDPOINT,param, HttpMethod.POST);
        if(map.getStatusCode().is2xxSuccessful()){
            System.out.println("insertion effectuer avec succes");
        }else{
            throw new Exception("Erreur lors de l'insertion");
        }
    }
    public void delete(String name)throws Exception{
        ResponseEntity<Map> map = frappeService.send(SALARY_SLIP_ENDPOINT+"/"+name,null, HttpMethod.DELETE);
        if(map.getStatusCode().is2xxSuccessful()){
            System.out.println("insertion effectuer avec succes");
        }else{
            throw new Exception("Erreur lors de la suppression");
        }
    }
    public void cancel(String name)throws Exception{
        Map<String,Object> param = new HashMap<>();
        param.put("docstatus",2);
        ResponseEntity<Map> map = frappeService.send(SALARY_SLIP_ENDPOINT+"/"+name,param, HttpMethod.PUT);
        if(map.getStatusCode().is2xxSuccessful()){
            System.out.println("insertion effectuer avec succes");
        }else{
            throw new Exception("Erreur lors de l'insertion");
        }
    }

}