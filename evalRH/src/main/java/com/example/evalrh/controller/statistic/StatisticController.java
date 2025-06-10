package com.example.evalrh.controller.statistic;

import com.example.evalrh.service.salary.SalarySlipService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/statistic")

public class StatisticController {
    private final SalarySlipService salarySlipService;

    public StatisticController(SalarySlipService salarySlipService) {
        this.salarySlipService = salarySlipService;
    }

    @GetMapping("/detailsPerMonth")
    public String detailsPerMonth(@RequestParam String year, Model model) {
        model.addAttribute("year", year);
        model.addAttribute("salaries", salarySlipService.getAllSalarySlipDetailsPerMonthByYear(year));
        return "statistic/detailsPerMonth";
    }
    @GetMapping()
    public String index(){
        return "statistic/detailsPerMonth";
    }
}
