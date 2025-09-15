package timdev.timdev.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import timdev.timdev.dto.TruckRequestDTO;
import timdev.timdev.entity.Truck;
import timdev.timdev.entity.TruckFatsReport;
import timdev.timdev.entity.TruckOilsReport;
import timdev.timdev.enums.OilStatus;
import timdev.timdev.service.ModelService;
import timdev.timdev.service.TruckFatsReportService;
import timdev.timdev.service.TruckOilsReportService;
import timdev.timdev.service.TruckService;

@AllArgsConstructor
@Controller
@RequestMapping("/admin/trucks")
public class TruckWebController {

    private final TruckService truckService;
    private final ModelService modelService;
    private final TruckFatsReportService fatsReportService;
    private final TruckOilsReportService oilReportService;


    @GetMapping
    public String index(Model model,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "10") String sizeParam,
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
        model.addAttribute("truck", new TruckRequestDTO());
        model.addAttribute("models", modelService.getAll());
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
        dto.setModelId(t.getModel().getId());
        dto.setYear(t.getYear());
        dto.setKmForFatsShoot(t.getKmForFatsShoot());

        model.addAttribute("truck", dto);
        model.addAttribute("models", modelService.getAll());
        return "trucks/form";
    }

    @PostMapping({"/", "/update/{id}"})
    public String createOrUpdateTruck(@PathVariable(required = false) Long id,
                                    @Valid @ModelAttribute("truck") TruckRequestDTO truckDTO,
                                    BindingResult result,
                                    Model model, RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("models", modelService.getAll());
            return "trucks/form";
        }

        try {
            Truck truck;
            if (id != null) {
                truck = truckService.findById(id).orElse(new Truck());
                truck.setId(id);
            } else {
                truck = new Truck();
            }

            truck.setLicensePlate(truckDTO.getLicensePlate());
            truck.setYear(truckDTO.getYear());
            truck.setKmForFatsShoot(truckDTO.getKmForFatsShoot());

            // Set the model
            modelService.findById(truckDTO.getModelId()).ifPresent(truck::setModel);

            truckService.save(truck);
            redirectAttributes.addFlashAttribute("success", (id == null ? "Truck created successfully!" : "Truck updated successfully!"));
            return "redirect:/admin/trucks";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Something went wrong: " + e.getMessage());
            model.addAttribute("models", modelService.getAll());
            return "trucks/form";
        }
    }


    @PostMapping("/create")
    public String createTruck(@Valid @ModelAttribute("truck") TruckRequestDTO truckDTO,
                            BindingResult result,
                            Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("models", modelService.getAll());
            return "trucks/form";
        }

        try {
            Truck truck = new Truck();
            truck.setLicensePlate(truckDTO.getLicensePlate());
            truck.setYear(truckDTO.getYear());
            truck.setKmForFatsShoot(truckDTO.getKmForFatsShoot());

            // Set the model
            modelService.findById(truckDTO.getModelId()).ifPresent(truck::setModel);

            truckService.save(truck);
            redirectAttributes.addFlashAttribute("success", "Truck created successfully!");
            return "redirect:/admin/trucks";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Something went wrong: " + e.getMessage());
            model.addAttribute("models", modelService.getAll());
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
                if(truck.getLastFatsReport() != null) {
                    redirectAttributes.addFlashAttribute("error", "This Truck can not delete");
                    return "redirect:/admin/trucks";
                }
                truckService.deleteById(id);
                redirectAttributes.addFlashAttribute("success", "Truck deleted successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Something went wrong: " + e.getMessage());
        }
        return "redirect:/admin/trucks";
    }



    

    // @GetMapping("/fats/edit/{id}")
    // public String showFormEditFatsShooted(@PathVariable Long id, Model model) {

    //     TruckFatsReport report = fatsReportService.findById(id)
    //             .orElseThrow(() -> new RuntimeException("Truck not found"));

    //     model.addAttribute("report", report);
    //     return "trucks/edit_change_fats";
    // }

    // @GetMapping("/fats/delete/{id}")
    // public String deleteFatsShooted(@PathVariable Long id, Model model) {

    //     TruckFatsReport report = fatsReportService.findById(id)
    //             .orElseThrow(() -> new RuntimeException("Truck not found"));

    //     model.addAttribute("report", report);
    //     return "redirect:/admin/trucks?success=Fats report deleted successfully!";
    // }











    //  Shoot fats
    @GetMapping("/shoot/fats")
    public String indexShootFats(Model model,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "10") String sizeParam,
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
        return "fats_shoot/list";
    }

    @GetMapping("/fats/shoot")
    public String showAddDistanceForm(@RequestParam(name = "truck_id", required = false) Long truckId, Model model) {

        Truck truck = truckService.findById(truckId)
                .orElseThrow(() -> new RuntimeException("Truck not found"));

        TruckFatsReport report = new TruckFatsReport();
        report.setTruck(truck);
        report.setStatus(OilStatus.COMPLETED);
        report.setCurrentKm(truck.getCurrentKm());

        model.addAttribute("truck", truck);
        model.addAttribute("truckFatsReport", report);
        model.addAttribute("statuses", OilStatus.values());
        model.addAttribute("currentDate", LocalDate.now());
        return "fats_shoot/shoot";
    }

    @PostMapping("/fats/shoot/{id}")
    public String saveChangeFats(
        @PathVariable("id") Long truckId,
        @Valid @ModelAttribute("truckFatsReport") TruckFatsReport report,
        BindingResult result,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        Truck truck = truckService.findById(truckId)
                .orElseThrow(() -> new RuntimeException("Truck not found"));

        if (result.hasErrors()) {
            model.addAttribute("truck", truck);
            model.addAttribute("statuses", OilStatus.values());
            model.addAttribute("currentDate", LocalDate.now());
            return "fats_shoot/shoot";
        }

        // calculate next range
        Double nextKmForFatShot = truck.getKmForFatsShoot() + truck.getCurrentKm();

        // always set truck & timestamps
        report.setId(null);
        report.setTruck(truck);
        report.setStatus(report.getStatus());
        report.setNextRange(nextKmForFatShot);
        report.setStatus(OilStatus.COMPLETED);
        report.setKmForFatsShoot(truck.getKmForFatsShoot());
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());
        fatsReportService.save(report);
        

        truck.setNextFatsRange(nextKmForFatShot);

        truck.setStatus(report.getStatus());


        truckService.save(truck);

        redirectAttributes.addFlashAttribute("success", "Truck "+ truck.getLicensePlate() + " បានបាញ់ខ្លាញ់ដោយជោគជ័យ");

        return "redirect:/admin/trucks/shoot/fats";
    }

    @GetMapping("/fats/reports")
    public String fatsReports(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") String sizeParam,
            @RequestParam(value = "all", defaultValue = "false") boolean showAll,
            @RequestParam(value = "truck_id", required = false) Long truckId,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate toDate
    ) {
        // load trucks for filter dropdown
        List<Truck> trucks = truckService.getAll();

        List<TruckFatsReport> reports;
        int totalPages = 1;
        int size;
        
        if ("all".equalsIgnoreCase(sizeParam)) {
            size = Integer.MAX_VALUE;
        } else {
            size = Integer.parseInt(sizeParam); 
        }
        if (showAll) {
            // fetch all reports with filter
            reports = fatsReportService.getAllFiltered(truckId, fromDate, toDate);
        } else {
            Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
            Page<TruckFatsReport> truckPage = fatsReportService.getAllWithPageable(pageable, truckId, fromDate, toDate);
            reports = truckPage.getContent();
            totalPages = truckPage.getTotalPages();
        }

        // put everything into model
        model.addAttribute("reports", reports);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", sizeParam);
        model.addAttribute("showAll", showAll);

        // preserve filters in the view
        model.addAttribute("selectedTruckId", truckId != null ? truckId : null);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("trucks", trucks);

        return "fats_shoot/reports";
    }




    // oils
    @GetMapping("/change/oils")
    public String indexChangeOils(Model model,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "10") String sizeParam,
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
        return "oils_change/list";
    }

    @GetMapping("/oils/change")
    public String showOilChangeForm(@RequestParam(name = "truck_id", required = false) Long truckId, Model model) {

        Truck truck = truckService.findById(truckId)
                .orElseThrow(() -> new RuntimeException("Truck not found"));

        TruckOilsReport report = new TruckOilsReport();
        report.setTruck(truck);
        report.setStatus(OilStatus.COMPLETED);
        report.setCurrentKm(truck.getCurrentKm());

        model.addAttribute("truck", truck);
        model.addAttribute("truckOilsReport", report);
        model.addAttribute("statuses", OilStatus.values());
        model.addAttribute("currentDate", LocalDate.now());
        return "oils_change/change";
    }

    @PostMapping("/oils/change/{id}")
    public String saveChangeOil(
        @PathVariable("id") Long truckId,
        @Valid @ModelAttribute("truckOilsReport") TruckOilsReport report,
        BindingResult result,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        Truck truck = truckService.findById(truckId)
                .orElseThrow(() -> new RuntimeException("Truck not found"));

        if (result.hasErrors()) {
            model.addAttribute("truck", truck);
            model.addAttribute("statuses", OilStatus.values());
            model.addAttribute("currentDate", LocalDate.now());
            return "oils_change/change";
        }

        // calculate next range
        Double nextKmForOilsChange = truck.getKmForOilsChange() + truck.getCurrentKm();

        // always set truck & timestamps
        report.setId(null);
        report.setTruck(truck);
        report.setStatus(report.getStatus());
        report.setNextRange(nextKmForOilsChange);
        report.setStatus(OilStatus.COMPLETED);
        report.setKmForOilsChange(truck.getKmForOilsChange());
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());
        oilReportService.save(report);
        

        truck.setNextOilsRange(nextKmForOilsChange);

        truck.setStatus(report.getStatus());


        truckService.save(truck);

        redirectAttributes.addFlashAttribute("success", "Truck "+ truck.getLicensePlate() + " បានបាញ់ខ្លាញ់ដោយជោគជ័យ");

        return "redirect:/admin/trucks/change/oils";
    }

    @GetMapping("/oils/reports")
    public String oilsReports(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") String sizeParam,
            @RequestParam(value = "all", defaultValue = "false") boolean showAll,
            @RequestParam(value = "truck_id", required = false) Long truckId,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate toDate
    ) {
        // load trucks for filter dropdown
        List<Truck> trucks = truckService.getAll();

        List<TruckOilsReport> reports;
        int totalPages = 1;
        int size;
        
        if ("all".equalsIgnoreCase(sizeParam)) {
            size = Integer.MAX_VALUE;
        } else {
            size = Integer.parseInt(sizeParam); 
        }
        if (showAll) {
            // fetch all reports with filter
            reports = oilReportService.getAllFiltered(truckId, fromDate, toDate);
        } else {
            Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
            Page<TruckOilsReport> truckPage = oilReportService.getAllWithPageable(pageable, truckId, fromDate, toDate);
            reports = truckPage.getContent();
            totalPages = truckPage.getTotalPages();
        }

        // put everything into model
        model.addAttribute("reports", reports);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", sizeParam);
        model.addAttribute("showAll", showAll);

        // preserve filters in the view
        model.addAttribute("selectedTruckId", truckId != null ? truckId : null);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("trucks", trucks);

        return "oils_change/reports";
    }








}
