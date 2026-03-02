// package timdev.timdev.controller.api.v1;

// import java.io.File;
// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.UUID;
// import java.util.stream.Collectors;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.data.domain.Page;
// import org.springframework.format.annotation.DateTimeFormat;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.core.annotation.AuthenticationPrincipal;
// import org.springframework.validation.BindingResult;
// import org.springframework.web.bind.annotation.DeleteMapping;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.PutMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;
// import org.springframework.web.multipart.MultipartFile;
// import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import timdev.timdev.dto.CustomUserDetails;
// import timdev.timdev.dto.ItemNoteDTO;
// import timdev.timdev.dto.api.CategoryItemRequest;
// import timdev.timdev.dto.api.ServiceCheckerRequest;
// import timdev.timdev.dto.api.ServiceCheckerResponseDTO;
// import timdev.timdev.dto.api.ServiceCheckerWithDeviceInfoRequest;
// import timdev.timdev.entity.Driver;
// import timdev.timdev.entity.ServiceChecker;
// import timdev.timdev.entity.User;
// import timdev.timdev.exception.ResourceNotFoundException;
// import timdev.timdev.service.DriverService;
// import timdev.timdev.service.ServiceCheckerService;


// @Slf4j
// @RestController
// @RequestMapping("/api/v1/service-checkers")
// @RequiredArgsConstructor
// public class ServiceCheckerController {

//     private final ServiceCheckerService serviceCheckerService;
//     private final DriverService driverService;

//     @Value("${file.upload-dir}")
//     private String uploadDir;

//     @GetMapping("/filters")
//     public ResponseEntity<List<ServiceChecker>> getByDateFilter(
//         @RequestParam("dateFilter") String dateFilter,
//         @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
//         @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
//     ) {
//         List<ServiceChecker> data = serviceCheckerService.getByDateFilter(dateFilter, startDate, endDate);
//         return ResponseEntity.ok(data);
//     }


//     @GetMapping("/list")
//     public ResponseEntity<?> getServiceCheckers(
//             @RequestParam(defaultValue = "1") int page,
//             @RequestParam(defaultValue = "20") int limit,
//             @RequestParam(required = false) String dateFilter,
//             @RequestParam(required = true) String deviceId) {
        
//         try {
//             // Calculate date range based on filter
//             LocalDate startDate = null;
//             LocalDate endDate = null;
            
//             if (dateFilter != null) {
//                 LocalDate today = LocalDate.now();
                
//                 switch (dateFilter) {
//                     case "today" -> {
//                         startDate = today;
//                         endDate = today;
//                     }
//                     case "yesterday" -> {
//                         LocalDate yesterday = today.minusDays(1);
//                         startDate = yesterday;
//                         endDate = yesterday;
//                     }
//                     case "last7Days" -> {
//                         startDate = today.minusDays(7);
//                         endDate = today;
//                     }
//                     case "last30Days" -> {
//                         startDate = today.minusDays(30);
//                         endDate = today;
//                     }
//                 }
//             }
            
//             // Get paginated service checkers
//             // Page<ServiceChecker> serviceCheckersPage = serviceCheckerService.getServiceCheckers(
//             //     page, limit, startDate, endDate, driverId);

//             Page<ServiceChecker> serviceCheckersPage = serviceCheckerService.getByDeviceId(page, limit, startDate, endDate, deviceId);
            
            
            
//             // Convert to DTOs
//             List<ServiceCheckerResponseDTO> serviceCheckerDTOs = serviceCheckersPage.getContent()
//                 .stream()
//                 .map(ServiceCheckerResponseDTO::new)
//                 .collect(Collectors.toList());
            
//             // Prepare response
//             Map<String, Object> response = new HashMap<>();
//             response.put("data", serviceCheckerDTOs);
//             response.put("currentPage", serviceCheckersPage.getNumber());
//             response.put("totalItems", serviceCheckersPage.getTotalElements());
//             response.put("totalPages", serviceCheckersPage.getTotalPages());
//             response.put("hasMore", serviceCheckersPage.getNumber() < serviceCheckersPage.getTotalPages() - 1);
            
//             return ResponseEntity.ok(response);
            
//         } catch (Exception e) {
//             // Map<String, String> errorResponse = new HashMap<>();
//             // errorResponse.put("error", "Failed to retrieve service checkers: " + e.getMessage());
//             // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);

//             Map<String, String> errorResponse = new HashMap<>();
//                 errorResponse.put("error", "A checklist already exists for this driver on ");
//                 return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
//         }
//     }

    

