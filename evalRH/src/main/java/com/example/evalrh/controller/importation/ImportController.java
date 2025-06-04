package com.example.evalrh.controller.importation;

import com.example.evalrh.service.importation.EmployeeImportService;
import com.example.evalrh.service.importation.ImportService;
import com.example.evalrh.service.importation.SalarySlipImportService;
import com.example.evalrh.service.importation.SalaryStructureImportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/import")
public class ImportController {
    private final EmployeeImportService employeeImportService;
    private final SalaryStructureImportService salaryStructureImportService;
    private final SalarySlipImportService salarySlipImportService;
    private final ImportService importService;

    public ImportController(EmployeeImportService employeeImportService, SalaryStructureImportService salaryStructureImportService, SalarySlipImportService salarySlipImportService, ImportService importService) {
        this.employeeImportService = employeeImportService;
        this.salaryStructureImportService = salaryStructureImportService;
        this.salarySlipImportService = salarySlipImportService;
        this.importService = importService;
    }

    @GetMapping
    public String showForm() {
        return "import/form";
    }

    @PostMapping
    public String uploadEmployeesCsv(@RequestParam("file1") MultipartFile file1,@RequestParam("file2") MultipartFile file2,@RequestParam("file3") MultipartFile file3, Model model) {
        if (file1.isEmpty() || file2.isEmpty() ) {
            model.addAttribute("message", "Veuillez sélectionner des fichier CSV.");
            return "import/form";
        }
        try {
            List<Map<String, Object>> employees= employeeImportService.prepareEmployee(file1);
            List<Map<String, Object>> salaryStructures= salaryStructureImportService.prepareSalaryStructure(file2);
            List<Map<String, Object>> salarySlips= salarySlipImportService.prepareSalarySlip(file3);
            model.addAttribute("message", importService.importData(employees,salaryStructures,salarySlips).getBody());
            model.addAttribute("messageType", "success");
            return "import/form";
        } catch (Exception e) {
            model.addAttribute("message", "Erreur lors de l'importation: " + e.getMessage());
            model.addAttribute("messageType", "error");
            return "import/form";
        }
    }
}