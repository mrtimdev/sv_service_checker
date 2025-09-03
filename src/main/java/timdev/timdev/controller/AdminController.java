package timdev.timdev.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;
import timdev.timdev.dto.CustomUserDetails;
import timdev.timdev.entity.Request;
import timdev.timdev.entity.Setting;
import timdev.timdev.entity.User;
import timdev.timdev.enums.ApprovalStatus;
import timdev.timdev.enums.RoleType;
import timdev.timdev.repository.SettingRepository;
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


    @Autowired
    private SettingRepository settingRepo;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String showDashboard(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Get current user
        User user = userDetails.getUser();
        model.addAttribute("user", user);
        long totalRequests = 0;
        long pendingRequests = 0;
        long approvedRequests = 0;
        long rejectedRequests = 0;
        List<Request> recentRequests;

        if (!RoleType.REPAIRMAN.equals(user.getRole())) {
            // Get request statistics
            totalRequests = requestService.getTotalRequestCount();
            pendingRequests = requestService.getRequestCountByStatus(ApprovalStatus.PENDING);
            approvedRequests = requestService.getRequestCountByStatus(ApprovalStatus.APPROVED);
            rejectedRequests = requestService.getRequestCountByStatus(ApprovalStatus.REJECTED);
            recentRequests = requestService.findRecentRequests(5);
        } else {
            // Get request statistics
            totalRequests = requestService.getTotalRequestCountByUser(user);
            pendingRequests = requestService.getRequestCountByStatusAndUser(ApprovalStatus.PENDING,  user);
            approvedRequests = requestService.getRequestCountByStatusAndUser(ApprovalStatus.APPROVED,  user);
            rejectedRequests = requestService.getRequestCountByStatusAndUser(ApprovalStatus.REJECTED,  user);
            recentRequests = requestService.findRecentRequestsByUser(user, 5);
        }
        
        
        
        model.addAttribute("totalRequests", totalRequests);
        model.addAttribute("pendingRequests", pendingRequests);
        model.addAttribute("approvedRequests", approvedRequests);
        model.addAttribute("rejectedRequests", rejectedRequests);
        
        model.addAttribute("recentRequests", recentRequests);
        
        return "admin/dashboard";
    }
    @GetMapping("/settings")
    public String settings(Model model) {
        Setting setting = settingRepo.findById(1L).orElse(null);
        model.addAttribute("approvedLevels", new String[]{"LEVEL_1", "LEVEL_2", "LEVEL_3"});
        model.addAttribute("setting", setting);
        model.addAttribute("pageTitle", "Settings");
        return "admin/settings";
    }

    @PostMapping("/settings")
    public String saveSettings(@Valid @ModelAttribute("setting") Setting request,
        BindingResult result, Model model) {
        Setting setting_ = settingRepo.findById(1L).orElse(null);
        setting_.setApprovedLevel(request.getApprovedLevel());
        settingRepo.save(setting_);
        model.addAttribute("pageTitle", "Settings");
        return "redirect:/settings";
    }




}