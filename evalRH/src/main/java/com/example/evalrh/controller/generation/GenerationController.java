package com.example.evalrh.controller.generation;

import com.example.evalrh.service.employee.EmployeeService;
import com.example.evalrh.service.generation.GenerationSalaryService;
import com.example.evalrh.service.salary.SalarySlipService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/generate")
public class GenerationController {
    private final EmployeeService employeeService;
    private final GenerationSalaryService generationSalaryService;
    private final SalarySlipService salarySlipService;

    public GenerationController(EmployeeService employeeService, GenerationSalaryService generationSalaryService, SalarySlipService salarySlipService) {
        this.employeeService = employeeService;
        this.generationSalaryService = generationSalaryService;
        this.salarySlipService = salarySlipService;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("employees", employeeService.getAllEmployees());
        return "generation/generate";
    }
    @PostMapping
    public String generate(Model model, @RequestParam String employee,@RequestParam String debut,@RequestParam String fin,@RequestParam(required = false) String salaire) {
        String[] startDateParts = debut.split("-");
        String[] endDateParts = fin.split("-");
        try {
            generationSalaryService.generateSalarySlips(employee,startDateParts[1],startDateParts[0],endDateParts[1],endDateParts[0],salaire);
            model.addAttribute("message","Salaire(s) generé(s) avec succes");
        }catch (Exception e){
            model.addAttribute("error",e.getMessage());
            e.printStackTrace();
        }
        model.addAttribute("employees",employeeService.getAllEmployees());
        return "generation/generate";

    }
}
