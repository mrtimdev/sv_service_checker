package timdev.timdev.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


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
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
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
import lombok.AllArgsConstructor;
import timdev.timdev.dto.TruckDistanceDto;
import timdev.timdev.dto.TruckDistanceForm;
import timdev.timdev.entity.Truck;
import timdev.timdev.entity.TruckDistance;
import timdev.timdev.entity.User;
import timdev.timdev.repository.TruckRepository;
import timdev.timdev.service.TruckDistanceService;
import timdev.timdev.service.TruckService;
import timdev.timdev.service.UserService;

@AllArgsConstructor
@Controller
@RequestMapping("/truck-distances")
public class TruckDistanceWebController {

    private TruckDistanceService truckDistanceService;
    private TruckService truckService;
    private UserService userService;
    private TruckRepository truckRepo;

  


    // Display all truck distances
    @GetMapping
    public Object listTruckDistances(
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "50") String sizeParam,
        @RequestParam(value = "all", defaultValue = "false") boolean showAll, 
        @RequestParam(value = "truck_id", required = false) Long truckId,
        @RequestParam(value = "fromDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate fromDate,
        @RequestParam(value = "toDate", required = false) @DateTimeFormat(pattern = "MMM dd, yyyy") LocalDate toDate,
        @RequestParam(value = "sortBy", defaultValue = "distanceDate") String sortBy,
        @RequestParam(value = "order", defaultValue = "desc") String order,
        @RequestParam(value = "export", required = false) String export,
        HttpServletResponse response,
        Model model) throws IOException 
    {
        List<Truck> trucks = truckService.getAll();
        List<TruckDistance> distancesPage;
        int totalPages = 1;
        int size = "all".equalsIgnoreCase(sizeParam) ? Integer.MAX_VALUE : Integer.parseInt(sizeParam);

        // build Sort dynamically
        Sort.Direction direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;

        // map frontend sortBy values to entity fields
        String sortField;
        switch (sortBy) {
            case "truckId":
                sortField = "truck.id";
                break;
            case "truckLicensePlate":
                sortField = "truck.licensePlate"; // assuming field name
                break;
            case "distanceId":
                sortField = "id";
                break;
            case "distanceDate":
            default:
                sortField = "date"; // assuming TruckDistance.date field
                break;
        }

        Sort sort = Sort.by(direction, sortField);
        
        if (showAll) {
            // fetch all reports with filter
            distancesPage = truckDistanceService.getAllFiltered(truckId, fromDate, toDate, sort);
        } else {
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<TruckDistance> truckPage = truckDistanceService.getAllWithPageable(pageable, truckId, fromDate, toDate);
            distancesPage = truckPage.getContent();
            totalPages = truckPage.getTotalPages();
        }


        double totalDistance = distancesPage.stream()
                                    .mapToDouble(TruckDistance::getDistance)
                                    .sum();

        String totalDistanceFormatted = String.format("%,.2f km", totalDistance);


        if ("excel".equalsIgnoreCase(export)) {
            exportTruckDistancesToExcel(distancesPage, totalDistanceFormatted, response);
            return null; 
        }


        // put everything into model
        model.addAttribute("distances", distancesPage);
        model.addAttribute("totalDistance", totalDistanceFormatted);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", sizeParam);
        model.addAttribute("showAll", showAll);

        // preserve filters in the view
        model.addAttribute("selectedTruckId", truckId != null ? truckId : null);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("trucks", trucks);

        model.addAttribute("sortBy", sortBy);
        model.addAttribute("order", order);

        return "truck-distances/list";
    }


    private void exportTruckDistancesToExcel(List<TruckDistance> distances, String totalDistance, HttpServletResponse response) throws IOException {
    String fileName = "Truck Distances-" + LocalDate.now() + ".xlsx";
    String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
            .replace("+", "%20");
    
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
    response.setCharacterEncoding("UTF-8");
    
    try (Workbook workbook = new XSSFWorkbook()) {
        Sheet sheet = workbook.createSheet("Truck Distances");
        
        // Create styles
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle totalStyle = createTotalStyle(workbook);
            
        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Date", "Truck License Plate", "Distance (km)", "Recorded By"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Create data rows
        int rowNum = 1;
        for (TruckDistance distance : distances) {
            Row row = sheet.createRow(rowNum++);
            
            // Date
            Cell dateCell = row.createCell(0);
            dateCell.setCellValue(distance.getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
            dateCell.setCellStyle(dataStyle);
            
            // Truck License Plate
            Cell truckCell = row.createCell(1);
            truckCell.setCellValue(distance.getTruck().getLicensePlate());
            truckCell.setCellStyle(dataStyle);
            
            // Distance
            Cell distanceCell = row.createCell(2);
            distanceCell.setCellValue(distance.getDistance());
            distanceCell.setCellStyle(dataStyle);
            
            // Recorded By
            Cell recordedByCell = row.createCell(3);
            recordedByCell.setCellValue(distance.getCreatedBy() != null ? 
                distance.getCreatedBy().getUsername() : "System");
            recordedByCell.setCellStyle(dataStyle);
        }
        
        // Add summary section
        addSummarySection(sheet, rowNum, distances.size(), totalDistance, totalStyle, headers.length);
        
        // Apply auto-filter to the data range (header + all data rows)
        if (rowNum > 1) { // Only apply if there's data
            sheet.setAutoFilter(new CellRangeAddress(0, rowNum - 1, 0, headers.length - 1));
        }
        
        // Freeze the header row
        sheet.createFreezePane(0, 1);
        
        // Auto-size and optimize columns
        optimizeColumnSizes(sheet, headers.length);
        
        // Set the active cell to A2 for better user experience
        sheet.setActiveCell(new CellAddress("A2"));
        
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            workbook.write(outputStream);
            outputStream.flush();
        }
    }
}

private CellStyle createHeaderStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setBold(true);
    font.setColor(IndexedColors.WHITE.getIndex());
    style.setFont(font);
    style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    style.setBorderBottom(BorderStyle.MEDIUM);
    style.setBorderTop(BorderStyle.MEDIUM);
    style.setBorderLeft(BorderStyle.MEDIUM);
    style.setBorderRight(BorderStyle.MEDIUM);
    style.setAlignment(HorizontalAlignment.CENTER);
    return style;
}

