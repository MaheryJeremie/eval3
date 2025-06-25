package com.example.evalrh.service.update;

import com.example.evalrh.service.FrappeService;
import com.example.evalrh.service.salary.SalarySlipService;
import com.example.evalrh.service.salary.SalaryStructureAssignmentService;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UpdateSalaryService {
    private final FrappeService frappeService;
    private final SalarySlipService salarySlipService;
    private final SalaryStructureAssignmentService salaryStructureAssignmentService;
    private final static String UPDATE_ENDPOINT = "/method/erpnext.api.updateSalary.modif";

    public UpdateSalaryService(FrappeService frappeService, SalarySlipService salarySlipService, SalaryStructureAssignmentService salaryStructureAssignmentService) {
        this.frappeService = frappeService;
        this.salarySlipService = salarySlipService;
        this.salaryStructureAssignmentService = salaryStructureAssignmentService;
    }
    public void modification(String component,String salaryValue,String condition,String pourcentage)throws Exception{

        Map<String,Object> params = new HashMap<>();
        params.put("name",component);
        params.put("condition",condition);
        params.put("salaire",salaryValue);
        ResponseEntity<Map> response = frappeService.send(UPDATE_ENDPOINT, params, HttpMethod.POST);
        System.out.println(response.getBody().get("message"));
        List<Map<String,Object>> body = (List<Map<String, Object>>) response.getBody().get("message");
        if(body.size() == 0){
            return;
        }else {
            for(Map<String,Object> assignment:body){
                double nouveau = Double.parseDouble(String.valueOf(assignment.get("base")))* (1+(Double.valueOf(pourcentage)/100));
                String ssaName = String.valueOf(assignment.get("name"));
                String ssName = String.valueOf(assignment.get("slip_name"));
                String salary_structure = String.valueOf(assignment.get("salary_structure"));
                String employee = String.valueOf(assignment.get("employee"));
                String employeeName = String.valueOf(assignment.get("employee_name"));
                String start_date = String.valueOf(assignment.get("start_date"));
                String end_date = String.valueOf(assignment.get("end_date"));
                salarySlipService.cancel(ssName);
                salarySlipService.delete(ssName);
                salaryStructureAssignmentService.cancel(ssaName);
                salaryStructureAssignmentService.delete(ssaName);
                salaryStructureAssignmentService.insert(employee,salary_structure,start_date,String.valueOf(nouveau));
                salarySlipService.insert(employee,employeeName,salary_structure,start_date,end_date);
            }
        }

    }
}
