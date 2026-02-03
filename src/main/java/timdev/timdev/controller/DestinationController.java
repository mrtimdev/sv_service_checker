package timdev.timdev.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.val;
import timdev.timdev.dto.CustomUserDetails;
import timdev.timdev.dto.DestinationAjaxDTO;
import timdev.timdev.dto.ExcelImportResult;
import timdev.timdev.dto.Status;
import timdev.timdev.entity.Destination;
import timdev.timdev.entity.DestinationSetting;
import timdev.timdev.entity.Truck;
import timdev.timdev.service.DestinationService;
import timdev.timdev.service.DestinationSettingService;
import timdev.timdev.service.NotificationService;
import timdev.timdev.service.PermissionChecker;
import timdev.timdev.service.TruckService;


@RequestMapping("/destinations")
@AllArgsConstructor
@Controller
public class DestinationController {
    
    private DestinationService service;
    private DestinationSettingService destinationSettingService;
    private TruckService truckService;

    private NotificationService notificationService;
    private PermissionChecker permissionChecker;


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Object index(
        @RequestParam(value = "query", defaultValue = "") String query,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "200") String sizeParam,
        @RequestParam(value = "sortBy", defaultValue = "distanceDate") String sortBy,
        @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate startDate,
        @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate endDate,
        @RequestParam(value = "order", defaultValue = "desc") String order,
        @RequestParam(value = "export", required = false) String export,
        HttpServletResponse response,
        RedirectAttributes redirectAttributes,
        Model model) throws IOException
    {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            redirectAttributes.addFlashAttribute("error", "Start date cannot be after end date!");
            return "redirect:/destinations";
        }

        // ===== SORT =====
        Sort.Direction direction = "asc".equalsIgnoreCase(order)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        String sortField = switch (sortBy) {
            case "code" -> "code";
            case "id" -> "id";
            case "name" -> "name";
            case "distance" -> "distance";
            default -> "id";
        };

        Sort sort = Sort.by(direction, sortField);

        // ===== PAGEABLE / ALL =====
        boolean fetchAll = "all".equalsIgnoreCase(sizeParam);
        int size = fetchAll ? Integer.MAX_VALUE : Integer.parseInt(sizeParam);

        Pageable pageable = fetchAll
                ? Pageable.unpaged()
                : PageRequest.of(page, size, sort);

        List<Status> statuses = List.of(Status.PENDING);

        Page<Destination> resultPage =
                service.findByFilters(query, startDate, endDate, statuses, pageable);

        List<Destination> destinationPage = resultPage.getContent();
        int totalPages = fetchAll ? 1 : resultPage.getTotalPages();

        long totalElements = fetchAll
        ? destinationPage.size()
        : resultPage.getTotalElements();

        long startIndex = fetchAll ? 1 : (long) page * size + 1;

        long endIndex = fetchAll
                ? totalElements
                : calculateEndIndex(page, size, totalElements);



        // ===== EXPORT =====
        if ("excel".equalsIgnoreCase(export)) {
            service.exportExcel(destinationPage, response);
            return null;
        }

        if ("excel-for-company-truck".equalsIgnoreCase(export)) {
            service.exportExcelForImportCompanyTruck(destinationPage, response);
            return null;
        }

        // ===== MODEL =====
        model.addAttribute("data", destinationPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", sizeParam);
        model.addAttribute("pageSizeNumber", size);
        model.addAttribute("query", query);

        model.addAttribute("sortBy", sortBy);
        model.addAttribute("order", order);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        model.addAttribute("startIndex", startIndex);
        model.addAttribute("endIndex", endIndex);
        model.addAttribute("totalElements", totalElements); 

        return "destinations/index";
    }

    private long calculateEndIndex(int currentPage, int pageSizeNumber, long totalElements) {
        long endIndex = (long) currentPage * pageSizeNumber + pageSizeNumber;
        return Math.min(endIndex, totalElements);
    }


    // own record for user destinations
    @PreAuthorize("hasAuthority('DESTINATION_VIEW')")
    @GetMapping({"u", "/own-records"})
    public Object indexOwnRecords(
        @RequestParam(value = "query", defaultValue = "") String query,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "200") String sizeParam,
        @RequestParam(value = "all", defaultValue = "false") boolean showAll, 
        @RequestParam(value = "sortBy", defaultValue = "distanceDate") String sortBy,
        @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate startDate,
        @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate endDate,
        @RequestParam(value = "order", defaultValue = "desc") String order,
        @RequestParam(value = "export", required = false) String export,
        HttpServletResponse response,
        RedirectAttributes redirectAttributes,
        Model model) throws IOException 
    {
        List<Destination> destinationPage;
        int totalPages = 1;
        
        
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            redirectAttributes.addFlashAttribute("error", "Start date cannot be after end date!");
            return "redirect:/u";
        }

        int size;
        
        if ("all".equalsIgnoreCase(sizeParam)) {
            size = Integer.MAX_VALUE;
        } else {
            size = Integer.parseInt(sizeParam); 
        }

        // build Sort dynamically
        Sort.Direction direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;

        // map frontend sortBy values to entity fields
        String sortField;
        sortField = switch (sortBy) {
            case "code" -> "code";
            case "id" -> "id";
            case "name" -> "name";
            case "distance" -> "distance";
            default -> "id";
        };

        List<Status> statuses = List.of(Status.PENDING);
        Sort sort = Sort.by(direction, sortField);
        destinationPage = service.findByFilterQueriesAndSortWithStatus(startDate, endDate, query, statuses, sort);
        
        if (showAll) {
            // fetch all reports with filter
            destinationPage = service.getAllFiltered(query, sort);
        } else {
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Destination> withPage = service.findByFilterQueriesWithStatusAndPage(query, startDate, endDate, pageable, statuses);
            destinationPage = withPage.getContent();
            totalPages = withPage.getTotalPages();
        }

        if ("excel".equalsIgnoreCase(export)) {
            service.exportExcel(destinationPage, response);
            return null;
        }

        // put everything into model
        model.addAttribute("data", destinationPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", sizeParam);
        model.addAttribute("pageSizeNumber", size);
        model.addAttribute("query", query);

        model.addAttribute("showAll", showAll);

        model.addAttribute("sortBy", sortBy);
        model.addAttribute("order", order);

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "destinations/index_own_records";
    }

    @PreAuthorize("hasAuthority('DESTINATION_VIEW')")
    @GetMapping({"u/report", "/own-records/report"})
    public Object indexOwnRecordsReport(
        @RequestParam(value = "query", defaultValue = "") String query,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "200") String sizeParam,
        @RequestParam(value = "all", defaultValue = "false") boolean showAll, 
        @RequestParam(value = "sortBy", defaultValue = "distanceDate") String sortBy,
        @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate startDate,
        @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate endDate,
        @RequestParam(value = "order", defaultValue = "desc") String order,
        @RequestParam(value = "export", required = false) String export,
        HttpServletResponse response,
        RedirectAttributes redirectAttributes,
        Model model) throws IOException 
    {
        List<Destination> destinationPage;
        int totalPages = 1;
        
        
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            redirectAttributes.addFlashAttribute("error", "Start date cannot be after end date!");
            return "redirect:/u/report";
        }

        int size;
        
        if ("all".equalsIgnoreCase(sizeParam)) {
            size = Integer.MAX_VALUE;
        } else {
            size = Integer.parseInt(sizeParam); 
        }

        // build Sort dynamically
        Sort.Direction direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;

        // map frontend sortBy values to entity fields
        String sortField;
        switch (sortBy) {
            case "code":
                sortField = "code"; 
                break;
            case "id":
                sortField = "id";
                break;
            case "name":
                sortField = "name";
                break;
            case "distance":
                sortField = "distance";
                break;
            default:
                sortField = "id";
                break;
        }

        List<Status> statuses = List.of(Status.PENDING, Status.COMPLETED);
        Sort sort = Sort.by(direction, sortField);
        destinationPage = service.findByFilterQueriesAndSortWithStatus(startDate, endDate, query, statuses, sort);
        
        if (showAll) {
            // fetch all reports with filter
            destinationPage = service.getAllFiltered(query, sort);
        } else {
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Destination> withPage = service.findByFilterQueriesWithStatusAndPage(query, startDate, endDate, pageable, statuses);
            destinationPage = withPage.getContent();
            totalPages = withPage.getTotalPages();
        }

        if ("excel".equalsIgnoreCase(export)) {
            service.exportExcel(destinationPage, response);
            return null;
        }

        // put everything into model
        model.addAttribute("data", destinationPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", sizeParam);
        model.addAttribute("pageSizeNumber", size);
        model.addAttribute("query", query);

        model.addAttribute("showAll", showAll);

        model.addAttribute("sortBy", sortBy);
        model.addAttribute("order", order);

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "destinations/index_own_records_report";
    }



    @GetMapping("/form")
    public String create(Model model) {
        model.addAttribute("trucks", truckService.getAll());
        model.addAttribute("destination", new Destination());
        return "destinations/form";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        Destination destination = service.findById(id);
        model.addAttribute("trucks", truckService.getAll());
        model.addAttribute("destination", destination);
        model.addAttribute("currentDate", destination.getDate());
        return "destinations/form";
    }


    @PostMapping("/store")
    public String store(
            @ModelAttribute Destination destination,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        Destination existingData = null;

        if (destination.getId() != null) {
            existingData = service.findById(destination.getId());
        }

        DestinationSetting setting = destination.getSetting();
        if (setting == null) {
            redirectAttributes.addFlashAttribute("error", "Destination Code '" + destination.getCode() + "' not found in Destination Settings");
            redirectAttributes.addFlashAttribute("destination", destination);
            model.addAttribute("trucks", truckService.getAll());
            return "redirect:/destinations/form";
        }
        Truck truck = destination.getTruck();
        // Validate distance against DestinationSettings
        if (destination.getDistance() > setting.getDistance()) {
            String message = truck.getLicensePlate() + " Distance " + destination.getDistance() + 
                            " exceeds maximum allowed distance " + setting.getDistance() + 
                            " for destination '" + destination.getCode() + "'";
            redirectAttributes.addFlashAttribute("error", message);
            redirectAttributes.addFlashAttribute("destination", destination);
            model.addAttribute("trucks", truckService.getAll());
            return "redirect:/destinations/form";
        }
        boolean isUpdate = (existingData != null);
        Destination ds = (existingData != null) ? existingData : new Destination();

        ds.setDate(destination.getDate());
        ds.setCode(setting.getCode());
        ds.setName(setting.getName());
        ds.setDistance(setting.getDistance());
        ds.setTruck(destination.getTruck());
        ds.setNote(destination.getNote());
        ds.setSetting(setting);

        service.save(ds);

        sendNotificationDestinationCreateOrUpdate(
            ds,
            currentUser.getUsername(),
            isUpdate
        );


        redirectAttributes.addFlashAttribute("success", setting.getCode() + " | " + setting.getName() + " | "+ setting.getDistanceFormat() +" Destination saved successfully!" + " For " + truck.getLicensePlate());
        if (!permissionChecker.hasRole("ADMIN")) {
            return "redirect:/destinations/u";
        }
        return "redirect:/destinations";
    }


    private void sendNotificationDestinationCreateOrUpdate(
            Destination destination,
            String username,
            boolean isUpdate
    ) {
        try {

            String action = isUpdate ? "✏️ Destination Updated" : "🆕 Destination Created";
            String statusEmoji = "🚛";

            String currentTime = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm"));

            String message = String.format(
                    "*%s*\n\n" +
                    "📅 *Date:* %s\n" +
                    "🚛 *Truck:* %s\n" +
                    "📍 *Destination:* %s - %s\n" +
                    "📏 *Distance:* %d km\n" +
                    "📝 *Note:* %s\n\n" +
                    "👤 *User:* %s\n" +
                    "⏰ *Time:* %s",
                    action,
                    destination.getDate(),
                    destination.getTruck().getLicensePlate(),
                    destination.getCode(),
                    destination.getName(),
                    destination.getDistance(),
                    destination.getNote() == null ? "-" : destination.getNote(),
                    username,
                    currentTime
            );

            notificationService.sendMarkdownNotification(action, message);

        } catch (Exception e) {
            System.err.println("Failed to send Destination notification: " + e.getMessage());
        }
    }




    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Truck truck = truckService.findById(id).orElse(null);
        service.deleteById(id);
        redirectAttributes.addFlashAttribute("success", truck.getLicensePlate()+ ": Destination deleted successfully!");
        return "redirect:/destinations";
    }
    @GetMapping("/u/delete/{id}")
    public String deleteByUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Truck truck = truckService.findById(id).orElse(null);
        service.deleteById(id);
        redirectAttributes.addFlashAttribute("success", truck.getLicensePlate()+ ": Destination deleted successfully!");
        return "redirect:/destinations/u";
    }


    @GetMapping("/import")
    public String showImportForm() {
        return "destinations/import"; 
    }

    // @PostMapping("/import")
    // public String importExcel(@RequestParam("file") MultipartFile file,
    //                         RedirectAttributes redirectAttributes,
    //                         @AuthenticationPrincipal CustomUserDetails userDetails) {

    //     // Validate file type
    //     if (file.isEmpty()) {
    //         redirectAttributes.addFlashAttribute("error", "Please select a file to import");
    //         return "redirect:/destinations/import";
    //     }

    //     if (!file.getOriginalFilename().endsWith(".xlsx") &&
    //         !file.getOriginalFilename().endsWith(".xls")) {

    //         redirectAttributes.addFlashAttribute("error",
    //                 "Please upload an Excel file (.xlsx or .xls)");
    //         return "redirect:/destinations/import";
    //     }

    //     try {
    //         // Step 1: Read Excel (includes excel errors)
    //         ExcelImportResult excelResult = service.readExcelFile(file);

    //         // Step 2: Validate & Save to DB (NO error merging)
    //         List<String> dbErrors = service.validateAndSaveDestinations(excelResult, userDetails);

    //         int totalRows = excelResult.getDestinations().size();
    //         int successCount = totalRows - dbErrors.size();

    //         int errorCount = 0;
    //         if(successCount > 0) {
    //             redirectAttributes.addFlashAttribute("success",
    //                 "Imported successfully: " + successCount + " rows");
    //         }
            

    //         // Collect Excel + DB errors separately (NOT merged)
    //         List<String> allErrors = new ArrayList<>();
    //         allErrors.addAll(excelResult.getErrorMessages()); // Excel errors
    //         allErrors.addAll(dbErrors);                       // DB errors only

    //         if (!allErrors.isEmpty()) {
    //             redirectAttributes.addFlashAttribute("errorCount", allErrors.size());
    //             redirectAttributes.addFlashAttribute("errorDetails", allErrors);
    //         }

    //     } catch (Exception e) {
    //         redirectAttributes.addFlashAttribute("error",
    //                 "Import failed: " + e.getMessage());
    //     }

    //     return "redirect:/destinations/import";
    // }



    @PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file,
                            RedirectAttributes redirectAttributes,
                            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String username = userDetails != null ? userDetails.getUsername() : "System";
        String fileName = file.getOriginalFilename();

        // Validate file type
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a file to import");
            return "redirect:/destinations/import";
        }

        if (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls")) {
            redirectAttributes.addFlashAttribute(
                "error", "Please upload an Excel file (.xlsx or .xls)"
            );
            return "redirect:/destinations/import";
        }

        try {
            // Step 1: Read Excel
            ExcelImportResult excelResult = service.readExcelFile(file);

            // Step 2: Validate & Save to DB
            List<String> dbErrors =
                service.validateAndSaveDestinations(excelResult, userDetails);

            int totalRows = excelResult.getDestinations().size();
            int excelErrorCount = excelResult.getErrorMessages().size();
            int dbErrorCount = dbErrors.size();

            int successCount = totalRows - dbErrorCount;
            int errorCount = excelErrorCount + dbErrorCount;

            // UI messages
            if (successCount > 0) {
                redirectAttributes.addFlashAttribute(
                    "success", "Imported successfully: " + successCount + " rows"
                );
            }

            if (errorCount > 0) {
                List<String> allErrors = new ArrayList<>();
                allErrors.addAll(excelResult.getErrorMessages());
                allErrors.addAll(dbErrors);

                redirectAttributes.addFlashAttribute("errorCount", errorCount);
                redirectAttributes.addFlashAttribute("errorDetails", allErrors);
            }

            // ✅ ALWAYS send summary Telegram message
            sendImportNotification(
                successCount,
                errorCount,
                fileName,
                username,
                "Import Notification",
                "Destinations Import Completed"
            );

        } catch (Exception e) {

            String errorMsg = "Import failed: " + e.getMessage();
            redirectAttributes.addFlashAttribute("error", errorMsg);

            // 🚨 Only system-level failure
            sendErrorNotification(fileName, username, errorMsg);
        }

        return "redirect:/destinations/import";
    }




    private void sendImportNotification(int successCount, int errorCount, 
                                       String fileName, String username, String title, String subTitle) {
        try {
            String statusEmoji = errorCount > 0 ? "⚠️" : "✅";
            String subTitle_ = statusEmoji + " *"+subTitle+"*";
            
            String currentTime = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm"));
            
            String message = String.format(
                "%s\n\n" +
                "📄 *File:* %s\n" +
                "👤 *User:* %s\n" +
                "⏰ *Time:* %s\n\n" +
                "📊 *Results:*\n\n" +
                "✅ Success: %d rows\n" +
                "❌ Errors: %d rows\n\n" +
                "_Import completed via Excel upload_",
                subTitle_,
                fileName,
                username,
                currentTime,
                successCount,
                errorCount
            );
            
            notificationService.sendMarkdownNotification(title, message);
            
        } catch (Exception e) {
            // Log error but don't break the import process
            System.err.println("Failed to send Telegram notification: " + e.getMessage());
        }
    }
    
    /**
     * Send error notification
     */
    private void sendErrorNotification(String fileName, String username, String errorMessage) {
        try {
            String message = String.format(
                "🚨 *Import Failed*\n\n" +
                "📄 *File:* %s\n" +
                "👤 *User:* %s\n" +
                "⏰ *Time:* %s\n\n" +
                "❌ *Error:* %s\n\n" +
                "_Please check the file and try again_",
                fileName,
                username,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm")),
                errorMessage
            );
            
            notificationService.sendErrorNotification("Import failed", message);
            
        } catch (Exception e) {
            System.err.println("Failed to send error notification: " + e.getMessage());
        }
    }

    @ResponseBody
    @GetMapping(value = "/ajax/pending", produces = "application/json")
    public List<Map<String, Object>> pendingDestinations(
        @RequestParam(name = "q", required = false, defaultValue = "") String query,
        @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
        @RequestParam(name = "destinationId", required = false) Long destinationId
    ) {

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        DateTimeFormatter destinationDate = DateTimeFormatter.ofPattern("MMM dd, yyyy"); // for flatpickr();

        return service.searchPending(query, destinationId)
            .stream()
            .limit(limit)
            .map(d -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", d.getId());
                map.put("date", d.getDate().format(destinationDate));
                map.put("truckLicensePlate", d.getTruck().getLicensePlate());
                map.put("truckId", d.getTruck().getId());
                map.put("settingCode", d.getSetting().getCode());
                map.put("settingName", d.getSetting().getName());
                map.put("settingDistance", d.getSetting().getDistance());

                String label =
                        d.getDate().format(fmt) + "  |  " +
                        d.getTruck().getLicensePlate() + "  |  " +
                        d.getSetting().getCode() + "  |  " +
                        d.getSetting().getName();

                map.put("text", label);
                return map;
            })
            .toList();
    }

    @ResponseBody
    @GetMapping(value = "/ajax/{id}", produces = "application/json")
    public Map<String, Object> ajaxGetDestinationById(@PathVariable Long id) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        DateTimeFormatter destinationDate = DateTimeFormatter.ofPattern("MMM dd, yyyy");

        Destination d = service.findById(id);

        Map<String, Object> map = new HashMap<>();
        map.put("id", d.getId());
        map.put("date", d.getDate().format(destinationDate));
        map.put("truckLicensePlate", d.getTruck().getLicensePlate());
        map.put("truckId", d.getTruck().getId());
        map.put("settingCode", d.getSetting().getCode());
        map.put("settingName", d.getSetting().getName());
        map.put("settingDistance", d.getSetting().getDistance());

        String label =
                d.getDate().format(fmt) + "  |  " +
                d.getTruck().getLicensePlate() + "  |  " +
                d.getSetting().getCode() + "  |  " +
                d.getSetting().getName();

        map.put("text", label); // ✅ needed for Select2

        return map;
    }

    // get settings

    @ResponseBody
    @GetMapping(value = "/ajax/settings", produces = "application/json")
    public List<Map<String, Object>> ajaxGetDestinationSettings(
            @RequestParam(name = "q", required = false, defaultValue = "") String query,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit
    ) {

        return service.searchSettingsByCodeAndName(query)
                .stream()
                .limit(limit)
                .map(d -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", d.getId());
                    map.put("settingCode", d.getCode());
                    map.put("settingName", d.getName());
                    map.put("settingDistance", d.getDistance());
                    map.put("text", d.getCode() + " | " + d.getName() + " | " + d.getDistanceFormat() );
                    return map;
                })
                .toList();
    }

    @ResponseBody
    @GetMapping(value = "/ajax/checking-existing-entries", produces = "application/json")
    public Map<String, Object> ajaxCheckIsExistingDestinationsByTruckAndDate(
            @RequestParam(name = "settingId", required = false) Long settingId,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate date,
            @RequestParam(name = "truckId", required = false) Long truckId,
            @RequestParam(name = "destinationId", required = false) Long destinationId
    ) {

         Map<String, Object> response = new HashMap<>();

        boolean isExist = service.existsByDateAndTruckAndSetting(
                date,
                truckId,
                settingId,
                destinationId
        );

        response.put("isExist", isExist);

        if (isExist) {
            response.put("message", "This destination already exists for this truck and date.");
        } else {
            response.put("message", "No existing record found.");
        }

        return response;
    }

    
}
