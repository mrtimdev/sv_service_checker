package timdev.timdev.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import timdev.timdev.dto.ItemNoteDTO;
import timdev.timdev.entity.Driver;
import timdev.timdev.entity.ServiceChecker;
import timdev.timdev.enums.ServiceCheckerStatus;
import timdev.timdev.service.DriverService;
import timdev.timdev.service.ExcelExportService;
import timdev.timdev.service.InspectionService;
import timdev.timdev.service.ServiceCheckerService;

@Controller
@RequestMapping("/admin/service-checkers")
@RequiredArgsConstructor
public class ServiceCheckerWebController {

    private final ServiceCheckerService serviceCheckerService;
    private final DriverService driverService;
    private final InspectionService inspectionService;

    private final ExcelExportService excelExportService;


    @GetMapping("/filters")
@ResponseBody
public Map<String, Object> getByDateFilterAjax(
    @RequestParam(value = "dateFilter", defaultValue = "all") String dateFilter,
    @RequestParam(value = "startDate", required = false)
    @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate startDate,
    @RequestParam(value = "endDate", required = false)
    @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate endDate,
    @RequestParam(value = "driverId", required = false) Long driverId,
    // DataTables parameters
    @RequestParam(value = "draw", defaultValue = "0") int draw,
    @RequestParam(value = "start", defaultValue = "0") int start,
    @RequestParam(value = "length", defaultValue = "10") int length,
    @RequestParam(value = "search[value]", defaultValue = "") String searchValue,
    @RequestParam(value = "order[0][column]", defaultValue = "0") int orderColumn,
    @RequestParam(value = "order[0][dir]", defaultValue = "asc") String orderDirection
) {
    
    // Get filtered data
    List<ServiceChecker> data = serviceCheckerService.getByDateAndDriverFilter(driverId, dateFilter, startDate, endDate);
    
    // Apply search filter if provided
    if (!searchValue.isEmpty()) {
        data = filterData(data, searchValue);
    }
    
    // Get total records count (before pagination)
    int totalRecords = data.size();
    
    // Apply sorting
    data = sortData(data, orderColumn, orderDirection);
    
    // Apply pagination
    List<ServiceChecker> paginatedData = paginateData(data, start, length);
    
    // Prepare DataTables response
    Map<String, Object> response = new HashMap<>();
    response.put("draw", draw);
    response.put("recordsTotal", totalRecords);
    response.put("recordsFiltered", totalRecords); // Same as total since we filtered in memory
    response.put("data", convertToDataTablesFormat(paginatedData));
    
    return response;
}

// Helper method to filter data based on search value
private List<ServiceChecker> filterData(List<ServiceChecker> data, String searchValue) {
    String searchLower = searchValue.toLowerCase();
    return data.stream()
        .filter(sc -> 
            (sc.getDriver().getFirstName() != null && sc.getDriver().getFirstName().toLowerCase().contains(searchLower)) ||
            (sc.getDriver().getLastName() != null && sc.getDriver().getLastName().toLowerCase().contains(searchLower)) ||
            (sc.getDriver().getNativeName() != null && sc.getDriver().getNativeName().toLowerCase().contains(searchLower)) ||
            (sc.getStatus() != null && sc.getStatus().toString().toLowerCase().contains(searchLower)) ||
            (sc.getDate() != null && sc.getDate().toString().contains(searchValue))
        )
        .collect(Collectors.toList());
}

// Helper method to sort data
private List<ServiceChecker> sortData(List<ServiceChecker> data, int orderColumn, String orderDirection) {
    Comparator<ServiceChecker> comparator;
    
    switch (orderColumn) {
        case 0: // ID
            comparator = Comparator.comparing(ServiceChecker::getId);
            break;
        case 1: // Date
            comparator = Comparator.comparing(ServiceChecker::getDate);
            break;
        case 2: // Driver Name
            comparator = Comparator.comparing(sc -> sc.getDriver().getFirstName() + " " + sc.getDriver().getLastName());
            break;
        case 3: // Plate Number
            comparator = Comparator.comparing(sc -> sc.getDriver().getNativeName());
            break;
        case 4: // Status
            comparator = Comparator.comparing(ServiceChecker::getStatus);
            break;
        case 5: // Created At
            comparator = Comparator.comparing(ServiceChecker::getCreatedAt);
            break;
        default:
            comparator = Comparator.comparing(ServiceChecker::getId);
    }
    
    if ("desc".equalsIgnoreCase(orderDirection)) {
        comparator = comparator.reversed();
    }
    
    return data.stream()
        .sorted(comparator)
        .collect(Collectors.toList());
}

// Helper method to paginate data
private List<ServiceChecker> paginateData(List<ServiceChecker> data, int start, int length) {
    int end = Math.min(start + length, data.size());
    if (start > data.size()) {
        return Collections.emptyList();
    }
    return data.subList(start, end);
}

// Convert ServiceChecker objects to DataTables format
private List<Map<String, Object>> convertToDataTablesFormat(List<ServiceChecker> data) {
    return data.stream().map(sc -> {
        Map<String, Object> row = new HashMap<>();
        row.put("id", sc.getId());
        row.put("date", sc.getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        row.put("driverName", sc.getDriver().getFirstName() + " " + sc.getDriver().getLastName());
        row.put("nativeName", sc.getDriver().getNativeName());
        row.put("checkedCount", sc.getCheckedCount());
        row.put("notCheckedCount", sc.getNotCheckedCount());
        row.put("issuesStatus", sc.issuesStatus());
        row.put("status", sc.getStatus().toString());
        row.put("createdAt", sc.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
        row.put("updatedAt", sc.getUpdatedAt() != null ? 
            sc.getUpdatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) : "Never");
        row.put("updatedBy", sc.getUpdatedBy() != null ? sc.getUpdatedBy().getUsername() : "");
        row.put("actions", getActionButtons(sc.getId()));
        return row;
    }).collect(Collectors.toList());
}

// Generate HTML action buttons
private String getActionButtons(Long id) {
    return "<div class='flex space-x-2'>" +
           "<a href='/admin/service-checkers/" + id + "' class='text-blue-600 hover:text-blue-900'>View</a>" +
           "<a href='/admin/service-checkers/edit/" + id + "' class='text-green-600 hover:text-green-900'>Edit</a>" +
           "<form action='/admin/service-checkers/delete/" + id + "' method='post' style='display: inline;'>" +
           "<input type='hidden' name='_method' value='delete' />" +
           "<button type='submit' class='text-red-600 hover:text-red-900' onclick='return confirm(\"Are you sure?\")'>Delete</button>" +
           "</form>" +
           "</div>";
}



    @GetMapping("/export")
    public ResponseEntity<byte[]> exportServiceCheckersExcel(
        @RequestParam(value = "dateFilter", defaultValue = "all") String dateFilter,
        @RequestParam(value = "startDate", required = false)
        @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate startDate,
        @RequestParam(value = "endDate", required = false)
        @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate endDate,
        @RequestParam(value = "driverId", required = false) Long driverId
    ) throws IOException {

        // Fetch service checkers with items and notes eagerly loaded
        List<ServiceChecker> data = serviceCheckerService.getByDateAndDriverFilter(driverId, dateFilter, startDate, endDate);

        ByteArrayInputStream in = excelExportService.exportToExcel(data);

        // Create filename with timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "service_checkers_with_details_" + timestamp + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(in.readAllBytes());
    }


    @GetMapping("/create")
    public String showCreateForm(Model model) {

        model.addAttribute("serviceChecker", new ServiceChecker());
        model.addAttribute("drivers", driverService.getAllDrivers());
        model.addAttribute("currentDate", LocalDate.now());
        return "service-checkers/create-form";
    }

    @PostMapping
    public String create(@ModelAttribute ServiceChecker serviceChecker, RedirectAttributes redirectAttributes) {

         if (serviceChecker.getDriver() != null && serviceChecker.getDriver().getId() != null) {
            Driver driver = driverService.getDriverById(serviceChecker.getDriver().getId())
                    .orElseThrow(() -> new RuntimeException("Driver not found"));
            if (serviceCheckerService.existsByDriverAndDate(driver, serviceChecker.getDate())) {

                redirectAttributes.addFlashAttribute("error", "A checklist already exists for this driver on " + driver.getFullName());
                return "redirect:/admin/service-checkers/create";
            }
        }
        
        ServiceChecker serviceCheckerCreated = serviceCheckerService.create(serviceChecker);
        redirectAttributes.addFlashAttribute("success", "Service checker created successfully " + serviceCheckerCreated.getTitle());
        return "redirect:/admin/service-checkers";
    }

    @GetMapping
    public String getAll(Model model) {
        List<ServiceChecker> data = serviceCheckerService.getAll();
        model.addAttribute("drivers", driverService.getAllDrivers());
        model.addAttribute("serviceCheckers", data);
        return "service-checkers/index";
    }

    @GetMapping("/{id}")
    public String getById(@PathVariable Long id, Model model) {
        ServiceChecker data = serviceCheckerService.getById(id);
        model.addAttribute("serviceChecker", data);
        return "service-checkers/view";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("drivers", driverService.getAllDrivers());
        model.addAttribute("serviceChecker", serviceCheckerService.getById(id));
        return "service-checkers/edit-form";
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute ServiceChecker serviceChecker) {
        serviceCheckerService.update(id, serviceChecker);
        return "redirect:/admin/service-checkers";
    }

    // @DeleteMapping("/{id}")
    // public String deleteByFormSubmit(@PathVariable Long id) {
    //     serviceCheckerService.delete(id);
    //     return "redirect:/admin/service-checkers";
    // }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            serviceCheckerService.delete(id);
            return ResponseEntity.ok().body(Map.of(
                "status", "success",
                "message", "Service checker deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", "Failed to delete service checker: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/bulk-delete")
    @ResponseBody
    public ResponseEntity<?> bulkDelete(@RequestBody Map<String, List<Long>> request) {
        try {
            List<Long> ids = request.get("ids");
            serviceCheckerService.deleteAllById(ids);
            return ResponseEntity.ok().body(Map.of(
                "status", "success",
                "message", ids.size() + " items deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", "Failed to delete items: " + e.getMessage()
            ));
        }
    }



    @PatchMapping("/{id}/status")
    @ResponseBody
    public ResponseEntity<?> updateStatus(
        @PathVariable Long id,
        @RequestBody Map<String, String> request) {
        
        try {
            ServiceCheckerStatus status = ServiceCheckerStatus.valueOf(request.get("status"));
            serviceCheckerService.updateStatus(id, status);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                Map.of("message", "Invalid status value"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }



    @GetMapping("/new")
    public String showCreateFormNew(Model model) {
        model.addAttribute("drivers", driverService.getAllDrivers());
        model.addAttribute("currentDate", LocalDate.now());
        model.addAttribute("serviceChecker", new ServiceChecker());
        model.addAttribute("categories", inspectionService.getAllCategoriesWithItems());
        return "service-checkers/new-form";
    }

    @PostMapping("/new")
    public String submitForm(
        @ModelAttribute("serviceChecker") ServiceChecker checker,
        @RequestParam Map<String, String> allParams,
        BindingResult result,
        RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "admin/service-checkers/form";
        }

        try {
            if (checker.getDriver() != null && checker.getDriver().getId() != null) {
                Driver driver = driverService.getDriverById(checker.getDriver().getId())
                        .orElseThrow(() -> new RuntimeException("Driver not found"));
                if (serviceCheckerService.existsByDriverAndDate(driver, checker.getDate())) {

                    redirectAttributes.addFlashAttribute("error", "A checklist already exists for this driver on " + driver.getFullName());
                    return "redirect:/admin/service-checkers/new";
                }
            }
            Map<Long, List<ItemNoteDTO>> categoryItems = processFormParameters(allParams);
            serviceCheckerService.createWithInspections(checker, categoryItems);
            redirectAttributes.addFlashAttribute("success", "Service checker created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating service checker: " + e.getMessage());
        }

        return "redirect:/admin/service-checkers";
    }



    @GetMapping("/new/edit/{id}")
    public String showEditFormNew(@PathVariable Long id, Model model) {
        ServiceChecker checker = serviceCheckerService.getById(id);
        
        model.addAttribute("drivers", driverService.getAllDrivers());
        model.addAttribute("serviceChecker", checker);
        model.addAttribute("categories", inspectionService.getAllCategoriesWithItems());
        return "service-checkers/new-edit-form";
    }

    @PostMapping("/new/update/{id}")
    public String updateServiceChecker(
        @PathVariable Long id,
        @ModelAttribute ServiceChecker checker,
        @RequestParam Map<String, String> params,
        RedirectAttributes redirectAttributes) {
        
        try {
            if (checker.getDriver() != null && checker.getDriver().getId() != null) {
                Driver driver = driverService.getDriverById(checker.getDriver().getId())
                        .orElseThrow(() -> new RuntimeException("Driver not found"));

                if (serviceCheckerService.existsByDriverAndDateAndIdNot(driver, checker.getDate(), id)) {
                    redirectAttributes.addFlashAttribute("error",
                        "A checklist already exists for this driver on " + driver.getFullName());
                    return "redirect:/admin/service-checkers/new/edit/" + id;
                }
            }
            Map<Long, List<ItemNoteDTO>> categoryItems = processFormParameters(params);
            serviceCheckerService.updateWithInspections(id, checker, categoryItems);
            redirectAttributes.addFlashAttribute("success", "Service checker updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating service checker: " + e.getMessage());
        }
        
        return "redirect:/admin/service-checkers";
    }

    private Map<Long, List<ItemNoteDTO>> processFormParameters(Map<String, String> params) {
        Map<Long, List<ItemNoteDTO>> categoryItems = new HashMap<>();
        
        // Find all category parameters
        params.keySet().stream()
            .filter(key -> key.startsWith("category_"))
            .forEach(key -> {
                Long categoryId = Long.valueOf(key.substring("category_".length()));
                String[] itemIds = params.get(key).split(",");
                
                List<ItemNoteDTO> notes = Arrays.stream(itemIds)
                    .map(itemId -> {
                        boolean passed = "true".equals(params.get("passed_" + itemId));
                        String note = params.get("note_" + itemId);
                        return new ItemNoteDTO(Long.parseLong(itemId), passed, note);
                    })
                    .collect(Collectors.toList());
                
                categoryItems.put(categoryId, notes);
            });
        
        return categoryItems;
    }
}