//     @PostMapping("/upload")
//     public ResponseEntity<?> uploadImage(@RequestParam("image") MultipartFile image) {
//         try {
//             log.info("📸 Receiving image upload: {}", image.getOriginalFilename());
            
//             // Use absolute path - create uploads directory in your project root
//             String projectDir = System.getProperty("user.dir");
//             String uploadDir = projectDir + File.separator + "uploads";
            
//             File directory = new File(uploadDir);
            
//             log.info("📁 Upload directory: {}", directory.getAbsolutePath());
            
//             // Create directory if not exists
//             if (!directory.exists()) {
//                 boolean created = directory.mkdirs();
//                 if (created) {
//                     log.info("✅ Created upload directory: {}", directory.getAbsolutePath());
//                 } else {
//                     log.error("❌ Failed to create upload directory");
//                     return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                         .body(Map.of("error", "Could not create upload directory"));
//                 }
//             }
            
//             // Check write permissions
//             if (!directory.canWrite()) {
//                 log.error("❌ Upload directory is not writable: {}", directory.getAbsolutePath());
//                 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                     .body(Map.of("error", "Upload directory is not writable"));
//             }
            
//             // Create unique filename
//             String originalFileName = image.getOriginalFilename();
//             String fileExtension = "";
//             if (originalFileName != null && originalFileName.contains(".")) {
//                 fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
//             } else {
//                 fileExtension = ".jpg"; // Default extension
//             }
            
//             String fileName = "checklist_" + 
//                 System.currentTimeMillis() + "_" + 
//                 UUID.randomUUID().toString().substring(0, 8) + 
//                 fileExtension;
            
//             // Save file
//             String filePath = directory.getAbsolutePath() + File.separator + fileName;
//             File dest = new File(filePath);
            
//             log.info("💾 Saving to: {}", filePath);
//             image.transferTo(dest);
            
//             // Verify file was saved
//             if (!dest.exists()) {
//                 throw new RuntimeException("File was not saved successfully");
//             }
            
//             log.info("✅ Image saved successfully: {} ({} bytes)", fileName, dest.length());
            
//             // Return accessible URL
//             String imageUrl = "/uploads/" + fileName;
            
//             Map<String, String> response = new HashMap<>();
//             response.put("imageUrl", imageUrl);
//             response.put("path", filePath);
//             response.put("fileName", fileName);
            
//             return ResponseEntity.ok(response);
            
//         } catch (Exception e) {
//             log.error("❌ Upload failed: {}", e.getMessage(), e);
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                 .body(Map.of("error", "Failed to upload image: " + e.getMessage()));
//         }
//     }

//     @PostMapping
//     public ResponseEntity<?> createServiceChecker(@RequestBody ServiceCheckerWithDeviceInfoRequest request) {
//         log.info("Received request to create service checker: {}", request);
        
//         try {
//             // Validate request
//             if (request.getDate() == null) {
//                 return ResponseEntity.badRequest().body(Map.of("error", "Date is required"));
//             }
            
//             if (request.getLicensePlate() == null || request.getLicensePlate().trim().isEmpty()) {
//                 return ResponseEntity.badRequest().body(Map.of("error", "License plate is required"));
//             }
            
//             if (request.getCategories() == null || request.getCategories().isEmpty()) {
//                 return ResponseEntity.badRequest().body(Map.of("error", "Categories are required"));
//             }
            
//             // Check if checklist already exists for this license plate on the given date
//             if (serviceCheckerService.existsByLicensePlateAndDate(
//                     request.getLicensePlate().toUpperCase(), request.getDate())) {
                
//                 String message = String.format(
//                     "A checklist already exists for license plate %s on %s",
//                     request.getLicensePlate().toUpperCase(),
//                     request.getDate().toString()
//                 );
                
//                 Map<String, String> errorResponse = new HashMap<>();
//                 errorResponse.put("error", message);
//                 errorResponse.put("conflict", "true");
//                 return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
//             }
            
//             // Create ServiceChecker entity
//             ServiceChecker checker = new ServiceChecker();
//             checker.setDate(request.getDate());
//             checker.setLicensePlate(request.getLicensePlate().toUpperCase());
//             checker.setImagePath(request.getImagePath());
//             checker.setLicensePlateEstimated(request.getLicensePlateEstimated().toUpperCase());
            
//             // Set device info
//             if (request.getDeviceInfo() != null) {
//                 checker.setDeviceId(request.getDeviceInfo().getDeviceId());
//                 checker.setDeviceModel(request.getDeviceInfo().getDeviceModel());
//                 checker.setDevicePlatform(request.getDeviceInfo().getPlatform());
//                 checker.setAppVersion(request.getDeviceInfo().getAppVersion());
//             }
            
