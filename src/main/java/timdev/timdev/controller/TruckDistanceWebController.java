package timdev.timdev.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
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
        @RequestParam(value = "size", defaultValue = "20") String sizeParam,
        @RequestParam(value = "all", defaultValue = "false") boolean showAll, 
        @RequestParam(value = "truck_id", required = false) Long truckId,
        @RequestParam(value = "fromDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate fromDate,
        @RequestParam(value = "toDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate toDate,
        @RequestParam(value = "sortBy", defaultValue = "distanceDate") String sortBy,
        @RequestParam(value = "order", defaultValue = "desc") String order,
        Model model) 
    {
        List<Truck> trucks = truckService.getAll();
        List<TruckDistance> distancesPage;
        int totalPages = 1;
        int size = "all".equalsIgnoreCase(sizeParam) ? Integer.MAX_VALUE : Integer.parseInt(sizeParam);

        // build Sort dynamically
        Sort.Direction direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;

        // map frontend sortBy values to entity fields
        String sortField;
        switch (sortBy) {
            case "truckId":
                sortField = "truck.id";
                break;
            case "truckLicensePlate":
                sortField = "truck.licensePlate"; // assuming field name
                break;
            case "distanceId":
                sortField = "id";
                break;
            case "distanceDate":
            default:
                sortField = "date"; // assuming TruckDistance.date field
                break;
        }

        Sort sort = Sort.by(direction, sortField);
        
        if (showAll) {
            // fetch all reports with filter
            distancesPage = truckDistanceService.getAllFiltered(truckId, fromDate, toDate, sort);
        } else {
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<TruckDistance> truckPage = truckDistanceService.getAllWithPageable(pageable, truckId, fromDate, toDate);
            distancesPage = truckPage.getContent();
            totalPages = truckPage.getTotalPages();
        }


        int totalDistance = (int) distancesPage.stream()
                                       .mapToDouble(TruckDistance::getDistance)
                                       .sum();
        String totalDistanceFormatted = String.format("%,d km", totalDistance);

        // put everything into model
        model.addAttribute("distances", distancesPage);
        model.addAttribute("totalDistance", totalDistanceFormatted);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", sizeParam);
        model.addAttribute("showAll", showAll);

        // preserve filters in the view
        model.addAttribute("selectedTruckId", truckId != null ? truckId : null);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("trucks", trucks);

        model.addAttribute("sortBy", sortBy);
        model.addAttribute("order", order);

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
                         
                
                truckDistanceService.delete(id);
                Truck truck = distance.getTruck(); 
                if (truck != null) {
                    double newKm = truck.getCurrentKm() - distance.getDistance();
                    truck.setCurrentKm(newKm);
                    truck.setUpdatedAt(LocalDateTime.now());
                    truckService.save(truck);
                }
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
                for (TruckDistance td : distances) {
                    Truck truck = td.getTruck();          
                    if (truck != null) {
                        double newKm = truck.getCurrentKm() + td.getDistance();
                        truck.setCurrentKm(newKm);
                        truck.setUpdatedAt(LocalDateTime.now());
                        truckService.save(truck);             
                    }
                }
                redirectAttributes.addFlashAttribute("success", "File imported successfully!");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Failed to import file: " + e.getMessage());
            }
            return "redirect:/truck-distances";
        }

}