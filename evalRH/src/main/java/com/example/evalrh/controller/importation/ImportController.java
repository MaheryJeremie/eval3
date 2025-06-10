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
        if (file1.isEmpty() || file2.isEmpty() || file3.isEmpty()) {
            model.addAttribute("message", "Veuillez sélectionner des fichier CSV.");
            model.addAttribute("messageType","error");
            return "import/form";
        }
        try {
            List<Map<String, Object>> employees = employeeImportService.prepareEmployee(file1);
            List<Map<String, Object>> salaryStructures = salaryStructureImportService.prepareSalaryStructure(file2);
            List<Map<String, Object>> salarySlips = salarySlipImportService.prepareSalarySlip(file3);

            String fullMessage = importService.importData(employees, salaryStructures, salarySlips)
                    .getBody()
                    .get("message")
                    .toString();

            boolean success = fullMessage.contains("success=true");
            String displayMessage;

            if (success) {
                // Cas de succès
                displayMessage = fullMessage.substring(fullMessage.indexOf("message=") + 8,
                                fullMessage.lastIndexOf("}"))
                        .trim();
                model.addAttribute("messageType", "success");
            } else {
                // Cas d'erreur - on extrait seulement la partie error=
                int errorStart = fullMessage.indexOf("error=") + 6;
                int errorEnd = fullMessage.indexOf(", traceback=");
                if (errorEnd == -1) errorEnd = fullMessage.length();

                displayMessage = fullMessage.substring(errorStart, errorEnd).trim();
                model.addAttribute("messageType", "error");
            }

            model.addAttribute("message", displayMessage);
            return "import/form";

        } catch (Exception e) {
            model.addAttribute("message", e.getMessage());
            model.addAttribute("messageType", "error");
            return "import/form";
        }
    }
}