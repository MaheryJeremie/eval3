package com.example.evalrh.controller.update;

import com.example.evalrh.service.salary.SalaryComponentService;
import com.example.evalrh.service.update.UpdateSalaryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/update")

public class UpdateController {
    private final SalaryComponentService salaryComponentService;
    private final UpdateSalaryService updateSalaryService;

    public UpdateController(SalaryComponentService salaryComponentService, UpdateSalaryService updateSalaryService) {
        this.salaryComponentService = salaryComponentService;
        this.updateSalaryService = updateSalaryService;
    }


    @GetMapping
    public String index(Model model) {
        model.addAttribute("components", salaryComponentService.getAllSalaryComponents());
        return "update/form";
    }
    @PostMapping
    public String modification(@RequestParam String component, @RequestParam String salaire, @RequestParam String condition, @RequestParam String pourcentage, Model model) {
        try {
            updateSalaryService.modification(component, salaire, condition, pourcentage);
            model.addAttribute("message", "Modification effectué");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("components", salaryComponentService.getAllSalaryComponents());
        return "update/form";
    }
}
