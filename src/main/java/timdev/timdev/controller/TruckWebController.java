package timdev.timdev.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import timdev.timdev.dto.TruckRequestDTO;
import timdev.timdev.entity.Truck;
import timdev.timdev.enums.TruckSize;
import timdev.timdev.service.TruckDistanceService;
import timdev.timdev.service.TruckService;

@AllArgsConstructor
@Controller
@RequestMapping("/admin/trucks")
public class TruckWebController {

    private TruckService truckService;

    private TruckDistanceService truckDistanceService;


    @GetMapping
    public String index(Model model,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "20") String sizeParam,
        @RequestParam(value = "all", defaultValue = "false") boolean showAll,
        @RequestParam(value = "licensePlate", required = false) String licensePlate
    ) {

        List<Truck> trucks;
        int totalPages = 1;
        int size;
        
        if ("all".equalsIgnoreCase(sizeParam)) {
            size = Integer.MAX_VALUE;
        } else {
            size = Integer.parseInt(sizeParam); 
        }


        if (showAll) {
            if (licensePlate != null && !licensePlate.isEmpty()) {
                trucks = truckService.findByLicensePlateContaining(licensePlate);
            } else {
                trucks = truckService.getAll();
            }
        } else {
            Pageable pageable = PageRequest.of(page, size);
            Page<Truck> truckPage;
            if (licensePlate != null && !licensePlate.isEmpty()) {
                truckPage = truckService.findByLicensePlateContainingWithPageable(licensePlate, pageable);
            } else {
                truckPage = truckService.getAllWithPageable(pageable);
            }
            trucks = truckPage.getContent();
            totalPages = truckPage.getTotalPages();
        }

        model.addAttribute("trucks", trucks);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", sizeParam);
        model.addAttribute("showAll", showAll);
        model.addAttribute("licensePlate", licensePlate);  
        return "trucks/list";
    }


    



    @GetMapping("/form")
    public String showForm(Model model) {
        model.addAttribute("sizes", TruckSize.values());
        model.addAttribute("truck", new TruckRequestDTO());
        return "trucks/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Optional<Truck> truck = truckService.findById(id);
        if (truck.isEmpty()) {
            model.addAttribute("error", "Truck not found");
            return "redirect:/admin/trucks";
        }

        Truck t = truck.get();
        TruckRequestDTO dto = new TruckRequestDTO();
        dto.setId(t.getId());
        dto.setLicensePlate(t.getLicensePlate());
        dto.setGroup(t.getGroup());
        dto.setSize(t.getSize());

        model.addAttribute("truck", dto);
        model.addAttribute("sizes", TruckSize.values());
        return "trucks/form";
    }

    @PostMapping({"", "/update/{id}"})
    public String createOrUpdateTruck(@PathVariable(required = false) Long id,
                                    @Valid @ModelAttribute("truck") TruckRequestDTO truckDTO,
                                    BindingResult result,
                                    Model model, RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("sizes", TruckSize.values());
            return "trucks/form";
        }

        try {
            Optional<Truck> existingTruck = truckService.findByLicensePlate(truckDTO.getLicensePlate());
            if (existingTruck.isPresent() && (id == null || !existingTruck.get().getId().equals(id))) {
                model.addAttribute("error", "License plate "+ truckDTO.getLicensePlate() +" already exists!");
                model.addAttribute("sizes", TruckSize.values());
                return "trucks/form";
            }
            Truck truck;
            if (id != null) {
                truck = truckService.findById(id).orElse(new Truck());
                truck.setId(id);
            } else {
                truck = new Truck();
            }

            truck.setLicensePlate(truckDTO.getLicensePlate());
            truck.setGroup(truckDTO.getGroup());
            truck.setSize(truckDTO.getSize());

            truckService.save(truck);
            redirectAttributes.addFlashAttribute("success", (id == null ? "Truck created successfully!" : "Truck updated successfully!"));
            return "redirect:/admin/trucks";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Something went wrong: " + e.getMessage());
            model.addAttribute("sizes", TruckSize.values());
            return "trucks/form";
        }
    }


    @PostMapping("/create")
    public String createTruck(@Valid @ModelAttribute("truck") TruckRequestDTO truckDTO,
                            BindingResult result,
                            Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("sizes", TruckSize.values());
            return "trucks/form";
        }

        try {
            
            Truck truck = new Truck();
            truck.setLicensePlate(truckDTO.getLicensePlate());
            truck.setGroup(truckDTO.getGroup());

            truckService.save(truck);
            redirectAttributes.addFlashAttribute("success", "Truck created successfully!");
            return "redirect:/admin/trucks";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Something went wrong: " + e.getMessage());
            model.addAttribute("sizes", TruckSize.values());
            return "trucks/form";
        }
    }

    // Delete truck by ID
    @GetMapping("/delete/{id}")
    public String deleteTruck(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Optional<Truck> truckOpt = truckService.findById(id);
            if (truckOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Truck not found");
            } else {
                Truck truck = truckOpt.get();
                
                truckService.deleteById(id);
                redirectAttributes.addFlashAttribute("success", "Truck deleted successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Something went wrong: " + e.getMessage());
        }
        return "redirect:/admin/trucks";
    }






}