private CellStyle createDataStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderTop(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);
    return style;
}

private CellStyle createTotalStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setBold(true);
    style.setFont(font);
    style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    style.setBorderBottom(BorderStyle.MEDIUM);
    style.setBorderTop(BorderStyle.MEDIUM);
    style.setBorderLeft(BorderStyle.MEDIUM);
    style.setBorderRight(BorderStyle.MEDIUM);
    return style;
}

private void addSummarySection(Sheet sheet, int startRow, int recordCount, String totalDistance, CellStyle style, int numColumns) {
    int currentRow = startRow + 1;
    
    // Record count
    Row countRow = sheet.createRow(currentRow++);
    Cell countLabelCell = countRow.createCell(1);
    countLabelCell.setCellValue("Total Records:");
    countLabelCell.setCellStyle(style);
    
    Cell countValueCell = countRow.createCell(2);
    countValueCell.setCellValue(recordCount);
    countValueCell.setCellStyle(style);
    
    // Total distance
    Row distanceRow = sheet.createRow(currentRow++);
    Cell distanceLabelCell = distanceRow.createCell(1);
    distanceLabelCell.setCellValue("Total Distance:");
    distanceLabelCell.setCellStyle(style);
    
    Cell distanceValueCell = distanceRow.createCell(2);
    distanceValueCell.setCellValue(totalDistance);
    distanceValueCell.setCellStyle(style);
    
    // Export date
    Row dateRow = sheet.createRow(currentRow++);
    Cell dateLabelCell = dateRow.createCell(1);
    dateLabelCell.setCellValue("Export Date:");
    dateLabelCell.setCellStyle(style);
    
    Cell dateValueCell = dateRow.createCell(2);
    dateValueCell.setCellValue(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
    dateValueCell.setCellStyle(style);
}

private void optimizeColumnSizes(Sheet sheet, int numColumns) {
    for (int i = 0; i < numColumns; i++) {
        sheet.autoSizeColumn(i);
        int currentWidth = sheet.getColumnWidth(i);
        // Set reasonable column widths with some padding
        int newWidth = Math.min(currentWidth + 1000, 8000); // Cap at reasonable width
        sheet.setColumnWidth(i, newWidth);
    }
}

    @GetMapping("/create/v2")
    public String showCreateFormV2(Model model) {
        List<Truck> trucks = truckService.getAll();
    
        // Initialize the form with proper data structure
        TruckDistanceForm form = new TruckDistanceForm();
        // Add one empty entry to start with
        form.getDistances().add(new TruckDistanceDto());
        
        model.addAttribute("trucks", trucks);
        model.addAttribute("truckDistanceForm", form);
        return "truck-distances/addv2";
    }

    // Show form for creating multiple truck distances
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        List<Truck> trucks = truckService.getAll();
    
        // Initialize the form with proper data structure
        TruckDistanceForm form = new TruckDistanceForm();
        // Add one empty entry to start with
        form.getDistances().add(new TruckDistanceDto());
        
        model.addAttribute("trucks", trucks);
        model.addAttribute("truckDistanceForm", form);
        return "truck-distances/add";
    }

    // Handle form submission for creating multiple truck distances
    @PostMapping("/create")
    public String createTruckDistances(@ModelAttribute TruckDistanceForm form, 
                                      BindingResult result, 
                                      Model model) {
        if (result.hasErrors()) {
            model.addAttribute("trucks", truckService.getAll());
            return "truck-distances/add";
        }
        
        // Get current user (you'll need to implement this based on your auth system)
        User currentUser = userService.getCurrentUser();
        
        // Create and save each truck distance
        for (TruckDistanceDto dto : form.getDistances()) {
            Truck truck = truckService.findById(dto.getTruckId()).orElse(null);
            TruckDistance distance = new TruckDistance();
            distance.setDate(dto.getDate());
            distance.setTruck(truck);
            distance.setDistance(dto.getDistance());
            distance.setCreatedBy(currentUser);
            
            truckDistanceService.save(distance);

          
            truckRepo.save(truck);
        }
        
        return "redirect:/truck-distances?success";
    }


    // Show edit form for a single truck distance
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        TruckDistance distance = truckDistanceService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid truck distance ID: " + id));
        
        List<Truck> trucks = truckService.getAll();
        
        // Convert to DTO for editing
        TruckDistanceDto dto = new TruckDistanceDto();
        dto.setId(distance.getId());
        dto.setDate(distance.getDate());
        dto.setTruckId(distance.getTruck().getId());
        dto.setDistance(distance.getDistance());
        
        model.addAttribute("trucks", trucks);
        model.addAttribute("distance", dto);
        model.addAttribute("id", id);
        
        return "truck-distances/edit";
    }

    // Handle edit form submission
    @PostMapping("/edit/{id}")
    public String updateTruckDistance(@PathVariable("id") Long id, 
                                    @ModelAttribute("distance") TruckDistanceDto dto,
                                    BindingResult result, 
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            dto.setId(id);
            model.addAttribute("trucks", truckService.getAll());
            return "truck-distances/edit";
        }
        
        try {
            TruckDistance distance = truckDistanceService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid truck distance ID: " + id));
            
            // Get current user for updatedBy field
            User currentUser = userService.getCurrentUser();
            Truck truck = truckService.findById(dto.getTruckId()).orElse(null);
            // Update fields
            distance.setDate(dto.getDate());
            distance.setTruck(truck);
            distance.setDistance(dto.getDistance());
            distance.setUpdatedBy(currentUser);
            
            truckDistanceService.save(distance);

            

            truckRepo.save(truck);
            
            redirectAttributes.addFlashAttribute("success", "Distance record updated successfully");
            return "redirect:/truck-distances";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating distance record: " + e.getMessage());
            return "redirect:/truck-distances/edit/" + id;
        }
    }



    // Show multi-edit form
    @GetMapping("/edit-multi")
    public String showMultiEditForm(@RequestParam("ids") List<Long> ids, Model model) {
        List<TruckDistance> distances = truckDistanceService.findAllById(ids);
        List<Truck> trucks = truckService.getAll();
        
        // Convert to DTOs for editing
        TruckDistanceForm form = new TruckDistanceForm();
        for (TruckDistance distance : distances) {
             if (distance != null) {
                TruckDistanceDto dto = new TruckDistanceDto();
                dto.setId(distance.getId());
                dto.setDate(distance.getDate());
                dto.setTruckId(distance.getTruck().getId());
                dto.setDistance(distance.getDistance());
                form.getDistances().add(dto);
            }
        }
        
        model.addAttribute("trucks", trucks);
        model.addAttribute("truckDistanceForm", form);
        model.addAttribute("isEditMode", true);
        return "truck-distances/edit-multi";
    }

    // Handle multi-edit form submission
    @PostMapping("/edit-multi")
    public String updateTruckDistances(@ModelAttribute TruckDistanceForm form, 
                                    BindingResult result, 
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("trucks", truckService.getAll());
            model.addAttribute("isEditMode", true);
            return "truck-distances/add";
        }
        
        try {
            int updatedCount = 0;
            for (TruckDistanceDto dto : form.getDistances()) {
                if (dto.getId() != null) {
                    TruckDistance distance = truckDistanceService.findById(dto.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid truck distance ID: " + dto.getId()));
                    Truck truck = truckService.findById(dto.getTruckId()).orElse(null);
                    // Update fields
                    distance.setDate(dto.getDate());
                    distance.setTruck(truck);
                    distance.setDistance(dto.getDistance());
                    
                    truckDistanceService.save(distance);


                  

                    truckRepo.save(truck);
                    updatedCount++;
                }
            }
            
            redirectAttributes.addFlashAttribute("success", "Successfully updated " + updatedCount + " distance entries");
            return "redirect:/truck-distances";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating distance records: " + e.getMessage());
            return "redirect:/truck-distances";
        }
    }

        // Handle delete request
        @GetMapping("/delete/{id}")
        public String deleteTruckDistance(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
            try {
                TruckDistance distance = truckDistanceService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid truck distance ID: " + id));
                         
                
                truckDistanceService.delete(id);
                Truck truck = distance.getTruck(); 
                if (truck != null) {
                    
                }
                redirectAttributes.addFlashAttribute("success", "Distance record deleted successfully");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Error deleting distance record: " + e.getMessage());
            }
            
            return "redirect:/truck-distances";
        }



        // upload excel file
        @GetMapping("/import")
        public String showImportForm() {
            return "truck-distances/import"; 
        }

        @PostMapping("/import")
        public String importExcel(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
            try {
                List<TruckDistance> distances = truckDistanceService.importFromExcel(file, truckService);
                
                truckDistanceService.saveAll(distances);
                for (TruckDistance td : distances) {
                    Truck truck = td.getTruck();          
                    if (truck != null) {
                        
                    }
                }
                redirectAttributes.addFlashAttribute("success", "File imported successfully!");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Failed to import file: " + e.getMessage());
            }
            return "redirect:/truck-distances";
        }

}