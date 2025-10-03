package timdev.timdev.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import timdev.timdev.dto.CustomUserDetails;
import timdev.timdev.dto.InspectionRequestDTO;
import timdev.timdev.entity.Inspection;
import timdev.timdev.entity.Truck;
import timdev.timdev.entity.User;
import timdev.timdev.enums.InspectionStatus;
import timdev.timdev.service.InspectionService;
import timdev.timdev.service.TruckService;

@AllArgsConstructor
@Controller
@RequestMapping("/inspections")
public class InspectionsWebController {

    private final InspectionService inspectionService;

    private final TruckService truckService;


    // @GetMapping
    // public String list(
    //     Model model,
    //     @RequestParam(value = "page", defaultValue = "0") int page,
    //     @RequestParam(value = "size", defaultValue = "10") String sizeParam,
    //     @RequestParam(value = "all", defaultValue = "false") boolean showAll,
    //     @RequestParam(value = "licensePlate", required = false) String licensePlate,
    //     @RequestParam(value = "fromDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate fromDate,
    //     @RequestParam(value = "toDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate toDate,
    //     @RequestParam(value = "expiredFromDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate expiredFromDate,
    //     @RequestParam(value = "expiredToDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate expiredToDate
    // ) {
    //     model.addAttribute("inspections", inspectionService.findAll());
    //     List<Inspection> inspections;
    //     int totalPages = 1;
    //     int size;
        
    //     if ("all".equalsIgnoreCase(sizeParam)) {
    //         size = Integer.MAX_VALUE;
    //     } else {
    //         size = Integer.parseInt(sizeParam); 
    //     }


    //      if (showAll) {
    //         inspections = inspectionService.findAllFiltered(
    //             licensePlate, fromDate, toDate, expiredFromDate, expiredToDate
    //         );
    //     } else {
    //         Pageable pageable = PageRequest.of(page, size);
    //         Page<Inspection> itemsPage = inspectionService.findAllFilteredWithPageable(
    //             licensePlate, fromDate, toDate, expiredFromDate, expiredToDate, pageable
    //         );
    //         inspections = itemsPage.getContent();
    //         totalPages = itemsPage.getTotalPages();
    //     }
 
    //     // inspections.sort(Comparator.comparingLong(Inspection::expiredDurationDays));

    //     model.addAttribute("inspections", inspections);
    //     model.addAttribute("currentPage", page);
    //     model.addAttribute("totalPages", totalPages);
    //     model.addAttribute("pageSize", sizeParam);
    //     model.addAttribute("showAll", showAll);
    //     model.addAttribute("licensePlate", licensePlate);  
    //     model.addAttribute("fromDate", fromDate);
    //     model.addAttribute("toDate", toDate);
    //     model.addAttribute("expiredFromDate", expiredFromDate);
    //     model.addAttribute("expiredToDate", expiredToDate);
    //     return "inspections/list";
    // }


