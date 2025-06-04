package com.example.evalrh.controller.employee;

import com.example.evalrh.service.department.DepartmentService;
import com.example.evalrh.service.employee.EmployeeService;
import com.example.evalrh.service.gender.GenderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/employees")

public class EmployeeController {
    private final EmployeeService employeeService;
    private final DepartmentService departmentService;
    private final GenderService genderService;

    public EmployeeController(EmployeeService employeeService, DepartmentService departmentService, GenderService genderService) {
        this.employeeService = employeeService;
        this.departmentService = departmentService;
        this.genderService = genderService;
    }

    @GetMapping
    public String getAllEmployees(Model model) {
        model.addAttribute("employees", employeeService.getAllEmployees());
        model.addAttribute("departments",departmentService.getAllDepartments());
        model.addAttribute("genders",genderService.getAllGenders());
        return "employee/list";
    }
    @GetMapping("/search")
    public String getAllEmployeesFiltered(Model model,
                                          @RequestParam(required = false) String name,
                                          @RequestParam(required = false) String employee_name,
                                          @RequestParam(required = false) String department,
                                          @RequestParam(required = false) String gender,
                                          @RequestParam(required = false) String status,
                                          @RequestParam(required = false) String date_of_joining_min,
                                          @RequestParam(required = false) String date_of_joining_max) {
        model.addAttribute("employees", employeeService.getAllEmployeesFiltered(name,employee_name,department,gender,status,date_of_joining_min,date_of_joining_max));
        model.addAttribute("departments",departmentService.getAllDepartments());
        model.addAttribute("genders",genderService.getAllGenders());
        return "employee/list";
    }
}
