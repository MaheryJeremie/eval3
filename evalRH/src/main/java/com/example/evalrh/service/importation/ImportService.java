package com.example.evalrh.service.importation;

import com.example.evalrh.service.FrappeService;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service

public class ImportService {
    private final FrappeService frappeService;
    private final String IMPORT_ENDPOINT ="/method/erpnext.api.import.import_fichier";
    public ImportService(FrappeService frappeService) {
        this.frappeService = frappeService;
    }
    public ResponseEntity<Map> importData(List<Map<String, Object>> employees,List<Map<String, Object>> salaryStructures,List<Map<String, Object>> salarySlips) {
        Map<String,List<Map<String,Object>>> map= new HashMap<>();
        map.put("employees",employees);
        map.put("salaryStructures",salaryStructures);
        map.put("salarySlips",salarySlips);
        return frappeService.send(IMPORT_ENDPOINT,map, HttpMethod.POST);
    }
}
