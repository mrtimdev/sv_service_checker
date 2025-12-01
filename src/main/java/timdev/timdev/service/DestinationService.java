package timdev.timdev.service;

import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

                // A → License Plate
                String licensePlate = getCellStringValue(currentRow.getCell(0));

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
                String code = getCellStringValue(currentRow.getCell(1));
                if (code == null || code.trim().isEmpty()) {
                    result.addError("Row " + excelRow + ": Destination Code is required");
                    continue;
                }
                destination.setCode(code);

                // C → Name
                String name = getCellStringValue(currentRow.getCell(2));
                if (name == null || name.trim().isEmpty()) {
                    result.addError("Row " + excelRow + ": Destination Name is required");
                    continue;
                }
                destination.setName(name);

                // D → Distance
                double distance = getCellNumericValue(currentRow.getCell(3));
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

    public void exportExcel(List<Destination> destinations, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=Trucks-destinations.xlsx");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Truck Destinations Report");

        // Column widths
        int[] widths = {5000, 6000, 6000, 15000, 5000, 4000, 4000, 4000, 8000, 6000};
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
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("របាយការណ៏តួរលេខចាក់ប្រេងឡាន");
        titleCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));

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
        sheet.addMergedRegion(new CellRangeAddress(1,2,7,7)); // "ប្រេងផ្សេងៗ"
        sheet.addMergedRegion(new CellRangeAddress(1,2,8,8)); // "សរុបប្រេងចាក់អោយឡាន"
        sheet.addMergedRegion(new CellRangeAddress(1,2,9,9)); // "ផ្សេងៗ"

        // Row 2: English headers (subtitles)
        Row headerRow2 = sheet.createRow(2);
        String[] englishHeaders = {
            "Date",
            "License plate",
            "Type Of Truck",
            "Total Destination",
            "Total KM",
            "មធ្យមភាគ",
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
        
            row.createCell(0).setCellValue(d.getCreatedAt() != null ? d.getCreatedAt().toLocalDate().format(dateFormatter).toString() : "");
            row.createCell(1).setCellValue(d.getTruck() != null ? d.getTruck().getLicensePlate() : "");
            row.createCell(2).setCellValue(d.getTruck() != null ? d.getTruck().getGroup() : "");
            row.createCell(3).setCellValue(d.getName() != null ? d.getName() : "");
            row.createCell(4).setCellValue(d.getSetting() != null ? d.getSetting().getDistance() : 0);
            row.createCell(5).setCellValue(""); // average
            row.createCell(6).setCellValue(""); // litre
            row.createCell(7).setCellValue(""); // other fuel
            row.createCell(8).setCellValue(""); // total fuel
            row.createCell(9).setCellValue(""); // other

            for (int i = 0; i <= 9; i++) {
                row.getCell(i).setCellStyle(bodyStyle);
            }
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }




    
}
