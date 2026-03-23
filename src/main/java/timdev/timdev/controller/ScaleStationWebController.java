package timdev.timdev.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import timdev.timdev.entity.ScaleStation;
import timdev.timdev.repository.ScaleStationRepository;

@AllArgsConstructor
@Controller
@RequestMapping("/admin/scale-stations")
public class ScaleStationWebController {

    private final ScaleStationRepository scaleStationRepository;

    // List all scale stations
    @GetMapping
    public String index(Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") String sizeParam,
            @RequestParam(value = "all", defaultValue = "false") boolean showAll,
            @RequestParam(value = "name", required = false) String name) {

        List<ScaleStation> data = scaleStationRepository.findAll();
        model.addAttribute("data", data);
        return "scale-stations/list";
    }

    // Show form for new scale station
    @GetMapping("/form")
    public String showForm(Model model) {
        model.addAttribute("scale", new ScaleStation());
        return "scale-stations/form";
    }

    // Show form to edit existing scale station
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<ScaleStation> scale = scaleStationRepository.findById(id);
        if (scale.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Scale station not found");
            return "redirect:/admin/scale-stations";
        }
        model.addAttribute("scale", scale.get());
        return "scale-stations/form";
    }

    // Create or update scale station
    @PostMapping({ "", "/update/{id}" })
    public String createOrUpdateScaleStation(@PathVariable(required = false) Long id,
            @Valid @ModelAttribute("scale") ScaleStation dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "scale-stations/form";
        }

        try {
            ScaleStation scaleStation;
            if (id != null) {
                scaleStation = scaleStationRepository.findById(id).orElse(new ScaleStation());
                scaleStation.setId(id);
            } else {
                scaleStation = new ScaleStation();
            }

            // Only set fields that exist in the entity
            scaleStation.setName(dto.getName());
            scaleStation.setDescription(dto.getDescription());

            scaleStationRepository.save(scaleStation);

            redirectAttributes.addFlashAttribute("success",
                    (id == null ? "Scale station created successfully!" : "Scale station updated successfully!"));

            return "redirect:/admin/scale-stations";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Something went wrong: " + e.getMessage());
            return "scale-stations/form";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteScaleStation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<ScaleStation> scale = scaleStationRepository.findById(id);

        if (scale.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Scale station not found!");
            return "redirect:/admin/scale-stations";
        }

        ScaleStation scaleStation = scale.get();

        // Check if any DestinationScaleStation is linked
        if (scaleStation.getDestinationScaleStations() != null
                && !scaleStation.getDestinationScaleStations().isEmpty()) {
            redirectAttributes.addFlashAttribute("error",
                    "Cannot delete scale station. There are destinations linked to this station!");
            return "redirect:/admin/scale-stations";
        }

        // Safe to delete
        scaleStationRepository.delete(scaleStation);
        redirectAttributes.addFlashAttribute("success", "Scale station deleted successfully!");
        return "redirect:/admin/scale-stations";
    }
}