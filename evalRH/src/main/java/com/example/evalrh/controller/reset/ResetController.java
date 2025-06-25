package com.example.evalrh.controller.reset;

import com.example.evalrh.service.reset.ResetService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reset")
public class ResetController {
    private final ResetService resetService;

    public ResetController(ResetService resetService) {
        this.resetService = resetService;
    }

    @GetMapping
    public String index() {
        return "reset/index";
    }
    @GetMapping("/resetBase")
    public String resetBase(Model model) {
        resetService.reset();
        model.addAttribute("message","Base de données nettoyée");
        return "reset/index";
    }
}
