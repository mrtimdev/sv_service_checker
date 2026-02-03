package timdev.timdev.service;

import java.io.IOException;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletResponse;
import timdev.timdev.entity.Average;
import timdev.timdev.entity.Measurement;
import timdev.timdev.entity.Truck;
import timdev.timdev.repository.AverageRepository;
import timdev.timdev.repository.MeasurementRepository;
import timdev.timdev.service.TruckService;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@AllArgsConstructor
@Service
@Transactional
public class AverageService {

    private TruckService truckService;
    private AverageRepository averageRepo;
    private MeasurementRepository measurementRepo;

    // Create new Average
    public void addAverage(Long truckId, Long measurementId, double value) {
        Truck truck = truckService.findById(truckId).orElse(null);
        Measurement measurement = measurementRepo.findById(measurementId)
                .orElseThrow(() -> new RuntimeException("Measurement not found"));

        // Check uniqueness
        boolean exists = averageRepo.existsByTruckIdAndMeasurementId(truckId, measurementId);
        if (exists) {
            throw new RuntimeException("This measurement already exists for this truck");
        }

        Average average = new Average();
        average.setTruck(truck);
        average.setMeasurement(measurement);
        average.setValue(value);

        averageRepo.save(average);
    }

    // Update existing Average
    public void updateAverage(Long id, Long truckId, Long measurementId, double value) {
        Average average = averageRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Average not found"));

        Truck truck = truckService.findById(truckId).orElse(null);
        Measurement measurement = measurementRepo.findById(measurementId)
                .orElseThrow(() -> new RuntimeException("Measurement not found"));

        // Check uniqueness (ignore current average)
        boolean exists = averageRepo.existsByTruckIdAndMeasurementIdAndIdNot(truckId, measurementId, id);
        if (exists) {
            throw new RuntimeException("This measurement already exists for this truck");
        }

        average.setTruck(truck);
        average.setMeasurement(measurement);
        average.setValue(value);

        averageRepo.save(average);
    }

    // Delete Average
    public void deleteById(Long id) {
        if (!averageRepo.existsById(id)) {
            throw new RuntimeException("Average not found");
        }
        averageRepo.deleteById(id);
    }

    // List all
    public List<Average> getAll() {
        return averageRepo.findAll();
    }

    // Get one by id
    public Average getById(Long id) {
        return averageRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Average not found"));
    }

    
    public List<Average> findByTruck(Long truckId) {
        return averageRepo.findByTruckId(truckId);
    }

   
    public Page<Average> findByTruck(Long truckId, Pageable pageable) {
        return averageRepo.findByTruckId(truckId, pageable);
    }

    
    public Average findByTruckAndMeasurement(Long truckId, Long measurementId) {
        return averageRepo.findByTruckIdAndMeasurementId(truckId, measurementId)
                .orElse(null);
    }

   
    public List<Measurement> getMeasurementsByTruck(Long truckId) {
        return averageRepo.findMeasurementsByTruckId(truckId);
    }

    public Double getAverageValue(Long truckId, Long measurementId) {
        return averageRepo.findAverageByTruckAndMeasurement(truckId, measurementId)
                .map(Average::getValue)
                .orElse(null);
    }

    public Page<Average> findByTruckWithFilter(Long truckId, Pageable pageable) {
        if (truckId != null) {
            return averageRepo.findByTruckId(truckId, pageable);
        } else {
            return averageRepo.findAll(pageable);
        }
    }


    @Transactional
    public void saveMultiple(Long truckId, Map<String, String> params) {
        Truck truck = truckService.findById(truckId).orElseThrow(
            () -> new RuntimeException("Truck not found with id: " + truckId)
        );

        for (String key : params.keySet()) {
            // Only process measurement values
            if (!key.startsWith("value_")) continue;

            Long measurementId = Long.valueOf(key.replace("value_", ""));
            String rawValue = params.get(key);

            // Skip empty values
            if (rawValue == null || rawValue.isBlank()) continue;

            double value = Double.parseDouble(rawValue);

            // Find existing record for update
            Optional<Average> existingOpt = averageRepo.findByTruckIdAndMeasurementId(truckId, measurementId);

            if (existingOpt.isPresent()) {
                // UPDATE existing record
                Average existing = existingOpt.get();
                existing.setValue(value);
                averageRepo.save(existing);
            } else {
                // CREATE new record (check for duplicates)
                boolean duplicate = averageRepo.existsByTruckIdAndMeasurementId(truckId, measurementId);
                
                if (duplicate) {
                    // This should not happen since we already checked with find, but keeping for safety
                    throw new RuntimeException("Measurement already exists for this truck");
                }

                // Verify measurement exists
                Measurement measurement = measurementRepo.findById(measurementId)
                    .orElseThrow(() -> new RuntimeException("Measurement not found with id: " + measurementId));

                Average avg = new Average();
                avg.setTruck(truck);
                avg.setMeasurement(measurement);
                avg.setValue(value);

                averageRepo.save(avg);
            }
        }
    }


