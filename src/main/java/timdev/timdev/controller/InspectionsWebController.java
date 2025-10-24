package timdev.timdev.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import timdev.timdev.dto.CustomUserDetails;
import timdev.timdev.dto.InspectionRequestDTO;
import timdev.timdev.entity.Inspection;
import timdev.timdev.entity.TruckInspection;
import timdev.timdev.entity.User;
import timdev.timdev.enums.InspectionStatus;
import timdev.timdev.service.InspectionService;
import timdev.timdev.service.TruckInspectionService;

@AllArgsConstructor
@Controller
@RequestMapping("/inspections")
public class InspectionsWebController {

    @Autowired
    private InspectionService inspectionService;
    @Autowired
    private TruckInspectionService truckService;


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

        List<TruckInspection> allTrucks;
        if (licensePlate != null && !licensePlate.isEmpty()) {
            allTrucks = truckService.findByLicensePlateContaining(licensePlate);
        } else {
            allTrucks = truckService.getAll();
        }

        // allTrucks.sort(Comparator.comparingInt(this::getColorPriority));

        allTrucks.sort((a, b) -> {
            int colorCompare = Integer.compare(getColorPriority(a), getColorPriority(b));
            if (colorCompare != 0) return colorCompare;

            // Same color group → sort by days ascending (most expired first)
            return Long.compare(a.expiredDurationDays(), b.expiredDurationDays());
        });

        // allTrucks.sort((a, b) -> {
        //     long da = a.expiredDurationDays() - 30;
        //     long db = b.expiredDurationDays() - 30;

        //     boolean aExpired = da < 0;
        //     boolean bExpired = db < 0;

        //     if (aExpired && bExpired) {
        //         return Long.compare(da, db); // reverse for expired ones
        //     } else if (aExpired) {
        //         return -1; // expired ones first
        //     } else if (bExpired) {
        //         return 1;
        //     }
        //     return Long.compare(da, db); // normal for non-expired
        // });

        List<TruckInspection> trucks;
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

    private void exportInspectionsToExcel(List<TruckInspection> trucks, HttpServletResponse response) throws IOException {
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
            TruckInspection truck = trucks.get(i);
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

    private CellStyle getInspectionCellStyle(Workbook workbook, TruckInspection truck) {
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
        int priority = getColorPriority(truck);

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

    // private int getColorPriority(TruckInspection truck) {
    //     if (truck.getLastInspection() == null) return 3;
    //     if (truck.getLastInspection().isExpired()) return 0; // expired → no priority

    //     long daysRemaining = truck.getLastInspection().expiredDurationDays() - 30;

    //     if (daysRemaining > 0 && daysRemaining <= 30) {
    //     // Will expire within the next 30 days
    //     return 2;
    //     } else if (daysRemaining < 0) {
    //         // Already expired → red
    //         return 1;
    //     }
    //     // More than 30 days left → normal
    //     return 4;
    // }

    private int getColorPriority(TruckInspection truck) {
        if (truck.getLastInspection() == null) return 4;
        
        long days = truck.expiredDurationDays();

        if (days <= 30) {
            if (truck.getLastInspection().isExpired()) return 0;
            return 1; // 🔴 red — expired or will expire soon
        } else if (days <= 90) {
            return 2; // 🟡 yellow — mid-term expiring
        }
        return 3; // ⚪ normal
    }



    @GetMapping("/reports")
    public Object inspectionsReports(
        Model model,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "50") String sizeParam,
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
        TruckInspection truck = truckService.findById(truckId)
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


        model.addAttribute("selectedTruckInspectionId", truckId != null ? truckId : null);
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
            createInspectionReportCell(row, 2, inspection.getTruckInspection().getLicensePlate());
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
            inspectionDTO.setTruckInspectionId(truckId);
        } else {
            inspectionDTO.setTruckInspectionId(null);
            // Truck truck = truckService.findById(truckId)
            //     .orElseThrow(() -> new RuntimeException("Truck not found"));

            // model.addAttribute("selectedTruck", truck);
        }
        

        
        model.addAttribute("selectedTruckInspectionId", truckId != null ? truckId : null);
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
        TruckInspection truck = truckService.findById(inspectionDTO.getTruckInspectionId()).orElse(null);

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
        List<Inspection> oldInspections = inspectionService.findByTruckInspectionId(truck.getId());
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
        inspection.setTruckInspection(truck);

        

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
        dto.setTruckInspectionId(inspection.getTruckInspection() != null ? inspection.getTruckInspection().getId() : null);
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
            Optional<Inspection> inspectionOpt = inspectionService.findById(id);
            if (inspectionOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Inspection not found.");
                return "redirect:/inspections/reports";
            }

            Inspection inspection = inspectionOpt.get();

            Optional<TruckInspection> truckOpt = truckService.findById(inspection.getTruckInspection().getId());
            if (truckOpt.isPresent()) {
                TruckInspection truck = truckOpt.get();

                // Update truck's last inspection if needed
                if (truck.getLastInspection() != null && truck.getLastInspection().getId().equals(inspection.getId())) {
                    // The inspection being deleted is the last inspection
                    truck.setLastInspection(null);
                    truck.setExpiredDate(null);
                }

                truckService.save(truck);

                String truckPlate = truck.getLicensePlate();

                // Delete inspection
                inspectionService.deleteInspection(id);

                redirectAttributes.addFlashAttribute("success", truckPlate + " inspection deleted successfully.");
            } else {
                // No truck found
                inspectionService.deleteInspection(id);
                redirectAttributes.addFlashAttribute("success", "Inspection deleted successfully.");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete inspection: " + e.getMessage());
        }

        return "redirect:/inspections/reports";
    }




