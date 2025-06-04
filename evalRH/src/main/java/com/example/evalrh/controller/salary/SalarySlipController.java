package com.example.evalrh.controller.salary;

import com.example.evalrh.service.pdf.SalarySlipPdfExporter;
import com.example.evalrh.service.salary.SalarySlipService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/salarySlip")

public class SalarySlipController {
    private final SalarySlipService salarySlipService;
    private final SalarySlipPdfExporter salarySlipPdfExporter;
    @Autowired

    public SalarySlipController(SalarySlipService salarySlipService, SalarySlipPdfExporter salarySlipPdfExporter) {
        this.salarySlipService = salarySlipService;
        this.salarySlipPdfExporter = salarySlipPdfExporter;
    }

    @GetMapping("/byEmployee")
    public String salarySlipByEmployee(@RequestParam String employee, Model model) {
        model.addAttribute("employee", employee);
        model.addAttribute("salaries", salarySlipService.getSalarySlipByEmployee(employee));
        return "salary/salarySlipByEmployee";
    }
    @GetMapping()
    public String allSalarySlip(Model model) {
        model.addAttribute("salaries", salarySlipService.getAllSalarySlip());
        return "salary/allSalarySlip";
    }
    @GetMapping("/search")
    public String salarySlipSearch(Model model, @RequestParam String month,@RequestParam String page) {
        List<Map<String, Object>> salaries=salarySlipService.getSalarySlipByMonth(month);
        model.addAttribute("salaries", salaries);
        model.addAttribute("employee",salaries.get(0).get("employee"));
        return "salary/"+page;
    }
    @GetMapping("/exportPdf")
    public void exportPdf(
            @RequestParam String name,
            HttpServletResponse response) {

        try {
            Map<String, Object> slipData = salarySlipService.getSalaryByName(name);

            if (slipData == null || slipData.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Fiche de paie non trouvée pour: " + name);
                return;
            }

            response.setContentType("application/pdf");

            String employeeNameForFile = Optional.ofNullable(slipData.get("employee_name"))
                    .map(Object::toString)
                    .orElse("Employe")
                    .replaceAll("[^a-zA-Z0-9.-]", "_");
            String dateForFile = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String fileName = "FicheDePaie_" + employeeNameForFile + "_" + dateForFile + ".pdf";

            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            salarySlipPdfExporter.exportToPdf(slipData, response.getOutputStream());

            response.flushBuffer();

        } catch (com.itextpdf.text.DocumentException e) {
            System.err.println("Erreur iText lors de la génération du PDF: " + e.getMessage());
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erreur lors de la génération du PDF.");
            } catch (IOException ignored) {}
        } catch (IOException e) {
            System.err.println("Erreur d'IO lors de l'envoi du PDF: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur inattendue: " + e.getMessage());
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erreur inattendue du serveur.");
            } catch (IOException ignored) {}
        }
    }
}
