package timdev.timdev.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

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
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import timdev.timdev.dto.CustomUserDetails;
import timdev.timdev.dto.TruckRequestDTO;
import timdev.timdev.entity.Truck;
import timdev.timdev.entity.TruckFatsReport;
import timdev.timdev.entity.TruckOilsReport;
import timdev.timdev.entity.User;
import timdev.timdev.enums.OilStatus;
import timdev.timdev.enums.TruckSize;
import timdev.timdev.service.ModelService;
import timdev.timdev.service.TruckDistanceService;
import timdev.timdev.service.TruckFatsReportService;
import timdev.timdev.service.TruckOilsReportService;
import timdev.timdev.service.TruckService;

@AllArgsConstructor
@Controller
@RequestMapping("/admin/trucks")
public class TruckWebController {

    private final TruckService truckService;
    private final ModelService modelService;
    private final TruckFatsReportService fatsReportService;
    private final TruckOilsReportService oilsReportService;

    private final TruckDistanceService truckDistanceService;


    @GetMapping
    public String index(Model model,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "20") String sizeParam,
        @RequestParam(value = "all", defaultValue = "false") boolean showAll,
        @RequestParam(value = "licensePlate", required = false) String licensePlate
    ) {

        List<Truck> trucks;
        int totalPages = 1;
        int size;
        
        if ("all".equalsIgnoreCase(sizeParam)) {
            size = Integer.MAX_VALUE;
        } else {
            size = Integer.parseInt(sizeParam); 
        }


        if (showAll) {
            if (licensePlate != null && !licensePlate.isEmpty()) {
                trucks = truckService.findByLicensePlateContaining(licensePlate);
            } else {
                trucks = truckService.getAll();
            }
        } else {
            Pageable pageable = PageRequest.of(page, size);
            Page<Truck> truckPage;
            if (licensePlate != null && !licensePlate.isEmpty()) {
                truckPage = truckService.findByLicensePlateContainingWithPageable(licensePlate, pageable);
            } else {
                truckPage = truckService.getAllWithPageable(pageable);
            }
            trucks = truckPage.getContent();
            totalPages = truckPage.getTotalPages();
        }

        model.addAttribute("trucks", trucks);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", sizeParam);
        model.addAttribute("showAll", showAll);
        model.addAttribute("licensePlate", licensePlate);  
        return "trucks/list";
    }


    



    @GetMapping("/form")
    public String showForm(Model model) {
        
        model.addAttribute("sizes", TruckSize.values());
        model.addAttribute("truck", new TruckRequestDTO());
        model.addAttribute("models", modelService.getAll());
        return "trucks/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Optional<Truck> truck = truckService.findById(id);
        if (truck.isEmpty()) {
            model.addAttribute("error", "Truck not found");
            return "redirect:/admin/trucks";
        }

        Truck t = truck.get();
        TruckRequestDTO dto = new TruckRequestDTO();
        dto.setId(t.getId());
        dto.setLicensePlate(t.getLicensePlate());
        dto.setModelId(t.getModel().getId());
        dto.setYear(t.getYear());
        dto.setKmForFatsShoot(t.getKmForFatsShoot());
        dto.setKmForOilsChange(t.getKmForOilsChange());
        dto.setSize(t.getSize());
        model.addAttribute("sizes", TruckSize.values());
        model.addAttribute("truck", dto);
        model.addAttribute("models", modelService.getAll());
        return "trucks/form";
    }

    @PostMapping({"/", "/update/{id}"})
    public String createOrUpdateTruck(@PathVariable(required = false) Long id,
                                    @Valid @ModelAttribute("truck") TruckRequestDTO truckDTO,
                                    BindingResult result,
                                    Model model, RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("sizes", TruckSize.values());
            model.addAttribute("models", modelService.getAll());
            return "trucks/form";
        }

        try {
            Truck truck;
            if (id != null) {
                truck = truckService.findById(id).orElse(new Truck());
                truck.setId(id);
            } else {
                truck = new Truck();
            }

            truck.setLicensePlate(truckDTO.getLicensePlate());
            truck.setYear(truckDTO.getYear());
            truck.setKmForFatsShoot(truckDTO.getKmForFatsShoot());
            truck.setKmForOilsChange(truckDTO.getKmForOilsChange());
            truck.setSize(truckDTO.getSize());
            // Set the model
            modelService.findById(1L).ifPresent(truck::setModel);

            truckService.save(truck);
            redirectAttributes.addFlashAttribute("success", (id == null ? "Truck created successfully!" : "Truck updated successfully!"));
            return "redirect:/admin/trucks";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Something went wrong: " + e.getMessage());
            model.addAttribute("models", modelService.getAll());
            return "trucks/form";
        }
    }


    @PostMapping("/create")
    public String createTruck(@Valid @ModelAttribute("truck") TruckRequestDTO truckDTO,
                            BindingResult result,
                            Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("sizes", TruckSize.values());
            model.addAttribute("models", modelService.getAll());
            return "trucks/form";
        }

        try {
            Truck truck = new Truck();
            truck.setLicensePlate(truckDTO.getLicensePlate());
            truck.setYear(truckDTO.getYear());
            truck.setKmForFatsShoot(truckDTO.getKmForFatsShoot());
            truck.setKmForOilsChange(truckDTO.getKmForOilsChange());
            truck.setSize(truckDTO.getSize());

            // Set the model
            modelService.findById(1L).ifPresent(truck::setModel);

            truckService.save(truck);
            redirectAttributes.addFlashAttribute("success", "Truck created successfully!");
            return "redirect:/admin/trucks";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Something went wrong: " + e.getMessage());
            model.addAttribute("models", modelService.getAll());
            return "trucks/form";
        }
    }

    // Delete truck by ID
    @GetMapping("/delete/{id}")
    public String deleteTruck(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Optional<Truck> truckOpt = truckService.findById(id);
            if (truckOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Truck not found");
            } else {
                Truck truck = truckOpt.get();
                if(truck.getLastFatsReport() != null) {
                    redirectAttributes.addFlashAttribute("error", "This Truck can not delete");
                    return "redirect:/admin/trucks";
                }
                truckService.deleteById(id);
                redirectAttributes.addFlashAttribute("success", "Truck deleted successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Something went wrong: " + e.getMessage());
        }
        return "redirect:/admin/trucks";
    }



    

    // @GetMapping("/fats/edit/{id}")
    // public String showFormEditFatsShooted(@PathVariable Long id, Model model) {

    //     TruckFatsReport report = fatsReportService.findById(id)
    //             .orElseThrow(() -> new RuntimeException("Truck not found"));

    //     model.addAttribute("report", report);
    //     return "trucks/edit_change_fats";
    // }

    // @GetMapping("/fats/delete/{id}")
    // public String deleteFatsShooted(@PathVariable Long id, Model model) {

    //     TruckFatsReport report = fatsReportService.findById(id)
    //             .orElseThrow(() -> new RuntimeException("Truck not found"));

    //     model.addAttribute("report", report);
    //     return "redirect:/admin/trucks?success=Fats report deleted successfully!";
    // }











    //  Shoot fats
    @GetMapping("/shoot/fats")
    public Object indexShootFats(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") String sizeParam,
            @RequestParam(value = "all", defaultValue = "false") boolean showAll,
            @RequestParam(value = "licensePlate", required = false) String licensePlate,
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

        allTrucks.sort(Comparator.comparingInt(this::getFatsPriority));

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
            exportFatsToExcel(trucks, response);
            return null; 
        }

        model.addAttribute("trucks", trucks);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", sizeParam);
        model.addAttribute("showAll", showAll);
        model.addAttribute("licensePlate", licensePlate);

        return "fats_shoot/list";
    }


    private int getFatsPriority(Truck truck) {
        // higher priority first (smaller number = higher priority)
        if (truck.getKmForFatsShoot() == 4000) {
            if (truck.getKmFatsBalance() < 500) return 1; // red
            if (truck.getKmFatsBalance() <= 500) return 2; // yellow
            return 3; // normal
        }
        if (truck.getKmForFatsShoot() == 3500 || truck.getKmForFatsShoot() == 2500) {
            if (truck.getKmFatsBalance() < 200) return 1; // red
            if (truck.getKmFatsBalance() <= 200) return 2; // yellow
            return 3; // normal
        }
        return 4; // default
    }


    private int getOilsPriority(Truck truck) {
        Double balance = truck.getKmOilsBalance();

        if (balance == null) {
            return 4; // fallback
        }
        if (balance < 0) {
            return 1; // danger
        }
        if (balance == 500) {
            return 2; // safe
        }
        if (balance > 0 && balance < 500) {
            return 3; // warning
        }
        return 4; // fallback / unknown
    }


    private String getOilsColorClass(Truck truck) {
        Double balance = truck.getKmOilsBalance();

        if (balance == null) {
            return "insufficient final-condition";
        }

        if (balance < 0) {
            return "bg-[#ff0000] need-to-update text-white"; // danger
        }
        if (balance == 500) {
            return "bg-[#E87A7AFF] need-to-update text-white"; // safe
        }
        if (balance > 0 && balance < 500) {
            return "bg-yellow-500 need-to-update text-white"; // warning
        }
        return "insufficient final-condition"; // fallback
    }



    private void exportOilsToExcel(List<Truck> trucks, HttpServletResponse response) throws IOException {
        // Set response headers
        String fileName = "oil-change-report-" + LocalDate.now() + ".xlsx";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                .replace("+", "%20");
        
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
        response.setCharacterEncoding("UTF-8");
            // Create workbook and sheet
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Oil Change Report");
        
        // Create header row with styling
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);
        
        String[] headers = {
            "#", "License Plate", "Model", "Year", 
            "KM for Oil Change", "Current KM", "Last Oil Change Date",
            "Last Oil Change KM", "Next Range", "KM Balance"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 5000); // Set column width
        }
        
        // Create data rows with color coding
        int rowNum = 1;
        for (int i = 0; i < trucks.size(); i++) {
            Truck truck = trucks.get(i);
            Row row = sheet.createRow(rowNum++);
            
            // Apply color style based on oil balance
            CellStyle rowStyle = getOilBalanceCellStyle(workbook, truck);
            
            // Populate data
            createCell(row, 0, i + 1, rowStyle); // #
            createCell(row, 1, truck.getLicensePlate(), rowStyle);
            createCell(row, 2, truck.getModel() != null ? truck.getModel().getName() : "", rowStyle);
            createCell(row, 3, truck.getYear(), rowStyle);
            createCell(row, 4, truck.getKmForOilsChange(), rowStyle);
            createCell(row, 5, truck.getCurrentKm(), rowStyle);
            
            // Last oil change data
            String lastChangeDate = truck.getLastOilsReport() != null && truck.getLastOilsReport().getDate() != null ?
                truck.getLastOilsReport().getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "";
            Double lastChangeKm = truck.getLastOilsReport() != null ? truck.getLastOilsReport().getCurrentKm() : null;
            Double nextRange = truck.getLastOilsReport() != null ? truck.getLastOilsReport().getNextRange() : null;
            
            createCell(row, 6, lastChangeDate, rowStyle);
            createCell(row, 7, lastChangeKm, rowStyle);
            createCell(row, 8, nextRange, rowStyle);
            createCell(row, 9, truck.getKmOilsBalance(), rowStyle);
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


    // បាញ់ខ្លាញ់
    private void exportFatsToExcel(List<Truck> trucks, HttpServletResponse response) throws IOException {
        // Set response headers
        String fileName = "Fats-change-report-" + LocalDate.now() + ".xlsx";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                .replace("+", "%20");
        
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
        response.setCharacterEncoding("UTF-8");
            // Create workbook and sheet
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Fats Change Report");
        
        // Create header row with styling
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);
        
        String[] headers = {
            "#", "License Plate", "Model", "Year", 
            "KM for Fats Change", "Current KM", "Last Fats Change Date",
            "Last Fats Change KM", "Next Range", "KM Balance"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 5000); // Set column width
        }
        
        // Create data rows with color coding
        int rowNum = 1;
        for (int i = 0; i < trucks.size(); i++) {
            Truck truck = trucks.get(i);
            Row row = sheet.createRow(rowNum++);
            
            // Apply color style based on oil balance
            CellStyle rowStyle = getFatBalanceCellStyle(workbook, truck);
            
            // Populate data
            createCell(row, 0, i + 1, rowStyle); // #
            createCell(row, 1, truck.getLicensePlate(), rowStyle);
            createCell(row, 2, truck.getModel() != null ? truck.getModel().getName() : "", rowStyle);
            createCell(row, 3, truck.getYear(), rowStyle);
            createCell(row, 4, truck.getKmForFatsShoot(), rowStyle);
            createCell(row, 5, truck.getCurrentKm(), rowStyle);
            
            // Last Fat change data
            String lastChangeDate = truck.getLastFatsReport() != null && truck.getLastFatsReport().getDate() != null ?
                truck.getLastFatsReport().getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "";
            Double lastChangeKm = truck.getLastFatsReport() != null ? truck.getLastFatsReport().getCurrentKm() : null;
            Double nextRange = truck.getLastFatsReport() != null ? truck.getLastFatsReport().getNextRange() : null;
            
            createCell(row, 6, lastChangeDate, rowStyle);
            createCell(row, 7, lastChangeKm, rowStyle);
            createCell(row, 8, nextRange, rowStyle);
            createCell(row, 9, truck.getKmFatsBalance(), rowStyle);
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

    private CellStyle getFatBalanceCellStyle(Workbook workbook, Truck truck) {
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
        int priority = getFatsPriority(truck);

        switch (priority) {
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



    private CellStyle getOilBalanceCellStyle(Workbook workbook, Truck truck) {
        CellStyle style = workbook.createCellStyle();
        
        // Common styling
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // Apply colors based on oil balance (matching your HTML colors)
        Double balance = truck.getKmOilsBalance();
        
        if (balance == null) {
            // Default style - no special background
            return style;
        }
        
        if (balance < 0) {
            // Red background for negative balance
            style.setFillForegroundColor(IndexedColors.RED.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // White text for better contrast
            Font font = workbook.createFont();
            font.setColor(IndexedColors.WHITE.getIndex());
            style.setFont(font);
        } else if (balance == 500) {
            // Light red background
            style.setFillForegroundColor(IndexedColors.CORAL.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // White text for better contrast
            Font font = workbook.createFont();
            font.setColor(IndexedColors.WHITE.getIndex());
            style.setFont(font);
        } else if (balance > 0 && balance < 500) {
            // Yellow background for warning
            style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // Dark text for contrast on yellow
            Font font = workbook.createFont();
            font.setColor(IndexedColors.BLACK.getIndex());
            style.setFont(font);
        }
        // For balance > 500, use default style (no background)
        
        return style;
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




    @GetMapping("/fats/shoot")
            public String showAddDistanceForm(@RequestParam(name = "truck_id", required = false) Long truckId, Model model) {

        Truck truck = truckService.findById(truckId)
                .orElseThrow(() -> new RuntimeException("Truck not found"));

        TruckFatsReport report = new TruckFatsReport();
        report.setTruck(truck);
        report.setStatus(OilStatus.COMPLETED);
        report.setCurrentKm(truck.getCurrentKm());

        model.addAttribute("truck", truck);
        model.addAttribute("truckFatsReport", report);
        model.addAttribute("statuses", OilStatus.values());
        model.addAttribute("currentDate", LocalDate.now());
        return "fats_shoot/shoot";
    }

    @PostMapping("/fats/shoot/{id}")
    public String saveChangeFats(
        @PathVariable("id") Long truckId,
        @Valid @ModelAttribute("truckFatsReport") TruckFatsReport report,
        BindingResult result,
        Model model,
        RedirectAttributes redirectAttributes,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Truck truck = truckService.findById(truckId)
                .orElseThrow(() -> new RuntimeException("Truck not found"));

        if (result.hasErrors()) {
            model.addAttribute("truck", truck);
            model.addAttribute("statuses", OilStatus.values());
            model.addAttribute("currentDate", LocalDate.now());
            return "fats_shoot/shoot";
        }

        // calculate next range
        Double nextKmForFatShot = truck.getKmForFatsShoot() + report.getDistanceKm();
        User user = userDetails.getUser();
        // always set truck & timestamps
        report.setId(null);
        report.setTruck(truck);
        report.setStatus(report.getStatus());
        report.setNextRange(nextKmForFatShot);
        report.setStatus(OilStatus.COMPLETED);
        report.setKmForFatsShoot(truck.getKmForFatsShoot());
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());

        report.setDistanceKm(report.getDistanceKm());
        report.setCreatedBy(user);
        report.setUpdatedBy(user);
        fatsReportService.save(report);
        

        truck.setNextFatsRange(nextKmForFatShot);

        truck.setStatus(report.getStatus());


        truckService.save(truck);

        redirectAttributes.addFlashAttribute("success", "Truck "+ truck.getLicensePlate() + " បានបាញ់ខ្លាញ់ដោយជោគជ័យ");

        return "redirect:/admin/trucks/shoot/fats";
    }

    @GetMapping("/fats/reports")
    public String fatsReports(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") String sizeParam,
            @RequestParam(value = "all", defaultValue = "false") boolean showAll,
            @RequestParam(value = "truck_id", required = false) Long truckId,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate toDate
    ) {
        // load trucks for filter dropdown
        List<Truck> trucks = truckService.getAll();

        List<TruckFatsReport> reports;
        int totalPages = 1;
        int size;
        
        if ("all".equalsIgnoreCase(sizeParam)) {
            size = Integer.MAX_VALUE;
        } else {
            size = Integer.parseInt(sizeParam); 
        }
        if (showAll) {
            // fetch all reports with filter
            reports = fatsReportService.getAllFiltered(truckId, fromDate, toDate);
        } else {
            Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
            Page<TruckFatsReport> truckPage = fatsReportService.getAllWithPageable(pageable, truckId, fromDate, toDate);
            reports = truckPage.getContent();
            totalPages = truckPage.getTotalPages();
        }

        // put everything into model
        model.addAttribute("reports", reports);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", sizeParam);
        model.addAttribute("showAll", showAll);

        // preserve filters in the view
        model.addAttribute("selectedTruckId", truckId != null ? truckId : null);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("trucks", trucks);

        return "fats_shoot/reports";
    }




    // oils
    @GetMapping("/change/oils")
    public Object indexChangeOils(Model model,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "20") String sizeParam,
        @RequestParam(value = "all", defaultValue = "false") boolean showAll,
        @RequestParam(value = "licensePlate", required = false) String licensePlate,
        @RequestParam(value = "export", required = false) String export,
        HttpServletResponse response
    ) throws IOException {
        List<Truck> allTrucks;
        if (licensePlate != null && !licensePlate.isEmpty()) {
            allTrucks = truckService.findByLicensePlateContaining(licensePlate);
        } else {
            allTrucks = truckService.getAll();
        }

        allTrucks.sort(Comparator.comparingInt(this::getOilsPriority));
        
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
            exportOilsToExcel(trucks, response);
            return null;
        }

        model.addAttribute("trucks", trucks);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", sizeParam);
        model.addAttribute("showAll", showAll);
        model.addAttribute("licensePlate", licensePlate);  
        return "oils_change/list";
    }

    @GetMapping("/oils/change")
    public String showOilChangeForm(@RequestParam(name = "truck_id", required = false) Long truckId, Model model) {

        Truck truck = truckService.findById(truckId)
                .orElseThrow(() -> new RuntimeException("Truck not found"));

        TruckOilsReport report = new TruckOilsReport();
        report.setTruck(truck);
        report.setStatus(OilStatus.COMPLETED);
        report.setCurrentKm(truck.getCurrentKm());

        model.addAttribute("truck", truck);
        model.addAttribute("truckOilsReport", report);
        model.addAttribute("statuses", OilStatus.values());
        model.addAttribute("currentDate", LocalDate.now());
        return "oils_change/change";
    }

    @GetMapping("/oils/get-distance-km-with-date-truck")
    public ResponseEntity<BigDecimal> getDistanceKmWithDateTruck(
            @RequestParam("truckId") Long truckId,
            @RequestParam(value = "date", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate selectedDate) {

        Double totalDistance = truckDistanceService.getTotalDistanceFromDate(truckId, selectedDate);
        BigDecimal rounded = BigDecimal.valueOf(totalDistance)
                .setScale(2, RoundingMode.HALF_UP);

        return ResponseEntity.ok(rounded);
    }

 
    @PostMapping("/oils/change/{id}")
    public String saveChangeOil(
        @PathVariable("id") Long truckId,
        @Valid @ModelAttribute("truckOilsReport") TruckOilsReport report,
        BindingResult result,
        Model model,
        RedirectAttributes redirectAttributes,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Truck truck = truckService.findById(truckId)
                .orElseThrow(() -> new RuntimeException("Truck not found"));

        if (result.hasErrors()) {
            model.addAttribute("truck", truck);
            model.addAttribute("statuses", OilStatus.values());
            model.addAttribute("currentDate", LocalDate.now());
            return "oils_change/change";
        }

        // calculate next range
        Double nextKmForOilsChange = truck.getKmForOilsChange() + report.getDistanceKm();

        User user = userDetails.getUser();
        // always set truck & timestamps
        report.setId(null);
        report.setTruck(truck);
        report.setStatus(report.getStatus());
        report.setNextRange(nextKmForOilsChange);
        report.setStatus(OilStatus.COMPLETED);
        report.setKmForOilsChange(truck.getKmForOilsChange());
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());
        
        report.setDistanceKm(report.getDistanceKm());
        report.setCreatedBy(user);
        report.setUpdatedBy(user);
        oilsReportService.save(report);
        

        truck.setNextOilsRange(nextKmForOilsChange);

        truck.setStatus(report.getStatus());


        truckService.save(truck);

        redirectAttributes.addFlashAttribute("success", "Truck "+ truck.getLicensePlate() + " បានបាញ់ខ្លាញ់ដោយជោគជ័យ");

        return "redirect:/admin/trucks/change/oils";
    }

    @GetMapping("/oils/change/edit/{reportId}")
    public String editChangeOil(
            @PathVariable("reportId") Long reportId,
            Model model
    ) {
        TruckOilsReport report = oilsReportService.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Oil report not found"));

        Truck truck = report.getTruck();

        model.addAttribute("truck", truck);
        model.addAttribute("truckOilsReport", report);
        model.addAttribute("statuses", OilStatus.values());
        model.addAttribute("currentDate", report.getDate()); // show existing date

        return "oils_change/edit";
    }

    @PostMapping("/oils/change/update/{reportId}")
    public String updateChangeOil(
            @PathVariable("reportId") Long reportId,
            @Valid @ModelAttribute("truckOilsReport") TruckOilsReport formReport,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TruckOilsReport existing = oilsReportService.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Oil report not found"));

        Truck truck = existing.getTruck();

        if (result.hasErrors()) {
            model.addAttribute("truck", truck);
            model.addAttribute("statuses", OilStatus.values());
            return "oils_change/edit";
        }

        // Calculate next range
        Double nextKmForOilsChange =
                truck.getKmForOilsChange() + formReport.getDistanceKm();

        User user = userDetails.getUser();

        // Update fields
        existing.setDate(formReport.getDate());
        existing.setDistanceKm(formReport.getDistanceKm());
        existing.setLiterQuantityOfOils(formReport.getLiterQuantityOfOils());
        existing.setNote(formReport.getNote());
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(user);

        // Reset statuses
        existing.setNextRange(nextKmForOilsChange);
        existing.setStatus(OilStatus.COMPLETED);

        oilsReportService.save(existing);

        // Update truck
        truck.setNextOilsRange(nextKmForOilsChange);
        truck.setStatus(existing.getStatus());

        truckService.save(truck);

        redirectAttributes.addFlashAttribute("success",
                "បានកែប្រែការប្ដូរប្រេងរបស់ឡាន " + truck.getLicensePlate() + " ដោយជោគជ័យ!");

        return "redirect:/admin/trucks/change/oils";
    }



    @GetMapping("/oils/delete/{id}")
        public String deleteOilReport(
                @PathVariable("id") Long reportId,
                RedirectAttributes redirectAttributes
        ) {
            try {
                oilsReportService.deleteById(reportId);
                redirectAttributes.addFlashAttribute("success", "បានលុបរបាយការណ៍បាញ់ខ្លាញ់ដោយជោគជ័យ!");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "មានបញ្ហាក្នុងការលុបរបាយការណ៍!");
            }

            return "redirect:/admin/trucks/change/oils";
        }


    @GetMapping("/oils/reports")
    public String oilsReports(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") String sizeParam,
            @RequestParam(value = "all", defaultValue = "false") boolean showAll,
            @RequestParam(value = "truck_id", required = false) Long truckId,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate toDate
    ) {
        // load trucks for filter dropdown
        List<Truck> trucks = truckService.getAll();

        List<TruckOilsReport> reports;
        int totalPages = 1;
        int size;
        
        if ("all".equalsIgnoreCase(sizeParam)) {
            size = Integer.MAX_VALUE;
        } else {
            size = Integer.parseInt(sizeParam); 
        }
        if (showAll) {
            // fetch all reports with filter
            reports = oilsReportService.getAllFiltered(truckId, fromDate, toDate);
        } else {
            Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
            Page<TruckOilsReport> truckPage = oilsReportService.getAllWithPageable(pageable, truckId, fromDate, toDate);
            reports = truckPage.getContent();
            totalPages = truckPage.getTotalPages();
        }

        // put everything into model
        model.addAttribute("reports", reports);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", sizeParam);
        model.addAttribute("showAll", showAll);

        // preserve filters in the view
        model.addAttribute("selectedTruckId", truckId != null ? truckId : null);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("trucks", trucks);

        return "oils_change/reports";
    }





    // export as excel and pdf file
    @GetMapping("/fats/reports/export")
    public void exportFatShootReports(
            @RequestParam(value = "truck_id", required = false) Long truckId,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate toDate,
            @RequestParam(value = "excel", required = false, defaultValue = "false") boolean excel,
            @RequestParam(value = "pdf", required = false, defaultValue = "false") boolean pdf,
            HttpServletResponse response) throws IOException {

        // fetch filtered data
        List<TruckFatsReport> reports = fatsReportService.getAllFiltered(truckId, fromDate, toDate);

        if (excel) {
            // Export to Excel
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=fats-reports.xlsx");

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Fats Reports");

                int rowIdx = 0;
                // Header row
                Row headerRow = sheet.createRow(rowIdx++);
                String[] headers = {"#", "License Plate", "Current Km", "Date", "Shot Km", "Next Range", "Note"};
                for (int i = 0; i < headers.length; i++) {
                    headerRow.createCell(i).setCellValue(headers[i]);
                }

                int index = 1;
                for (TruckFatsReport report : reports) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(index++);
                    row.createCell(1).setCellValue(report.getTruck().getLicensePlate());
                    row.createCell(2).setCellValue(report.getTruck().getCurrentKm());
                    row.createCell(3).setCellValue(report.getDate().toString());
                    row.createCell(4).setCellValue(report.getCurrentKm());
                    row.createCell(5).setCellValue(report.getNextRange());
                    row.createCell(6).setCellValue(report.getNote() != null ? report.getNote().replaceAll("\\<.*?\\>", "") : "");
                }

                workbook.write(response.getOutputStream());
            }

        } else if (pdf) {
            // Export to PDF
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=fats-reports.pdf");

            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            try {
                com.itextpdf.text.pdf.PdfWriter.getInstance(document, response.getOutputStream());
                document.open();
                document.add(new com.itextpdf.text.Paragraph("Fats Reports"));
                document.add(new com.itextpdf.text.Paragraph(" "));

                com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(7);
                table.setWidthPercentage(100);
                table.setSpacingBefore(10f);

                // headers
                Stream.of("#", "License Plate", "Current Km", "Date", "Shot Km", "Next Range", "Note")
                        .forEach(headerTitle -> {
                            com.itextpdf.text.pdf.PdfPCell headerCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(headerTitle));
                            headerCell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
                            table.addCell(headerCell);
                        });

                int index = 1;
                for (TruckFatsReport report : reports) {
                    table.addCell(String.valueOf(index++));
                    table.addCell(report.getTruck().getLicensePlate());
                    table.addCell(String.valueOf(report.getTruck().getCurrentKm()));
                    table.addCell(report.getDate().toString());
                    table.addCell(String.valueOf(report.getCurrentKm()));
                    table.addCell(String.valueOf(report.getNextRange()));
                    table.addCell(report.getNote() != null ? report.getNote().replaceAll("\\<.*?\\>", "") : "");
                }

                document.add(table);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                document.close();
            }
        }
    }

    // oils
    @GetMapping("/oils/reports/export")
    public void exportOilChangeReports(
            @RequestParam(value = "truck_id", required = false) Long truckId,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate toDate,
            @RequestParam(value = "excel", required = false, defaultValue = "false") boolean excel,
            @RequestParam(value = "pdf", required = false, defaultValue = "false") boolean pdf,
            HttpServletResponse response) throws IOException {

        // fetch filtered data
        List<TruckOilsReport> reports = oilsReportService.getAllFiltered(truckId, fromDate, toDate);

        if (excel) {
            // Export to Excel
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=Oils-reports.xlsx");

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Oils Reports");

                int rowIdx = 0;
                // Header row
                Row headerRow = sheet.createRow(rowIdx++);
                String[] headers = {"#", "License Plate", "Current Km", "Date", "Shot Km", "Next Range", "Note"};
                for (int i = 0; i < headers.length; i++) {
                    headerRow.createCell(i).setCellValue(headers[i]);
                }

                int index = 1;
                for (TruckOilsReport report : reports) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(index++);
                    row.createCell(1).setCellValue(report.getTruck().getLicensePlate());
                    row.createCell(2).setCellValue(report.getTruck().getCurrentKm());
                    row.createCell(3).setCellValue(report.getDate().toString());
                    row.createCell(4).setCellValue(report.getCurrentKm());
                    row.createCell(5).setCellValue(report.getNextRange());
                    row.createCell(6).setCellValue(report.getNote() != null ? report.getNote().replaceAll("\\<.*?\\>", "") : "");
                }

                workbook.write(response.getOutputStream());
            }

        } else if (pdf) {
            // Export to PDF
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=Oils-reports.pdf");

            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            try {
                com.itextpdf.text.pdf.PdfWriter.getInstance(document, response.getOutputStream());
                document.open();
                document.add(new com.itextpdf.text.Paragraph("Oils Reports"));
                document.add(new com.itextpdf.text.Paragraph(" "));

                com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(7);
                table.setWidthPercentage(100);
                table.setSpacingBefore(10f);

                // headers
                Stream.of("#", "License Plate", "Current Km", "Date", "Shot Km", "Next Range", "Note")
                        .forEach(headerTitle -> {
                            com.itextpdf.text.pdf.PdfPCell headerCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(headerTitle));
                            headerCell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
                            table.addCell(headerCell);
                        });

                int index = 1;
                for (TruckOilsReport report : reports) {
                    table.addCell(String.valueOf(index++));
                    table.addCell(report.getTruck().getLicensePlate());
                    table.addCell(String.valueOf(report.getTruck().getCurrentKm()));
                    table.addCell(report.getDate().toString());
                    table.addCell(String.valueOf(report.getCurrentKm()));
                    table.addCell(String.valueOf(report.getNextRange()));
                    table.addCell(report.getNote() != null ? report.getNote().replaceAll("\\<.*?\\>", "") : "");
                }

                document.add(table);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                document.close();
            }
        }
    }





}
