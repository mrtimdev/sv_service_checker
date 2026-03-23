package timdev.timdev.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import timdev.timdev.entity.Port;
import timdev.timdev.repository.PortRepository;

@AllArgsConstructor
@Controller
@RequestMapping("/admin/ports")
public class PortWebController {

    private final PortRepository repository;

    // List all Ports
    @GetMapping
    public String index(Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") String sizeParam,
            @RequestParam(value = "all", defaultValue = "false") boolean showAll,
            @RequestParam(value = "name", required = false) String name) {

        List<Port> data = repository.findAll();
        model.addAttribute("data", data);
        return "ports/list";
    }

    // Show form for new Port
    @GetMapping("/form")
    public String showForm(Model model) {
        model.addAttribute("port", new Port());
        return "ports/form";
    }

    // Show form to edit existing Port
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Port> portOpt = repository.findById(id);
        if (portOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Port not found");
            return "redirect:/admin/ports";
        }
        model.addAttribute("port", portOpt.get());
        return "ports/form";
    }

    // Create or update Port
    @PostMapping({ "", "/update/{id}" })
    public String createOrUpdatePort(@PathVariable(required = false) Long id,
            @Valid @ModelAttribute("port") Port dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "ports/form";
        }

        try {
            Port port;
            if (id != null) {
                port = repository.findById(id).orElse(new Port());
                port.setId(id);
            } else {
                port = new Port();
            }

            // Only set fields that exist in the entity
            port.setName(dto.getName());
            port.setDescription(dto.getDescription());

            repository.save(port);

            redirectAttributes.addFlashAttribute("success",
                    (id == null ? "Port created successfully!" : "Port updated successfully!"));

            return "redirect:/admin/ports";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Something went wrong: " + e.getMessage());
            return "ports/form";
        }
    }

    @GetMapping("/delete/{id}")
    public String deletePort(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<Port> portOpt = repository.findById(id);

        if (portOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Port not found!");
            return "redirect:/admin/ports";
        }

        Port port = portOpt.get();

        // Check if any DestinationPort is linked
        if (port.getDestinationPorts() != null
                && !port.getDestinationPorts().isEmpty()) {
            redirectAttributes.addFlashAttribute("error",
                    "Cannot delete Port. There are destinations linked to this station!");
            return "redirect:/admin/ports";
        }

        // Safe to delete
        repository.delete(port);
        redirectAttributes.addFlashAttribute("success", "Port deleted successfully!");
        return "redirect:/admin/ports";
    }
}