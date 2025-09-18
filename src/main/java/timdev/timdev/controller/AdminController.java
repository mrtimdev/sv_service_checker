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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import timdev.timdev.dto.CustomUserDetails;
import timdev.timdev.dto.FatsOilsSettingsForm;
import timdev.timdev.entity.Request;
import timdev.timdev.entity.Setting;
import timdev.timdev.entity.Truck;
import timdev.timdev.entity.TruckFatsReport;
import timdev.timdev.entity.TruckOilsReport;
import timdev.timdev.entity.User;
import timdev.timdev.enums.ApprovalStatus;
import timdev.timdev.enums.RoleType;
import timdev.timdev.repository.SettingRepository;
import timdev.timdev.service.ApprovalService;
import timdev.timdev.service.FatsOilsSettingService;
import timdev.timdev.service.RequestService;
import timdev.timdev.service.TruckFatsReportService;
import timdev.timdev.service.TruckOilsReportService;
import timdev.timdev.service.TruckService;
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
    @Autowired
    private TruckService truckService;

    @Autowired private FatsOilsSettingService fatsOilsSettingService;

    @Autowired
    private TruckFatsReportService fatsReportService;
    @Autowired
    private TruckOilsReportService oilsReportService;

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


        List<TruckFatsReport> recentFats = fatsReportService
                .findTop10ByOrderByDateDesc(); // implement in repo or service

        // latest 10 oils reports
        List<TruckOilsReport> recentOils = oilsReportService
                .findTop10ByOrderByDateDesc();

        model.addAttribute("recentFats", recentFats);
        model.addAttribute("recentOils", recentOils);
        
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
        setting_.setKmForFatsShoot(request.getKmForFatsShoot());
        setting_.setKmForOilsChange(request.getKmForOilsChange());
        setting_.setKmFats(request.getKmFats());
        setting_.setKmOils(request.getKmOils());
        settingRepo.save(setting_);
        List<Truck> trucks = truckService.getAll();
        for (Truck truck : trucks) {
            truck.setKmFatsBetween(setting_.getKmFats());
            truck.setKmOilsBetween(setting_.getKmOils());
            truck.setKmForFatsShoot(request.getKmForFatsShoot());
            truck.setKmForOilsChange(request.getKmForOilsChange());
        }
        truckService.saveAll(trucks);
        model.addAttribute("pageTitle", "Settings");
        return "redirect:/settings";
    }



    @PostMapping("/fats-oils-settings/update")
    public String updateAllSettings(
        FatsOilsSettingsForm form,
        RedirectAttributes redirectAttributes
    ) {
        if (form.getSettings() != null) {
            form.getSettings().forEach(fatsOilsSettingService::save);
        }

        redirectAttributes.addFlashAttribute("success", "Fats and Oils settings successfully updated.");
        return "redirect:/fats-oils-settings";
    }

    @GetMapping("/fats-oils-settings")
    public String listSettings(Model model) {
        FatsOilsSettingsForm form = new FatsOilsSettingsForm();
        form.setSettings(fatsOilsSettingService.getAll());
        model.addAttribute("form", form);
        model.addAttribute("settings", fatsOilsSettingService.getAll());
        return "admin/fats_oils_settings"; // name of the template
    }





}