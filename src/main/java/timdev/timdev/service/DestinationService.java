package timdev.timdev.service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import timdev.timdev.dto.CustomUserDetails;
import timdev.timdev.dto.ExcelImportResult;
import timdev.timdev.entity.Destination;
import timdev.timdev.entity.DestinationSetting;
import timdev.timdev.entity.Truck;
import timdev.timdev.entity.User;
import timdev.timdev.repository.DestinationRepository;

@AllArgsConstructor
@Service
public class DestinationService {

    private DestinationRepository repository;
    private TruckService truckservice;
    private DestinationSettingService destinationSettingService;

    public Destination findByCode(String code) {
        return repository.findByCode(code);
    }
    public Destination findByName(String name) {
        return repository.findByName(name);
    }

    public Iterable<Destination> getAll() {
        return repository.findAll();
    }

    public List<Destination> getAllFiltered(String search, Sort sort) {
        return repository.searchAll(search, sort);
    }

    public Page<Destination> getAllWithPageable(String search, Pageable pageable, Sort sort) {
        return repository.searchWithPage(search, pageable);
    }

    public Destination save(Destination destination) {
        return repository.save(destination);
    }   

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
    public Destination findById(Long id) {
        return repository.findById(id).orElse(null);
    }



    public List<Destination> readExcelFile_old(MultipartFile file) throws IOException {
        List<Destination> destinations = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
            Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(2);

            // Start reading from row 1 (skip header)
            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {

                Row currentRow = sheet.getRow(rowNum);
                if (currentRow == null || isRowEmpty(currentRow)) {
                    continue;
                }

                Destination destination = new Destination();

                // --- Column A → License Plate ---
                String licensePlate = getCellStringValue(currentRow.getCell(0));
                Truck truck = null;
                if (!licensePlate.isEmpty()) {
                    truck = truckservice.findByLicensePlate(licensePlate).orElse(null);
                }

                // --- Column B → Code ---
                destination.setCode(getCellStringValue(currentRow.getCell(1)));

                // --- Column C → Name ---
                destination.setName(getCellStringValue(currentRow.getCell(2)));

                // --- Column D → Distance ---
                destination.setDistance(getCellNumericValue(currentRow.getCell(3)));

                // --- Column E → Optional field (if needed) ---
                // example: destination.setType(getCellStringValue(currentRow.getCell(4)));

                // Store found truck (null allowed)
                destination.setTruck(truck);

                destinations.add(destination);
            }
        }

