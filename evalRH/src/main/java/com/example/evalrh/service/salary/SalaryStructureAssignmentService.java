package com.example.evalrh.service.salary;

import com.example.evalrh.service.FrappeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SalaryStructureAssignmentService {
    private final FrappeService frappeService;
    private final String ASSIGNMENT_ENDPOINT = "/resource/Salary Structure Assignment";

    @Autowired
    public SalaryStructureAssignmentService(FrappeService frappeService) {
        this.frappeService = frappeService;
    }

    public void cancel(String name)throws Exception{
        Map<String,Object> param = new HashMap<>();
        param.put("docstatus",2);
        ResponseEntity<Map> map = frappeService.send(ASSIGNMENT_ENDPOINT+"/"+name,param, HttpMethod.PUT);
        if(map.getStatusCode().is2xxSuccessful()){
            System.out.println("insertion effectuer avec succes");
        }else{
            throw new Exception("Erreur lors de l'insertion");
        }
    }
    public void submit(String name)throws Exception{
        Map<String,Object> param = new HashMap<>();
        param.put("docstatus",1);
        ResponseEntity<Map> map = frappeService.send(ASSIGNMENT_ENDPOINT+"/"+name,param, HttpMethod.PUT);
        if(map.getStatusCode().is2xxSuccessful()){
            System.out.println("insertion effectuer avec succes");
        }else{
            throw new Exception("Erreur lors de l'insertion");
        }
    }
    public void delete(String name)throws Exception{
        ResponseEntity<Map> map = frappeService.send(ASSIGNMENT_ENDPOINT+"/"+name,null, HttpMethod.DELETE);
        if(map.getStatusCode().is2xxSuccessful()){
            System.out.println("insertion effectuer avec succes");
        }else{
            throw new Exception("Erreur lors de la suppression");
        }
    }
    public void update(String name,String employee,String salary_structure,String from_date, String base)throws Exception{
        Map<String,Object> param = new HashMap<>();
        param.put("employee",employee);
        param.put("salary_structure",salary_structure);
        param.put("from_date",from_date);
        param.put("base",base);
        ResponseEntity<Map> map = frappeService.send(ASSIGNMENT_ENDPOINT+"/"+name,param, HttpMethod.PUT);
        if(map.getStatusCode().is2xxSuccessful()){
            System.out.println("insertion effectuer avec succes");
        }else{
            throw new Exception("Erreur lors de l'insertion");
        }
    }
    public void insert(String employee,String salary_structure,String from_date, String base)throws Exception{
        Map<String,Object> param = new HashMap<>();
        param.put("employee",employee);
        param.put("salary_structure",salary_structure);
        param.put("from_date",from_date);
        param.put("base",base);
        param.put("docstatus",1);
        ResponseEntity<Map> map = frappeService.send(ASSIGNMENT_ENDPOINT,param, HttpMethod.POST);
        if(map.getStatusCode().is2xxSuccessful()){
            System.out.println("insertion effectuer avec succes");
        }else{
            throw new Exception("Erreur lors de l'insertion");
        }
    }
}