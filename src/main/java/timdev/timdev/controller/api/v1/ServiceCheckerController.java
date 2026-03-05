package timdev.timdev.controller.api.v1;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import timdev.timdev.dto.CustomUserDetails;
import timdev.timdev.dto.ItemNoteDTO;
import timdev.timdev.dto.api.CategoryItemRequest;
import timdev.timdev.dto.api.ServiceCheckerRequest;
import timdev.timdev.dto.api.ServiceCheckerResponseDTO;
import timdev.timdev.dto.api.ServiceCheckerWithDeviceInfoRequest;
import timdev.timdev.entity.Driver;
import timdev.timdev.entity.InspectionItem;
import timdev.timdev.entity.ServiceChecker;
import timdev.timdev.entity.User;
import timdev.timdev.exception.ResourceNotFoundException;
import timdev.timdev.exception.UnauthorizedException;
import timdev.timdev.repository.UserRepository;
import timdev.timdev.service.DriverService;
import timdev.timdev.service.InspectionItemService;
import timdev.timdev.service.ServiceCheckerService;
import timdev.timdev.util.JwtUtil;

@Slf4j
@RestController
@RequestMapping("/api/v1/service-checkers")
@RequiredArgsConstructor
public class ServiceCheckerController {

    private final ServiceCheckerService serviceCheckerService;
    private final DriverService driverService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    private final InspectionItemService inspectionItemService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // Helper method to validate token
    private String validateToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            throw new UnauthorizedException("Invalid or expired token");
        }

        return jwtUtil.getUsernameFromToken(token);
    }

    @GetMapping("/filters")
    public ResponseEntity<List<ServiceChecker>> getByDateFilter(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("dateFilter") String dateFilter,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            validateToken(authHeader);
            List<ServiceChecker> data = serviceCheckerService.getByDateFilter(dateFilter, startDate, endDate);
            return ResponseEntity.ok(data);
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> getServiceCheckers(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String dateFilter,
            @RequestParam(required = true) String deviceId) {

        try {
            validateToken(authHeader);

            // Calculate date range based on filter
            LocalDate startDate = null;
            LocalDate endDate = null;

            if (dateFilter != null) {
                LocalDate today = LocalDate.now();

                switch (dateFilter) {
                    case "today" -> {
                        startDate = today;
                        endDate = today;
                    }
                    case "yesterday" -> {
                        LocalDate yesterday = today.minusDays(1);
                        startDate = yesterday;
                        endDate = yesterday;
                    }
                    case "last7Days" -> {
                        startDate = today.minusDays(7);
                        endDate = today;
                    }
                    case "last30Days" -> {
                        startDate = today.minusDays(30);
                        endDate = today;
                    }
                }
            }

            Page<ServiceChecker> serviceCheckersPage = serviceCheckerService.getByDeviceId(page, limit, startDate,
                    endDate, null);

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

        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve service checkers: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<?> getServiceCheckerById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {

        try {
            validateToken(authHeader);

            ServiceChecker serviceChecker = serviceCheckerService.getById(id);

            // ✅ Convert to DTO
            ServiceCheckerResponseDTO responseDTO = new ServiceCheckerResponseDTO(serviceChecker);

            return ResponseEntity.ok(responseDTO);

        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve service checker"));
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("image") MultipartFile image) {
        try {
            validateToken(authHeader);

            log.info("📸 Receiving image upload: {}", image.getOriginalFilename());

            // Use absolute path - create uploads directory in your project root
            String projectDir = System.getProperty("user.dir");
            String uploadDir = projectDir + File.separator + "uploads";

            File directory = new File(uploadDir);

            log.info("📁 Upload directory: {}", directory.getAbsolutePath());

            // Create directory if not exists
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                if (created) {
                    log.info("✅ Created upload directory: {}", directory.getAbsolutePath());
                } else {
                    log.error("❌ Failed to create upload directory");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("error", "Could not create upload directory"));
                }
            }

            // Check write permissions
            if (!directory.canWrite()) {
                log.error("❌ Upload directory is not writable: {}", directory.getAbsolutePath());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Upload directory is not writable"));
            }

            // Create unique filename
            String originalFileName = image.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            } else {
                fileExtension = ".jpg"; // Default extension
            }

            String fileName = "checklist_" +
                    System.currentTimeMillis() + "_" +
                    UUID.randomUUID().toString().substring(0, 8) +
                    fileExtension;

            // Save file
            String filePath = directory.getAbsolutePath() + File.separator + fileName;
            File dest = new File(filePath);

            log.info("💾 Saving to: {}", filePath);
            image.transferTo(dest);

            // Verify file was saved
            if (!dest.exists()) {
                throw new RuntimeException("File was not saved successfully");
            }

            log.info("✅ Image saved successfully: {} ({} bytes)", fileName, dest.length());

            // Return accessible URL
            String imageUrl = "/uploads/" + fileName;

            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            response.put("path", filePath);
            response.put("fileName", fileName);

            return ResponseEntity.ok(response);

        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("❌ Upload failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload image: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createServiceChecker(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ServiceCheckerWithDeviceInfoRequest request) {

        log.info("Received request to create service checker: {}", request);

        try {
            String username = validateToken(authHeader);

            // Get the user from repository
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // Validate request
            if (request.getDate() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Date is required"));
            }

            if (request.getLicensePlate() == null || request.getLicensePlate().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "License plate is required"));
            }

            if (request.getCategories() == null || request.getCategories().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Categories are required"));
            }

            // Check if checklist already exists for this license plate on the given date
            // if (serviceCheckerService.existsByLicensePlateAndDate(
            // request.getLicensePlate().toUpperCase(), request.getDate())) {

            // String message = String.format(
            // "A checklist already exists for license plate %s on %s",
            // request.getLicensePlate().toUpperCase(),
            // request.getDate().toString()
            // );

            // Map<String, String> errorResponse = new HashMap<>();
            // errorResponse.put("error", message);
            // errorResponse.put("conflict", "true");
            // return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            // }

            // Create ServiceChecker entity
            ServiceChecker checker = new ServiceChecker();
            checker.setDate(request.getDate());
            checker.setLicensePlate(request.getLicensePlate().toUpperCase());
            checker.setImagePath(request.getImagePath());
            checker.setLicensePlateEstimated(
                    request.getLicensePlateEstimated() != null ? request.getLicensePlateEstimated().toUpperCase()
                            : null);

            // Set device info
            if (request.getDeviceInfo() != null) {
                checker.setDeviceId(request.getDeviceInfo().getDeviceId());
                checker.setDeviceModel(request.getDeviceInfo().getDeviceModel());
                checker.setDevicePlatform(request.getDeviceInfo().getPlatform());
                checker.setAppVersion(request.getDeviceInfo().getAppVersion());
            }

            checker.setCreatedBy(currentUser); // Set the user who created this
            checker.setCreatedAt(LocalDateTime.now());
            checker.setUpdatedAt(LocalDateTime.now());

            Map<Long, List<ItemNoteDTO>> categoryItems = new HashMap<>();

            for (CategoryItemRequest categoryRequest : request.getCategories()) {
                List<ItemNoteDTO> itemNotes = categoryRequest.getItems().stream()
                        .map(item -> {
                            Optional<InspectionItem> itemOpt = inspectionItemService
                                    .getItemById(item.getItemId());
                            if (itemOpt.isEmpty()) {
                                throw new RuntimeException("Invalid item ID: " + item.getItemId());
                            }
                            InspectionItem inspectionItem = itemOpt.get();
                            ItemNoteDTO noteDTO = new ItemNoteDTO();
                            noteDTO.setItemId(item.getItemId());
                            noteDTO.setPassed(item.getPassed());
                            noteDTO.setNote(item.getNote());
                            noteDTO.setIsRequired(inspectionItem.getIsRequired());
                            return noteDTO;
                        })
                        .collect(Collectors.toList());

                categoryItems.put(categoryRequest.getCategoryId(), itemNotes);
            }

            // Create service checker with inspections
            ServiceChecker createdChecker = serviceCheckerService.createWithInspections(checker, categoryItems);

            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("message", "Service checker created successfully!");
            successResponse.put("id", createdChecker.getId().toString());
            successResponse.put("licensePlate", createdChecker.getLicensePlate());
            successResponse.put("date", createdChecker.getDate().toString());

            log.info("Service checker created successfully with ID: {}", createdChecker.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(successResponse);

        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Error creating service checker: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error creating service checker: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/old")
    public ResponseEntity<?> createServiceChecker_old(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ServiceCheckerRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        try {
            validateToken(authHeader);

            if (result.hasErrors()) {
                return ResponseEntity.badRequest().body("Invalid request data");
            }

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
                        .map(item -> new ItemNoteDTO(item.getItemId(), item.getPassed(), item.getNote(),
                                item.getIsRequired()))
                        .collect(Collectors.toList());

                categoryItems.put(categoryRequest.getCategoryId(), itemNotes);
            }

            // Create service checker with inspections
            ServiceChecker createdChecker = serviceCheckerService.createWithInspections(checker, categoryItems);

            Map<String, String> successResponse = new HashMap<>();
            successResponse.put("message", "Service checker created successfully!");
            successResponse.put("id", createdChecker.getId().toString());

            return ResponseEntity.status(HttpStatus.CREATED).body(successResponse);

        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error creating service checker: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getServiceChecker(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        try {
            validateToken(authHeader);

            ServiceChecker serviceChecker = serviceCheckerService.getById(id);

            ServiceCheckerResponseDTO responseDTO = new ServiceCheckerResponseDTO(serviceChecker);
            return ResponseEntity.ok(responseDTO);

        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
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
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestBody ServiceCheckerWithDeviceInfoRequest request,
            BindingResult result) {
        try {

            String username = validateToken(authHeader);
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            if (result.hasErrors()) {
                return ResponseEntity.badRequest().body("Invalid request data");
            }

            // Get existing checker first
            ServiceChecker existingChecker = serviceCheckerService.getById(id);

            // Create updated entity
            ServiceChecker checker = new ServiceChecker();
            checker.setDate(request.getDate());
            checker.setLicensePlate(request.getLicensePlate().toUpperCase());
            checker.setLicensePlateEstimated(
                    request.getLicensePlateEstimated() != null ? request.getLicensePlateEstimated().toUpperCase()
                            : null);

            // Handle image update
            if (request.getImagePath() != null) {

                // Delete old image if exists
                if (existingChecker.getImagePath() != null) {
                    String path = existingChecker.getImagePath().replaceFirst("^/", "");
                    File oldImage = new File(path);
                    if (oldImage.exists()) {
                        boolean deleted = oldImage.delete();
                        if (deleted) {
                            log.info("Deleted old image: {}", existingChecker.getImagePath());
                        } else {
                            log.warn("Failed to delete old image: {}", existingChecker.getImagePath());
                        }
                    }
                }

                // Set new image
                checker.setImagePath(request.getImagePath());

            } else {
                // Keep old image if no new one provided
                checker.setImagePath(existingChecker.getImagePath());
            }
            checker.setUpdatedAt(LocalDateTime.now());
            checker.setUpdatedBy(currentUser);

            // Convert category items
            Map<Long, List<ItemNoteDTO>> categoryItems = new HashMap<>();

            for (CategoryItemRequest categoryRequest : request.getCategories()) {

                List<ItemNoteDTO> itemNotes = categoryRequest.getItems().stream()
                        .map(item -> new ItemNoteDTO(
                                item.getItemId(),
                                item.getPassed(),
                                item.getNote(),
                                item.getIsRequired()))

                        .collect(Collectors.toList());

                categoryItems.put(categoryRequest.getCategoryId(), itemNotes);
            }

            // Update
            ServiceChecker updatedChecker = serviceCheckerService.updateWithInspections(id, checker, categoryItems);

            Map<String, String> successResponse = new HashMap<>();
            successResponse.put("message", "Service checker updated successfully!");
            successResponse.put("id", updatedChecker.getId().toString());

            return ResponseEntity.ok(successResponse);

        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
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
    public ResponseEntity<List<ServiceChecker>> getAll(@RequestHeader("Authorization") String authHeader) {
        try {
            validateToken(authHeader);
            return ResponseEntity.ok(serviceCheckerService.getAll());
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/{id}/web")
    public ResponseEntity<ServiceChecker> getById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        try {
            validateToken(authHeader);
            return ResponseEntity.ok(serviceCheckerService.getById(id));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/web")
    public ResponseEntity<ServiceChecker> update(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestBody ServiceChecker serviceChecker) {
        try {
            validateToken(authHeader);
            return ResponseEntity.ok(serviceCheckerService.update(id, serviceChecker));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        try {
            validateToken(authHeader);
            serviceCheckerService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Service checker deleted successfully"));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Map<String, String>> cancel(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestParam String reason) {
        try {
            String username = validateToken(authHeader);

            // Get the user from repository (optional, for logging)
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            serviceCheckerService.cancel(id, reason, currentUser);
            Map<String, String> successResponse = new HashMap<>();
            successResponse.put("message", "Service checker cancelled successfully!");
            return ResponseEntity.ok(successResponse);
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (ResourceNotFoundException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error cancelling service checker: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}