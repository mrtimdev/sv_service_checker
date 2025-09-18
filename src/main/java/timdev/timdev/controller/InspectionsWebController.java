package timdev.timdev.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import timdev.timdev.dto.CustomUserDetails;
import timdev.timdev.dto.InspectionRequestDTO;
import timdev.timdev.entity.Inspection;
import timdev.timdev.entity.Truck;
import timdev.timdev.entity.User;
import timdev.timdev.service.InspectionService;
import timdev.timdev.service.TruckService;

@AllArgsConstructor
@Controller
@RequestMapping("/inspections")
public class InspectionsWebController {

    private final InspectionService inspectionService;

    private final TruckService truckService;


    @GetMapping
    public String list(
        Model model,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "10") String sizeParam,
        @RequestParam(value = "all", defaultValue = "false") boolean showAll,
        @RequestParam(value = "licensePlate", required = false) String licensePlate,
        @RequestParam(value = "fromDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate fromDate,
        @RequestParam(value = "toDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate toDate,
        @RequestParam(value = "expiredFromDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate expiredFromDate,
        @RequestParam(value = "expiredToDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate expiredToDate
    ) {
        model.addAttribute("inspections", inspectionService.findAll());
        List<Inspection> inspections;
        int totalPages = 1;
        int size;
        
        if ("all".equalsIgnoreCase(sizeParam)) {
            size = Integer.MAX_VALUE;
        } else {
            size = Integer.parseInt(sizeParam); 
        }


         if (showAll) {
            inspections = inspectionService.findAllFiltered(
                licensePlate, fromDate, toDate, expiredFromDate, expiredToDate
            );
        } else {
            Pageable pageable = PageRequest.of(page, size);
            Page<Inspection> itemsPage = inspectionService.findAllFilteredWithPageable(
                licensePlate, fromDate, toDate, expiredFromDate, expiredToDate, pageable
            );
            inspections = itemsPage.getContent();
            totalPages = itemsPage.getTotalPages();
        }
 
        // inspections.sort(Comparator.comparingLong(Inspection::expiredDurationDays));

        model.addAttribute("inspections", inspections);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", sizeParam);
        model.addAttribute("showAll", showAll);
        model.addAttribute("licensePlate", licensePlate);  
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("expiredFromDate", expiredFromDate);
        model.addAttribute("expiredToDate", expiredToDate);
        return "inspections/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        
        InspectionRequestDTO inspectionDTO = new InspectionRequestDTO();
        model.addAttribute("inspectionForm", inspectionDTO);
        model.addAttribute("inspection", new Inspection());
        model.addAttribute("trucks", truckService.getAll());
        model.addAttribute("currentDate", LocalDate.now());
        return "inspections/form";
    }

    @PostMapping("/save")
    public String save(
        @Valid @ModelAttribute("inspectionForm") InspectionRequestDTO inspectionDTO,
        BindingResult result,
        Model model,
        RedirectAttributes redirectAttributes,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (result.hasErrors()) {
            model.addAttribute("inspectionForm", inspectionDTO);
            model.addAttribute("inspection", new Inspection());
            model.addAttribute("trucks", truckService.getAll());
            return "inspections/form";
        }
        User user = userDetails.getUser();
        Inspection inspection;
        if (inspectionDTO.getId() != null) {
            inspection = inspectionService.findById(inspectionDTO.getId()).orElse(new Inspection());
            inspection.setUpdatedAt(LocalDateTime.now());
            inspection.setUpdatedBy(user);
        } else {
            inspection = new Inspection();
        }

        inspection.setDate(inspectionDTO.getDate());
        inspection.setExpiredDate(inspectionDTO.getExpiredDate());
        inspection.setQuantity(inspectionDTO.getQuantity());
        inspection.setNote(inspectionDTO.getNote());
        Truck truck = truckService.findById(inspectionDTO.getTruckId()).orElse(null);
        inspection.setTruck(truck);

        
        inspection.setCreatedBy(user);

        truck.setExpiredDate(inspectionDTO.getExpiredDate());
        truckService.save(truck);
        


        inspection.setCreatedAt(LocalDateTime.now());
        

        inspectionService.save(inspection);

        redirectAttributes.addFlashAttribute("success", truck.getLicensePlate()+  " Inspection saved successfully.");
        return "redirect:/inspections";
    }

    @GetMapping("/edit/{id}")
    public String editInspection(@PathVariable Long id, Model model) {
        Inspection inspection = inspectionService.findById(id).orElse(null);
        InspectionRequestDTO dto = toDto(inspection);
        model.addAttribute("inspectionForm", dto);
        model.addAttribute("inspection", new Inspection());
        model.addAttribute("trucks", truckService.getAll());
        return "inspections/edit";
    }

    private InspectionRequestDTO toDto(Inspection inspection) {
        if (inspection == null) return null;
        InspectionRequestDTO dto = new InspectionRequestDTO();
        dto.setId(inspection.getId());
        dto.setDate(inspection.getDate());
        dto.setTruckId(inspection.getTruck() != null ? inspection.getTruck().getId() : null);
        dto.setExpiredDate(inspection.getExpiredDate());
        dto.setQuantity(inspection.getQuantity());
        dto.setNote(inspection.getNote());
        return dto;
    }



    @GetMapping("/delete/{id}")
    public String delete(
        @PathVariable Long id,
        RedirectAttributes redirectAttributes
    ) {
        Inspection inspection = inspectionService.findById(id).orElse(null);
        Truck truck = truckService.findById(inspection.getTruck().getId()).orElse(null);
        inspectionService.delete(id);
        redirectAttributes.addFlashAttribute("success", truck.getLicensePlate() + " Inspection deleted successfully.");
        return "redirect:/inspections";
    }
}