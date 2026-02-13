package timdev.timdev.controller;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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

@Controller
@RequestMapping("/admin/trucks")
public class TruckWebController {

    @Autowired
    private TruckService truckService;
    @Autowired
    private ModelService modelService;
    @Autowired
    private TruckFatsReportService fatsReportService;
    @Autowired
    private TruckOilsReportService oilsReportService;
    @Autowired
    private TruckDistanceService truckDistanceService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @GetMapping
    public String index(Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") String sizeParam,
            @RequestParam(value = "all", defaultValue = "false") boolean showAll,
            @RequestParam(value = "licensePlate", required = false) String licensePlate) {

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
        dto.setYear(0);
        dto.setKmForFatsShoot(t.getKmForFatsShoot());
        dto.setKmForOilsChange(t.getKmForOilsChange());
        dto.setSize(t.getSize());
        dto.setGroupName(t.getGroupName());
        dto.setModelName(t.getModelName());
        dto.setYearOfManufacture(t.getYearOfManufacture());
        model.addAttribute("sizes", TruckSize.values());
        model.addAttribute("truck", dto);
        model.addAttribute("models", modelService.getAll());
        return "trucks/form";
    }

    @PostMapping({ "/", "/update/{id}" })
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

            truck.setGroupName(truckDTO.getGroupName());
            truck.setModelName(truckDTO.getModelName());
            truck.setYearOfManufacture(truckDTO.getYearOfManufacture());
            // Set the model
            modelService.findById(1L).ifPresent(truck::setModel);

            truckService.save(truck);
            redirectAttributes.addFlashAttribute("success",
                    (id == null ? "Truck [" + truck.getLicensePlate() + "] created successfully!"
                            : "Truck [" + truck.getLicensePlate() + "] updated successfully!"));
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
            truck.setYear(0);
            truck.setKmForFatsShoot(truckDTO.getKmForFatsShoot());
            truck.setKmForOilsChange(truckDTO.getKmForOilsChange());
            truck.setSize(truckDTO.getSize());