//             checker.setCreatedAt(LocalDateTime.now());
            
//             Map<Long, List<ItemNoteDTO>> categoryItems = new HashMap<>();

//             for (CategoryItemRequest categoryRequest : request.getCategories()) {
//                 List<ItemNoteDTO> itemNotes = categoryRequest.getItems().stream()
//                         .map(item -> new ItemNoteDTO(
//                                 item.getItemId(),
//                                 item.getPassed(),
//                                 item.getNote()
//                         ))
//                         .collect(Collectors.toList());

//                 categoryItems.put(categoryRequest.getCategoryId(), itemNotes);
//             }

            
//             // Create service checker with inspections
//             ServiceChecker createdChecker = serviceCheckerService.createWithInspections(checker, categoryItems);
            
//             Map<String, Object> successResponse = new HashMap<>();
//             successResponse.put("message", "Service checker created successfully!");
//             successResponse.put("id", createdChecker.getId().toString());
//             successResponse.put("licensePlate", createdChecker.getLicensePlate());
//             successResponse.put("date", createdChecker.getDate().toString());
            
//             log.info("Service checker created successfully with ID: {}", createdChecker.getId());
            
//             return ResponseEntity.status(HttpStatus.CREATED).body(successResponse);
            
//         } catch (Exception e) {
//             log.error("Error creating service checker: {}", e.getMessage(), e);
//             Map<String, String> errorResponse = new HashMap<>();
//             errorResponse.put("error", "Error creating service checker: " + e.getMessage());
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
//         }
//     }

//     @PostMapping("/old")
//     public ResponseEntity<?> createServiceChecker_old(
//             @RequestBody ServiceCheckerRequest request,
//             BindingResult result,
//             RedirectAttributes redirectAttributes,
//             @AuthenticationPrincipal CustomUserDetails userDetails
//     ) {
        
//         if (result.hasErrors()) {
//             return ResponseEntity.badRequest().body("Invalid request data");
//         }
        
//         try {
//             // Check if driver exists
//             Driver driver = driverService.getDriverById(request.getDriverId())
//                     .orElseThrow(() -> new RuntimeException("Driver not found with id: " + request.getDriverId()));
            
//             // Check if checklist already exists for this driver on the given date
//             if (serviceCheckerService.existsByDriverAndDate(driver, request.getDate())) {
//                 Map<String, String> errorResponse = new HashMap<>();
//                 errorResponse.put("error", "A checklist already exists for this driver on " + request.getDate());
//                 return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
//             }
//             User user = userDetails.getUser();
//             // Convert request to ServiceChecker entity
//             ServiceChecker checker = new ServiceChecker();
//             checker.setDate(request.getDate());
//             checker.setDriver(driver);
//             checker.setCreatedBy(user);
            
//             // Convert category items to the format expected by service
//             Map<Long, List<ItemNoteDTO>> categoryItems = new HashMap<>();
//             for (CategoryItemRequest categoryRequest : request.getCategories()) {
//                 List<ItemNoteDTO> itemNotes = categoryRequest.getItems().stream()
//                         .map(item -> new ItemNoteDTO(item.getItemId(), item.getPassed(), item.getNote()))
//                         .collect(Collectors.toList());
                
//                 categoryItems.put(categoryRequest.getCategoryId(), itemNotes);
//             }
            
//             // Create service checker with inspections
//             ServiceChecker createdChecker = serviceCheckerService.createWithInspections(checker, categoryItems);
            
//             Map<String, String> successResponse = new HashMap<>();
//             successResponse.put("message", "Service checker created successfully!");
//             successResponse.put("id", createdChecker.getId().toString());
            
//             return ResponseEntity.status(HttpStatus.CREATED).body(successResponse);
            
//         } catch (Exception e) {
//             Map<String, String> errorResponse = new HashMap<>();
//             errorResponse.put("error", "Error creating service checker: " + e.getMessage());
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
//         }
//     }


//     @GetMapping("/{id}")
//     public ResponseEntity<?> getServiceChecker(@PathVariable Long id) {
//         try {
//             ServiceChecker serviceChecker = serviceCheckerService.getById(id);
            
//             ServiceCheckerResponseDTO responseDTO = new ServiceCheckerResponseDTO(serviceChecker);
//             return ResponseEntity.ok(responseDTO);
            
//         } catch (ResourceNotFoundException e) {
//             Map<String, String> errorResponse = new HashMap<>();
//             errorResponse.put("error", e.getMessage());
//             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            
//         } catch (Exception e) {
//             Map<String, String> errorResponse = new HashMap<>();
//             errorResponse.put("error", "Failed to retrieve service checker: " + e.getMessage());
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
//         }
//     }
    
