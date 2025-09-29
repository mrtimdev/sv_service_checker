package timdev.timdev.controller.api.v1;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;
import timdev.timdev.dto.CustomUserDetails;
import timdev.timdev.dto.ItemNoteDTO;
import timdev.timdev.dto.api.CategoryItemRequest;
import timdev.timdev.dto.api.ServiceCheckerRequest;
import timdev.timdev.dto.api.ServiceCheckerResponseDTO;
import timdev.timdev.entity.Driver;
import timdev.timdev.entity.ServiceChecker;
import timdev.timdev.entity.User;
import timdev.timdev.exception.ResourceNotFoundException;
import timdev.timdev.service.DriverService;
import timdev.timdev.service.ServiceCheckerService;

@RestController
@RequestMapping("/api/v1/service-checkers")
@RequiredArgsConstructor
public class ServiceCheckerController {

    private final ServiceCheckerService serviceCheckerService;
    private final DriverService driverService;

    @GetMapping("/filters")
    public ResponseEntity<List<ServiceChecker>> getByDateFilter(
        @RequestParam("dateFilter") String dateFilter,
        @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<ServiceChecker> data = serviceCheckerService.getByDateFilter(dateFilter, startDate, endDate);
        return ResponseEntity.ok(data);
    }


    @GetMapping("/list")
    public ResponseEntity<?> getServiceCheckers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String dateFilter,
            @RequestParam(required = false) Long driverId) {
        
        try {
            // Calculate date range based on filter
            LocalDate startDate = null;
            LocalDate endDate = null;
            
            if (dateFilter != null) {
                LocalDate today = LocalDate.now();
                
                switch (dateFilter) {
                    case "today":
                        startDate = today;
                        endDate = today;
                        break;
                    case "yesterday":
                        LocalDate yesterday = today.minusDays(1);
                        startDate = yesterday;
                        endDate = yesterday;
                        break;
                    case "last7Days":
                        startDate = today.minusDays(7);
                        endDate = today;
                        break;
                    case "last30Days":
                        startDate = today.minusDays(30);
                        endDate = today;
                        break;
                }
            }
            
            // Get paginated service checkers
            Page<ServiceChecker> serviceCheckersPage = serviceCheckerService.getServiceCheckers(
                page, limit, startDate, endDate, driverId);
            
            // Convert to DTOs
            List<ServiceCheckerResponseDTO> serviceCheckerDTOs = serviceCheckersPage.getContent()
                .stream()
                .map(ServiceCheckerResponseDTO::new)
                .collect(Collectors.toList());
            
            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("data", serviceCheckerDTOs);
            response.put("currentPage", serviceCheckersPage.getNumber());
            response.put("totalItems", serviceCheckersPage.getTotalElements());
            response.put("totalPages", serviceCheckersPage.getTotalPages());
            response.put("hasMore", serviceCheckersPage.getNumber() < serviceCheckersPage.getTotalPages() - 1);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // Map<String, String> errorResponse = new HashMap<>();
            // errorResponse.put("error", "Failed to retrieve service checkers: " + e.getMessage());
            // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);

            Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "A checklist already exists for this driver on ");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }
    }


    @PostMapping
    public ResponseEntity<?> createServiceChecker(
            @RequestBody ServiceCheckerRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body("Invalid request data");
        }
        
        try {
            // Check if driver exists
            Driver driver = driverService.getDriverById(request.getDriverId())
                    .orElseThrow(() -> new RuntimeException("Driver not found with id: " + request.getDriverId()));
            
            // Check if checklist already exists for this driver on the given date
            if (serviceCheckerService.existsByDriverAndDate(driver, request.getDate())) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "A checklist already exists for this driver on " + request.getDate());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            }
            User user = userDetails.getUser();
            // Convert request to ServiceChecker entity
            ServiceChecker checker = new ServiceChecker();
            checker.setDate(request.getDate());
            checker.setDriver(driver);
            checker.setCreatedBy(user);
            
            // Convert category items to the format expected by service
            Map<Long, List<ItemNoteDTO>> categoryItems = new HashMap<>();
            for (CategoryItemRequest categoryRequest : request.getCategories()) {
                List<ItemNoteDTO> itemNotes = categoryRequest.getItems().stream()
                        .map(item -> new ItemNoteDTO(item.getItemId(), item.getPassed(), item.getNote()))
                        .collect(Collectors.toList());
                
                categoryItems.put(categoryRequest.getCategoryId(), itemNotes);
            }
            
            // Create service checker with inspections
            ServiceChecker createdChecker = serviceCheckerService.createWithInspections(checker, categoryItems);
            
            Map<String, String> successResponse = new HashMap<>();
            successResponse.put("message", "Service checker created successfully!");
            successResponse.put("id", createdChecker.getId().toString());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(successResponse);
            
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error creating service checker: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getServiceChecker(@PathVariable Long id) {
        try {
            ServiceChecker serviceChecker = serviceCheckerService.getById(id);
            
            ServiceCheckerResponseDTO responseDTO = new ServiceCheckerResponseDTO(serviceChecker);
            return ResponseEntity.ok(responseDTO);
            
        } catch (ResourceNotFoundException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve service checker: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateServiceChecker(
            @PathVariable Long id,
            @RequestBody ServiceCheckerRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body("Invalid request data");
        }
        
        try {
            // Check if driver exists
            Driver driver = driverService.getDriverById(request.getDriverId())
                    .orElseThrow(() -> new RuntimeException("Driver not found with id: " + request.getDriverId()));
            
            User user = userDetails.getUser();
            // Convert request to ServiceChecker entity
            ServiceChecker checker = new ServiceChecker();
            checker.setDate(request.getDate());
            checker.setDriver(driver);
            checker.setUpdatedBy(user);
            
            // Convert category items to the format expected by service
            Map<Long, List<ItemNoteDTO>> categoryItems = new HashMap<>();
            for (CategoryItemRequest categoryRequest : request.getCategories()) {
                List<ItemNoteDTO> itemNotes = categoryRequest.getItems().stream()
                        .map(item -> new ItemNoteDTO(item.getItemId(), item.getPassed(), item.getNote()))
                        .collect(Collectors.toList());
                
                categoryItems.put(categoryRequest.getCategoryId(), itemNotes);
            }
            
            // Update service checker
            ServiceChecker updatedChecker = serviceCheckerService.updateWithInspections(id, checker, categoryItems);
            
            Map<String, String> successResponse = new HashMap<>();
            successResponse.put("message", "Service checker updated successfully!");
            successResponse.put("id", updatedChecker.getId().toString());
            
            return ResponseEntity.ok(successResponse);
            
        } catch (ResourceNotFoundException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error updating service checker: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    

    @GetMapping
    public List<ServiceChecker> getAll() {
        return serviceCheckerService.getAll();
    }

    @GetMapping("/{id}/web")
    public ServiceChecker getById(@PathVariable Long id) {
        return serviceCheckerService.getById(id);
    }

    @PutMapping("/{id}/web")
    public ServiceChecker update(@PathVariable Long id, @RequestBody ServiceChecker serviceChecker) {
        return serviceCheckerService.update(id, serviceChecker);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        serviceCheckerService.delete(id);
    }
}