            truck.setGroupName(truckDTO.getGroupName());
            truck.setModelName(truckDTO.getModelName());
            truck.setYearOfManufacture(truckDTO.getYearOfManufacture());

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
                if (truck.getLastFatsReport() != null || truck.getLastOilsReport() != null) {
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

    // TruckFatsReport report = fatsReportService.findById(id)
    // .orElseThrow(() -> new RuntimeException("Truck not found"));

    // model.addAttribute("report", report);
    // return "trucks/edit_change_fats";
    // }

    // @GetMapping("/fats/delete/{id}")
    // public String deleteFatsShooted(@PathVariable Long id, Model model) {

    // TruckFatsReport report = fatsReportService.findById(id)
    // .orElseThrow(() -> new RuntimeException("Truck not found"));

    // model.addAttribute("report", report);
    // return "redirect:/admin/trucks?success=Fats report deleted successfully!";
    // }

    // Shoot fats
    @GetMapping("/shoot/fats")
    public Object indexShootFats(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "100") String sizeParam,
            @RequestParam(value = "all", defaultValue = "false") boolean showAll,
            @RequestParam(value = "licensePlate", required = false) String licensePlate,
            @RequestParam(value = "export", required = false) String export,
            HttpServletResponse response) throws IOException {

        List<Truck> allTrucks;
        if (licensePlate != null && !licensePlate.isEmpty()) {
            allTrucks = truckService.advancedFilter(licensePlate);
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
        model.addAttribute("size", size);
        model.addAttribute("showAll", showAll);
        model.addAttribute("licensePlate", licensePlate);

        return "fats_shoot/list";
    }

    private int getFatsPriority(Truck truck) {
        // higher priority first (smaller number = higher priority)
        if (truck.getKmForFatsShoot() == 4000) {
            if (truck.getKmFatsBalance() < 500)
                return 1; // red
            if (truck.getKmFatsBalance() <= 500)
                return 2; // yellow
            return 3; // normal
        }
        if (truck.getKmForFatsShoot() == 3500 || truck.getKmForFatsShoot() == 2500) {
            if (truck.getKmFatsBalance() < 200)
                return 1; // red
            if (truck.getKmFatsBalance() <= 200)
                return 2; // yellow
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
                "#", "License Plate", "Model", "Year", "ផ្នែកការរ៉ាស់",
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
            createCell(row, 2, truck.getModelName() != null ? truck.getModelName() : "", rowStyle);
            createCell(row, 3, truck.getYearOfManufacture() != null ? truck.getYearOfManufacture() : "", rowStyle);
            createCell(row, 4, truck.getGroupName() != null ? truck.getGroupName() : "", rowStyle);
            createCell(row, 5, truck.getKmForOilsChange(), rowStyle);
            createCell(row, 6, truck.getCurrentKm(), rowStyle);

            // Last oil change data
            String lastChangeDate = truck.getLastOilsReport() != null && truck.getLastOilsReport().getDate() != null
                    ? truck.getLastOilsReport().getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                    : "";
            Double lastChangeKm = truck.getLastOilsReport() != null ? truck.getLastOilsReport().getCurrentKm() : null;
            Double nextRange = truck.getLastOilsReport() != null ? truck.getLastOilsReport().getNextRange() : null;

            createCell(row, 7, lastChangeDate, rowStyle);
            createCell(row, 8, lastChangeKm, rowStyle);
            createCell(row, 9, nextRange, rowStyle);
            createCell(row, 10, truck.getKmOilsBalance(), rowStyle);
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
                "#", "License Plate", "Model", "Year", "ផ្នែកការរ៉ាស់",
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
            createCell(row, 2, truck.getModelName() != null ? truck.getModelName() : "", rowStyle);
            createCell(row, 3, truck.getYearOfManufacture() != null ? truck.getYearOfManufacture() : "", rowStyle);
            createCell(row, 4, truck.getGroupName() != null ? truck.getGroupName() : "", rowStyle);
            createCell(row, 5, truck.getKmForFatsShoot(), rowStyle);
            createCell(row, 6, truck.getCurrentKm(), rowStyle);

            // Last Fat change data
            String lastChangeDate = truck.getLastFatsReport() != null && truck.getLastFatsReport().getDate() != null
                    ? truck.getLastFatsReport().getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                    : "";
            Double lastChangeKm = truck.getLastFatsReport() != null ? truck.getLastFatsReport().getCurrentKm() : null;
            Double nextRange = truck.getLastFatsReport() != null ? truck.getLastFatsReport().getNextRange() : null;

            createCell(row, 7, lastChangeDate, rowStyle);
            createCell(row, 8, lastChangeKm, rowStyle);
            createCell(row, 9, nextRange, rowStyle);
            createCell(row, 10, truck.getKmFatsBalance(), rowStyle);
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
    public String saveFatsShoot(
            @PathVariable("id") Long truckId,
            @Valid @ModelAttribute("truckFatsReport") TruckFatsReport report,
            @RequestParam("file") MultipartFile file,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Truck truck = truckService.findById(truckId)
                .orElseThrow(() -> new RuntimeException("Truck not found"));

        if (result.hasErrors()) {
            model.addAttribute("truck", truck);
            model.addAttribute("statuses", OilStatus.values());
            model.addAttribute("currentDate", LocalDate.now());
            return "fats_shoot/shoot";
        }

        if (!file.isEmpty()) {

            try {

                File uploadPath = new File(uploadDir);
                if (!uploadPath.exists()) {
                    uploadPath.mkdirs();
                }

                String originalName = file.getOriginalFilename();
                String extension = "";

                if (originalName != null && originalName.contains(".")) {
                    extension = originalName.substring(originalName.lastIndexOf("."));
                }

                String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
                Path filePath = Paths.get(uploadDir, fileName);

                file.transferTo(filePath);

                report.setFilePath("/uploads/" + fileName);

            } catch (IOException e) {
                throw new RuntimeException("File upload failed", e);
            }
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

        report.setLocationChanged(report.getLocationChanged());

        report.setDistanceKm(report.getDistanceKm());
        report.setCreatedBy(user);
        report.setUpdatedBy(user);
        fatsReportService.save(report);

        truck.setNextFatsRange(nextKmForFatShot);

        truck.setStatus(report.getStatus());

        truckService.save(truck);

        redirectAttributes.addFlashAttribute("success", "Truck " + truck.getLicensePlate() + " បានបាញ់ខ្លាញ់ដោយជោគជ័យ");

        return "redirect:/admin/trucks/shoot/fats";
    }

    @GetMapping("/fats/shoot/edit/{reportId}")
    public String showEditFatForm(
            @PathVariable("reportId") Long reportId,
            Model model) {
        TruckFatsReport report = fatsReportService.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Oil report not found"));

        Truck truck = report.getTruck();

        model.addAttribute("truck", truck);
        model.addAttribute("truckFatsReport", report);
        model.addAttribute("statuses", OilStatus.values());
        model.addAttribute("currentDate", report.getDate());

        return "fats_shoot/edit";
    }

    @PostMapping("/fats/shoot/update/{reportId}")
    public String updateFatsShoot(
            @PathVariable("reportId") Long reportId,
            @Valid @ModelAttribute("truckFatsReport") TruckFatsReport formReport,
            @RequestParam("file") MultipartFile file,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        TruckFatsReport existing = fatsReportService.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Fats report not found"));

        Truck truck = existing.getTruck();

        if (result.hasErrors()) {
            model.addAttribute("truck", truck);
            model.addAttribute("statuses", OilStatus.values());
            return "fats_shoot/edit";
        }

        User user = userDetails.getUser();

        // =========================
        // FILE UPDATE
        // =========================
        if (file != null && !file.isEmpty()) {

            try {

                File uploadPath = new File(uploadDir);
                if (!uploadPath.exists()) {
                    uploadPath.mkdirs();
                }

                // 🔥 Delete old file
                if (existing.getFilePath() != null) {
                    String oldFileName = existing.getFilePath().replace("/uploads/", "");
                    Path oldFilePath = Paths.get(uploadDir, oldFileName);
                    Files.deleteIfExists(oldFilePath);
                }

                // 🔥 Generate random file name
                String originalName = file.getOriginalFilename();
                String extension = "";

                if (originalName != null && originalName.contains(".")) {
                    extension = originalName.substring(originalName.lastIndexOf("."));
                }

                String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
                Path newFilePath = Paths.get(uploadDir, fileName);

                file.transferTo(newFilePath);

                existing.setFilePath("/uploads/" + fileName);

            } catch (IOException e) {
                throw new RuntimeException("File upload failed", e);
            }
        }

        // =========================
        // UPDATE DATA
        // =========================

        Double nextKmForFatShot = truck.getKmForFatsShoot() + formReport.getDistanceKm();

        existing.setDate(formReport.getDate());
        existing.setDistanceKm(formReport.getDistanceKm());
        existing.setLiterQuantityOfFats(formReport.getLiterQuantityOfFats());
        existing.setNote(formReport.getNote());
        existing.setLocationChanged(formReport.getLocationChanged());

        existing.setNextRange(nextKmForFatShot);
        existing.setStatus(OilStatus.COMPLETED);
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(user);

        fatsReportService.save(existing);

        // =========================
        // UPDATE TRUCK
        // =========================
        truck.setNextFatsRange(nextKmForFatShot);
        truck.setStatus(existing.getStatus());

        truckService.save(truck);

        redirectAttributes.addFlashAttribute("success",
                "បានកែប្រែការបាញ់ខ្លាញ់របស់ឡាន " +
                        truck.getLicensePlate() + " ដោយជោគជ័យ!");

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
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate toDate) {
        List<Truck> trucks = truckService.getAll();

        int size;
        if ("all".equalsIgnoreCase(sizeParam) || showAll) {
            size = Integer.MAX_VALUE;
            page = 0; // Reset to first page when showing all
        } else {
            size = Integer.parseInt(sizeParam);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        Page<TruckFatsReport> truckPage = fatsReportService.getAllWithPageable(pageable, truckId, fromDate, toDate);

        // put everything into model
        model.addAttribute("reports", truckPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", truckPage.getTotalPages());
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
            @RequestParam(value = "size", defaultValue = "100") String sizeParam,
            @RequestParam(value = "all", defaultValue = "false") boolean showAll,
            @RequestParam(value = "licensePlate", required = false) String licensePlate,
            @RequestParam(value = "export", required = false) String export,
            HttpServletResponse response) throws IOException {
        List<Truck> allTrucks;
        if (licensePlate != null && !licensePlate.isEmpty()) {
            allTrucks = truckService.advancedFilter(licensePlate);
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
        model.addAttribute("size", size);
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
            @RequestParam("file") MultipartFile file,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Truck truck = truckService.findById(truckId)
                .orElseThrow(() -> new RuntimeException("Truck not found"));

        if (result.hasErrors()) {
            model.addAttribute("truck", truck);
            model.addAttribute("statuses", OilStatus.values());
            model.addAttribute("currentDate", LocalDate.now());
            return "oils_change/change";
        }

        if (!file.isEmpty()) {

            try {

                File uploadPath = new File(uploadDir);
                if (!uploadPath.exists()) {
                    uploadPath.mkdirs();
                }

                String originalName = file.getOriginalFilename();
                String extension = "";

                if (originalName != null && originalName.contains(".")) {
                    extension = originalName.substring(originalName.lastIndexOf("."));
                }

                String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
                Path filePath = Paths.get(uploadDir, fileName);

                file.transferTo(filePath);

                report.setFilePath("/uploads/" + fileName);

            } catch (IOException e) {
                throw new RuntimeException("File upload failed", e);
            }
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

        report.setLocationChanged(report.getLocationChanged());

        report.setDistanceKm(report.getDistanceKm());
        report.setCreatedBy(user);
        report.setUpdatedBy(user);
        oilsReportService.save(report);

        truck.setNextOilsRange(nextKmForOilsChange);

        truck.setStatus(report.getStatus());

        truckService.save(truck);

        redirectAttributes.addFlashAttribute("success", "Truck " + truck.getLicensePlate() + " បានបាញ់ខ្លាញ់ដោយជោគជ័យ");

        return "redirect:/admin/trucks/change/oils";
    }

    @GetMapping("/oils/change/edit/{reportId}")
    public String editChangeOil(
            @PathVariable("reportId") Long reportId,
            Model model) {
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
            @RequestParam("file") MultipartFile file,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        TruckOilsReport existing = oilsReportService.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Oil report not found"));

        Truck truck = existing.getTruck();

        if (result.hasErrors()) {
            model.addAttribute("truck", truck);
            model.addAttribute("statuses", OilStatus.values());
            return "oils_change/edit";
        }

        // Handle file update
        if (file != null && !file.isEmpty()) {

            try {

                File uploadPath = new File(uploadDir);
                if (!uploadPath.exists()) {
                    uploadPath.mkdirs();
                }

                // 🔥 1️⃣ Delete old file if exists
                if (existing.getFilePath() != null) {

                    String oldFileName = existing.getFilePath().replace("/uploads/", "");
                    Path oldFilePath = Paths.get(uploadDir, oldFileName);

                    Files.deleteIfExists(oldFilePath);
                }

                // 🔥 2️⃣ Generate random file name
                String originalName = file.getOriginalFilename();
                String extension = "";

                if (originalName != null && originalName.contains(".")) {
                    extension = originalName.substring(originalName.lastIndexOf("."));
                }

                String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
                Path newFilePath = Paths.get(uploadDir, fileName);

                file.transferTo(newFilePath);

                existing.setFilePath("/uploads/" + fileName);

            } catch (IOException e) {
                throw new RuntimeException("File upload failed", e);
            }
        }

        // Calculate next range
        Double nextKmForOilsChange = truck.getKmForOilsChange() + formReport.getDistanceKm();

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

        existing.setLocationChanged(formReport.getLocationChanged());

        oilsReportService.save(existing);

        // Update truck
        truck.setNextOilsRange(nextKmForOilsChange);
        truck.setStatus(existing.getStatus());

        truckService.save(truck);

        redirectAttributes.addFlashAttribute("success",
                "បានកែប្រែការប្ដូរប្រេងរបស់ឡាន " + truck.getLicensePlate() + " ដោយជោគជ័យ!");

        return "redirect:/admin/trucks/change/oils";
    }

    @GetMapping("/fats/shoot/delete/{reportId}")
    public String deleteFatsShoot(
            @PathVariable("reportId") Long reportId,
            RedirectAttributes redirectAttributes) {
        // 1️⃣ Find existing report
        TruckFatsReport existing = fatsReportService.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Fats report not found"));

        Truck truck = existing.getTruck();
        fatsReportService.deleteById(existing.getId());
        try {
            // 2️⃣ Delete uploaded file if exists
            if (existing.getFilePath() != null) {
                String fileName = existing.getFilePath().replace("/uploads/", "");
                Path filePath = Paths.get(uploadDir, fileName);
                Files.deleteIfExists(filePath);
            }

            // 3️⃣ Update truck next range
            // Subtract the distance of this deleted report from next range
            Double newNextRange = truck.getNextFatsRange() - existing.getDistanceKm();
            if (newNextRange < 0) {
                newNextRange = 0.0; // prevent negative
            }
            truck.setNextFatsRange(newNextRange);

            // Optional: recalculate truck status if needed
            truck.setStatus(OilStatus.PENDING); // or your logic

            truckService.save(truck);

            // 4️⃣ Delete report from DB

            redirectAttributes.addFlashAttribute("success",
                    "បានលុបការបាញ់ខ្លាញ់របស់ឡាន " + truck.getLicensePlate() + " ដោយជោគជ័យ!");

        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file", e);
        }

        return "redirect:/admin/trucks/fats/reports";
    }

    @GetMapping("/oils/change/delete/{reportId}")
    public String deleteOilsReport(
            @PathVariable("reportId") Long reportId,
            RedirectAttributes redirectAttributes) {
        // 1️⃣ Find existing report
        TruckOilsReport existing = oilsReportService.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Oil report not found"));

        Truck truck = existing.getTruck();
        // 4️⃣ Delete report from DB
        oilsReportService.delete(existing);
        try {
            // 2️⃣ Delete uploaded file if exists
            if (existing.getFilePath() != null) {
                String fileName = existing.getFilePath().replace("/uploads/", "");
                Path filePath = Paths.get(uploadDir, fileName);
                Files.deleteIfExists(filePath);
            }

            // 3️⃣ Update truck next range
            // Subtract the distance of this deleted report from next oils range
            Double newNextOilsRange = truck.getNextOilsRange() - existing.getDistanceKm();
            if (newNextOilsRange < 0) {
                newNextOilsRange = 0.0; // prevent negative
            }
            truck.setNextOilsRange(newNextOilsRange);

            // Optional: update truck status
            truck.setStatus(OilStatus.PENDING); // or your logic

            truckService.save(truck);

            redirectAttributes.addFlashAttribute("success",
                    "បានលុបការប្ដូរប្រេងរបស់ឡាន " + truck.getLicensePlate() + " ដោយជោគជ័យ!");

        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file", e);
        }

        return "redirect:/admin/trucks/oils/reports";
    }

    @GetMapping("/oils/reports")
    public String oilsReports(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") String sizeParam,
            @RequestParam(value = "all", defaultValue = "false") boolean showAll,
            @RequestParam(value = "truck_id", required = false) Long truckId,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate toDate) {
        List<Truck> trucks = truckService.getAll();

        int size;
        if ("all".equalsIgnoreCase(sizeParam) || showAll) {
            size = Integer.MAX_VALUE;
            page = 0; // Reset to first page when showing all
        } else {
            size = Integer.parseInt(sizeParam);
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        Page<TruckOilsReport> truckPage = oilsReportService.getAllWithPageable(pageable, truckId, fromDate, toDate);

        // put everything into model
        model.addAttribute("reports", truckPage);
        model.addAttribute("currentPage", truckPage.getNumber());
        model.addAttribute("totalPages", truckPage.getTotalPages());
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

            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") String sizeParam,
            @RequestParam(value = "all", defaultValue = "false") boolean showAll,
            HttpServletResponse response) throws IOException {

        int size;
        if ("all".equalsIgnoreCase(sizeParam) || showAll) {
            size = Integer.MAX_VALUE;
            page = 0;
        } else {
            size = Integer.parseInt(sizeParam);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        Page<TruckFatsReport> truckPage = fatsReportService.getAllWithPageable(pageable, truckId, fromDate, toDate);
        List<TruckFatsReport> reports = truckPage.getContent();

        if (excel) {
            // Export to Excel
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=fats-reports.xlsx");

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Fats Reports");

                // Enable auto-sizing for columns
                sheet.autoSizeColumn(0);
                for (int i = 0; i < 13; i++) {
                    sheet.setColumnWidth(i, 5000); // Set default width
                }

                // Create a style for the blue header
                CellStyle headerStyle = workbook.createCellStyle();
                headerStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                headerStyle.setBorderBottom(BorderStyle.THIN);
                headerStyle.setBorderTop(BorderStyle.THIN);
                headerStyle.setBorderLeft(BorderStyle.THIN);
                headerStyle.setBorderRight(BorderStyle.THIN);

                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerFont.setColor(IndexedColors.WHITE.getIndex());
                headerStyle.setFont(headerFont);
                headerStyle.setAlignment(HorizontalAlignment.CENTER);

                // Create a style for normal cells
                CellStyle cellStyle = workbook.createCellStyle();
                cellStyle.setBorderBottom(BorderStyle.THIN);
                cellStyle.setBorderTop(BorderStyle.THIN);
                cellStyle.setBorderLeft(BorderStyle.THIN);
                cellStyle.setBorderRight(BorderStyle.THIN);
                cellStyle.setWrapText(true); // Enable text wrapping

                int rowIdx = 0;
                // Header row
                Row headerRow = sheet.createRow(rowIdx++);
                String[] headers = { "#", "លេខឡាន", "ប្រភេទ/ម៉ាក", "ផ្នែកការរ៉ាស់", "ឆ្នាំផលិត", "Current Km", "Date",
                        "Shot Km", "Next Range", "Liter Quantity", "Location Changed", "File Attached", "Note",
                        "Created At", "Created By", "Latest Update" };

                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                int index = 1;
                // Create hyperlink style
                CellStyle hyperlinkStyle = workbook.createCellStyle();
                Font hyperlinkFont = workbook.createFont();
                hyperlinkFont.setUnderline(Font.U_SINGLE);
                hyperlinkFont.setColor(IndexedColors.BLUE.getIndex());
                hyperlinkStyle.setFont(hyperlinkFont);
                hyperlinkStyle.setBorderBottom(BorderStyle.THIN);
                hyperlinkStyle.setBorderTop(BorderStyle.THIN);
                hyperlinkStyle.setBorderLeft(BorderStyle.THIN);
                hyperlinkStyle.setBorderRight(BorderStyle.THIN);

                for (TruckFatsReport report : reports) {
                    Row row = sheet.createRow(rowIdx++);

                    // Apply cell style to all cells in the row
                    for (int i = 0; i < headers.length; i++) {
                        row.createCell(i).setCellStyle(cellStyle);
                    }

                    row.getCell(0).setCellValue(index++);
                    row.getCell(1).setCellValue(report.getTruck() != null ? report.getTruck().getLicensePlate() : "");

                    row.getCell(2).setCellValue(
                            report.getTruck().getModelName() != null ? report.getTruck().getModelName() : "");
                    row.getCell(3).setCellValue(
                            report.getTruck().getGroupName() != null ? report.getTruck().getGroupName() : "");
                    row.getCell(4)
                            .setCellValue(report.getTruck().getYearOfManufacture() != null
                                    ? report.getTruck().getYearOfManufacture()
                                    : "");

                    row.getCell(5).setCellValue(report.getTruck() != null ? report.getTruck().getCurrentKm() : 0);
                    row.getCell(6).setCellValue(report.getDate() != null ? report.getDate().toString() : "");
                    row.getCell(7).setCellValue(report.getCurrentKm());
                    row.getCell(8).setCellValue(report.getNextRange());
                    row.getCell(9).setCellValue(report.getLiterQuantityOfFats());
                    row.getCell(10).setCellValue(report.getLocationChanged());

                    Cell fileCell = row.getCell(11);
                    if (fileCell == null)
                        fileCell = row.createCell(11);

                    if (report.getFilePath() != null && !report.getFilePath().isEmpty()) {
                        fileCell.setCellValue("Yes");
                    } else {
                        fileCell.setCellValue("No");
                    }

                    row.getCell(12)
                            .setCellValue(report.getNote() != null ? report.getNote().replaceAll("\\<.*?\\>", "") : "");
                    row.getCell(13)
                            .setCellValue(report.getCreatedAt() != null
                                    ? report.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"))
                                    : "");
                    row.getCell(14).setCellValue(report.getCreatedBy() != null ? report.getCreatedBy().fullName() : "");
                    row.getCell(15).setCellValue(report.getUpdatedAt() != null
                            ? report.getUpdatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"))
                            : (report.getCreatedAt() != null
                                    ? report.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"))
                                    : ""));
                }

                // Auto-size columns after data is added
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                    // Ensure minimum width
                    if (sheet.getColumnWidth(i) < 3000) {
                        sheet.setColumnWidth(i, 3000);
                    }
                }

                workbook.write(response.getOutputStream());
            }

        } else if (pdf) {
            // Export to PDF
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=fats-reports.pdf");

            Document document = new Document(PageSize.A4.rotate()); // Landscape orientation
            try {
                PdfWriter.getInstance(document, response.getOutputStream());
                document.open();

                // Add title with styling
                com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16,
                        BaseColor.DARK_GRAY);
                Paragraph title = new Paragraph("Fats Reports", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                title.setSpacingAfter(20);
                document.add(title);

                // Add subtitle with date range if provided
                if (fromDate != null || toDate != null) {
                    com.itextpdf.text.Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10,
                            BaseColor.GRAY);
                    String dateRange = "Date Range: " +
                            (fromDate != null ? fromDate.toString() : "Start") + " - " +
                            (toDate != null ? toDate.toString() : "End");
                    Paragraph subtitle = new Paragraph(dateRange, subtitleFont);
                    subtitle.setAlignment(Element.ALIGN_CENTER);
                    subtitle.setSpacingAfter(15);
                    document.add(subtitle);
                }

                PdfPTable table = new PdfPTable(13); // Updated to 13 columns
                table.setWidthPercentage(100);
                table.setSpacingBefore(10f);
                table.setWidths(new float[] { 1, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 2, 3 });

                // Create blue header style
                com.itextpdf.text.Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10,
                        BaseColor.WHITE);
                BaseColor headerColor = new BaseColor(41, 128, 185); // Nice blue color

                // headers
                String[] pdfHeaders = { "#", "License Plate", "Current Km", "Date", "Shot Km", "Next Range",
                        "Liter Qty", "Location Changed", "File", "Note", "Created At", "Created By", "Latest Update" };

                for (String headerTitle : pdfHeaders) {
                    PdfPCell headerCell = new PdfPCell(new Phrase(headerTitle, headerFont));
                    headerCell.setBackgroundColor(headerColor);
                    headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    headerCell.setPadding(5);
                    headerCell.setBorderWidth(1);
                    table.addCell(headerCell);
                }

                // Create normal cell style
                com.itextpdf.text.Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);

                int index = 1;
                for (TruckFatsReport report : reports) {
                    // #
                    addTableCell(table, String.valueOf(index++), cellFont, Element.ALIGN_CENTER);

                    // License Plate
                    addTableCell(table, report.getTruck() != null ? report.getTruck().getLicensePlate() : "", cellFont,
                            Element.ALIGN_LEFT);

                    // Current Km
                    addTableCell(table,
                            report.getTruck() != null ? String.valueOf(report.getTruck().getCurrentKm()) : "0",
                            cellFont, Element.ALIGN_RIGHT);

                    // Date
                    addTableCell(table, report.getDate() != null ? report.getDate().toString() : "", cellFont,
                            Element.ALIGN_CENTER);

                    // Shot Km
                    addTableCell(table, String.valueOf(report.getCurrentKm()), cellFont, Element.ALIGN_RIGHT);

                    // Next Range
                    addTableCell(table, String.valueOf(report.getNextRange()), cellFont, Element.ALIGN_RIGHT);

                    // Liter Quantity
                    addTableCell(table, String.valueOf(report.getLiterQuantityOfFats()), cellFont, Element.ALIGN_RIGHT);

                    // Location Changed
                    addTableCell(table, report.getLocationChanged(), cellFont, Element.ALIGN_CENTER);

                    PdfPCell fileCell;

                    if (report.getFilePath() != null && !report.getFilePath().isEmpty()) {
                        fileCell = new PdfPCell(new Phrase("Yes", cellFont));
                    } else {
                        fileCell = new PdfPCell(new Phrase("No", cellFont));
                    }

                    fileCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    fileCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    fileCell.setPadding(5);
                    fileCell.setBorderWidth(1);

                    table.addCell(fileCell);

                    // Note
                    String cleanNote = report.getNote() != null ? report.getNote().replaceAll("\\<.*?\\>", "") : "";
                    addTableCell(table, cleanNote, cellFont, Element.ALIGN_LEFT);

                    // Created At
                    String createdAt = report.getCreatedAt() != null
                            ? report.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
                            : "";
                    addTableCell(table, createdAt, cellFont, Element.ALIGN_CENTER);

                    // Created By
                    addTableCell(table, report.getCreatedBy() != null ? report.getCreatedBy().fullName() : "", cellFont,
                            Element.ALIGN_LEFT);

                    // Latest Update
                    String updatedAt = report.getUpdatedAt() != null
                            ? report.getUpdatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
                            : (report.getCreatedAt() != null
                                    ? report.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
                                    : "");
                    addTableCell(table, updatedAt, cellFont, Element.ALIGN_CENTER);
                }

                document.add(table);

                // Add footer with page numbers
                document.add(new Paragraph(" "));
                com.itextpdf.text.Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY);
                Paragraph footer = new Paragraph(
                        "Exported on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                        footerFont);
                footer.setAlignment(Element.ALIGN_CENTER);
                document.add(footer);

            } catch (Exception e) {
                e.printStackTrace();
                throw new IOException("Error generating PDF", e);
            } finally {
                document.close();
            }
        }
    }

    // Helper method for PDF table cells
    private void addTableCell(PdfPTable table, String text, com.itextpdf.text.Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        cell.setBorderWidth(1);
        table.addCell(cell);
    }

    // oils
    @GetMapping("/oils/reports/export")
    public void exportOilChangeReports(
            @RequestParam(value = "truck_id", required = false) Long truckId,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate toDate,
            @RequestParam(value = "excel", required = false, defaultValue = "false") boolean excel,
            @RequestParam(value = "pdf", required = false, defaultValue = "false") boolean pdf,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") String sizeParam,
            @RequestParam(value = "all", defaultValue = "false") boolean showAll,
            HttpServletResponse response) throws IOException {

        int size;
        if ("all".equalsIgnoreCase(sizeParam) || showAll) {
            size = Integer.MAX_VALUE;
            page = 0;
        } else {
            size = Integer.parseInt(sizeParam);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        Page<TruckOilsReport> truckPage = oilsReportService.getAllWithPageable(pageable, truckId, fromDate, toDate);
        List<TruckOilsReport> reports = truckPage.getContent();

        if (excel) {
            // Export to Excel
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=oil-reports.xlsx");

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Oil Reports");

                // Set default column widths
                for (int i = 0; i < 13; i++) {
                    sheet.setColumnWidth(i, 5000);
                }

                // Create a style for the blue header
                CellStyle headerStyle = workbook.createCellStyle();
                headerStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                headerStyle.setBorderBottom(BorderStyle.THIN);
                headerStyle.setBorderTop(BorderStyle.THIN);
                headerStyle.setBorderLeft(BorderStyle.THIN);
                headerStyle.setBorderRight(BorderStyle.THIN);

                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerFont.setColor(IndexedColors.WHITE.getIndex());
                headerStyle.setFont(headerFont);
                headerStyle.setAlignment(HorizontalAlignment.CENTER);
                headerStyle.setVerticalAlignment(VerticalAlignment.CENTER.CENTER);

                // Create a style for normal cells
                CellStyle cellStyle = workbook.createCellStyle();
                cellStyle.setBorderBottom(BorderStyle.THIN);
                cellStyle.setBorderTop(BorderStyle.THIN);
                cellStyle.setBorderLeft(BorderStyle.THIN);
                cellStyle.setBorderRight(BorderStyle.THIN);
                cellStyle.setWrapText(true);

                int rowIdx = 0;
                // Header row
                Row headerRow = sheet.createRow(rowIdx++);
                headerRow.setHeight((short) 500); // Set header row height
                String[] headers = { "#", "លេខឡាន", "ប្រភេទ/ម៉ាក", "ផ្នែកការរ៉ាស់", "ឆ្នាំផលិត", "Current Km", "Date",
                        "Shot Km", "Next Range",
                        "Liter Quantity", "Location Changed", "File Attached", "Note",
                        "Created At", "Created By", "Latest Update" };

                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                int index = 1;
                // Create hyperlink style for file attachments
                CellStyle hyperlinkStyle = workbook.createCellStyle();
                Font hyperlinkFont = workbook.createFont();
                hyperlinkFont.setUnderline(Font.U_SINGLE);
                hyperlinkFont.setColor(IndexedColors.BLUE.getIndex());
                hyperlinkStyle.setFont(hyperlinkFont);
                hyperlinkStyle.setBorderBottom(BorderStyle.THIN);
                hyperlinkStyle.setBorderTop(BorderStyle.THIN);
                hyperlinkStyle.setBorderLeft(BorderStyle.THIN);
                hyperlinkStyle.setBorderRight(BorderStyle.THIN);

                for (TruckOilsReport report : reports) {
                    Row row = sheet.createRow(rowIdx++);

                    // Apply cell style to all cells in the row
                    for (int i = 0; i < headers.length; i++) {
                        row.createCell(i).setCellStyle(cellStyle);
                    }

                    row.getCell(0).setCellValue(index++);
                    row.getCell(1).setCellValue(report.getTruck() != null ? report.getTruck().getLicensePlate() : "");
                    row.getCell(2).setCellValue(
                            report.getTruck().getModelName() != null ? report.getTruck().getModelName() : "");
                    row.getCell(3).setCellValue(
                            report.getTruck().getGroupName() != null ? report.getTruck().getGroupName() : "");
                    row.getCell(4)
                            .setCellValue(report.getTruck().getYearOfManufacture() != null
                                    ? report.getTruck().getYearOfManufacture()
                                    : "");
                    row.getCell(5).setCellValue(report.getTruck() != null ? report.getTruck().getCurrentKm() : 0);
                    row.getCell(6).setCellValue(report.getDate() != null ? report.getDate().toString() : "");
                    row.getCell(7).setCellValue(report.getCurrentKm());
                    row.getCell(8).setCellValue(report.getNextRange());
                    row.getCell(9).setCellValue(report.getLiterQuantityOfOils());
                    row.getCell(10).setCellValue(report.getLocationChanged());

                    Cell fileCell = row.getCell(11);
                    if (fileCell == null)
                        fileCell = row.createCell(11);

                    if (report.getFilePath() != null && !report.getFilePath().isEmpty()) {
                        fileCell.setCellValue("Yes");
                    } else {
                        fileCell.setCellValue("No");
                    }

                    row.getCell(12)
                            .setCellValue(report.getNote() != null ? report.getNote().replaceAll("\\<.*?\\>", "") : "");
                    row.getCell(13)
                            .setCellValue(report.getCreatedAt() != null
                                    ? report.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"))
                                    : "");
                    row.getCell(14).setCellValue(report.getCreatedBy() != null ? report.getCreatedBy().fullName() : "");
                    row.getCell(15).setCellValue(report.getUpdatedAt() != null
                            ? report.getUpdatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"))
                            : (report.getCreatedAt() != null
                                    ? report.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"))
                                    : ""));
                }

                // Auto-size columns
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                    if (sheet.getColumnWidth(i) < 3000) {
                        sheet.setColumnWidth(i, 3000);
                    }
                }

                workbook.write(response.getOutputStream());
            }

        } else if (pdf) {
            // Export to PDF
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=oil-reports.pdf");

            com.itextpdf.text.Document document = new com.itextpdf.text.Document(
                    com.itextpdf.text.PageSize.A4.rotate()); // Landscape
            try {
                com.itextpdf.text.pdf.PdfWriter.getInstance(document, response.getOutputStream());
                document.open();

                // Add title with styling
                com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
                        com.itextpdf.text.Font.FontFamily.HELVETICA,
                        16,
                        com.itextpdf.text.Font.BOLD,
                        com.itextpdf.text.BaseColor.DARK_GRAY);
                com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("Oil Change Reports", titleFont);
                title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                title.setSpacingAfter(20);
                document.add(title);

                // Add subtitle with date range if provided
                if (fromDate != null || toDate != null) {
                    com.itextpdf.text.Font subtitleFont = new com.itextpdf.text.Font(
                            com.itextpdf.text.Font.FontFamily.HELVETICA,
                            10,
                            com.itextpdf.text.Font.NORMAL,
                            com.itextpdf.text.BaseColor.GRAY);
                    String dateRange = "Date Range: " +
                            (fromDate != null ? fromDate.toString() : "Start") + " - " +
                            (toDate != null ? toDate.toString() : "End");
                    com.itextpdf.text.Paragraph subtitle = new com.itextpdf.text.Paragraph(dateRange, subtitleFont);
                    subtitle.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                    subtitle.setSpacingAfter(15);
                    document.add(subtitle);
                }

                com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(13); // 13 columns
                table.setWidthPercentage(100);
                table.setSpacingBefore(10f);
                table.setWidths(new float[] { 1, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 2, 3 }); // Column widths

                // Create blue header style
                com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(
                        com.itextpdf.text.Font.FontFamily.HELVETICA,
                        10,
                        com.itextpdf.text.Font.BOLD,
                        com.itextpdf.text.BaseColor.WHITE);
                com.itextpdf.text.BaseColor headerColor = new com.itextpdf.text.BaseColor(41, 128, 185); // Nice blue

                // headers - all 13 columns
                String[] pdfHeaders = { "#", "License Plate", "Current Km", "Date", "Shot Km", "Next Range",
                        "Liter Quantity", "Location Changed", "File Attached", "Note",
                        "Created At", "Created By", "Latest Update" };

                for (String headerTitle : pdfHeaders) {
                    com.itextpdf.text.pdf.PdfPCell headerCell = new com.itextpdf.text.pdf.PdfPCell(
                            new com.itextpdf.text.Phrase(headerTitle, headerFont));
                    headerCell.setBackgroundColor(headerColor);
                    headerCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                    headerCell.setVerticalAlignment(com.itextpdf.text.Element.ALIGN_MIDDLE);
                    headerCell.setPadding(5);
                    headerCell.setBorderWidth(1);
                    table.addCell(headerCell);
                }

                // Create normal cell font
                com.itextpdf.text.Font cellFont = new com.itextpdf.text.Font(
                        com.itextpdf.text.Font.FontFamily.HELVETICA,
                        9,
                        com.itextpdf.text.Font.NORMAL,
                        com.itextpdf.text.BaseColor.BLACK);

                int index = 1;
                for (TruckOilsReport report : reports) {
                    // #
                    addTableCell(table, String.valueOf(index++), cellFont, com.itextpdf.text.Element.ALIGN_CENTER);

                    // License Plate
                    addTableCell(table,
                            report.getTruck() != null ? report.getTruck().getLicensePlate() : "",
                            cellFont,
                            com.itextpdf.text.Element.ALIGN_LEFT);

                    // Current Km
                    addTableCell(table,
                            report.getTruck() != null ? String.valueOf(report.getTruck().getCurrentKm()) : "0",
                            cellFont,
                            com.itextpdf.text.Element.ALIGN_RIGHT);

                    // Date
                    addTableCell(table,
                            report.getDate() != null ? report.getDate().toString() : "",
                            cellFont,
                            com.itextpdf.text.Element.ALIGN_CENTER);

                    // Shot Km
                    addTableCell(table,
                            String.valueOf(report.getCurrentKm()),
                            cellFont,
                            com.itextpdf.text.Element.ALIGN_RIGHT);

                    // Next Range
                    addTableCell(table,
                            String.valueOf(report.getNextRange()),
                            cellFont,
                            com.itextpdf.text.Element.ALIGN_RIGHT);

                    // Liter Quantity
                    addTableCell(table,
                            report.getLiterQuantityOfOils() != null ? String.valueOf(report.getLiterQuantityOfOils())
                                    : "0",
                            cellFont,
                            com.itextpdf.text.Element.ALIGN_RIGHT);

                    // Location Changed
                    addTableCell(table,
                            report.getLocationChanged(),
                            cellFont,
                            com.itextpdf.text.Element.ALIGN_CENTER);

                    PdfPCell fileCell;

                    if (report.getFilePath() != null && !report.getFilePath().isEmpty()) {
                        fileCell = new PdfPCell(new Phrase("Yes", cellFont));
                    } else {
                        fileCell = new PdfPCell(new Phrase("No", cellFont));
                    }

                    fileCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    fileCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    fileCell.setPadding(5);
                    fileCell.setBorderWidth(1);

                    table.addCell(fileCell);

                    // Note
                    String cleanNote = report.getNote() != null ? report.getNote().replaceAll("\\<.*?\\>", "") : "";
                    addTableCell(table, cleanNote, cellFont, com.itextpdf.text.Element.ALIGN_LEFT);

                    // Created At
                    String createdAt = report.getCreatedAt() != null
                            ? report.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
                            : "";
                    addTableCell(table, createdAt, cellFont, com.itextpdf.text.Element.ALIGN_CENTER);

                    // Created By
                    addTableCell(table,
                            report.getCreatedBy() != null ? report.getCreatedBy().fullName() : "",
                            cellFont,
                            com.itextpdf.text.Element.ALIGN_LEFT);

                    // Latest Update
                    String updatedAt = report.getUpdatedAt() != null
                            ? report.getUpdatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
                            : (report.getCreatedAt() != null
                                    ? report.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
                                    : "");
                    addTableCell(table, updatedAt, cellFont, com.itextpdf.text.Element.ALIGN_CENTER);
                }

                document.add(table);

                // Add footer with page numbers
                document.add(new com.itextpdf.text.Paragraph(" "));
                com.itextpdf.text.Font footerFont = new com.itextpdf.text.Font(
                        com.itextpdf.text.Font.FontFamily.HELVETICA,
                        8,
                        com.itextpdf.text.Font.NORMAL,
                        com.itextpdf.text.BaseColor.GRAY);
                com.itextpdf.text.Paragraph footer = new com.itextpdf.text.Paragraph(
                        "Exported on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                        footerFont);
                footer.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                document.add(footer);

            } catch (Exception e) {
                e.printStackTrace();
                throw new IOException("Error generating PDF", e);
            } finally {
                document.close();
            }
        }
    }

}
