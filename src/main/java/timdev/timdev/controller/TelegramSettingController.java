package timdev.timdev.controller;


import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import timdev.timdev.entity.TelegramSetting;
import timdev.timdev.service.TelegramSettingService;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/telegram-settings")
public class TelegramSettingController {
    

    @Autowired
    private TelegramSettingService telegramSettingService;
    
    @GetMapping
    public String listSettings(Model model) {
         List<TelegramSetting> settings = telegramSettingService.getAllSettings();

        long verifiedCount = telegramSettingService.countVerified();
        long notVerifiedCount = telegramSettingService.countNotVerified();
        long activedCount = telegramSettingService.countActived();
        model.addAttribute("settings", settings);
        model.addAttribute("verifiedCount", verifiedCount);
        model.addAttribute("notVerifiedCount", notVerifiedCount);
        model.addAttribute("activedCount", activedCount);
        return "telegram-settings/index";
    }
    
    @GetMapping("/create")
    public String createSettingForm(Model model) {
        model.addAttribute("setting", new TelegramSetting());
        return "telegram-settings/create";
    }
    
    @PostMapping("/store")
    public String storeSetting(@ModelAttribute TelegramSetting setting,
                               RedirectAttributes redirectAttributes) {
        try {
            // Validate bot token format
            if (!telegramSettingService.validateBotToken(setting.getBotToken())) {
                redirectAttributes.addFlashAttribute("error", "Invalid bot token format");
                return "redirect:/telegram-settings/create";
            }
            
            telegramSettingService.saveSetting(setting);
            redirectAttributes.addFlashAttribute("success", "Telegram setting saved successfully");
            return "redirect:/telegram-settings";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving setting: " + e.getMessage());
            return "redirect:/telegram-settings/create";
        }
    }
    
    @GetMapping("/edit/{id}")
    public String editSettingForm(@PathVariable Long id, Model model) {
        TelegramSetting setting = telegramSettingService.getSettingById(id)
                .orElseThrow(() -> new RuntimeException("Setting not found"));
        model.addAttribute("setting", setting);
        return "telegram-settings/edit";
    }
    
    @PostMapping("/update/{id}")
    public String updateSetting(@PathVariable Long id,
                                @ModelAttribute TelegramSetting setting,
                                RedirectAttributes redirectAttributes) {
        try {
            telegramSettingService.updateSetting(id, setting);
            redirectAttributes.addFlashAttribute("success", "Telegram setting updated successfully");
            return "redirect:/telegram-settings";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating setting: " + e.getMessage());
            return "redirect:/telegram-settings/edit/" + id;
        }
    }
    
    @PostMapping("/delete/{id}")
    public String deleteSetting(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        try {
            telegramSettingService.deleteSetting(id);
            redirectAttributes.addFlashAttribute("success", "Telegram setting deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting setting: " + e.getMessage());
        }
        return "redirect:/telegram-settings";
    }
    
    @PostMapping("/activate/{id}")
    public String activateSetting(@PathVariable Long id,
        @RequestParam(value = "active") Boolean active,
                                  RedirectAttributes redirectAttributes) {
        try {
            telegramSettingService.activateSetting(id, active);
            redirectAttributes.addFlashAttribute("success", "Telegram setting activated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error activating setting: " + e.getMessage());
        }
        return "redirect:/telegram-settings";
    }

    @PostMapping("/verify/{id}")
    public String verifyBot(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            TelegramSetting telegramSetting = telegramSettingService
                    .getSettingById(id)
                    .orElseThrow(() -> new RuntimeException("Telegram setting not found"));

            Map<String, Object> botInfo =
                    telegramSettingService.verifyTelegramBot(telegramSetting.getBotToken());

            // ✅ Extract username safely
            String username = botInfo.get("username") != null
                    ? botInfo.get("username").toString()
                    : null;

            // ✅ Set values correctly
            telegramSetting.setBotUsername(username);
            telegramSetting.setVerifiedAt(LocalDateTime.now());
            telegramSetting.setIsVerified(true);

            telegramSettingService.save(telegramSetting);

            redirectAttributes.addFlashAttribute(
                    "success", "Telegram bot verified successfully"
            );

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "error", "Error verifying bot token: " + e.getMessage()
            );
        }

        return "redirect:/telegram-settings";
    }

    
    @PostMapping("/test/{id}")
    public String testConnection(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        try {
            TelegramSetting setting = telegramSettingService.getSettingById(id)
                    .orElseThrow(() -> new RuntimeException("Setting not found"));
            
            if (setting.getChatId() == null || setting.getChatId().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Chat ID is required for testing");
                return "redirect:/telegram-settings";
            }
            
            boolean isConnected = telegramSettingService.testConnection(
                    setting.getBotToken(), setting.getChatId());
            
            if (isConnected) {
                redirectAttributes.addFlashAttribute("success", "Connection test successful!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Connection test failed. Check bot token and chat ID.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error testing connection: " + e.getMessage());
        }
        return "redirect:/telegram-settings";
    }
}
