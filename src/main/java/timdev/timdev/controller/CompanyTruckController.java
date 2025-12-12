package timdev.timdev.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import timdev.timdev.dto.CompanyTruckRequestDTO;
import timdev.timdev.dto.CustomUserDetails;
import timdev.timdev.dto.Measurement;
import timdev.timdev.dto.RequestStatus;
import timdev.timdev.dto.Status;
import timdev.timdev.entity.CompanyTruck;
import timdev.timdev.entity.Truck;
import timdev.timdev.entity.User;
import timdev.timdev.service.CompanyTruckService;
import timdev.timdev.service.TruckService;


@Controller
@AllArgsConstructor
@RequestMapping("/company-trucks")
public class CompanyTruckController {
    
    private CompanyTruckService service;
    private TruckService truckService;

    @GetMapping
    public Object index(Model model,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "20") String sizeParam,
        @RequestParam(value = "all", defaultValue = "false") boolean showAll,
        @RequestParam(value = "query", required = false) String query,
        @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate startDate,
        @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate endDate,
        @RequestParam(value = "export", required = false) String export,
        HttpServletResponse response,
        RedirectAttributes redirectAttributes
    ) throws IOException {

        List<CompanyTruck> trucks;
        int totalPages = 1;
        int size;

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            redirectAttributes.addFlashAttribute("error", "Start date cannot be after end date!");
            return "redirect:/company-trucks";
        }

        if ("all".equalsIgnoreCase(sizeParam)) {
            size = Integer.MAX_VALUE;
        } else {
            size = Integer.parseInt(sizeParam);
        }

        List<CompanyTruck> allTrucks;
        Sort sortByIdDesc = Sort.by(Sort.Direction.DESC, "id");

        allTrucks = service.findByFilterQueriesListAndSort(startDate, endDate, query, sortByIdDesc);

        if (showAll) {
            trucks = allTrucks;
        } else {
            Pageable pageable = PageRequest.of(page, size, sortByIdDesc);
            Page<CompanyTruck> truckPage = service.findByFilterQueriesPage(startDate, endDate, query, pageable);

            trucks = truckPage.getContent();
            totalPages = truckPage.getTotalPages();
        }

        if ("excel".equalsIgnoreCase(export)) {
            exportCompanyTrucksReportToExcel(trucks, response);
            return null;
        }

        model.addAttribute("trucks", trucks);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", sizeParam);
        model.addAttribute("pageSizeNumber", size);
        model.addAttribute("showAll", showAll);
        model.addAttribute("query", query);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "company-trucks/index";
    }



    // --- Create Form ---
    @GetMapping("/create")
    public String createForm(@RequestParam(name = "truck_id", required = false) Long truckId, Model model) {
        CompanyTruckRequestDTO dto = new CompanyTruckRequestDTO();
        if (truckId != null) {
            dto.setTruckId(truckId);
        }

        model.addAttribute("truckDto", dto);
        model.addAttribute("selectedTruckId", truckId);
        model.addAttribute("trucks", truckService.getAll());
        model.addAttribute("currentDate", LocalDate.now());
        model.addAttribute("measurements", Measurement.values());
        return "company-trucks/form";
    }

    // --- Edit Form ---
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        CompanyTruck companyTruck = service.getTruckById(id);
        CompanyTruckRequestDTO dto = service.convertToDto(companyTruck);

        model.addAttribute("truckDto", dto);
        model.addAttribute("trucks", truckService.getAll());
        model.addAttribute("currentDate", dto.getDate());
        model.addAttribute("measurements", Measurement.values());

        return "company-trucks/form";
    }

    // --- Save or Update ---
    @PostMapping("/save")
    public String saveTruck(@ModelAttribute("truckDto") CompanyTruckRequestDTO dto, RedirectAttributes redirectAttributes, @AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userDetails.getUser();
        Truck truck = truckService.findById(dto.getTruckId()).orElse(null);
        if (truck == null) {
            redirectAttributes.addFlashAttribute("error", "Truck not found!");
            return "redirect:/company-trucks";
        }

        // // ✅ Only check for existing record if creating new OR editing to a different date
        // boolean exists = service.isExistsByTruckAndDate(truck, dto.getDate());

        // if (exists) {
        //     // ✅ Fetch existing record by truck and date
        //     CompanyTruck existing = service.findByTruckAndDate(truck, dto.getDate());
        //     // If it's not the same record being edited, block it
        //     if (dto.getId() == null || !existing.getId().equals(dto.getId())) {
        //         redirectAttributes.addFlashAttribute(
        //             "error",
        //             "ឡានលេខ " + truck.getLicensePlate() + " មានរបាយការណ៍សម្រាប់ថ្ងៃ " + dto.getDate() + " រួចហើយ!"
        //         );
        //         return "redirect:/company-trucks/create?truckId=" + truck.getId();
        //     }
        // }
        if (dto.getId() == null) {
            dto.setCreatedBy(user.getId());
            service.createTruck(dto);
        } else {
            dto.setUpdatedBy(user.getId());
            service.updateTruck(dto.getId(), dto);
        }
        redirectAttributes.addFlashAttribute(
            "success",
            "✅ Report for truck " + truck.getLicensePlate() + " on " + dto.getDate() + " saved successfully!"
        );

        return "redirect:/company-trucks?licensePlate="+truck.getLicensePlate();
    }



    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Optional<CompanyTruck> truckOpt = service.findById(id);
            if (truckOpt.isEmpty()) {
                CompanyTruck companyTruck = truckOpt.get();
                redirectAttributes.addFlashAttribute("error", "Truck not found");
                return "redirect:/company-trucks?licensePlate="+companyTruck.getTruck().getLicensePlate();
            } else {
                CompanyTruck companyTruck = truckOpt.get();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
                String dateString = companyTruck.getDate().format(formatter);

                if (Status.DEDUCTED.equals(companyTruck.getStatus())) {
                    redirectAttributes.addFlashAttribute(
                    "error",
                        "Oops, This truck transaction " + companyTruck.getTruck().getLicensePlate() + " on " + dateString + " fuel has been filled, can not deletable!"
                    );

                    return "redirect:/company-trucks?licensePlate="+companyTruck.getTruck().getLicensePlate();
                }

                service.deleteById(id);
                redirectAttributes.addFlashAttribute("success", "Record on "+ dateString+ ", " + companyTruck.getTruck().getLicensePlate() + " deleted successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Something went wrong: " + e.getMessage());
        }
        return "redirect:/company-trucks";
    }

    @GetMapping("/import")
    public String showImportForm() {
        return "company-trucks/import"; 
    }

    @PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file,
                            RedirectAttributes redirectAttributes,
                            @AuthenticationPrincipal CustomUserDetails userDetails) {

        try {
            List<CompanyTruckRequestDTO> list = service.readExcel(file);

            int successCount = 0;
            List<String> errorMessages = new ArrayList<>();

            for (int i = 0; i < list.size(); i++) {
                CompanyTruckRequestDTO dto = list.get(i);
                try {
                    service.saveTruckFromExcel(dto, userDetails.getUser());
                    successCount++;
                } catch (Exception e) {
                    errorMessages.add("Row " + (i + 1) + " (" + dto.getLicensePlate() + "): " + e.getMessage());
                }
            }
            if(successCount > 0) {
                redirectAttributes.addFlashAttribute("success",
                    "Imported successfully: " + successCount + " rows");
            }
            
            
            if (!errorMessages.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorCount", errorMessages.size());
                redirectAttributes.addFlashAttribute("errorDetails", errorMessages);
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Import failed: " + e.getMessage());
        }

        return "redirect:/company-trucks/import";
    }

    private void exportCompanyTrucksReportToExcel(List<CompanyTruck> trucks, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"truck_oil_change_report.xlsx\"");
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("របាយការណ៏តួរលេខចាក់ប្រេងឡាន");
            
            // Create styles
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setAlignment(HorizontalAlignment.CENTER);
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setWrapText(true);
            
            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.setAlignment(HorizontalAlignment.CENTER);
            numberStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            numberStyle.setBorderTop(BorderStyle.THIN);
            numberStyle.setBorderBottom(BorderStyle.THIN);
            numberStyle.setBorderLeft(BorderStyle.THIN);
            numberStyle.setBorderRight(BorderStyle.THIN);
            numberStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
            
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setAlignment(HorizontalAlignment.CENTER);
            dateStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            dateStyle.setBorderTop(BorderStyle.THIN);
            dateStyle.setBorderBottom(BorderStyle.THIN);
            dateStyle.setBorderLeft(BorderStyle.THIN);
            dateStyle.setBorderRight(BorderStyle.THIN);
            dateStyle.setDataFormat(workbook.createDataFormat().getFormat("MMM dd, yyyy"));
            
            CellStyle datetimeStyle = workbook.createCellStyle();
            datetimeStyle.setAlignment(HorizontalAlignment.CENTER);
            datetimeStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            datetimeStyle.setBorderTop(BorderStyle.THIN);
            datetimeStyle.setBorderBottom(BorderStyle.THIN);
            datetimeStyle.setBorderLeft(BorderStyle.THIN);
            datetimeStyle.setBorderRight(BorderStyle.THIN);
            datetimeStyle.setDataFormat(workbook.createDataFormat().getFormat("MMM dd, yyyy HH:mm AM/PM"));

            // Create title row
            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(35);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("របាយការណ៏តួរលេខចាក់ប្រេងឡាន");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 11));
            
            // Create info row (date generated)
            Row infoRow = sheet.createRow(1);
            infoRow.setHeightInPoints(20);
            Cell infoCell = infoRow.createCell(0);
            infoCell.setCellValue("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm a")));
            CellStyle infoStyle = workbook.createCellStyle();
            Font infoFont = workbook.createFont();
            infoFont.setItalic(true);
            infoFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            infoStyle.setFont(infoFont);
            infoCell.setCellStyle(infoStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 11));
            
            // Create header row
            Row headerRow = sheet.createRow(2);
            headerRow.setHeightInPoints(25);
            
            String[] headers = {
                "កាលបរិច្ឆេទ", 
                "លេខឡាន", 
                "គោលដៅសរុប", 
                "ចម្ងាយសរុប (គីឡូម៉ែត្រ)", 
                "មធ្យមភាគ (Average)", 
                "ប្រភេទវាស់វែង",
                "ចំនួនប្រេង", 
                "ប្រេងផ្សេងៗ", 
                "សរុបប្រេងចាក់អោយឡាន",
                "បង្កើតនៅ", 
                "កែប្រែចុងក្រោយ", 
                "អ្នកបង្កើត"
            };
            
            for (int i = 0; i < headers.length; i++) {
                createCell(headerRow, i, headers[i], headerStyle);
            }
            
            // Create data rows
            int rowNum = 3;
            for (CompanyTruck truck : trucks) {
                Row row = sheet.createRow(rowNum++);
                row.setHeightInPoints(20);
                
                // Date
                createCell(row, 0, truck.getDate() != null ? truck.getDate() : LocalDate.now(), dateStyle);
                
                // License Plate
                createCell(row, 1, truck.getTruck() != null && truck.getTruck().getLicensePlate() != null ? 
                    truck.getTruck().getLicensePlate() : "", dataStyle);
                
                // Total Destination
                createCell(row, 2, truck.getTotalDestination() != null ? 
                    truck.getTotalDestination() : "0", dataStyle);
                
                // Total KM
                createCell(row, 3, truck.getTotalKm() != null ? 
                    truck.getTotalKmFormat() : 0.0, numberStyle);
                
                // Average
                createCell(row, 4, truck.getAverage() != null ? 
                    truck.getAverageFormat() : 0.0, numberStyle);
                
                // Measurement
                createCell(row, 5, truck.getMeasurement() != null ? 
                    truck.getMeasurement().name() : "", dataStyle);
                
                // Litre Quantity
                createCell(row, 6, truck.getLitreQuantity() != null ? 
                    truck.getLitreQuantityFormat() : 0.0, numberStyle);
                
                // Other Oils
                createCell(row, 7, truck.getOtherOils() != null ? 
                    truck.getOtherOils() : "", dataStyle);
                
                // Total Oils Change
                createCell(row, 8, truck.getTotalOilsChange() != null ? 
                    truck.getTotalOilsChangeFormat() : 0.0, numberStyle);
                
                // Created At
                createCell(row, 9, truck.getCreatedAt() != null ? 
                    truck.getCreatedAt() : LocalDateTime.now(), datetimeStyle);
                
                // Updated At
                createCell(row, 10, truck.getUpdatedAt() != null ? 
                    truck.getUpdatedAt() : "", datetimeStyle);
                
                // Created By
                createCell(row, 11, truck.getCreatedBy() != null && truck.getCreatedBy().fullName() != null ? 
                    truck.getCreatedBy().fullName() : "", dataStyle);
            }
            
            // Auto-size columns with padding
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                int currentWidth = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, Math.min(currentWidth + 1024, 256 * 256)); // Add padding but limit max width
            }
            
            // Freeze header rows (title, info, and header)
            sheet.createFreezePane(0, 3);
            
            // Add auto-filter to header row
            sheet.setAutoFilter(new CellRangeAddress(2, 2, 0, headers.length - 1));
            
            workbook.write(response.getOutputStream());
        }
    }

    private void createCell(Row row, int column, Object value, CellStyle style) {
        Cell cell = row.createCell(column);
        
        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof LocalDate) {
            cell.setCellValue((LocalDate) value);
        } else if (value instanceof LocalDateTime) {
            cell.setCellValue((LocalDateTime) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        }
        
        if (style != null) {
            cell.setCellStyle(style);
        }
    }

    // Safe getter methods to handle null values
    private Double safeGetDouble(Double value) {
        return value != null ? value : 0.0;
    }

    private String formatDate(LocalDate date) {
        if (date == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        return date.format(formatter);
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm a");
        return dateTime.format(formatter);
    }


    @GetMapping("/status/update")
    public String changeOilStatus(
            @RequestParam("id") Long id,
            @RequestParam("action") String action,
            @RequestParam("backUrl") String backUrl,
            @RequestParam(required = false) String requestNote,
            @RequestParam(required = false) Boolean isByAdmin,
            RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {

        Optional<CompanyTruck> optional = service.findById(id);
        if (optional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "CompanyTruck not found");
            return "redirect:"+backUrl;
        }

        CompanyTruck truck = optional.get();
        User user = userDetails.getUser();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        String dateString = truck.getDate().format(formatter);


        switch (action.toUpperCase()) {
            case "DEDUCTED" -> {
                truck.setStatus(Status.DEDUCTED);
                
                if (isByAdmin == null) {
                    truck.setDeductedBy(user);
                    truck.setDeductedAt(LocalDateTime.now());
                }
                
                redirectAttributes.addFlashAttribute("success", "Record on "+ dateString+ ", " + truck.getTruck().getLicensePlate() + " update status to " + action.toUpperCase());
            }
            case "PENDING" -> {
                truck.setStatus(Status.PENDING);
                

                if (isByAdmin != null) {
                    truck.setPendingBy(user);
                    truck.setPendingAt(LocalDateTime.now());
                }
                

                truck.setRequestStatus(RequestStatus.REQUESTED);
                redirectAttributes.addFlashAttribute("success", "Record on "+ dateString+ ", " + truck.getTruck().getLicensePlate() + " update status to " + action.toUpperCase());
            }
            case "REQUESTED" -> {
                truck.setRequestNote(requestNote.trim());
                truck.setRequestStatus(RequestStatus.REQUESTED);
                truck.setRequested(true);
                truck.setRequestedAt(LocalDateTime.now());
                truck.setRequestedBy(user);
                redirectAttributes.addFlashAttribute("success", "Record on "+ dateString+ ", " + truck.getTruck().getLicensePlate() + " is now on requesting!");
            }
            default -> {
                redirectAttributes.addFlashAttribute("error", "Invalid action");
                return "redirect:"+backUrl;
            }
        }

        service.saveTruck(truck);
        
        return "redirect:"+backUrl;
    }


    // user requested deduction only and approved

    @PostMapping("/approve/{id}")
    public String approve(@PathVariable Long id, @RequestParam("status") RequestStatus status, RedirectAttributes redirectAttributes,
    @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        CompanyTruck companyTruck = service.findById(id).orElse(null);
        if (companyTruck == null) {
            redirectAttributes.addFlashAttribute("error", "CompanyTruck not found!");
            return "redirect:/company-trucks/deduction/status";
        }

        User currentUser = userDetails.getUser();
        if(status.equals(RequestStatus.APPROVED)) {
            companyTruck.setRequestStatus(status);
            companyTruck.setApprovedAt(LocalDateTime.now());
            companyTruck.setApprovedBy(currentUser);
        }

        else if(status.equals(RequestStatus.REJECTED)) {
            companyTruck.setRequestStatus(status);
            companyTruck.setRejectedAt(LocalDateTime.now());
            companyTruck.setRejectedBy(currentUser);
        }
        else if(status.equals(status)) {
            companyTruck.setRequestStatus(RequestStatus.REQUESTED);
            companyTruck.setRejectedAt(LocalDateTime.now()); 
            companyTruck.setRejectedBy(currentUser);
        }
        
        service.saveTruck(companyTruck); 
        redirectAttributes.addFlashAttribute("success", "Request has been "+ status);
        return "redirect:/company-trucks/deduction/status";
    }

    @GetMapping("/deduction/status")
    public Object userRequestTruckForChangeToDeduction(Model model,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "20") String sizeParam,
        @RequestParam(value = "all", defaultValue = "false") boolean showAll,
        @RequestParam(value = "licensePlate", required = false) String licensePlate,
        @RequestParam(value = "export", required = false) String export,
        HttpServletResponse response
    ) throws IOException {

        List<CompanyTruck> trucks;
        int totalPages = 1;
        int size;
        
        if ("all".equalsIgnoreCase(sizeParam)) {
            size = Integer.MAX_VALUE;
        } else {
            size = Integer.parseInt(sizeParam); 
        }

        List<CompanyTruck> allTrucks;

        // ✅ Always sorted by ID DESC
        // Sort sortByIdDesc = Sort.by(Sort.Direction.DESC, "id");
        Sort sort = Sort.by(
            Sort.Order.desc("deductedAt"),
            Sort.Order.desc("id") // fallback if deductedAt is null
        );

        if (licensePlate != null && !licensePlate.isEmpty()) {
            allTrucks = service.findByLicensePlateContaining(licensePlate, sort);
        } else {
            allTrucks = service.getAllWIthSort(sort);
        }

        if (showAll) {
            trucks = allTrucks;
        } else {
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<CompanyTruck> truckPage;

            if (licensePlate != null && !licensePlate.isEmpty()) {
                truckPage = service.findByLicensePlateContainingWithPageable(licensePlate, pageable);
            } else {
                truckPage = service.getAllWithPageable(pageable);
            }

            trucks = truckPage.getContent();
            totalPages = truckPage.getTotalPages();
        }

        model.addAttribute("trucks", trucks);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", sizeParam);
        model.addAttribute("pageSizeNumber", size);
        model.addAttribute("showAll", showAll);
        model.addAttribute("licensePlate", licensePlate);  
        return "company-trucks/deduction_status";
    }


    // for user's records
    @GetMapping("/user-record")
    public Object companyTrucksForUser(Model model,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "20") String sizeParam,
        @RequestParam(value = "all", defaultValue = "false") boolean showAll,
        @RequestParam(value = "licensePlate", required = false) String licensePlate,
        @RequestParam(value = "query", required = false) String query,
        @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate startDate,
        @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate endDate,
        @RequestParam(value = "export", required = false) String export,
        HttpServletResponse response,
        RedirectAttributes redirectAttributes
    ) throws IOException {

        List<CompanyTruck> trucks;
        int totalPages = 1;
        int size;

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            redirectAttributes.addFlashAttribute("error", "Start date cannot be after end date!");
            return "redirect:/company-trucks/user-record";
        }

        if ("all".equalsIgnoreCase(sizeParam)) {
            size = Integer.MAX_VALUE;
        } else {
            size = Integer.parseInt(sizeParam);
        }

        List<CompanyTruck> allTrucks;
        Sort sortByIdDesc = Sort.by(Sort.Direction.DESC, "id");

        allTrucks = service.findByFilterQueriesListAndSortAndStatus(startDate, endDate, query, sortByIdDesc, Status.PENDING);

        if (showAll) {
            trucks = allTrucks;
        } else {
            Pageable pageable = PageRequest.of(page, size, sortByIdDesc);
            Page<CompanyTruck> truckPage = service.findByFilterQueriesPageAndStatus(startDate, endDate, query, pageable, Status.PENDING);

            trucks = truckPage.getContent();
            totalPages = truckPage.getTotalPages();
        }
        
        model.addAttribute("trucks", trucks);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", sizeParam);
        model.addAttribute("pageSizeNumber", size);
        model.addAttribute("showAll", showAll);
        model.addAttribute("licensePlate", licensePlate);  
        model.addAttribute("query", query);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "company-trucks/trucks_for_users_mark";
    }

    @GetMapping("/user-record/report")
    public Object companyTrucksForUserReport(Model model,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "20") String sizeParam,
        @RequestParam(value = "all", defaultValue = "false") boolean showAll,
        @RequestParam(value = "licensePlate", required = false) String licensePlate,
        @RequestParam(value = "query", required = false) String query,
        @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate startDate,
        @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate endDate,
        @RequestParam(value = "export", required = false) String export,
        HttpServletResponse response,
        RedirectAttributes redirectAttributes
    ) throws IOException {

        List<CompanyTruck> trucks;
        int totalPages = 1;
        int size;

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            redirectAttributes.addFlashAttribute("error", "Start date cannot be after end date!");
            return "redirect:/company-trucks/user-record";
        }

        if ("all".equalsIgnoreCase(sizeParam)) {
            size = Integer.MAX_VALUE;
        } else {
            size = Integer.parseInt(sizeParam);
        }

        List<CompanyTruck> allTrucks;
        Sort sortByIdDesc = Sort.by(Sort.Direction.DESC, "id");

        allTrucks = service.findByFilterQueriesListAndSortAndStatus(startDate, endDate, query, sortByIdDesc, Status.DEDUCTED);

        if (showAll) {
            trucks = allTrucks;
        } else {
            Pageable pageable = PageRequest.of(page, size, sortByIdDesc);
            Page<CompanyTruck> truckPage = service.findByFilterQueriesPageAndStatus(startDate, endDate, query, pageable, Status.DEDUCTED);

            trucks = truckPage.getContent();
            totalPages = truckPage.getTotalPages();
        }
        
        model.addAttribute("trucks", trucks);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", sizeParam);
        model.addAttribute("pageSizeNumber", size);
        model.addAttribute("showAll", showAll);
        model.addAttribute("licensePlate", licensePlate);  
        model.addAttribute("query", query);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "company-trucks/trucks_for_users_mark_report";
    }


}
