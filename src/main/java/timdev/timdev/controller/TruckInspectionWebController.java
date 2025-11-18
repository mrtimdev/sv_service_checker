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
import timdev.timdev.dto.TruckInspectionRequestDTO;
import timdev.timdev.entity.TruckInspection;
import timdev.timdev.enums.TruckSize;
import timdev.timdev.enums.TruckType;
import timdev.timdev.service.ModelService;
import timdev.timdev.service.TruckInspectionService;

@AllArgsConstructor
@Controller
@RequestMapping("/admin/trucks-inspection")
public class TruckInspectionWebController {

    private TruckInspectionService truckService;
    private ModelService modelService;


    @GetMapping
    public String index(Model model,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "50") String sizeParam,
        @RequestParam(value = "all", defaultValue = "false") boolean showAll,
        @RequestParam(value = "licensePlate", required = false) String licensePlate
    ) {

        List<TruckInspection> trucks;
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
            Page<TruckInspection> truckPage;
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
        return "trucks-inspection/list";
    }


    



    @GetMapping("/form")
    public String showForm(Model model) {
        model.addAttribute("truck", new TruckInspectionRequestDTO());
        model.addAttribute("models", modelService.getAll());
        model.addAttribute("sizes", TruckSize.values()); // 👈 add TruckSize enum
        model.addAttribute("types", TruckType.values()); // 👈 add TruckType enum
        return "trucks-inspection/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Optional<TruckInspection> truck = truckService.findById(id);
        if (truck.isEmpty()) {
            model.addAttribute("error", "Truck not found");
            return "redirect:/admin/trucks-inspection";
        }

        TruckInspection t = truck.get();
        TruckInspectionRequestDTO dto = new TruckInspectionRequestDTO();
        dto.setId(t.getId());
        dto.setLicensePlate(t.getLicensePlate());
        dto.setModelId(t.getModel().getId());
        dto.setYear(t.getYear());
        dto.setSize(t.getSize());
        dto.setType(t.getType());

        model.addAttribute("truck", dto);
        model.addAttribute("models", modelService.getAll());
        model.addAttribute("sizes", TruckSize.values()); // 👈 add TruckSize enum
        model.addAttribute("types", TruckType.values()); // 👈 add TruckType enum
        return "trucks-inspection/form";
    }

    @PostMapping({"/", "/update/{id}"})
    public String createOrUpdateTruck(@PathVariable(required = false) Long id,
                                    @Valid @ModelAttribute("truck") TruckInspectionRequestDTO truckDTO,
                                    BindingResult result,
                                    Model model, RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("models", modelService.getAll());
            model.addAttribute("sizes", TruckSize.values()); // 👈 add TruckSize enum
            model.addAttribute("types", TruckType.values()); // 👈 add TruckType enum
            return "trucks-inspection/form";
        }

        try {
            TruckInspection truck;
            if (id != null) {
                truck = truckService.findById(id).orElse(new TruckInspection());
                truck.setId(id);
            } else {
                truck = new TruckInspection();
            }

            truck.setLicensePlate(truckDTO.getLicensePlate());
            truck.setYear(truckDTO.getYear());
            truck.setSize(truckDTO.getSize());
            truck.setType(truckDTO.getType());

            // Set the model
            modelService.findById(truckDTO.getModelId()).ifPresent(truck::setModel);

            truckService.save(truck);
            redirectAttributes.addFlashAttribute("success", (id == null ? "Truck created successfully!" : "Truck updated successfully!"));
            return "redirect:/admin/trucks-inspection";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Something went wrong: " + e.getMessage());
            model.addAttribute("models", modelService.getAll());
            model.addAttribute("sizes", TruckSize.values()); // 👈 add TruckSize enum
            model.addAttribute("types", TruckType.values()); // 👈 add TruckType enum
            return "trucks-inspection/form";
        }
    }


    @PostMapping("/create")
    public String createTruck(@Valid @ModelAttribute("truck") TruckInspectionRequestDTO truckDTO,
                            BindingResult result,
                            Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("models", modelService.getAll());
            model.addAttribute("sizes", TruckSize.values()); // 👈 add TruckSize enum
            model.addAttribute("types", TruckType.values()); // 👈 add TruckType enum
            return "trucks-inspection/form";
        }

        try {
            TruckInspection truck = new TruckInspection();
            truck.setLicensePlate(truckDTO.getLicensePlate());
            truck.setYear(truckDTO.getYear());
            truck.setSize(truckDTO.getSize());
            truck.setType(truckDTO.getType());

            // Set the model
            modelService.findById(truckDTO.getModelId()).ifPresent(truck::setModel);

            truckService.save(truck);
            redirectAttributes.addFlashAttribute("success", "Truck created successfully!");
            return "redirect:/admin/trucks-inspection";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Something went wrong: " + e.getMessage());
            model.addAttribute("models", modelService.getAll());
            model.addAttribute("sizes", TruckSize.values()); // 👈 add TruckSize enum
            model.addAttribute("types", TruckType.values()); // 👈 add TruckType enum
            return "trucks-inspection/form";
        }
    }

    // Delete truck by ID
    @GetMapping("/delete/{id}")
    public String deleteTruck(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Optional<TruckInspection> truckOpt = truckService.findById(id);
            if (truckOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Truck not found");
            } else {
                TruckInspection truck = truckOpt.get();
                if(truck.getLastInspection() != null) {
                    redirectAttributes.addFlashAttribute("error", "This Truck can not delete");
                    return "redirect:/admin/trucks-inspection";
                }
                truckService.deleteById(id);
                redirectAttributes.addFlashAttribute("success", "Truck deleted successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Something went wrong: " + e.getMessage());
        }
        return "redirect:/admin/trucks-inspection";
    }







    





}