//     @PutMapping("/{id}")
//     public ResponseEntity<?> updateServiceChecker(
//             @PathVariable Long id,
//             @RequestBody ServiceCheckerWithDeviceInfoRequest request,
//             BindingResult result
//     ) {

//         if (result.hasErrors()) {
//             return ResponseEntity.badRequest().body("Invalid request data");
//         }

//         try {

//             // Get existing checker first
//             ServiceChecker existingChecker = serviceCheckerService.getById(id);

//             // Create updated entity
//             ServiceChecker checker = new ServiceChecker();
//             checker.setDate(request.getDate());
//             checker.setLicensePlate(request.getLicensePlate().toUpperCase());
//             checker.setLicensePlateEstimated(
//                     request.getLicensePlateEstimated() != null
//                             ? request.getLicensePlateEstimated().toUpperCase()
//                             : null
//             );

//             // Handle image update
//             if (request.getImagePath() != null) {

//                 // Delete old image if exists
//                 if (existingChecker.getImagePath() != null) {
//                     String path = request.getImagePath().replaceFirst("^/", "");
//                     File oldImage = new File(path);
//                     if (oldImage.exists()) {
//                         boolean deleted = oldImage.delete();
//                         if (deleted) {
//                             log.info("Deleted old image: {}", existingChecker.getImagePath());
//                         } else {
//                             log.warn("Failed to delete old image: {}", existingChecker.getImagePath());
//                         }
//                     }
//                 }

//                 // Set new image
//                 checker.setImagePath(request.getImagePath());

//             } else {
//                 // Keep old image if no new one provided
//                 checker.setImagePath(existingChecker.getImagePath());
//             }

//             // Convert category items
//             Map<Long, List<ItemNoteDTO>> categoryItems = new HashMap<>();

//             for (CategoryItemRequest categoryRequest : request.getCategories()) {

//                 List<ItemNoteDTO> itemNotes = categoryRequest.getItems().stream()
//                         .map(item -> new ItemNoteDTO(
//                                 item.getItemId(),
//                                 item.getPassed(),
//                                 item.getNote()
//                         ))
//                         .collect(Collectors.toList());

//                 categoryItems.put(categoryRequest.getCategoryId(), itemNotes);
//             }

//             // Update
//             ServiceChecker updatedChecker =
//                     serviceCheckerService.updateWithInspections(id, checker, categoryItems);

//             Map<String, String> successResponse = new HashMap<>();
//             successResponse.put("message", "Service checker updated successfully!");
//             successResponse.put("id", updatedChecker.getId().toString());

//             return ResponseEntity.ok(successResponse);

//         } catch (ResourceNotFoundException e) {

//             Map<String, String> errorResponse = new HashMap<>();
//             errorResponse.put("error", e.getMessage());
//             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

//         } catch (Exception e) {

//             Map<String, String> errorResponse = new HashMap<>();
//             errorResponse.put("error", "Error updating service checker: " + e.getMessage());
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
//         }
//     }


    

//     @GetMapping
//     public List<ServiceChecker> getAll() {
//         return serviceCheckerService.getAll();
//     }

//     @GetMapping("/{id}/web")
//     public ServiceChecker getById(@PathVariable Long id) {
//         return serviceCheckerService.getById(id);
//     }

//     @PutMapping("/{id}/web")
//     public ServiceChecker update(@PathVariable Long id, @RequestBody ServiceChecker serviceChecker) {
//         return serviceCheckerService.update(id, serviceChecker);
//     }

//     @DeleteMapping("/{id}")
//     public void delete(@PathVariable Long id) {
//         serviceCheckerService.delete(id);
//     }

//     @PutMapping("/{id}/cancel")
//     public ResponseEntity<Map<String, String>> cancel(@PathVariable Long id, @RequestParam String reason, @AuthenticationPrincipal CustomUserDetails userDetails) {
//         try {
//             serviceCheckerService.cancel(id, reason, userDetails.getUser());
//             Map<String, String> successResponse = new HashMap<>();
//             successResponse.put("message", "Service checker cancelled successfully!");
//             return ResponseEntity.ok(successResponse);
//         } catch (ResourceNotFoundException e) {
//             Map<String, String> errorResponse = new HashMap<>();
//             errorResponse.put("error", e.getMessage());
//             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
//         } catch (Exception e) {
//             Map<String, String> errorResponse = new HashMap<>();
//             errorResponse.put("error", "Error cancelling service checker: " + e.getMessage());
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
//         }
//     }
// }