        return destinations;
    }


    private boolean isRowEmpty(Row row) {
        if (row == null) return true;

        for (int c = 0; c <= 4; c++) { // check A–E only
            Cell cell = row.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = cell.toString().trim();
                if (!value.isEmpty()) return false;
            }
        }
        return true;
    }


    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    // Check if it's an integer value
                    if (numericValue == Math.floor(numericValue)) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    try {
                        return String.valueOf(cell.getNumericCellValue());
                    } catch (Exception ex) {
                        return cell.getCellFormula();
                    }
                }
            default:
                return "";
        }
    }

    private double getCellNumericValue(Cell cell) {
        if (cell == null) return 0.0;
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                try {
                    return Double.parseDouble(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            case FORMULA:
                try {
                    return cell.getNumericCellValue();
                } catch (Exception e) {
                    try {
                        return Double.parseDouble(cell.getStringCellValue().trim());
                    } catch (NumberFormatException ex) {
                        return 0.0;
                    }
                }
            default:
                return 0.0;
        }
    }


    public List<String> validateAndSaveDestinations_old(List<Destination> destinations, CustomUserDetails userDetails) {
        List<String> errorMessages = new ArrayList<>();
        List<Destination> validDestinations = new ArrayList<>();
        
        for (int i = 0; i < destinations.size(); i++) {
            Destination destination = destinations.get(i);
            int rowNumber = i + 2; // +2 because Excel rows start at 1 and we skipped header
            
            try {
                // Validate required fields
                if (destination.getCode() == null || destination.getCode().trim().isEmpty()) {
                    errorMessages.add("Row " + rowNumber + ": Destination Code is required");
                    continue;
                }
                
                if (destination.getName() == null || destination.getName().trim().isEmpty()) {
                    errorMessages.add("Row " + rowNumber + ": Destination Name is required");
                    continue;
                }

                Truck truck = destination.getTruck();

                if (truck == null) {
                    errorMessages.add("Row " + rowNumber + ": Truck " + destination.getTruck().getLicensePlate() +" is required");
                    continue;
                }

                DestinationSetting setting = destinationSettingService.findByCode(destination.getCode());

                if (setting == null) {
                    setting = destinationSettingService.findByName(destination.getName());
                }

                if (setting == null) {
                    errorMessages.add("Row " + rowNumber + ": Destination '" + destination.getCode() +
                                    "' or name '" + destination.getName() + "' not found in Destination Settings");
                    continue;
                }
                destination.setCode(setting.getCode());
                destination.setName(setting.getName());

                destination.setSetting(setting);


                
                
                // Validate distance against DestinationSettings
                // if (destination.getDistance() > setting.getDistance()) {
                //     errorMessages.add("Row " + rowNumber + ": "+ truck.getLicensePlate() + " Distance " + destination.getDistance() + 
                //                     " exceeds maximum allowed distance " + setting.getDistance() + 
                //                     " for destination '" + destination.getCode() + "'");
                //     continue;
                // }

                
                
                // Validate truck license plate
                if (truck.getLicensePlate() != null && !truck.getLicensePlate().trim().isEmpty()) {
                    Truck truckOpt = truckservice.findByLicensePlate(truck.getLicensePlate()).orElse(null);
                    if (truckOpt == null) {
                        errorMessages.add("Row " + rowNumber + ": Truck with license plate '" + 
                                        truck.getLicensePlate() + "' not found");
                        continue;
                    }
                    destination.setTruck(truckOpt);
                }
                
                // Set audit fields
                User user = userDetails.getUser();
                destination.setCreatedBy(user);
                // destination.setUpdatedBy(userDetails.getUser());
                
                validDestinations.add(destination);
                
            } catch (Exception e) {
                errorMessages.add("Row " + rowNumber + ": Error processing row - " + e.getMessage());
            }
        }
        
        // Save all valid destinations
        if (!validDestinations.isEmpty()) {
            try {
                repository.saveAll(validDestinations);
            } catch (Exception e) {
                errorMessages.add("Error saving destinations to database: " + e.getMessage());
            }
        }
        
        return errorMessages;
    }


    public ExcelImportResult readExcelFile(MultipartFile file) throws IOException {
        ExcelImportResult result = new ExcelImportResult();

        try (InputStream inputStream = file.getInputStream();
            Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(2);

            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {

                Row currentRow = sheet.getRow(rowNum);
                if (currentRow == null || isRowEmpty(currentRow)) {
                    continue;
                }

                int excelRow = (rowNum + 1) - 1; // Excel rows start at row 1

                Destination destination = new Destination();

                String dateString = getCellStringValue(currentRow.getCell(0));
                if (dateString == null) {
                    result.addError("Row " + excelRow + ": The Date of each destination is required");
                    continue;
                }
                LocalDate date = parseDate(dateString);
                
                destination.setDate(date);

                // A → License Plate
                String licensePlate = getCellStringValue(currentRow.getCell(1));

                if (licensePlate == null || licensePlate.trim().isEmpty()) {
                    result.addError("Row " + excelRow + ": License Plate is required");
                    continue;
                }

                Truck truck = truckservice.findByLicensePlate(licensePlate).orElse(null);
                if (truck == null) {
                    result.addError("Row " + excelRow + ": Truck '" + licensePlate + "' not found");
                    continue;
                }
                destination.setTruck(truck);

                // B → Code
                String code = getCellStringValue(currentRow.getCell(2));
                if (code == null || code.trim().isEmpty()) {
                    result.addError("Row " + excelRow + ": Destination Code is required");
                    continue;
                }
                destination.setCode(code);

                // C → Name
                String name = getCellStringValue(currentRow.getCell(3));
                if (name == null || name.trim().isEmpty()) {
                    result.addError("Row " + excelRow + ": Destination Name is required");
                    continue;
                }
                destination.setName(name);

                // D → Distance
                double distance = getCellNumericValue(currentRow.getCell(4));
                destination.setDistance(distance);


                // Find DestinationSetting
                DestinationSetting setting =
                        destinationSettingService.findByCode(destination.getCode());

                if (setting == null) {
                    setting = destinationSettingService.findByName(destination.getName());
                }

                if (setting == null) {
                    result.addError("Row " + excelRow + ": Setting not found for code " +
                            destination.getCode() + " or name " + destination.getName());
                    continue;
                }
                destination.setSetting(setting);

                result.getDestinations().add(destination);
            }
        }

        return result;
    }

    private LocalDate parseDate(String dateString) {
    if (dateString == null || dateString.trim().isEmpty()) {
        return null;
    }

    // Java Date.toString() format: Fri Oct 03 00:00:00 ICT 2025
    DateTimeFormatter javaDateFormatter =
            DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);

    DateTimeFormatter[] formatters = {
        DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        javaDateFormatter
    };

    for (DateTimeFormatter formatter : formatters) {
        try {
            // For Java date style, convert ZonedDateTime → LocalDate
            if (formatter == javaDateFormatter) {
                ZonedDateTime zdt = ZonedDateTime.parse(dateString, formatter);
                return zdt.toLocalDate();
            }

            return LocalDate.parse(dateString, formatter);
        } catch (Exception ignored) {}
    }

    throw new RuntimeException("Invalid date format: " + dateString);
}


    public List<String> validateAndSaveDestinations(ExcelImportResult excelResult,
                                                CustomUserDetails userDetails) {

        List<String> dbErrors = new ArrayList<>();
        List<Destination> validDestinations = new ArrayList<>();

        List<Destination> destinations = excelResult.getDestinations();

        for (int i = 0; i < destinations.size(); i++) {
            Destination destination = destinations.get(i);
            int rowNumber = i + 2;

            try {
                destination.setCreatedBy(userDetails.getUser());

                validDestinations.add(destination);

            } catch (Exception e) {
                dbErrors.add("Row " + rowNumber + ": Error - " + e.getMessage());
            }
        }

        // Save valid rows
        if (!validDestinations.isEmpty()) {
            try {
                repository.saveAll(validDestinations);
            } catch (Exception e) {
                dbErrors.add("Database error: " + e.getMessage());
            }
        }

        return dbErrors; // ONLY DB validation errors
    }

    public void exportExcelForImportCompanyTruck(List<Destination> destinations, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=Trucks-destinations.xlsx");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Truck Destinations Report");

        // Column widths
        int[] widths = {5000, 6000, 6000, 15000, 5000, 4000, 4000, 4000, 8000, 6000, 8000, 6000};
        for (int i = 0; i < widths.length; i++) sheet.setColumnWidth(i, widths[i]);

        // ===== STYLES =====
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setWrapText(true);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        CellStyle bodyStyle = workbook.createCellStyle();
        bodyStyle.setBorderBottom(BorderStyle.THIN);
        bodyStyle.setBorderTop(BorderStyle.THIN);
        bodyStyle.setBorderLeft(BorderStyle.THIN);
        bodyStyle.setBorderRight(BorderStyle.THIN);

        // ===== DATE STYLE =====
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.cloneStyleFrom(bodyStyle);
        CreationHelper createHelper = workbook.getCreationHelper();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MMM-yyyy"));

        // ===== HEADER ROWS =====
        // Row 0: Title
        Row titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(30); 
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("របាយការណ៏តួរលេខចាក់ប្រេងឡាន");
        titleCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 10));

        // Row 1: Khmer headers
        Row headerRow1 = sheet.createRow(1);
        String[] khmerHeaders = {
            "កាលបរិច្ឆេទ",
            "ស្លាកលេខឡាន",
            "ប្រភេទឡាន",
            "គោលដៅសរុប",
            "ចម្ងាយ (KM)",
            "កម្រិតស៊ីប្រេង",
            "ចំនួនប្រេង",
            "ចំនួនប្រេង",
            "ប្រេងផ្សេងៗ",
            "សរុបប្រេងចាក់អោយឡាន",
            "ផ្សេងៗ"
        };

        for (int i = 0; i < khmerHeaders.length; i++) {
            Cell cell = headerRow1.createCell(i);
            cell.setCellValue(khmerHeaders[i]);
            cell.setCellStyle(headerStyle);
        }

        // Merge fuel-related columns only (example: "កម្រិតស៊ីប្រេង" spans 2 subcolumns)
        sheet.addMergedRegion(new CellRangeAddress(1,1,5,6));
        // Merge last 3 headers with row 3 (no English subtitle)
        sheet.addMergedRegion(new CellRangeAddress(1,2,8,8)); // "ប្រេងផ្សេងៗ"
        sheet.addMergedRegion(new CellRangeAddress(1,2,9,9)); // "សរុបប្រេងចាក់អោយឡាន"
        sheet.addMergedRegion(new CellRangeAddress(1,2,10,10)); // "ផ្សេងៗ"

        // Row 2: English headers (subtitles)
        Row headerRow2 = sheet.createRow(2);
        String[] englishHeaders = {
            "Date",
            "License plate",
            "Type Of Truck",
            "Total Destination",
            "Total KM",
            "មធ្យមភាគ",
            "កម្រិតស៊ី",
            "Litre",
        };

        for (int i = 0; i < englishHeaders.length; i++) {
            Cell cell = headerRow2.createCell(i);
            cell.setCellValue(englishHeaders[i]);
            cell.setCellStyle(headerStyle);
        }


        // Create a cell style for "L 100" (prefix L)
        CellStyle kmStyle = workbook.createCellStyle();
        kmStyle.cloneStyleFrom(bodyStyle);
        DataFormat format = workbook.createDataFormat();
        kmStyle.setDataFormat(format.getFormat("\"L\" #")); // displays L 100

        // Create a cell style for "100 L" (suffix L)
        CellStyle litreStyle = workbook.createCellStyle();
        litreStyle.cloneStyleFrom(bodyStyle);
        litreStyle.setDataFormat(format.getFormat("# \"L\"")); // displays 100 L

        // ===== BODY ROWS =====
        int rowIndex = 3;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        for (Destination d : destinations) {
            Row row = sheet.createRow(rowIndex++);
        
            row.createCell(0).setCellValue(d.getCreatedAt() != null ? d.getDate().format(dateFormatter).toString() : "");
            row.createCell(1).setCellValue(d.getTruck() != null ? d.getTruck().getLicensePlate() : "");
            row.createCell(2).setCellValue(d.getTruck() != null ? d.getTruck().getGroup() : "");
            row.createCell(3).setCellValue(d.getSetting() != null ? d.getSetting().getName() : "");
            row.createCell(4).setCellValue(d.getSetting() != null ? d.getSetting().getDistance() : 0);
            row.createCell(5).setCellValue(""); 
            row.createCell(6).setCellValue(""); 
            row.createCell(7).setCellValue(""); 
            row.createCell(8).setCellValue(""); 
            row.createCell(9).setCellValue("");
            row.createCell(10).setCellValue(""); 

            for (int i = 0; i <= 10; i++) {
                row.getCell(i).setCellStyle(bodyStyle);
            }
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    // for report

    public void exportExcel(List<Destination> destinations, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=Truck-Destinations-Detailed-Report.xlsx");
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Destinations Detailed Report");
            
            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle subHeaderStyle = createSubHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle numericStyle = createNumericStyle(workbook);
            
            // Create main header with logo
            createMainHeader(sheet, workbook, titleStyle);
            
            // Create report info section
            int currentRow = createReportInfoSection(sheet, destinations, subHeaderStyle, dataStyle);
            
            // Create column headers
            currentRow = createColumnHeaders(sheet, currentRow, headerStyle);
            
            // Populate data
            populateDestinationData(sheet, destinations, currentRow, dataStyle, dateStyle, numericStyle);
            
            // Auto-size columns
            autoSizeColumns(sheet);
            
            // Add footer
            addFooter(sheet, workbook, dataStyle);
            
            workbook.write(response.getOutputStream());
        } catch (Exception e) {
            throw new IOException("Error generating Excel report", e);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // Background color
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // Font
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Arial");
        style.setFont(font);
        
        // Borders
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        
        // Alignment
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return style;
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        font.setFontHeightInPoints((short) 18);
        font.setFontName("Calibri");
        style.setFont(font);
        
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return style;
    }

    private CellStyle createSubHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.DARK_GREEN.getIndex());
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        
        style.setAlignment(HorizontalAlignment.LEFT);
        
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // Borders
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // Wrap text
        style.setWrapText(true);
        
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MMM-yyyy"));
        return style;
    }

    private CellStyle createNumericStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setAlignment(HorizontalAlignment.RIGHT);
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("#,##0.00"));
        return style;
    }

    private void createMainHeader(Sheet sheet, Workbook workbook, CellStyle titleStyle) {
        // Merge cells for main title
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 12));
        
        Row titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(30);
        
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("🚚 TRUCK DESTINATIONS DETAILED REPORT");
        titleCell.setCellStyle(titleStyle);
        
        // Add subtitle
        Row subtitleRow = sheet.createRow(1);
        subtitleRow.setHeightInPoints(20);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 12));
        
        CellStyle subtitleStyle = workbook.createCellStyle();
        Font subtitleFont = workbook.createFont();
        subtitleFont.setItalic(true);
        subtitleFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        subtitleFont.setFontHeightInPoints((short) 10);
        subtitleStyle.setFont(subtitleFont);
        subtitleStyle.setAlignment(HorizontalAlignment.CENTER);
        
        Cell subtitleCell = subtitleRow.createCell(0);
        subtitleCell.setCellValue("Comprehensive Destination Tracking & Logistics Management");
        subtitleCell.setCellStyle(subtitleStyle);
        
        // Add decorative separator
        Row separatorRow = sheet.createRow(2);
        separatorRow.setHeightInPoints(15);
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 12));
        
        Cell separatorCell = separatorRow.createCell(0);
        CellStyle separatorStyle = workbook.createCellStyle();
        separatorStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        separatorStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        separatorCell.setCellStyle(separatorStyle);
    }

    private int createReportInfoSection(Sheet sheet, List<Destination> destinations, CellStyle subHeaderStyle, CellStyle dataStyle) {
        int rowIndex = 3;
        
        Row infoRow1 = sheet.createRow(rowIndex++);
        Row infoRow2 = sheet.createRow(rowIndex++);
        Row infoRow3 = sheet.createRow(rowIndex++);
        
        // Report Generated Date
        Cell dateLabelCell = infoRow1.createCell(0);
        dateLabelCell.setCellValue("Report Generated:");
        dateLabelCell.setCellStyle(subHeaderStyle);
        
        Cell dateValueCell = infoRow1.createCell(1);
        dateValueCell.setCellValue(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm a")));
        dateValueCell.setCellStyle(dataStyle);
        
        // Total Records
        Cell totalLabelCell = infoRow1.createCell(3);
        totalLabelCell.setCellValue("Total Destinations:");
        totalLabelCell.setCellStyle(subHeaderStyle);
        
        Cell totalValueCell = infoRow1.createCell(4);
        totalValueCell.setCellValue(destinations.size());
        totalValueCell.setCellStyle(dataStyle);
        
        // Report Period
        if (!destinations.isEmpty()) {
            LocalDate minDate = destinations.stream()
                .map(Destination::getDate)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());
            
            LocalDate maxDate = destinations.stream()
                .map(Destination::getDate)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());
            
            Cell periodLabelCell = infoRow2.createCell(0);
            periodLabelCell.setCellValue("Report Period:");
            periodLabelCell.setCellStyle(subHeaderStyle);
            
            Cell periodValueCell = infoRow2.createCell(1);
            periodValueCell.setCellValue(minDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) + 
                                    " to " + maxDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
            periodValueCell.setCellStyle(dataStyle);
        }
        
        // Add empty row before data table
        sheet.createRow(rowIndex++).setHeightInPoints(10);
        
        return rowIndex;
    }

    private int createColumnHeaders(Sheet sheet, int startRow, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(startRow);
        headerRow.setHeightInPoints(25);
        
        String[] headers = {
            "Date", "Truck","Destination Code", "Destination Name", 
            "Distance (km)", "Created At", "Created By", "Last Updated"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        return startRow + 1;
    }

    private void populateDestinationData(Sheet sheet, List<Destination> destinations, int startRow, 
                                        CellStyle dataStyle, CellStyle dateStyle, CellStyle numericStyle) {
        int rowIndex = startRow;
        
        for (Destination destination : destinations) {
            Row row = sheet.createRow(rowIndex++);
            row.setHeightInPoints(20);
            
            int colIndex = 0;
            
            // Date
            Cell dateCell = row.createCell(colIndex++);
            if (destination.getDate() != null) {
                dateCell.setCellValue(java.sql.Date.valueOf(destination.getDate()));
                dateCell.setCellStyle(dateStyle);
            } else {
                dateCell.setCellValue("N/A");
                dateCell.setCellStyle(dataStyle);
            }

             // Truck Info
            Cell licensePlate = row.createCell(colIndex++);
            if (destination.getTruck() != null) {
                licensePlate.setCellValue(destination.getTruck().getLicensePlate() != null ? 
                                        destination.getTruck().getLicensePlate() : "");
            } else {
                licensePlate.setCellValue("N/A");
            }
            
            // Destination Code
            Cell codeCell = row.createCell(colIndex++);
            codeCell.setCellValue(destination.getCode() != null ? destination.getCode() : "");
            codeCell.setCellStyle(dataStyle);
            
            // Destination Name
            Cell nameCell = row.createCell(colIndex++);
            nameCell.setCellValue(destination.getSetting() != null ? destination.getSetting().getName() : "");
            nameCell.setCellStyle(dataStyle);
            
            // Distance
            Cell distanceCell = row.createCell(colIndex++);
            distanceCell.setCellValue(destination.getSetting().getDistanceFormat());
            distanceCell.setCellStyle(numericStyle);
            
            
            licensePlate.setCellStyle(dataStyle);
            
            // Created At
            Cell createdAtCell = row.createCell(colIndex++);
            if (destination.getCreatedAt() != null) {
                createdAtCell.setCellValue(java.sql.Timestamp.valueOf(destination.getCreatedAt()));
                CellStyle dateTimeStyle = sheet.getWorkbook().createCellStyle();
                dateTimeStyle.cloneStyleFrom(dataStyle);
                CreationHelper createHelper = sheet.getWorkbook().getCreationHelper();
                dateTimeStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MMM-yyyy HH:mm a"));
                createdAtCell.setCellStyle(dateTimeStyle);
            } else {
                createdAtCell.setCellValue("N/A");
                createdAtCell.setCellStyle(dataStyle);
            }
            
            // Created By
            Cell createdByCell = row.createCell(colIndex++);
            createdByCell.setCellValue(destination.getCreatedBy() != null ? 
                                    destination.getCreatedBy().getUsername() : "System");
            createdByCell.setCellStyle(dataStyle);
            
            // Updated At
            Cell updatedAtCell = row.createCell(colIndex);
            if (destination.getUpdatedAt() != null) {
                updatedAtCell.setCellValue(java.sql.Timestamp.valueOf(destination.getUpdatedAt()));
                CellStyle dateTimeStyle = sheet.getWorkbook().createCellStyle();
                dateTimeStyle.cloneStyleFrom(dataStyle);
                CreationHelper createHelper = sheet.getWorkbook().getCreationHelper();
                dateTimeStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MMM-yyyy HH:mm a"));
                updatedAtCell.setCellStyle(dateTimeStyle);
            } else {
                updatedAtCell.setCellValue("N/A");
                updatedAtCell.setCellStyle(dataStyle);
            }
        }
    }

    private void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i < 13; i++) {
            sheet.autoSizeColumn(i);
            // Add some padding
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 500);
        }
    }

    private void addFooter(Sheet sheet, Workbook workbook, CellStyle dataStyle) {
        int lastRowNum = sheet.getLastRowNum();
        Row footerRow = sheet.createRow(lastRowNum + 2);
        
        CellStyle footerStyle = workbook.createCellStyle();
        Font footerFont = workbook.createFont();
        footerFont.setItalic(true);
        footerFont.setColor(IndexedColors.GREY_40_PERCENT.getIndex());
        footerStyle.setFont(footerFont);
        
        Cell footerCell = footerRow.createCell(0);
        footerCell.setCellValue("Confidential - Generated by Vehicle Fuel Maintenance.");
        footerCell.setCellStyle(footerStyle);
        sheet.addMergedRegion(new CellRangeAddress(lastRowNum + 2, lastRowNum + 2, 0, 12));
    }




    // ----------------------------
    // PAGINATION + FILTER + SORT
    // ----------------------------

    public List<Destination> findByFilterQueriesWithList(
            String query,
            LocalDate startDate,
            LocalDate endDate,
            Sort sort
    ) {
        return repository.findByFilterQueriesAndSort(
                startDate,
                endDate,
                query,
                sort
        );
    }

    // Overload with date filters
    public Page<Destination> findByFilterQueriesWithPage(
            String query,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        return repository.findByFilterQueriesWithPage(
                startDate,
                endDate,
                query,
                pageable
        );
    }
    
}