    // truck's inspection list
    @GetMapping
    public Object index(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") String sizeParam,
            @RequestParam(value = "all", defaultValue = "false") boolean showAll,
            @RequestParam(value = "licensePlate", required = false) String licensePlate,
            @RequestParam(value = "expiredFromDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate expiredFromDate,
            @RequestParam(value = "expiredToDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate expiredToDate,
            @RequestParam(value = "export", required = false) String export,
            HttpServletResponse response
    ) throws IOException 
        {

        List<Truck> allTrucks;
        if (licensePlate != null && !licensePlate.isEmpty()) {
            allTrucks = truckService.findByLicensePlateContaining(licensePlate);
        } else {
            allTrucks = truckService.getAll();
        }

        allTrucks.sort(Comparator.comparingInt(this::getInspectionPriority));

        List<Truck> trucks;
        int totalPages;
        int size;
        
        if ("all".equalsIgnoreCase(sizeParam)) {
            size = Integer.MAX_VALUE;
        } else {
            size = Integer.parseInt(sizeParam); 
        }
        if (showAll) {
            trucks = allTrucks; 
            totalPages = 1;
        } else {
            int fromIndex = page * size;
            int toIndex = Math.min(fromIndex + size, allTrucks.size());
            if (fromIndex >= allTrucks.size()) {
                trucks = Collections.emptyList();
            } else {
                trucks = allTrucks.subList(fromIndex, toIndex);
            }
            totalPages = (int) Math.ceil(allTrucks.size() / (double) size);
        }

        if ("excel".equalsIgnoreCase(export)) {
            exportInspectionsToExcel(trucks, response);
            return null; 
        }

        model.addAttribute("trucks", trucks);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", sizeParam);
        model.addAttribute("showAll", showAll);
        model.addAttribute("licensePlate", licensePlate);

        model.addAttribute("expiredFromDate", expiredFromDate);
        model.addAttribute("expiredToDate", expiredToDate);

        return "inspections/list";
    }

    private void exportInspectionsToExcel(List<Truck> trucks, HttpServletResponse response) throws IOException {
        // Set response headers
        String fileName = "Inspections-Schedule-Reports-" + LocalDate.now() + ".xlsx";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                .replace("+", "%20");
        
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
        response.setCharacterEncoding("UTF-8");
            // Create workbook and sheet
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Inspections Report");
        
        // Create header row with styling
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);
        // Create green background style
        CellStyle greenHeaderStyle = createHeaderStyle(workbook);
        greenHeaderStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        greenHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Create white font for green headers
        Font whiteFont = workbook.createFont();
        whiteFont.setBold(true);
        whiteFont.setColor(IndexedColors.WHITE.getIndex());
        greenHeaderStyle.setFont(whiteFont);
        
        String[] headers = {
            "#", "License Plate", 
            "Inspection Date", "Inspection Expired Date", "Inspection Quantity",
            "Inspection Note", "Inspection Expired Durations", "Created", "Created By", 
        };
        Set<String> greenHeaders = Set.of(
            "Inspection Date",
            "Inspection Expired Date",
            "Inspection Quantity",
            "Inspection Note",
            "Inspection Expired Durations"
        );
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            if (greenHeaders.contains(headers[i])) {
                cell.setCellStyle(greenHeaderStyle);
            } else {
                cell.setCellStyle(headerStyle);
            }
            sheet.setColumnWidth(i, 5000); // Set column width
        }
        
        // Create data rows with color coding
        int rowNum = 1;
        for (int i = 0; i < trucks.size(); i++) {
            Truck truck = trucks.get(i);
            Row row = sheet.createRow(rowNum++);
            
            // Apply color style based on oil balance
            CellStyle rowStyle = getInspectionCellStyle(workbook, truck);

            // Last Fat change data
            String inspectionDate = truck.getLasInspection() != null && truck.getLasInspection().getDate() != null ?
                truck.getLasInspection().getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "";

            String inspectionExpiredDate = truck.getLasInspection() != null && truck.getLasInspection().getExpiredDate() != null ?
                truck.getLasInspection().getExpiredDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "";
            Integer quantity = truck.getLasInspection() != null ? truck.getLasInspection().getQuantity() : null;
            String note = truck.getLasInspection() != null ? truck.getLasInspection().getNote() : null;
            String expiredDurations = truck.getLasInspection() != null ? truck.getLasInspection().expiredDurationText() : null;

            String createdBy = truck.getLasInspection() != null ? truck.getLasInspection().getCreatedBy().fullName() : "";
            String created = truck.getLasInspection() != null ? truck.getLasInspection().getCreatedAgo() : "";
            
            // Populate data
            createCell(row, 0, i + 1, rowStyle); // #
            createCell(row, 1, truck.getLicensePlate(), rowStyle);
            createCell(row, 2, inspectionDate, rowStyle);
            createCell(row, 3, inspectionExpiredDate, rowStyle);
            createCell(row, 4, quantity, rowStyle);
            createCell(row, 5, note, rowStyle);
            createCell(row, 6, expiredDurations, rowStyle);
            createCell(row, 7, created, rowStyle);
            createCell(row, 8, createdBy, rowStyle);
        }
        
        // Auto-size columns for better fit
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        // Stream the workbook to response
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            workbook.write(outputStream);
            outputStream.flush();
        } finally {
            workbook.close();
        }
    }

    private void createInspectionReportCell(Row row, int column, Object value) {
        Cell cell = row.createCell(column);
        
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Long) {
            cell.setCellValue((Long) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof LocalDate) {
            cell.setCellValue(((LocalDate) value).format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        } else {
            cell.setCellValue(value.toString());
        }
    }


    private void createCell(Row row, int column, Object value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellStyle(style);
        
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Long) {
            cell.setCellValue((Long) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof LocalDate) {
            cell.setCellValue(((LocalDate) value).format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        } else {
            cell.setCellValue(value.toString());
        }
    }

    private CellStyle getInspectionReportCellStyle(Workbook workbook, Inspection inspection) {
        CellStyle style = workbook.createCellStyle();

        // Common border styling
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // Default font
        Font font = workbook.createFont();
        font.setColor(IndexedColors.BLACK.getIndex());

        

        style.setFont(font);
        return style;
    }

    private CellStyle getInspectionCellStyle(Workbook workbook, Truck truck) {
        CellStyle style = workbook.createCellStyle();

        // Common border styling
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // Default font
        Font font = workbook.createFont();
        font.setColor(IndexedColors.BLACK.getIndex());

        // Get priority
        int priority = getInspectionPriority(truck);

        switch (priority) {
            case 0 -> {
                // 🔴 Danger (red)
                style.setFillForegroundColor(IndexedColors.RED.getIndex());
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                font.setColor(IndexedColors.WHITE.getIndex());
            }

            case 1 -> {
                // 🔴 Danger (red)
                style.setFillForegroundColor(IndexedColors.RED.getIndex());
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                font.setColor(IndexedColors.WHITE.getIndex());
            }

            case 2 -> {
                // 🟡 Warning (yellow)
                style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                font.setColor(IndexedColors.BLACK.getIndex());
            }

            case 3 -> {
            }

            case 4 -> {
            }
        }

        style.setFont(font);
        return style;
    }



    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // Background color
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // Borders
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // Font
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        
        // Alignment
        style.setAlignment(HorizontalAlignment.CENTER);
        // style.setVerticalAlignment(VerticalAlignment.MIDDLE);
        
        return style;
    }

    private int getInspectionPriority(Truck truck) {
        if (truck.getLastInspection() == null) return 3;
        if (truck.getLastInspection().isExpired()) return 0; // expired → no priority

        long daysRemaining = truck.getLastInspection().expiredDurationDays() - 30;

        if (daysRemaining > 0 && daysRemaining <= 30) {
        // Will expire within the next 30 days
        return 2;
        } else if (daysRemaining < 0) {
            // Already expired → red
            return 1;
        }
        // More than 30 days left → normal
        return 3;
    }


    @GetMapping("/reports")
    public Object inspectionsReports(
        Model model,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "10") String sizeParam,
        @RequestParam(value = "all", defaultValue = "false") boolean showAll,
        @RequestParam(value = "licensePlate", required = false) String licensePlate,
        @RequestParam(name = "truckId", required = false) Long truckId,
        @RequestParam(value = "fromDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate fromDate,
        @RequestParam(value = "toDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate toDate,
        @RequestParam(value = "expiredFromDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate expiredFromDate,
        @RequestParam(value = "expiredToDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate expiredToDate,
        @RequestParam(value = "export", required = false) String export,
        HttpServletResponse response
    ) throws IOException  {
        model.addAttribute("inspections", inspectionService.findAll());
        
        List<Inspection> inspections;
        int totalPages = 1;
        int size;
        
        if ("all".equalsIgnoreCase(sizeParam)) {
            size = Integer.MAX_VALUE;
        } else {
            size = Integer.parseInt(sizeParam); 
        }

       if(truckId != null) {
        Truck truck = truckService.findById(truckId)
                .orElseThrow(() -> new RuntimeException("Truck not found"));
            licensePlate = truck.getLicensePlate();
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

        List<Inspection> inspectionsList = new ArrayList<>(inspections);
        inspectionsList.sort(createStatusComparator());


        if ("excel".equalsIgnoreCase(export)) {
            exportInspectionsReportToExcel(inspectionsList, response);
            return null; 
        }

        model.addAttribute("inspections", inspectionsList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", sizeParam);
        model.addAttribute("showAll", showAll);
        model.addAttribute("licensePlate", licensePlate);  
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("expiredFromDate", expiredFromDate);
        model.addAttribute("expiredToDate", expiredToDate);


        model.addAttribute("selectedTruckId", truckId != null ? truckId : null);
        model.addAttribute("inspection", new Inspection());
        model.addAttribute("trucks", truckService.getAll());
        return "inspections/reports";
    }


    private void exportInspectionsReportToExcel(List<Inspection> inspections, HttpServletResponse response) throws IOException {
        // Set response headers
        String fileName = "Inspections-Reports-" + LocalDate.now() + ".xlsx";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                .replace("+", "%20");
        
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
        response.setCharacterEncoding("UTF-8");
            // Create workbook and sheet
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Inspections Report");
        
        // Create header row with styling
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);
        // Create green background style
        CellStyle greenHeaderStyle = createHeaderStyle(workbook);
        greenHeaderStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        greenHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Create white font for green headers
        Font whiteFont = workbook.createFont();
        whiteFont.setBold(true);
        whiteFont.setColor(IndexedColors.WHITE.getIndex());
        greenHeaderStyle.setFont(whiteFont);
        
        String[] headers = {
            "#", "Date", "License Plate", "Expired Date", "Quantity", "Note", "Created", "Created By", 
        };
        Set<String> greenHeaders = Set.of(
            "Date",
            "Expired Date",
            "Quantity",
            "Note"
        );
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            if (greenHeaders.contains(headers[i])) {
                cell.setCellStyle(greenHeaderStyle);
            } else {
                cell.setCellStyle(headerStyle);
            }
            sheet.setColumnWidth(i, 5000); // Set column width
        }
        
        // Create data rows with color coding
        int rowNum = 1;
        for (int i = 0; i < inspections.size(); i++) {
            Inspection inspection = inspections.get(i);
            Row row = sheet.createRow(rowNum++);
            
            // Apply color style based on oil balance
            CellStyle rowStyle = getInspectionReportCellStyle(workbook, inspection);

            // Last Fat change data
            String inspectionDate = inspection.getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));

            String inspectionExpiredDate = inspection.getExpiredDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
            Integer quantity = inspection.getQuantity();
            String note = inspection.getNote();
            String createdBy = inspection.getCreatedBy().fullName();
            String created = inspection.getCreatedAgo();
            
            // Populate data
            createInspectionReportCell(row, 0, i + 1); // #
            createInspectionReportCell(row, 1, inspectionDate);
            createInspectionReportCell(row, 2, inspection.getTruck().getLicensePlate());
            createInspectionReportCell(row, 3, inspectionExpiredDate);
            createInspectionReportCell(row, 4, quantity);
            createInspectionReportCell(row, 5, note);
            createInspectionReportCell(row, 6, created);
            createInspectionReportCell(row, 7, createdBy);
        }
        
        // Auto-size columns for better fit
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        // Stream the workbook to response
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            workbook.write(outputStream);
            outputStream.flush();
        } finally {
            workbook.close();
        }
    }

    private Comparator<Inspection> createStatusComparator() {
        Map<InspectionStatus, Integer> statusOrder = Map.of(
            InspectionStatus.ACTIVE, 1,
            InspectionStatus.COMPLETED, 2,
            InspectionStatus.EXPIRED, 3
        );

        return Comparator
                .comparingInt((Inspection i) -> statusOrder.getOrDefault(i.getStatus(), 99))
                .thenComparingLong(Inspection::expiredDurationDays);
    }


    @GetMapping("/create")
    public String createForm(@RequestParam(name = "truck_id", required = false) Long truckId, Model model) {
        
        InspectionRequestDTO inspectionDTO = new InspectionRequestDTO();
        
        if (truckId != null) {
            inspectionDTO.setTruckId(truckId);
        } else {
            inspectionDTO.setTruckId(null);
            // Truck truck = truckService.findById(truckId)
            //     .orElseThrow(() -> new RuntimeException("Truck not found"));

            // model.addAttribute("selectedTruck", truck);
        }
        

        
        model.addAttribute("selectedTruckId", truckId != null ? truckId : null);
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
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam(value = "action", required = false) String action,
        @RequestParam(value = "formType", required = false) String formType
    ) {
        if (result.hasErrors()) {
            model.addAttribute("inspectionForm", inspectionDTO);
            model.addAttribute("inspection", new Inspection());
            model.addAttribute("trucks", truckService.getAll());
            if(inspectionDTO.getId() != null && "edit".equals(formType)) {
                return "inspections/edit";
            }
            return "inspections/form";
        }
        Truck truck = truckService.findById(inspectionDTO.getTruckId()).orElse(null);

        if (truck == null) {
            model.addAttribute("error_truck", "Truck not found.");
            return "inspections/form";
        }

       
        List<Inspection> overlaps = inspectionService.findOverlappingInspections(
            truck.getId(),
            inspectionDTO.getDate(),
            inspectionDTO.getExpiredDate()
        );

        if (inspectionDTO.getId() != null) {
            overlaps = overlaps.stream()
                    .filter(i -> !i.getId().equals(inspectionDTO.getId()))
                    .toList();
        }

        if (!overlaps.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", truck.getLicensePlate() + " Inspection overlaps with an existing active inspection!");
            return "redirect:/inspections/create";
        }

        // ✅ Complete all old inspections for this truck
        List<Inspection> oldInspections = inspectionService.findByTruckId(truck.getId());
        for (Inspection old : oldInspections) {
            if (!old.getId().equals(inspectionDTO.getId())) {
                old.setStatus(InspectionStatus.COMPLETED);
                inspectionService.save(old);
            }
        }
        
        User user = userDetails.getUser();
        Inspection inspection;
        if (inspectionDTO.getId() != null) {
            inspection = inspectionService.findById(inspectionDTO.getId()).orElse(new Inspection());
            inspection.setUpdatedAt(LocalDateTime.now());
            inspection.setUpdatedBy(user);
            inspection.setStatus(inspection.getStatus());
        } else {
            inspection = new Inspection();
            inspection.setCreatedAt(LocalDateTime.now());
            inspection.setCreatedBy(user);
            inspection.setStatus(InspectionStatus.ACTIVE);
        }

        

        inspection.setDate(inspectionDTO.getDate());
        inspection.setExpiredDate(inspectionDTO.getExpiredDate());
        inspection.setQuantity(inspectionDTO.getQuantity());
        inspection.setNote(inspectionDTO.getNote());
        inspection.setTruck(truck);

        

        truck.setExpiredDate(inspectionDTO.getExpiredDate());
        truckService.save(truck);
        


        inspection.setCreatedAt(LocalDateTime.now());
        

        inspectionService.save(inspection);

        if("edit".equals(formType)) {
            redirectAttributes.addFlashAttribute("success", truck.getLicensePlate() + " Inspection saved change successfully.");
            return "redirect:/inspections/reports?licensePlate="+truck.getLicensePlate();
        } else {
            if (null == action) { 
            redirectAttributes.addFlashAttribute("success", truck.getLicensePlate() + " Inspection saved successfully.");
            return "redirect:/inspections";
        } else 
            switch (action) {
                case "submit" -> {
                    redirectAttributes.addFlashAttribute("success", truck.getLicensePlate() + " Inspection submitted successfully.");
                    return "redirect:/inspections";
                }
                case "save_continue" -> {
                    redirectAttributes.addFlashAttribute("success", truck.getLicensePlate() + " Inspection saved. Continue more entry.");
                    return "redirect:/inspections/create";
                }
                default -> {
                    redirectAttributes.addFlashAttribute("success", truck.getLicensePlate() + " Inspection saved successfully.");
                    return "redirect:/inspections";
                }
            }
        }
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
    public String deleteInspection(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // Find the inspection
            Optional<Inspection> inspectionOpt = inspectionService.findById(id);
            if (inspectionOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Inspection not found.");
                return "redirect:/inspections/reports";
            }

            Inspection inspection = inspectionOpt.get();

            // Find the truck (optional check)
            Optional<Truck> truckOpt = truckService.findById(inspection.getTruck().getId());
            String truckPlate = truckOpt.map(Truck::getLicensePlate).orElse("Unknown Truck");

            // Delete the inspection
            inspectionService.delete(id);

            // Success message
            redirectAttributes.addFlashAttribute("success", truckPlate + " inspection deleted successfully.");

        } catch (Exception e) {
            // Catch any unexpected error
            redirectAttributes.addFlashAttribute("error", "Failed to delete inspection: " + e.getMessage());
        }

        return "redirect:/inspections/reports";
    }

}