    public void exportToCsv(List<Average> averages, HttpServletResponse response) {
        try {
            response.setContentType("text/csv");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"averages_export_" + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv\"");
            
            PrintWriter writer = response.getWriter();
            
            // Write CSV header
            writer.write("No,Truck License Plate,Truck Group,Average Value,Measurement,Measurement Native Name,Date\n");
            
            // Write data
            for (int i = 0; i < averages.size(); i++) {
                Average avg = averages.get(i);
                writer.write(String.format("%d,%s,%s,%.2f,%s,%s,%s\n",
                    i + 1,
                    avg.getTruck() != null ? avg.getTruck().getLicensePlate() : "",
                    avg.getTruck() != null ? avg.getTruck().getGroup() : "",
                    avg.getValue(),
                    avg.getMeasurement() != null ? avg.getMeasurement().getName() : "",
                    avg.getMeasurement() != null ? avg.getMeasurement().getNativeName() : "",
                    avg.getCreatedAt() != null ? avg.getCreatedAt().toString() : ""
                ));
            }
            
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException("Error exporting to CSV", e);
        }
    }
    
   
    public void exportToExcel(List<Average> averages, Truck truck, HttpServletResponse response, String fileName) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            
            Workbook workbook = createExcelWorkbook(averages, truck);
            workbook.write(response.getOutputStream());
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException("Error exporting to Excel", e);
        }
    }
    
    private Workbook createExcelWorkbook(List<Average> averages, Truck truck) {
        Workbook workbook = new XSSFWorkbook();
        
        // Create main data sheet
        Sheet sheet = workbook.createSheet("Averages Data");
        
        // Create header style
        CellStyle headerStyle = createHeaderStyle(workbook);
        
        // Create data styles
        CellStyle numberStyle = createNumberStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);
        
        // Create summary row if truck is selected
        int rowIdx = 0;
        if (truck != null) {
            rowIdx = createSummarySection(sheet, truck, averages, headerStyle, rowIdx);
            rowIdx++; // Empty row
        }
        
        // Create main header
        Row headerRow = sheet.createRow(rowIdx++);
        createMainHeader(headerRow, headerStyle);
        
        // Fill data
        for (int i = 0; i < averages.size(); i++) {
            Average avg = averages.get(i);
            Row dataRow = sheet.createRow(rowIdx++);
            fillDataRow(dataRow, avg, i + 1, numberStyle, dateStyle);
        }
        
        // Auto-size columns
        autoSizeColumns(sheet, 8);
        
        // Create chart sheet if enough data
        if (averages.size() > 1) {
            createChartSheet(workbook, averages, truck);
        }
        
        return workbook;
    }
    
    private int createSummarySection(Sheet sheet, Truck truck, List<Average> averages, CellStyle headerStyle, int startRow) {
        Row summaryHeader = sheet.createRow(startRow++);
        Cell summaryCell = summaryHeader.createCell(0);
        summaryCell.setCellValue("SUMMARY - " + truck.getLicensePlate() + " (" + truck.getGroup() + ")");
        summaryCell.setCellStyle(headerStyle);
        
        Row statsRow1 = sheet.createRow(startRow++);
        statsRow1.createCell(0).setCellValue("Total Records:");
        statsRow1.createCell(1).setCellValue(averages.size());
        
        if (!averages.isEmpty()) {
            Row statsRow2 = sheet.createRow(startRow++);
            statsRow2.createCell(0).setCellValue("Date Range:");
            statsRow2.createCell(1).setCellValue(getDateRange(averages));
            
            Row statsRow3 = sheet.createRow(startRow++);
            statsRow3.createCell(0).setCellValue("Average Value:");
            double total = averages.stream().mapToDouble(Average::getValue).sum();
            statsRow3.createCell(1).setCellValue(total / averages.size());
            
            // Format the average value cell
            CellStyle avgStyle = sheet.getWorkbook().createCellStyle();
            avgStyle.setDataFormat(sheet.getWorkbook().createDataFormat().getFormat("0.00"));
            statsRow3.getCell(1).setCellStyle(avgStyle);
        }
        
        return startRow;
    }
    
    private void createMainHeader(Row headerRow, CellStyle headerStyle) {
        String[] headers = {
            "No", "Truck License Plate", "Truck Group", 
            "Average Value", "Measurement", "Measurement Native Name", 
            "Created Date", "Notes"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }
    
    private void fillDataRow(Row row, Average avg, int index, CellStyle numberStyle, CellStyle dateStyle) {
        row.createCell(0).setCellValue(index);
        row.createCell(1).setCellValue(avg.getTruck() != null ? avg.getTruck().getLicensePlate() : "");
        row.createCell(2).setCellValue(avg.getTruck() != null ? avg.getTruck().getGroup() : "");
        
        Cell valueCell = row.createCell(3);
        valueCell.setCellValue(avg.getValue());
        valueCell.setCellStyle(numberStyle);
        
        row.createCell(4).setCellValue(avg.getMeasurement() != null ? avg.getMeasurement().getName() : "");
        row.createCell(5).setCellValue(avg.getMeasurement() != null ? avg.getMeasurement().getNativeName() : "");
        
        if (avg.getCreatedAt() != null) {
            Cell dateCell = row.createCell(6);
            dateCell.setCellValue(avg.getCreatedAt());
            dateCell.setCellStyle(dateStyle);
        }
        
        row.createCell(7).setCellValue(""); // Placeholder for notes
    }
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
    
    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }
    
    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd hh:mm"));
        return style;
    }
    
    private void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createChartSheet(Workbook workbook, List<Average> averages, Truck truck) {
        Sheet chartSheet = workbook.createSheet("Chart Data");
        
        // Prepare data for chart
        Row headerRow = chartSheet.createRow(0);
        headerRow.createCell(0).setCellValue("Date");
        headerRow.createCell(1).setCellValue("Average Value");
        
        for (int i = 0; i < averages.size(); i++) {
            Average avg = averages.get(i);
            Row row = chartSheet.createRow(i + 1);
            if (avg.getCreatedAt() != null) {
                row.createCell(0).setCellValue(avg.getCreatedAt());
            }
            row.createCell(1).setCellValue(avg.getValue());
        }
        
        // Auto-size chart sheet columns
        chartSheet.autoSizeColumn(0);
        chartSheet.autoSizeColumn(1);
    }
    
    private String getDateRange(List<Average> averages) {
        if (averages.isEmpty()) return "N/A";
        
        LocalDateTime minDate = averages.stream()
            .map(Average::getCreatedAt)
            .filter(Objects::nonNull)
            .min(LocalDateTime::compareTo)
            .orElse(null);
        
        LocalDateTime maxDate = averages.stream()
            .map(Average::getCreatedAt)
            .filter(Objects::nonNull)
            .max(LocalDateTime::compareTo)
            .orElse(null);
        
        if (minDate == null || maxDate == null) return "N/A";
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return minDate.format(formatter) + " to " + maxDate.format(formatter);
    }




    public Map<Long, Map<String, String>> getPivotData(Long truckId) {
        List<Average> averages;
        if (truckId != null) {
            averages = averageRepo.findByTruckIdOrderByCreatedAtDesc(truckId);
        } else {
            averages = averageRepo.findAllByOrderByCreatedAtDesc();
        }
        
        // Get all measurements
        List<Measurement> measurements = measurementRepo.getAllMeasurements();
        
        // Create pivot structure: Truck -> Measurement -> Value
        Map<Long, Map<String, String>> pivotData = new LinkedHashMap<>();
        
        for (Average average : averages) {
            if (average.getTruck() == null || average.getMeasurement() == null) {
                continue;
            }
            
            Long truckKey = average.getTruck().getId();
            String measurementKey = average.getMeasurement().getName();
            String value = String.format("%.2f — %s", 
                average.getValue(), 
                average.getMeasurement().getNativeName());
            
            pivotData
                .computeIfAbsent(truckKey, k -> new LinkedHashMap<>())
                .put(measurementKey, value);
        }
        
        return pivotData;
    }
    

    public void exportToExcelPivot(Map<Long, Map<String, String>> pivotData, Truck truck, 
                                  HttpServletResponse response, String fileName) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            
            Workbook workbook = createPivotExcelWorkbook(pivotData, truck);
            workbook.write(response.getOutputStream());
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException("Error exporting pivot to Excel", e);
        }
    }
    
    private Workbook createPivotExcelWorkbook(Map<Long, Map<String, String>> pivotData, Truck truck) {
        Workbook workbook = new XSSFWorkbook();
        
        // Create main sheet
        Sheet sheet = workbook.createSheet("Truck Measurement Averages");
        
        // Create styles
        CellStyle mainTitleStyle = createMainTitleStyle(workbook);
        CellStyle headerStyle = createPivotHeaderStyle(workbook);
        CellStyle dataStyle = createPivotDataStyle(workbook);
        CellStyle truckCellStyle = createTruckCellStyle(workbook);
        
        int rowIdx = 0;
        
        // Create main title
        Row titleRow = sheet.createRow(rowIdx++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("TRUCK MEASUREMENT AVERAGES SETTING");
        titleCell.setCellStyle(mainTitleStyle);
        
        // Merge title cells
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));
        
        rowIdx++; // Empty row
        
        // Create pivot table header
        Row headerRow = sheet.createRow(rowIdx++);
        
        // Get all measurement names for columns
        List<String> measurementNames = getAllMeasurementNames(pivotData);
        
        // Create header cells
        Cell truckHeader = headerRow.createCell(0);
        truckHeader.setCellValue("Truck");
        truckHeader.setCellStyle(headerStyle);
        
        for (int i = 0; i < measurementNames.size(); i++) {
            Cell headerCell = headerRow.createCell(i + 1);
            headerCell.setCellValue(measurementNames.get(i));
            headerCell.setCellStyle(headerStyle);
        }
        
        // Fill data rows
        List<Truck> allTrucks = getTrucksFromPivotData(pivotData);
        
        for (Truck currentTruck : allTrucks) {
            Map<String, String> truckData = pivotData.get(currentTruck.getId());
            if (truckData == null) continue;
            
            Row dataRow = sheet.createRow(rowIdx++);
            
            // Truck cell
            Cell truckCell = dataRow.createCell(0);
            truckCell.setCellValue(currentTruck.getLicensePlate() + " — " + currentTruck.getGroup());
            truckCell.setCellStyle(truckCellStyle);
            
            // Measurement cells
            for (int i = 0; i < measurementNames.size(); i++) {
                String measurementName = measurementNames.get(i);
                String value = truckData.get(measurementName);
                
                Cell dataCell = dataRow.createCell(i + 1);
                if (value != null) {
                    dataCell.setCellValue(value);
                    
                    // Apply conditional formatting for values
                    applyValueFormatting(dataCell, value);
                } else {
                    dataCell.setCellValue("-");
                    dataCell.setCellStyle(dataStyle);
                }
            }
        }
        
        // Auto-size columns
        for (int i = 0; i <= measurementNames.size(); i++) {
            sheet.autoSizeColumn(i);
        }
        
        // Add some additional formatting
        addPivotTableFormatting(sheet, rowIdx, measurementNames.size() + 1);
        
        // Create summary sheet
        createSummarySheet(workbook, pivotData, truck);
        
        return workbook;
    }
    
    private CellStyle createMainTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.MEDIUM);
        // style.setBorderBottom(IndexedColors.DARK_BLUE.getIndex());
        return style;
    }
    
    private CellStyle createPivotHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        
        // Add a subtle gradient effect
        style.setFillForegroundColor(new XSSFColor(new java.awt.Color(59, 89, 152), null));
        
        return style;
    }
    
    private CellStyle createTruckCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }
    
    private CellStyle createPivotDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // Add alternating row colors
        style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        return style;
    }
    
    private List<String> getAllMeasurementNames(Map<Long, Map<String, String>> pivotData) {
        Set<String> measurementNames = new LinkedHashSet<>();
        
        for (Map<String, String> truckData : pivotData.values()) {
            measurementNames.addAll(truckData.keySet());
        }
        
        return new ArrayList<>(measurementNames);
    }
    
    private List<Truck> getTrucksFromPivotData(Map<Long, Map<String, String>> pivotData) {
        List<Truck> trucks = new ArrayList<>();
        
        for (Long truckId : pivotData.keySet()) {
            truckService.findById(truckId).ifPresent(trucks::add);
        }
        
        // Sort trucks by license plate
        trucks.sort(Comparator.comparing(Truck::getLicensePlate));
        
        return trucks;
    }
    
    private void applyValueFormatting(Cell cell, String value) {
        // Extract numeric value for conditional formatting
        String[] parts = value.split(" — ");
        if (parts.length > 0) {
            try {
                double numericValue = Double.parseDouble(parts[0]);
                CellStyle style = cell.getSheet().getWorkbook().createCellStyle();
                style.setAlignment(HorizontalAlignment.CENTER);
                style.setDataFormat(cell.getSheet().getWorkbook().createDataFormat().getFormat("0.00"));
                
                // Apply color based on value
                if (numericValue > 0.20) {
                    // High values - light red
                    style.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 230, 230), null));
                    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                } else if (numericValue < 0.10) {
                    // Low values - light green
                    style.setFillForegroundColor(new XSSFColor(new java.awt.Color(230, 255, 230), null));
                    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                } else {
                    // Medium values - light yellow
                    style.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 255, 230), null));
                    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                }
                
                cell.setCellStyle(style);
                cell.setCellValue(value);
            } catch (NumberFormatException e) {
                // If can't parse as number, just use default style
                CellStyle defaultStyle = cell.getSheet().getWorkbook().createCellStyle();
                defaultStyle.setAlignment(HorizontalAlignment.CENTER);
                cell.setCellStyle(defaultStyle);
            }
        }
    }
    
    private void addPivotTableFormatting(Sheet sheet, int totalRows, int totalColumns) {
        // Add borders to the entire table
        for (int i = 2; i < totalRows; i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                for (int j = 0; j < totalColumns; j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null) {
                        CellStyle style = cell.getCellStyle();
                        if (style == null) {
                            style = sheet.getWorkbook().createCellStyle();
                        }
                        style.setBorderTop(BorderStyle.THIN);
                        style.setBorderBottom(BorderStyle.THIN);
                        style.setBorderLeft(BorderStyle.THIN);
                        style.setBorderRight(BorderStyle.THIN);
                        cell.setCellStyle(style);
                    }
                }
            }
        }
        
        // Freeze the header row
        sheet.createFreezePane(0, 2);
        
        // Apply grid lines
        sheet.setDisplayGridlines(true);
    }
    
    private void createSummarySheet(Workbook workbook, Map<Long, Map<String, String>> pivotData, Truck truck) {
        Sheet summarySheet = workbook.createSheet("Summary");
        
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        
        Row titleRow = summarySheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("TRUCK MEASUREMENT AVERAGES - SUMMARY");
        titleCell.setCellStyle(titleStyle);
        summarySheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
        
        // Add statistics
        Row statsRow1 = summarySheet.createRow(2);
        statsRow1.createCell(0).setCellValue("Total Trucks:");
        statsRow1.createCell(1).setCellValue(pivotData.size());
        
        Row statsRow2 = summarySheet.createRow(3);
        statsRow2.createCell(0).setCellValue("Total Measurements:");
        statsRow2.createCell(1).setCellValue(getAllMeasurementNames(pivotData).size());
        
        Row statsRow3 = summarySheet.createRow(4);
        statsRow3.createCell(0).setCellValue("Generated On:");
        statsRow3.createCell(1).setCellValue(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        if (truck != null) {
            Row truckRow = summarySheet.createRow(6);
            truckRow.createCell(0).setCellValue("Selected Truck:");
            truckRow.createCell(1).setCellValue(truck.getLicensePlate() + " — " + truck.getGroup());
        }
        
        // Auto-size columns
        summarySheet.autoSizeColumn(0);
        summarySheet.autoSizeColumn(1);
    }
    
  
    public void exportToCsvPivot(Map<Long, Map<String, String>> pivotData, HttpServletResponse response) {
        try {
            response.setContentType("text/csv");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"truck_measurement_averages_" + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv\"");
            
            PrintWriter writer = response.getWriter();
            
            // Get all measurement names
            List<String> measurementNames = getAllMeasurementNames(pivotData);
            
            // Write header
            writer.write("Truck");
            for (String measurement : measurementNames) {
                writer.write("," + measurement);
            }
            writer.write("\n");
            
            // Write data
            List<Truck> trucks = getTrucksFromPivotData(pivotData);
            for (Truck truck : trucks) {
                Map<String, String> truckData = pivotData.get(truck.getId());
                if (truckData == null) continue;
                
                writer.write("\"" + truck.getLicensePlate() + " — " + truck.getGroup() + "\"");
                
                for (String measurement : measurementNames) {
                    String value = truckData.get(measurement);
                    writer.write("," + (value != null ? "\"" + value + "\"" : "-"));
                }
                writer.write("\n");
            }
            
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException("Error exporting pivot to CSV", e);
        }
    }
}
