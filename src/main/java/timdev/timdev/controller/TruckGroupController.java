package timdev.timdev.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import timdev.timdev.entity.TruckGroup;
import timdev.timdev.service.TruckGroupService;

@Controller
@RequestMapping("/truck-groups")
public class TruckGroupController {
    

    private final TruckGroupService service;

    public TruckGroupController(TruckGroupService service) {
        this.service = service;
    }

    @GetMapping
    public String listTruckGroups(Model model) {
        model.addAttribute("items", service.getAll());
        return "truck-groups/index";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("truckGroup", new TruckGroup());
        return "truck-groups/form";
    }

    // Edit form
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        TruckGroup truckGroup = service.getById(id).orElse(null);
        if (truckGroup == null) {
            redirectAttributes.addFlashAttribute("error", "Truck group not found!");
            return "redirect:/truck-groups";
        }
        model.addAttribute("truckGroup", truckGroup);
        return "truck-groups/form";
    }

    // Save (both add and edit)
    @PostMapping("/save")
    public String saveTruckGroup(@ModelAttribute TruckGroup truckGroup,
                                 RedirectAttributes redirectAttributes) {
        try {
            if (truckGroup.getId() == null) {
                // New truck group
                service.save(truckGroup);
                redirectAttributes.addFlashAttribute("success", "Truck group added successfully!");
            } else {
                // Existing truck group
                service.update(truckGroup);
                redirectAttributes.addFlashAttribute("success", "Truck group updated successfully!");
            }
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/truck-groups";
    }
}
