package timdev.timdev.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import timdev.timdev.dto.CustomUserDetails;
import timdev.timdev.enums.ApprovalStatus;
import timdev.timdev.service.ApprovalService;
import timdev.timdev.service.RequestService;
import timdev.timdev.service.UserService;

@Controller
public class AdminController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private RequestService requestService;
    
    @Autowired
    private ApprovalService approvalService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        long totalUsers = userService.findAllUsers().size();
        long totalRequests = requestService.findAllRequests().size();
        long pendingRequests = requestService.findByStatus(ApprovalStatus.PENDING).size();
        
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalRequests", totalRequests);
        model.addAttribute("pendingRequests", pendingRequests);
        
        return "admin/dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        model.addAttribute("totalUsers", 1245);
        model.addAttribute("revenue", "$12,430");
        model.addAttribute("orders", 320);
        model.addAttribute("sessions", 58);
        model.addAttribute("user", userDetails.getUser());
        model.addAttribute("pageTitle", "Dashboard");
        return "admin/dashboard";
    }
    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("pageTitle", "Settings");
        return "admin/settings";
    }
}