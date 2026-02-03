package timdev.timdev.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import timdev.timdev.entity.Average;
import timdev.timdev.entity.Measurement;
import timdev.timdev.entity.Truck;
import timdev.timdev.repository.MeasurementRepository;
import timdev.timdev.service.AverageService;
import timdev.timdev.service.TruckService;

@Controller
@AllArgsConstructor
@RequestMapping("/averages")
public class AverageController {

    private TruckService truckService;
    private MeasurementRepository measurementRepo;
    private AverageService averageService;


    @GetMapping({"", "/list"})
    public String list(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") String sizeParam,
            @RequestParam(value = "truckId", required = false) Long truckId,
            @RequestParam(value = "export", required = false) String export,
            @RequestParam(value = "format", defaultValue = "csv") String exportFormat,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes
    ) throws IOException {

        // ===== SIZE / ALL =====
        boolean fetchAll = "all".equalsIgnoreCase(sizeParam);
        int size = fetchAll ? Integer.MAX_VALUE : 20;
        if (!fetchAll) {
            try {
                size = Integer.parseInt(sizeParam);
            } catch (NumberFormatException e) {
                size = 20;
            }
        }

        // ===== SORT =====
        Sort sort = Sort.by(Sort.Direction.DESC, "id");

        Pageable pageable = fetchAll
                ? Pageable.unpaged()
                : PageRequest.of(page, size, sort);

        // ===== FETCH DATA =====
        Page<Average> averagePage = averageService.findByTruckWithFilter(truckId, pageable);

        // ===== PAGINATION METADATA =====
        int totalPages = averagePage.getTotalPages();
        long totalElements = averagePage.getTotalElements();
        int startIndex = fetchAll ? 1 : page * size + 1;
        int endIndex = fetchAll ? (int) totalElements : startIndex + averagePage.getNumberOfElements() - 1;

        List<Truck> trucks = truckService.getAll();
        Truck selectedTruck;
        if(truckId != null) {
            selectedTruck = truckService.findById(truckId).orElse(null);
            model.addAttribute("selectedTruck", selectedTruck);
        } else {
            model.addAttribute("selectedTruck", null);
        }

        model.addAttribute("trucks", trucks);
        model.addAttribute("averages", averagePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", sizeParam);
        model.addAttribute("pageSizeNumber", size);
        model.addAttribute("startIndex", startIndex);
        model.addAttribute("endIndex", endIndex);
        model.addAttribute("totalElements", totalElements);
        model.addAttribute("truckId", truckId);

        // ===== PREPARE PIVOT DATA FOR VIEW =====
        Map<Long, Map<String, String>> pivotData = averageService.getPivotData(truckId);

        // ===== OPTIONAL CSV EXPORT =====
        if (export != null) {
            List<Average> averages = averagePage.getContent();
            
            if ("excel".equalsIgnoreCase(exportFormat) || "xlsx".equalsIgnoreCase(exportFormat)) {
                selectedTruck = truckId != null ? truckService.findById(truckId).orElse(null) : null;
                String fileName = generateExportFileName(selectedTruck, "xlsx");
                // averageService.exportToExcel(averages, selectedTruck, response, fileName);
                averageService.exportToExcelPivot(pivotData, selectedTruck, response, fileName);
                return null;
            } else {
                // Default CSV export
                // averageService.exportToCsv(averages, response);
                averageService.exportToCsvPivot(pivotData, response);
                return null;
            }
        }

        return "averages/index";
    }

    private String generateExportFileName(Truck truck, String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String truckPart = truck != null ? 
                "_" + truck.getLicensePlate().replaceAll("[^a-zA-Z0-9]", "_") : 
                "_all_trucks";
        return "averages" + truckPart + "_" + timestamp + "." + extension;
    }


    // Show form for new or edit Average
    @GetMapping("form")
    public String viewForm(Model model, @PathVariable(required = false) Long id) {

        Average avg;
        if (id != null) {
            avg = averageService.getById(id);
        } else {
            avg = new Average();
            avg.setValue(0);
        }

        List<Truck> trucks = truckService.findTrucksWithoutAverages(null);
        List<Measurement> measurements = measurementRepo.findAll();

        model.addAttribute("trucks", trucks);
        model.addAttribute("measurements", measurements);
        model.addAttribute("average", avg);

        return "averages/create-form";
    }

    @GetMapping("/form/{id}")
    public String editForm(Model model, @PathVariable(required = false) Long id) {

        Average avg;
        if (id != null) {
            avg = averageService.getById(id);
        } else {
            avg = new Average();
            avg.setValue(0);
        }

        List<Truck> trucks = truckService.findTrucksWithoutAverages(avg.getTruck() != null ? avg.getTruck().getId() : null);
        List<Measurement> measurements = measurementRepo.findAll();

        model.addAttribute("trucks", trucks);
        model.addAttribute("measurements", measurements);
        model.addAttribute("average", avg);

        return "averages/edit";
    }

    
    @PostMapping("/save-multiple")
    public String saveMultiple(
            @RequestParam Long truckId,
            @RequestParam Map<String, String> params,
            @RequestParam(value = "action", required = false) String action,
            RedirectAttributes ra
    ) {
        try {
            Truck truck = truckService.findById(truckId)
                    .orElseThrow(() -> new RuntimeException("Truck not found with id: " + truckId));
            averageService.saveMultiple(truckId, params);
            ra.addFlashAttribute("success", "Averages with Measurements for " + truck.getLicensePlate() + " saved successfully");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error saving averages: " + e.getMessage());
            // Consider redirecting back to form with preserved values
            return "redirect:/averages/form"; // or wherever your form is
        }
        if ("save_continue".equals(action)) {
            return "redirect:/averages/form";
        }
        return "redirect:/averages";
    }
    // Save or Update Average
    @PostMapping("/save")
    public String saveAverage(
            @RequestParam(required = false) Long id,
            @RequestParam Long truckId,
            @RequestParam Long measurementId,
            @RequestParam double value,
            RedirectAttributes ra
    ) {
        try {
            if (id != null) {
                // update existing
                averageService.updateAverage(id, truckId, measurementId, value);
                ra.addFlashAttribute("success", "Average updated successfully!");
            } else {
                // create new
                averageService.addAverage(truckId, measurementId, value);
                ra.addFlashAttribute("success", "Average saved successfully!");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/averages";
    }


    // Delete average
    @GetMapping("/delete/{id}")
    public String deleteAverage(@PathVariable Long id, RedirectAttributes ra) {
        try {
            averageService.deleteById(id);
            ra.addFlashAttribute("success", "Average deleted successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/averages/list";
    }

    @ResponseBody
    @GetMapping(value ="/ajax/measurements/by-truck", produces = "application/json")
    public List<Map<String, Object>> getMeasurementsWithAverage(
            @RequestParam("truckId") Long truckId
    ) {

        // Get all measurements with their average for this truck
        return averageService.getMeasurementsByTruck(truckId)
                .stream()
                .map(m -> {
                    Map<String, Object> map = new HashMap<>();

                    // Get average for this truck + measurement
                    Double avgValue = averageService.getAverageValue(truckId, m.getId());

                    map.put("id", m.getId());
                    map.put("name", m.getName());
                    map.put("text", m.getName() + " (" + m.getNativeName() + ")" +
                            (avgValue != null ? " - Avg: " + avgValue : ""));
                    map.put("average", avgValue);

                    return map;
                })
                .toList();
    }
}