    @GetMapping("/import")
    public String showImportForm() {
        return "inspections/import"; 
    }

    @PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes,
                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try (InputStream is = file.getInputStream();
            Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            User user = userDetails.getUser();

            // Skip header row (row 0)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String licensePlate = row.getCell(1).getStringCellValue().trim();
                TruckInspection truck = truckService.findByLicensePlate(licensePlate).orElse(null);
                if (truck == null) {
                    // Skip if no truck found
                    continue;
                }

                

                int quantity = (int) row.getCell(2).getNumericCellValue();

                LocalDate date = parseExcelDate(row.getCell(3));
                LocalDate expiredDate = parseExcelDate(row.getCell(4));

                if (date == null || expiredDate == null) {
                    redirectAttributes.addFlashAttribute("error", "Date or Expired Date is missing at row " + (i + 1));
                    return "redirect:/inspections/import";
                }

                String note = (row.getCell(5) != null) ? row.getCell(5).getStringCellValue() : "";


                List<Inspection> overlaps = inspectionService.findOverlappingInspections(
                    truck.getId(),
                    date,
                    expiredDate
                );

                if (!overlaps.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", truck.getLicensePlate() + " Inspection overlaps with an existing active inspection!");
                    return "redirect:/inspections/import";
                }

                // ✅ Complete all old inspections for this truck
                List<Inspection> oldInspections = inspectionService.findByTruckInspectionId(truck.getId());
                for (Inspection old : oldInspections) {
                    old.setStatus(InspectionStatus.COMPLETED);
                    inspectionService.save(old);
                }

                // ✅ Create new inspection
                Inspection inspection = new Inspection();
                inspection.setTruckInspection(truck);
                inspection.setQuantity(quantity);
                inspection.setDate(date);
                inspection.setExpiredDate(expiredDate);
                inspection.setNote(note);
                inspection.setCreatedAt(LocalDateTime.now());
                inspection.setCreatedBy(user);
                inspection.setStatus(InspectionStatus.ACTIVE);

                // ✅ Update truck expiredDate
                truck.setExpiredDate(expiredDate);
                truckService.save(truck);

                inspectionService.save(inspection);
            }

            redirectAttributes.addFlashAttribute("success", "File imported successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to import file: " + e.getMessage());
        }
        return "redirect:/inspections";
    }


    private LocalDate parseExcelDate(Cell cell) {
        if (cell == null) return null;

        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            } else if (cell.getCellType() == CellType.STRING) {
                String dateStr = cell.getStringCellValue().trim();
                if (dateStr.isEmpty()) return null;

                DateTimeFormatter[] formatters = {
                    DateTimeFormatter.ofPattern("dd-MMM-yy"),
                    DateTimeFormatter.ofPattern("dd/MM/yy"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                    DateTimeFormatter.ofPattern("MM/dd/yyyy")
                };

                for (DateTimeFormatter fmt : formatters) {
                    try {
                        return LocalDate.parse(dateStr, fmt);
                    } catch (DateTimeParseException ignored) {}
                }
                throw new RuntimeException("Unsupported date format: " + dateStr);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing date: " + e.getMessage());
        }

        return null;
    }

    @GetMapping("/download-template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        // Create workbook
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Truck Template");

        // Header row
        Row header = sheet.createRow(0);
        String[] columns = {"NO", "ស្លាកលេខ", "ចំនួនភ្លៅ", "ថ្ងៃចូលឆៀក", "សពុលភាពឆៀក", "ផ្សេងៗ"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(columns[i]);

            // Optional: style header
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            style.setFont(font);
            style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setAlignment(HorizontalAlignment.CENTER);
            cell.setCellStyle(style);
        }

        // Sample rows
        Object[][] sampleData = {
                {1, "3E-9570", 2, "08-Jan-25", "08-Jan-26", "Note your reason!"},
                {2, "3E-3363", 2, "22-Aug-24", "22-Aug-25", ""}
        };

        // Create styles for data cells
        CellStyle numberStyle = workbook.createCellStyle();
        numberStyle.setAlignment(HorizontalAlignment.CENTER);

        CellStyle textStyle = workbook.createCellStyle();
        textStyle.setAlignment(HorizontalAlignment.LEFT);

        CellStyle dateStyle = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MMM-yy"));
        dateStyle.setAlignment(HorizontalAlignment.CENTER);

        int rowNum = 1;
        for (Object[] rowData : sampleData) {
            Row row = sheet.createRow(rowNum++);
            for (int i = 0; i < rowData.length; i++) {
                Cell cell = row.createCell(i);
                Object value = rowData[i];

                if (value instanceof Integer) {
                    cell.setCellValue((Integer) value);
                    cell.setCellStyle(numberStyle);
                } else if (i == 3 || i == 4) { // Date columns
                    cell.setCellValue((String) value);
                    cell.setCellStyle(dateStyle);
                } else {
                    cell.setCellValue(value.toString());
                    cell.setCellStyle(textStyle);
                }
            }
        }

        // Autosize columns
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write to byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", "truck_inspections_template.xlsx");
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .headers(headers)
                .body(bos.toByteArray());
    }

}
