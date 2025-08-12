package timdev.timdev.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails user) {
        model.addAttribute("totalUsers", 1245);
        model.addAttribute("revenue", "$12,430");
        model.addAttribute("orders", 320);
        model.addAttribute("sessions", 58);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("pageTitle", "Dashboard");
        return "admin/dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model, @AuthenticationPrincipal UserDetails user) {
        model.addAttribute("totalUsers", 1245);
        model.addAttribute("revenue", "$12,430");
        model.addAttribute("orders", 320);
        model.addAttribute("sessions", 58);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("pageTitle", "Dashboard");
        return "admin/dashboard";
    }
    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("pageTitle", "Settings");
        return "admin/settings";
    }
}