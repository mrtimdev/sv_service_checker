package timdev.timdev.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.AllArgsConstructor;
import timdev.timdev.dto.TruckDistanceDto;
import timdev.timdev.dto.TruckDistanceForm;
import timdev.timdev.entity.Truck;
import timdev.timdev.entity.TruckDistance;
import timdev.timdev.entity.User;
import timdev.timdev.repository.TruckRepository;
import timdev.timdev.service.TruckDistanceService;
import timdev.timdev.service.TruckService;
import timdev.timdev.service.UserService;

@AllArgsConstructor
@Controller
@RequestMapping("/truck-distances")
public class TruckDistanceWebController {

    private final TruckDistanceService truckDistanceService;
    private final TruckService truckService;
    private final UserService userService;
    private final TruckRepository truckRepo;

  


    // Display all truck distances
    @GetMapping
    public String listTruckDistances(
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "10") String sizeParam,
        @RequestParam(value = "all", defaultValue = "false") boolean showAll, 
        Model model) 
    {
        Page<TruckDistance> distancesPage;
        int size;
        
        if ("all".equalsIgnoreCase(sizeParam)) {
            size = Integer.MAX_VALUE;
        } else {
            size = Integer.parseInt(sizeParam); 
        }

        if (showAll) {
            // If "all" is true, fetch all records without pagination
            List<TruckDistance> distances = truckDistanceService.getAll();
            model.addAttribute("distances", distances);
            model.addAttribute("totalPages", 1);
            model.addAttribute("currentPage", 0);
        } else {
            // Paginated fetch
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
            distancesPage = truckDistanceService.getAllPaged(pageable);

            model.addAttribute("distances", distancesPage.getContent());
            model.addAttribute("totalPages", distancesPage.getTotalPages());
            model.addAttribute("currentPage", page);
        }
        model.addAttribute("pageSize", sizeParam);
        model.addAttribute("showAll", showAll);
        return "truck-distances/list";
    }

    @GetMapping("/create/v2")
    public String showCreateFormV2(Model model) {
        List<Truck> trucks = truckService.getAll();
    
        // Initialize the form with proper data structure
        TruckDistanceForm form = new TruckDistanceForm();
        // Add one empty entry to start with
        form.getDistances().add(new TruckDistanceDto());
        
        model.addAttribute("trucks", trucks);
        model.addAttribute("truckDistanceForm", form);
        return "truck-distances/addv2";
    }

    // Show form for creating multiple truck distances
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        List<Truck> trucks = truckService.getAll();
    
        // Initialize the form with proper data structure
        TruckDistanceForm form = new TruckDistanceForm();
        // Add one empty entry to start with
        form.getDistances().add(new TruckDistanceDto());
        
        model.addAttribute("trucks", trucks);
        model.addAttribute("truckDistanceForm", form);
        return "truck-distances/add";
    }

    // Handle form submission for creating multiple truck distances
    @PostMapping("/create")
    public String createTruckDistances(@ModelAttribute TruckDistanceForm form, 
                                      BindingResult result, 
                                      Model model) {
        if (result.hasErrors()) {
            model.addAttribute("trucks", truckService.getAll());
            return "truck-distances/add";
        }
        
        // Get current user (you'll need to implement this based on your auth system)
        User currentUser = userService.getCurrentUser();
        
        // Create and save each truck distance
        for (TruckDistanceDto dto : form.getDistances()) {
            Truck truck = truckService.findById(dto.getTruckId()).orElse(null);
            TruckDistance distance = new TruckDistance();
            distance.setDate(dto.getDate());
            distance.setTruck(truck);
            distance.setDistance(dto.getDistance());
            distance.setCreatedBy(currentUser);
            
            truckDistanceService.save(distance);

            double newKm = truck.getCurrentKm() + dto.getDistance();
            truck.setCurrentKm(newKm);

            truckRepo.save(truck);
        }
        
        return "redirect:/truck-distances?success";
    }


    // Show edit form for a single truck distance
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        TruckDistance distance = truckDistanceService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid truck distance ID: " + id));
        
        List<Truck> trucks = truckService.getAll();
        
        // Convert to DTO for editing
        TruckDistanceDto dto = new TruckDistanceDto();
        dto.setId(distance.getId());
        dto.setDate(distance.getDate());
        dto.setTruckId(distance.getTruck().getId());
        dto.setDistance(distance.getDistance());
        
        model.addAttribute("trucks", trucks);
        model.addAttribute("distance", dto);
        model.addAttribute("id", id);
        
        return "truck-distances/edit";
    }

    // Handle edit form submission
    @PostMapping("/edit/{id}")
    public String updateTruckDistance(@PathVariable("id") Long id, 
                                    @ModelAttribute("distance") TruckDistanceDto dto,
                                    BindingResult result, 
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            dto.setId(id);
            model.addAttribute("trucks", truckService.getAll());
            return "truck-distances/edit";
        }
        
        try {
            TruckDistance distance = truckDistanceService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid truck distance ID: " + id));
            
            // Get current user for updatedBy field
            User currentUser = userService.getCurrentUser();
            Truck truck = truckService.findById(dto.getTruckId()).orElse(null);
            // Update fields
            distance.setDate(dto.getDate());
            distance.setTruck(truck);
            distance.setDistance(dto.getDistance());
            distance.setUpdatedBy(currentUser);
            
            truckDistanceService.save(distance);

            double newKm = truck.getCurrentKm() + dto.getDistance();
            truck.setCurrentKm(newKm);

            truckRepo.save(truck);
            
            redirectAttributes.addFlashAttribute("success", "Distance record updated successfully");
            return "redirect:/truck-distances";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating distance record: " + e.getMessage());
            return "redirect:/truck-distances/edit/" + id;
        }
    }



    // Show multi-edit form
    @GetMapping("/edit-multi")
    public String showMultiEditForm(@RequestParam("ids") List<Long> ids, Model model) {
        List<TruckDistance> distances = truckDistanceService.findAllById(ids);
        List<Truck> trucks = truckService.getAll();
        
        // Convert to DTOs for editing
        TruckDistanceForm form = new TruckDistanceForm();
        for (TruckDistance distance : distances) {
             if (distance != null) {
                TruckDistanceDto dto = new TruckDistanceDto();
                dto.setId(distance.getId());
                dto.setDate(distance.getDate());
                dto.setTruckId(distance.getTruck().getId());
                dto.setDistance(distance.getDistance());
                form.getDistances().add(dto);
            }
        }
        
        model.addAttribute("trucks", trucks);
        model.addAttribute("truckDistanceForm", form);
        model.addAttribute("isEditMode", true);
        return "truck-distances/edit-multi";
    }

    // Handle multi-edit form submission
    @PostMapping("/edit-multi")
    public String updateTruckDistances(@ModelAttribute TruckDistanceForm form, 
                                    BindingResult result, 
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("trucks", truckService.getAll());
            model.addAttribute("isEditMode", true);
            return "truck-distances/add";
        }
        
        try {
            int updatedCount = 0;
            for (TruckDistanceDto dto : form.getDistances()) {
                if (dto.getId() != null) {
                    TruckDistance distance = truckDistanceService.findById(dto.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid truck distance ID: " + dto.getId()));
                    Truck truck = truckService.findById(dto.getTruckId()).orElse(null);
                    // Update fields
                    distance.setDate(dto.getDate());
                    distance.setTruck(truck);
                    distance.setDistance(dto.getDistance());
                    
                    truckDistanceService.save(distance);


                    double newKm = truck.getCurrentKm() + dto.getDistance();
                    truck.setCurrentKm(newKm);

                    truckRepo.save(truck);
                    updatedCount++;
                }
            }
            
            redirectAttributes.addFlashAttribute("success", "Successfully updated " + updatedCount + " distance entries");
            return "redirect:/truck-distances";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating distance records: " + e.getMessage());
            return "redirect:/truck-distances";
        }
    }

        // Handle delete request
        @GetMapping("/delete/{id}")
        public String deleteTruckDistance(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
            try {
                TruckDistance distance = truckDistanceService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid truck distance ID: " + id));
                // Truck truck = distance.getTruck();

                // double newKm = truck.getCurrentKm() - distance.getDistance();
                // truck.setCurrentKm(Math.max(newKm, 0));
                // truckRepo.save(truck);
                truckDistanceService.delete(id);
                redirectAttributes.addFlashAttribute("success", "Distance record deleted successfully");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Error deleting distance record: " + e.getMessage());
            }
            
            return "redirect:/truck-distances";
        }



        // upload excel file
        @GetMapping("/import")
        public String showImportForm() {
            return "truck-distances/import"; 
        }

        @PostMapping("/import")
        public String importExcel(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
            try {
                List<TruckDistance> distances = truckDistanceService.importFromExcel(file, truckService);
                truckDistanceService.saveAll(distances);
                redirectAttributes.addFlashAttribute("success", "File imported successfully!");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Failed to import file: " + e.getMessage());
            }
            return "redirect:/truck-distances";
        }

}