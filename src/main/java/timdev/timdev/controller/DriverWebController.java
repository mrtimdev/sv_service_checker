package timdev.timdev.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import timdev.timdev.entity.Driver;
import timdev.timdev.service.DriverService;
import timdev.timdev.service.ServiceCheckerService;



@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/drivers")

public class DriverWebController {

    private final DriverService driverService;
    private final ServiceCheckerService serviceCheckerService;


    @GetMapping
    public String listDrivers(Model model) {
        model.addAttribute("drivers", driverService.getAllDrivers());
        return "drivers/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("driver", new Driver());
        return "drivers/form";
    }

    @PostMapping
    public String createDriver(@Valid @ModelAttribute Driver driver, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "drivers/form";
        }
        if (driverService.existsByPhone(driver.getPhone())) {
            redirectAttributes.addFlashAttribute("error_phone", "Phone number already exists");
        }

        // Check for duplicate plate number
        if (driverService.existsByPlateNumber(driver.getPlateNumber())) {
            redirectAttributes.addFlashAttribute("error_plate_number", "Plate number already exists");
        }

        // Additional validation if needed
        if (driver.getFirstName() == null || driver.getFirstName().isBlank()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (driver.getLastName() == null || driver.getLastName().isBlank()) {
            throw new IllegalArgumentException("Last name is required");
        }
        driverService.createDriver(driver);
        return "redirect:/admin/drivers";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Driver driver = driverService.getDriverById(id)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        model.addAttribute("driver", driver);
        return "drivers/form";
    }

    @PostMapping("/update/{id}")
    public String updateDriver(@PathVariable Long id, @Valid @ModelAttribute Driver driver, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "drivers/form";
        }
        driver.setId(id);
        driverService.updateDriver(id, driver);
        redirectAttributes.addFlashAttribute("success", driver.getFullName() + " Driver updated successfully.!");
        return "redirect:/admin/drivers";
    }

    @GetMapping("/delete/{id}")
    public String deleteDriver(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Driver driver = driverService.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Driver not found with id: " + id));
        
        // Check if driver is assigned to any service checkers
        boolean hasServiceCheckers = serviceCheckerService.existsByDriver(driver);
        
        if (hasServiceCheckers) {
            redirectAttributes.addFlashAttribute("error", driver.getFullName() + " Cannot delete driver: Driver is assigned to one or more service checkers");
            return "redirect:/admin/drivers";
        } 
        driverService.deleteDriver(id);
        return "redirect:/admin/drivers";
    }
}