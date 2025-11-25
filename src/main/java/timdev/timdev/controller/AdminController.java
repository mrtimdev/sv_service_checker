package timdev.timdev.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.AllArgsConstructor;
import timdev.timdev.dto.CustomUserDetails;
import timdev.timdev.service.TruckService;
import timdev.timdev.service.UserService;

@Controller
@AllArgsConstructor
public class AdminController {

    @Autowired
    private UserService userService;
    @Autowired
    private TruckService truckService;

    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        return "redirect:/dashboard/maintenance";
    }

    @GetMapping("/admin/dashboard")
    public String showDashboard(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return "redirect:/dashboard/maintenance";
    }

    @GetMapping("/dashboard/maintenance")
    public String maintenancePage(Model model) {
        model.addAttribute("title", "Maintenance Mode");
        model.addAttribute("message", "We're working hard to improve your experience");
        model.addAttribute("estimatedTime", "48 hours");
        model.addAttribute("progress", 65);
        return "coming_soon";
    }
    










}