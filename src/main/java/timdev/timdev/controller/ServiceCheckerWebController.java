package timdev.timdev.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import timdev.timdev.dto.AssignedVehicleDTO;
import timdev.timdev.dto.CustomUserDetails;
import timdev.timdev.dto.ExternalDriverDTO;
import timdev.timdev.dto.ItemNoteDTO;
import timdev.timdev.entity.Driver;
import timdev.timdev.entity.ServiceChecker;
import timdev.timdev.entity.User;
import timdev.timdev.enums.ServiceCheckerStatus;
import timdev.timdev.service.DriverProxyService;
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

    private final DriverProxyService driverProxyService;


    @GetMapping("/filters")
    @ResponseBody
    public Map<String, Object> getByDateFilterAjax(
        @RequestParam(value = "dateFilter", defaultValue = "all") String dateFilter,
        @RequestParam(value = "startDate", required = false)
        @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate startDate,
        @RequestParam(value = "endDate", required = false)
        @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate endDate,
        @RequestParam(value = "driverId", required = false) Long driverId,
        @RequestParam(value = "truckType", required = false) String truckType,
        // DataTables parameters
        @RequestParam(value = "draw", defaultValue = "0") int draw,
        @RequestParam(value = "start", defaultValue = "0") int start,
        @RequestParam(value = "length", defaultValue = "10") int length,
        @RequestParam(value = "search[value]", defaultValue = "") String searchValue,
        @RequestParam(value = "order[0][column]", defaultValue = "0") int orderColumn,
        @RequestParam(value = "order[0][dir]", defaultValue = "asc") String orderDirection,
        Authentication authentication,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        
        // Get filtered data
        List<ServiceChecker> data = serviceCheckerService.getByDateAndDriverFilter(driverId, dateFilter, startDate, endDate);
        if (orderColumn == 0 && "asc".equalsIgnoreCase(orderDirection)) {
            orderColumn = 1;       // Date column index
            orderDirection = "desc";
        }
        if (truckType != null && !truckType.isEmpty()) {
            data = data.stream()
                .filter(sc -> {
                    ExternalDriverDTO exDriver = sc.getExDriver() != null 
                    ? driverProxyService.getDriverById(sc.getExDriver().getId()) 
                    : null;

                    return exDriver != null
                        && exDriver.getAssignedVehicle() != null
                        && truckType.equals(exDriver.getAssignedVehicle().getTruckSize());
                })
                .collect(Collectors.toList());
        }
        // Apply search filter if provided
        if (!searchValue.isEmpty()) {
            data = filterData(data, searchValue, null);
        }
        
        
        
        // Apply sorting
        data = sortData(data, orderColumn, orderDirection);

        User user = userDetails.getUser();

        if (authentication != null && authentication.isAuthenticated()) {
            boolean isRoleUser = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));

            if (isRoleUser) {
                // Filter to only show records belonging to the logged-in user
                data = data.stream()
                        .filter(sc -> sc.getCreatedBy() != null 
                                    && sc.getCreatedBy().getId().equals(user.getId()))
                        .collect(Collectors.toList());
            }
        }

        // Get total records count (before pagination)
        int totalRecords = data.size();
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
   private List<ServiceChecker> filterData(List<ServiceChecker> data, String searchValue, String truckType) {
        String searchLower = searchValue.toLowerCase();

        return data.stream()
            .filter(sc -> {
                ExternalDriverDTO exDriver = sc.getExDriver() != null 
                    ? driverProxyService.getDriverById(sc.getExDriver().getId()) 
                    : null;

                boolean matchesDriverName = exDriver != null 
                    && exDriver.getFullName() != null 
                    && exDriver.getFullName().toLowerCase().contains(searchLower);

                boolean matchesLicensePlate = exDriver != null 
                    && exDriver.getAssignedVehicle() != null 
                    && exDriver.getAssignedVehicle().getLicensePlate() != null 
                    && exDriver.getAssignedVehicle().getLicensePlate().toLowerCase().contains(searchLower);

                boolean matchesStatus = sc.getStatus() != null 
                    && sc.getStatus().toString().toLowerCase().contains(searchLower);

                boolean matchesDate = sc.getDate() != null 
                    && sc.getDate().toString().contains(searchValue);


        //         boolean matchesTruck = truckType == null || truckType.isEmpty() 
        // || (exDriver != null 
        //     && exDriver.getAssignedVehicle() != null 
        //     && truckType.equals(exDriver.getAssignedVehicle().getTruckSize()));


                return matchesDriverName || matchesLicensePlate || matchesStatus || matchesDate;
            })
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
                comparator = Comparator.comparing(
                    sc -> {
                        ExternalDriverDTO exDriver = (sc.getExDriver() != null && sc.getExDriver().getId() != null)
                                ? driverProxyService.getDriverById(sc.getExDriver().getId())
                                : null;
                        return exDriver != null && exDriver.getFullName() != null ? exDriver.getFullName() : "";
                    },
                    String.CASE_INSENSITIVE_ORDER
                );
                break;
            case 3: // Plate Number
                // comparator = Comparator.comparing(sc -> sc.getDriver().getPlateNumber());
                comparator = Comparator.comparing(
                    sc -> Optional.ofNullable(sc.getExDriver())
                                .map(ExternalDriverDTO::getAssignedVehicle)
                                .map(v -> v.getLicensePlate() != null ? v.getLicensePlate() : "")
                                .orElse(""),
                    Comparator.nullsLast(String::compareToIgnoreCase)
                );
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
            // Safely get the stored value (could be null)
            ExternalDriverDTO storedExDriver = sc.getExDriver();

            // Safely get the ID (could also be null)
            Long exDriverId = null;
            if (storedExDriver != null) {
                exDriverId = storedExDriver.getId();
            }

            // Call proxy only if we have an ID
            ExternalDriverDTO freshExDriver = null;
            if (exDriverId != null) {
                try {
                    freshExDriver = driverProxyService.getDriverById(exDriverId);
                } catch (Exception e) {
                    // log and ignore if external service fails
                    System.out.println("Cannot fetch driver by id "+ exDriverId + ": " + e.getMessage());
                }
            }
            sc.setExDriver(freshExDriver);
            String driverName = Optional.ofNullable(sc.getExDriver())
                            .map(ExternalDriverDTO::getName)
                            .orElse("Unknown driver");

            String licensePlate = Optional.ofNullable(sc.getExDriver())
                .map(ExternalDriverDTO::getAssignedVehicle)
                .map(AssignedVehicleDTO::getLicensePlate)
                .orElse("Unknown plate");

           String truckType = Optional.ofNullable(sc.getExDriver())
                .map(ExternalDriverDTO::getAssignedVehicle)
                .map(AssignedVehicleDTO::getTruckSize)
                .orElse("Unknown type");

            Map<String, Object> row = new HashMap<>();
            row.put("id", sc.getId());
            row.put("date", sc.getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
            row.put("driverName", driverName);
            row.put("licensePlate", licensePlate);
            row.put("truckType", truckType);
            row.put("checkedCount", sc.getCheckedCount());
            row.put("notCheckedCount", sc.getNotCheckedCount());
            row.put("issuesStatus", sc.issuesStatus());
            row.put("status", sc.getStatus().toString());
            row.put("createdAt", sc.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
            row.put("updatedAt", sc.getUpdatedAt() != null ? 
                sc.getUpdatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) : "Never");
            row.put("updatedBy", sc.getUpdatedBy() != null ? sc.getUpdatedBy().fullName() : "");
            row.put("createdBy", sc.getCreatedBy() != null ? sc.getCreatedBy().fullName() : "");
            row.put("timeAgo", sc.getTimeAgo());
            row.put("hoursSinceEdit", sc.getHoursSinceEdit());
            row.put("editNote", sc.getEditNote());
            row.put("canEdit", sc.canEdit());
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
        @RequestParam(value = "driverId", required = false) Long driverId,
        @RequestParam(value = "truckType", required = false) String truckType,
        Authentication authentication,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws IOException {

        // Fetch service checkers with items and notes eagerly loaded
        List<ServiceChecker> data = serviceCheckerService.getByDateAndDriverFilter(driverId, dateFilter, startDate, endDate);
        if (truckType != null && !truckType.isEmpty()) {
            data = data.stream()
                .filter(sc -> {
                    ExternalDriverDTO exDriver = sc.getExDriver() != null 
                    ? driverProxyService.getDriverById(sc.getExDriver().getId()) 
                    : null;

                    return exDriver != null
                        && exDriver.getAssignedVehicle() != null
                        && truckType.equals(exDriver.getAssignedVehicle().getTruckSize());
                })
                .collect(Collectors.toList());
        }
        User user = userDetails.getUser();

        if (authentication != null && authentication.isAuthenticated()) {
            boolean isRoleUser = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));

            if (isRoleUser) {
                // Filter to only show records belonging to the logged-in user
                data = data.stream()
                        .filter(sc -> sc.getCreatedBy() != null 
                                    && sc.getCreatedBy().getId().equals(user.getId()))
                        .collect(Collectors.toList());
            }
        }
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
    public String create(@ModelAttribute ServiceChecker serviceChecker, 
        RedirectAttributes redirectAttributes,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

         if (serviceChecker.getDriver() != null && serviceChecker.getDriver().getId() != null) {
            Driver driver = driverService.getDriverById(serviceChecker.getDriver().getId())
                    .orElseThrow(() -> new RuntimeException("Driver not found"));
            if (serviceCheckerService.existsByDriverAndDate(driver, serviceChecker.getDate())) {
                LocalDate date = serviceChecker.getDate();
                String formatted = date.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"));
                String message = String.format(
                    "⚠️ Oops! %s already has a checklist for %s. Please check the existing record before creating a new one.",
                    driver.getFullName(),
                    formatted
                );
                
                redirectAttributes.addFlashAttribute("error", message);
                return "redirect:/admin/service-checkers/create";
            }
        }

        User user = userDetails.getUser();
        serviceChecker.setCreatedBy(user);
        
        ServiceChecker serviceCheckerCreated = serviceCheckerService.create(serviceChecker);
        redirectAttributes.addFlashAttribute("success", "Service checker created successfully " + serviceCheckerCreated.getTitle());
        return "redirect:/admin/service-checkers";
    }

    @GetMapping
    public String getAll(Authentication authentication, Model model) {
        List<ExternalDriverDTO> exDrivers = driverProxyService.getAllDrivers();
        model.addAttribute("drivers", exDrivers);

        if (authentication != null && authentication.isAuthenticated()) {
            if(authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"))) {
                    return "service-checkers/index";
            } else  {
                return "service-checkers/admin_index";
            }
        }
        
        return "service-checkers/admin_index";
    }

    @GetMapping("/{id}")
    public String getById(@PathVariable Long id, RedirectAttributes redirectAttributes, Authentication authentication, Model model) {
        
        ServiceChecker data = serviceCheckerService.getById(id);

       

        ExternalDriverDTO exDriver = driverProxyService.getDriverById(data.getExDriver().getId());

        if (exDriver == null) {
            redirectAttributes.addFlashAttribute("error", "Driver not found in external system!");
            return "redirect:/admin/dashboard";
        }
        data.setExDriver(exDriver);
        model.addAttribute("serviceChecker", data);
        if (authentication != null && authentication.isAuthenticated()) {
            if(authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"))) {
                    return "service-checkers/view";
            } else  {
                return "service-checkers/admin_view";
            }
        }
        return "service-checkers/admin_view";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, RedirectAttributes redirectAttributes, Model model) {
        ServiceChecker data = serviceCheckerService.getById(id);

        if (data == null && !data.canEdit()) {
            redirectAttributes.addFlashAttribute("error", "Cannot edit this record now. It's either too old or already edited recently.");
            return "redirect:/admin/service-checkers";
        }
        model.addAttribute("drivers", driverService.getAllDrivers());
        model.addAttribute("serviceChecker", data);
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

    @DeleteMapping("/{id}/by-owner")
    @ResponseBody
    public ResponseEntity<?> deleteByOwner(@PathVariable Long id) {
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
    @GetMapping("/{id}/by-owner")
    @ResponseBody
    public ResponseEntity<?> deleteByOwnerByUrl(@PathVariable Long id) {
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

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> delete(@PathVariable Long id) {
        ServiceChecker checker = serviceCheckerService.getById(id);
        if (checker != null && !checker.canEdit()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "status", "error",
                "message", "Cannot edit this record now. It's either too old or already edited recently."
            ));
        }
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
        List<Long> ids = request.get("ids");
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "No IDs provided"
            ));
        }

        List<Long> notAllowed = new ArrayList<>();

        for (Long id : ids) {
            ServiceChecker checker = serviceCheckerService.getById(id);
            if (checker != null && !checker.canEdit()) {
                notAllowed.add(id);
            }
        }

        if (!notAllowed.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "status", "error",
                "message", "Some records cannot be edited or deleted",
                "ids", notAllowed
            ));
        }

        try {
            for (Long id : ids) {
                serviceCheckerService.delete(id);
            }
            return ResponseEntity.ok().body(Map.of(
                "status", "success",
                "message", "Selected service checkers deleted successfully",
                "ids", ids
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", "Failed to delete service checkers: " + e.getMessage()
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
        List<ExternalDriverDTO> exDrivers = driverProxyService.getAllDrivers();
        model.addAttribute("drivers", exDrivers);
        model.addAttribute("currentDate", LocalDate.now());
        model.addAttribute("serviceChecker", new ServiceChecker());
        model.addAttribute("categories", inspectionService.getAllCategoriesWithItems());
        return "service-checkers/new-form";
    }

    @PostMapping("/new")
    public String submitForm(
        @ModelAttribute("serviceChecker") ServiceChecker checker,
        @RequestParam("driverId") Long driverId,
        @RequestParam Map<String, String> allParams,
        BindingResult result,
        RedirectAttributes redirectAttributes,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        if (result.hasErrors()) {
            return "admin/service-checkers/form";
        }

        try {
            ExternalDriverDTO exDriver = driverProxyService.getDriverById(driverId);

            if (driverId == null) {
                redirectAttributes.addFlashAttribute("error", "Driver not found in external system!");
                return "redirect:/admin/service-checkers/new";
            }
            // if (checker.getDriver() != null && checker.getDriver().getId() != null) {
            //     Driver driver = driverService.getDriverById(checker.getDriver().getId())
            //             .orElseThrow(() -> new RuntimeException("Driver not found"));
                if (serviceCheckerService.existsByExDriverIdAndDate(driverId, checker.getDate())) {
                    LocalDate date = checker.getDate();
                    String formatted = date.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"));
                    String message = String.format(
                        "⚠️ Oops! %s already has a checklist for %s. Please check the existing record before creating a new one.",
                        exDriver.getFullName(),
                        formatted
                    );
                    
                    redirectAttributes.addFlashAttribute("error", message);
                    return "redirect:/admin/service-checkers/new";
                }
            // }
            User user = userDetails.getUser();
            checker.setCreatedBy(user);
            checker.setExDriver(exDriver);
            Map<Long, List<ItemNoteDTO>> categoryItems = processFormParameters(allParams);
            serviceCheckerService.createV2WithExternalDriver(checker, categoryItems);
            redirectAttributes.addFlashAttribute("success", "Service checker created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating service checker: " + e.getMessage());
        }

        return "redirect:/admin/service-checkers";
    }



    @GetMapping("/new/edit/{id}")
    public String showEditFormNew(@PathVariable Long id, RedirectAttributes redirectAttributes, Model model) {
        ServiceChecker checker = serviceCheckerService.getById(id);

        if (checker != null && !checker.canEdit()) {
            redirectAttributes.addFlashAttribute("error", "Cannot edit this record now. It's either too old or already edited recently.");
            return "redirect:/admin/service-checkers";
        }

        List<ExternalDriverDTO> exDrivers = driverProxyService.getAllDrivers();
        model.addAttribute("drivers", exDrivers);
        
        // model.addAttribute("drivers", driverService.getAllDrivers());
        model.addAttribute("serviceChecker", checker);
        model.addAttribute("categories", inspectionService.getAllCategoriesWithItems());
        return "service-checkers/new-edit-form";
    }

    @PostMapping("/new/update/{id}")
    public String updateServiceChecker(
        @PathVariable Long id,
        @ModelAttribute ServiceChecker checker,
        @RequestParam("driverId") Long driverId,
        @RequestParam Map<String, String> params,
        RedirectAttributes redirectAttributes,
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        try {

            ExternalDriverDTO exDriver = driverProxyService.getDriverById(driverId);

            if (exDriver == null) {
                redirectAttributes.addFlashAttribute("error", "Driver not found in external system!");
                return "redirect:/admin/service-checkers/new";
            }

            // if (checker.getDriver() != null && checker.getDriver().getId() != null) {
            //     Driver driver = driverService.getDriverById(checker.getDriver().getId())
            //             .orElseThrow(() -> new RuntimeException("Driver not found"));

                if (serviceCheckerService.existsByExDriverIdAndDateAndIdNot(driverId, checker.getDate(), id)) {

                    LocalDate date = checker.getDate();
                    String formatted = date.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"));
                    String message = String.format(
                        "⚠️ Oops! %s already has a checklist for %s. Please check the existing record before creating a new one.",
                        exDriver.getFullName(),
                        formatted
                    );
                    
                    redirectAttributes.addFlashAttribute("error", message);
                    return "redirect:/admin/service-checkers/new/edit/" + id;
                }
            // }
            User user = userDetails.getUser();
            checker.setUpdatedBy(user);
            checker.setExDriver(exDriver);
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