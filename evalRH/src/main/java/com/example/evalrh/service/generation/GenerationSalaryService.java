package com.example.evalrh.service.generation;

import com.example.evalrh.service.FrappeService;
import com.example.evalrh.service.employee.EmployeeService;
import com.example.evalrh.service.salary.SalarySlipService;
import com.example.evalrh.service.salary.SalaryStructureAssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GenerationSalaryService {

    private final SalaryStructureAssignmentService assignmentService;
    private final SalarySlipService salarySlipService;
    private final EmployeeService employeeService;
    private final FrappeService frappeService;
    private final String GENERATION_ENDPOINT = "/method/erpnext.api.generate.generate";


    @Autowired
    public GenerationSalaryService(SalaryStructureAssignmentService assignmentService, SalarySlipService salarySlipService, EmployeeService employeeService, FrappeService frappeService) {
        this.assignmentService = assignmentService;
        this.salarySlipService = salarySlipService;
        this.employeeService = employeeService;
        this.frappeService = frappeService;
    }
    public void generateSalarySlips(String employee,String startMonth,String startYear,String endMonth,String endYear,String baseSalary) throws Exception {
        Map<String,Object> params = new HashMap<>();
        params.put("name",employee);
        params.put("moisDebutString",startMonth);
        params.put("anneeDebutString",startYear);
        params.put("moisFinString",endMonth);
        params.put("anneeFinString",endYear);
        if(baseSalary.isEmpty())
            params.put("salaire",null);
        else
            params.put("salaire",baseSalary);
        ResponseEntity<Map> response = frappeService.send(GENERATION_ENDPOINT, params, HttpMethod.POST );
        Map<String,Object> body = (Map<String, Object>) response.getBody().get("message");
        if (body != null && Boolean.FALSE.equals(body.get("success"))) {
            String errorMessage = (String) body.getOrDefault("message", "Erreur inconnue de l'ERPNext");
            throw new Exception("Erreur lors de la generation: " + errorMessage);
        }
    }

}