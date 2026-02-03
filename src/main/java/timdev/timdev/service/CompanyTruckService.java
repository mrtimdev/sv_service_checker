package timdev.timdev.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.hibernate.annotations.Array;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import timdev.timdev.dto.CompanyTruckRequestDTO;
import timdev.timdev.dto.Measurement;
import timdev.timdev.dto.Status;
import timdev.timdev.entity.CompanyTruck;
import timdev.timdev.entity.Destination;
import timdev.timdev.entity.DestinationSetting;
import timdev.timdev.entity.Truck;
import timdev.timdev.entity.User;
import timdev.timdev.enums.ApprovalStatus;
import timdev.timdev.repository.CompanyTruckRepository;
import timdev.timdev.repository.DestinationRepository;
import timdev.timdev.repository.DestinationSettingRepository;
import timdev.timdev.repository.MeasurementRepository;
import timdev.timdev.repository.TruckRepository;
import timdev.timdev.repository.UserRepository;


@Service
public class CompanyTruckService {

    @Autowired
    private CompanyTruckRepository repository;
    @Autowired
    private TruckRepository truckRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DestinationRepository destinationRepository;
    
    @Autowired
    private DestinationSettingRepository destinationSettingRepository;

    @Autowired private MeasurementRepository measurementRepository;

    public List<CompanyTruck> getAllTrucks() {
        return repository.findAll();
    }

    public CompanyTruck getTruckById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public CompanyTruck saveTruck(CompanyTruck truck) {
        return repository.save(truck);
    }

    public CompanyTruck updateTruck(Long id, CompanyTruck updatedTruck) {
        return repository.findById(id).map(truck -> {
            truck.setDate(updatedTruck.getDate());
            truck.setTruck(updatedTruck.getTruck());
            truck.setTotalDestination(updatedTruck.getTotalDestination());
            truck.setTotalKm(updatedTruck.getTotalKm());
            truck.setAverage(updatedTruck.getAverage());
            truck.setMeasurement(updatedTruck.getMeasurement());
            truck.setLitreQuantity(updatedTruck.getLitreQuantity());
            truck.setUpdatedBy(updatedTruck.getUpdatedBy());
            truck.setUpdatedAt(updatedTruck.getUpdatedAt());
            return repository.save(truck);
        }).orElse(null);
    }


    public CompanyTruck createTruck(CompanyTruckRequestDTO dto) {
        CompanyTruck truck = convertToEntity(dto);
        truck.setCreatedAt(LocalDateTime.now());
        truck.setOtherOils(dto.getOtherOils());
        truck.setTotalOilsChange(dto.getTotalOilsChange());

        if (dto.getDestinationId() != null) {
            Destination destination = destinationRepository.findById(dto.getDestinationId()).orElse(null);
            truck.setDestination(destination);
        }
        if (dto.getMeasurementId() != null) {
            timdev.timdev.entity.Measurement measurementEntity = measurementRepository
                    .findById(dto.getMeasurementId())
                    .orElse(null);

            if (measurementEntity != null) {
                // Convert entity name (English) to enum
                Measurement measurementEnum = Measurement.fromName(measurementEntity.getName());

                if (measurementEnum != null) {
                    truck.setMeasurement(measurementEnum);
                } else {
                    truck.setMeasurement(null); 
                }
            }
        }

        return repository.save(truck);
    }

    public CompanyTruck updateTruck(Long id, CompanyTruckRequestDTO dto) {
        CompanyTruck existing = getTruckById(id);

        


        User userUpdate = userRepository.findById(dto.getUpdatedBy()).orElse(null);
        existing.setDate(dto.getDate());
        existing.setTruck(truckRepository.findById(dto.getTruckId()).orElseThrow());
        existing.setTotalDestination(dto.getTotalDestination());
        existing.setTotalKm(dto.getTotalKm());
        existing.setAverage(dto.getAverage());
        existing.setLitreQuantity(dto.getLitreQuantity());
        existing.setNote(dto.getNote());
        existing.setOtherOils(dto.getOtherOils());
        existing.setTotalOilsChange(dto.getTotalOilsChange());
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(userUpdate != null ? userUpdate : null);

        if (dto.getDestinationId() != null) {
            Destination destination = destinationRepository.findById(dto.getDestinationId()).orElse(null);
            existing.setDestination(destination);
        }
        if (dto.getMeasurementId() != null) {
            timdev.timdev.entity.Measurement measurementEntity = measurementRepository
                    .findById(dto.getMeasurementId())
                    .orElse(null);

            if (measurementEntity != null) {
                // Convert entity name (English) to enum
                Measurement measurementEnum = Measurement.fromName(measurementEntity.getName());

                if (measurementEnum != null) {
                    existing.setMeasurement(measurementEnum);
                } else {
                    existing.setMeasurement(null); 
                }
            }
        }

        
        return repository.save(existing);
    }

    public CompanyTruck convertToEntity(CompanyTruckRequestDTO dto) {
        CompanyTruck truck = new CompanyTruck();

        User user = userRepository.findById(dto.getCreatedBy()).orElse(null);
        // User userUpdate = userRepository.findById(dto.getUpdatedBy()).orElse(null);

        truck.setDate(dto.getDate());
        truck.setTruck(truckRepository.findById(dto.getTruckId()).orElseThrow());
        truck.setTotalDestination(dto.getTotalDestination());
        truck.setTotalKm(dto.getTotalKm());
        truck.setAverage(dto.getAverage());
        truck.setMeasurement(dto.getMeasurement());
        truck.setLitreQuantity(dto.getLitreQuantity());
        truck.setNote(dto.getNote());
        truck.setOtherOils(dto.getOtherOils());
        truck.setTotalOilsChange(dto.getTotalOilsChange());
        truck.setCreatedAt(dto.getCreatedAt());
        truck.setUpdatedAt(dto.getUpdatedAt());
        truck.setCreatedBy(user);
        // truck.setUpdatedBy(userUpdate != null ? userUpdate : null);
        return truck;
    }

    public CompanyTruckRequestDTO convertToDto(CompanyTruck entity) {
        CompanyTruckRequestDTO dto = new CompanyTruckRequestDTO();
        dto.setId(entity.getId());
        dto.setDate(entity.getDate());
        dto.setTruckId(entity.getTruck() != null ? entity.getTruck().getId() : null);
        dto.setTotalDestination(entity.getTotalDestination());
        dto.setTotalKm(entity.getTotalKm());
        dto.setAverage(entity.getAverage());
        dto.setMeasurement(entity.getMeasurement());
        dto.setLitreQuantity(entity.getLitreQuantity());
        dto.setNote(entity.getNote());
        dto.setOtherOils(entity.getOtherOils());
        dto.setTotalOilsChange(entity.getTotalOilsChange());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null);
        dto.setUpdatedBy(entity.getUpdatedBy() != null ? entity.getUpdatedBy().getId() : null);
        return dto;
    }

    public void deleteTruck(Long id) {
        repository.deleteById(id);
    }



    // Fetch all trucks
    public List<CompanyTruck> getAll() {
        return repository.findAll();
    }

    public List<CompanyTruck> getAllWIthSort(Sort sort) {
        return repository.findAll(sort);
    }


    public List<CompanyTruck> findByLicensePlateContaining(String licensePlate, Sort sort) {
        return repository.findByTruck_LicensePlateContaining(licensePlate, sort);
    }

    public Page<CompanyTruck> findByFilterQueriesPage(LocalDate startDate, LocalDate endDate, String query, Pageable pageable) {

        return repository.findByFilterQueriesPage(startDate, endDate, query, pageable);
    }

     public Page<CompanyTruck> findByFilterQueriesPageAndStatus(LocalDate startDate, LocalDate endDate, String query, Pageable pageable, Status status) {

        return repository.findByFilterQueriesPageAndStatus(startDate, endDate, query, pageable, status);
    }

    public List<CompanyTruck> findByFilterQueriesList(LocalDate startDate, LocalDate endDate, String query, Pageable pageable) {

        return repository.findByFilterQueriesList(startDate, endDate, query, pageable);
    }

    public List<CompanyTruck> findByFilterQueriesListAndSort(LocalDate startDate, LocalDate endDate, String query, Sort sort) {

        return repository.findByFilterQueriesListAndSort(startDate, endDate, query, sort);
    }

    public List<CompanyTruck> findByFilterQueriesListAndSortAndStatus(LocalDate startDate, LocalDate endDate, String query, Sort sort, Status status) {

        return repository.findByFilterQueriesListAndSortAndStatus(startDate, endDate, query, sort, status);
    }


    // Fetch all with pagination
    public Page<CompanyTruck> getAllWithPageable(Pageable pageable) {
        return repository.findAll(pageable);
    }
    

    // Filter by license plate (non-paginated)
    public List<CompanyTruck> findByLicensePlateContaining(String licensePlate) {
        return repository.findByTruck_LicensePlateContainingIgnoreCase(licensePlate);
    }

    
    public List<CompanyTruck> findByStatus(Status status) {
        return repository.findByStatus(status);
    }

    
    public List<CompanyTruck> findByLicensePlateAndStatus(String licensePlate, Status status) {
        return repository
                .findByLicensePlateContainingAndStatus(licensePlate, status);
    }

    
    public Page<CompanyTruck> findByStatusWithPageable(Status status, Pageable pageable) {
        return repository.findByStatus(status, pageable);
    }

    
    public Page<CompanyTruck> findByLicensePlateAndStatusWithPageable(
            String licensePlate, Status status, Pageable pageable) {

        return repository
                .findByLicensePlateContainingAndStatus(licensePlate, status, pageable);
    }

    public Page<CompanyTruck> findByLicensePlateAndStatusInWithPageable(
            String licensePlate, List<Status> statuses, Pageable pageable) {

        return repository.findByLicensePlateContainingAndStatusIn(
                licensePlate, statuses, pageable
        );
    }


    



    // Filter by license plate (paginated)
    public Page<CompanyTruck> findByLicensePlateContainingWithPageable(String licensePlate, Pageable pageable) {
        return repository.findByTruck_LicensePlateContainingIgnoreCase(licensePlate, pageable);
    }

    public Page<CompanyTruck> findByTruckLicensePlateWithTruckWithPageable(String licensePlate, Pageable pageable) {
        return repository.findByTruckLicensePlateWithTruck(licensePlate, pageable);
    }

    public boolean isExistsByTruckAndDate(Truck companyTruck, LocalDate date) {
        boolean exists = repository.existsByTruckAndDate(companyTruck, date);
        if (exists) {
            return true;
        }
        return false;
    }

    public CompanyTruck findByTruckAndDate(Truck truck, LocalDate date) {
        return repository.findByTruckAndDate(truck, date).orElse(null);
    }


    public Optional<CompanyTruck> findById(Long id) {
        return repository.findById(id);
    }

    public void deleteById(Long id) {
        repository.deleteById(id); 
    }

    private String getCellValue(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) return "";

        CellType cellType = cell.getCellType();

        if (cellType == CellType.FORMULA) {
            CellValue evaluatedValue = evaluator.evaluate(cell);
            if (evaluatedValue == null) return "";

            return switch (evaluatedValue.getCellType()) {
                case STRING -> evaluatedValue.getStringValue().trim();
                case NUMERIC -> new DataFormatter().formatRawCellContents(
                        evaluatedValue.getNumberValue(),
                        cell.getCellStyle().getDataFormat(),
                        cell.getCellStyle().getDataFormatString()
                );
                case BOOLEAN -> String.valueOf(evaluatedValue.getBooleanValue());
                case ERROR -> "";
                default -> "";
            }; // or return "#ERROR"
        }

        // Non-formula cells
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell).trim();
    }





    public List<CompanyTruckRequestDTO> readExcel(MultipartFile file) {
        List<CompanyTruckRequestDTO> dataList = new ArrayList<>();
        
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            // IMPORTANT: Force POI to evaluate formulas
            evaluator.setIgnoreMissingWorkbooks(true); // Ignore missing external references
            evaluator.clearAllCachedResultValues(); // Clear any cached values
            // Start from row 3 (0-based index, so row 5 = Excel row 6)
            for (int rowNum = 5; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null) continue; // Skip empty rows
                
                CompanyTruckRequestDTO dto = new CompanyTruckRequestDTO();
                
                try {
                    // Read cells from column A to K (0 to 10)
                    for (int colNum = 0; colNum <= 10; colNum++) {
                        Cell cell = row.getCell(colNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        String value = getCellValue(cell, evaluator);
                        
                        String cellValue = getFormulaEvaluatedValue(cell, evaluator);
                        setCellValueToDTO(dto, colNum, cellValue);
                        // setCellValueToDTO(dto, colNum, cell, evaluator);
                        
                    }
                    
                    // Validate required fields before adding
                    if (isValidDTO(dto)) {
                        dataList.add(dto);
                    }
                    
                } catch (Exception e) {
                    throw new RuntimeException("Error processing row " + (rowNum + 1) + ": " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Error reading Excel file: " + e.getMessage());
        }
        
        return dataList;
    }

    private String getFormulaEvaluatedValue(Cell cell, FormulaEvaluator evaluator) {
    if (cell == null) {
        return "";
    }
    
    try {
        // Check if cell contains a formula
        if (cell.getCellType() == CellType.FORMULA) {
            try {
                // Evaluate the formula to get its value
                CellValue cellValue = evaluator.evaluate(cell);
                
                if (cellValue != null) {
                    switch (cellValue.getCellType()) {
                        case NUMERIC -> {
                            // Check if it's a date
                            if (DateUtil.isCellDateFormatted(cell)) {
                                // Format date properly
                                Date date = cell.getDateCellValue();
                                SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
                                return sdf.format(date);
                            } else {
                                // Return numeric value
                                double numValue = cellValue.getNumberValue();
                                // Remove trailing .0 if it's an integer
                                if (numValue == Math.floor(numValue)) {
                                    return String.valueOf((int) numValue);
                                }
                                return String.valueOf(numValue);
                            }
                        }
                        case STRING -> {
                            return cellValue.getStringValue().trim();
                        }
                        case BOOLEAN -> {
                            return String.valueOf(cellValue.getBooleanValue());
                        }
                        case ERROR -> {
                            // Formula resulted in an error (like #VALUE!, #N/A)
                            return getCachedFormulaValue(cell);
                        }
                        default -> {
                            return getCachedFormulaValue(cell);
                        }
                    }
                } else {
                    // Evaluation failed, try to get cached value
                    return getCachedFormulaValue(cell);
                }
            } catch (Exception e) {
                // If formula evaluation fails, try cached value
                return getCachedFormulaValue(cell);
            }
        } else {
            // Regular cell (not a formula)
            return getRegularCellValue(cell, evaluator);
        }
    } catch (Exception e) {
        return "";
    }
}

private String getCachedFormulaValue(Cell cell) {
    try {
        DataFormatter formatter = new DataFormatter();
        
        switch (cell.getCachedFormulaResultType()) {
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    return formatter.formatCellValue(cell);
                }
                double num = cell.getNumericCellValue();
                if (num == Math.floor(num)) {
                    return String.valueOf((int) num);
                }
                return String.valueOf(num);
            }
            case STRING -> {
                String str = cell.getStringCellValue();
                return str != null ? str.trim() : "";
            }
            case BOOLEAN -> {
                return String.valueOf(cell.getBooleanCellValue());
            }
            case ERROR -> {
                return ""; // Return empty for formula errors
            }
            default -> {
                return "";
            }
        }
    } catch (Exception e) {
        return "";
    }
}

/**
 * Gets value from regular (non-formula) cells
 */
private String getRegularCellValue(Cell cell, FormulaEvaluator evaluator) {
    try {
        DataFormatter formatter = new DataFormatter();
        String value = formatter.formatCellValue(cell, evaluator).trim();
        
        // Handle numeric formatting
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            // Already handled by DataFormatter
        }
        
        return value;
    } catch (Exception e) {
        return "";
    }
}

    private String getCellValueAsString(Cell cell) {
    if (cell == null) {
        return "";
    }
    
    try {
        switch (cell.getCellType()) {
            case STRING -> {
                return cell.getStringCellValue();
            }
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            }
            case BOOLEAN -> {
                return String.valueOf(cell.getBooleanCellValue());
            }
            case FORMULA -> {
                return cell.getCellFormula();
            }
            case BLANK -> {
                return "";
            }
            case ERROR -> {
                return "ERROR";
            }
            default -> {
                return "";
            }
        }
    } catch (Exception e) {
        return "";
    }
}

    // private void setCellValueToDTO(CompanyTruckRequestDTO dto, int columnIndex, Cell cell, FormulaEvaluator evaluator) {
    //     DataFormatter dataFormatter = new DataFormatter();
    //     String cellValue;
        
    //     try {
    //         // Check if cell has a formula error
    //         if (cell.getCellType() == CellType.FORMULA) {
    //             CellValue cellVal = evaluator.evaluate(cell);
    //             if (cellVal.getCellType() == CellType.ERROR) {
    //                 // Handle formula error - either log or use empty value
    //                 cellValue = "";
    //             } else {
    //                 cellValue = dataFormatter.formatCellValue(cell, evaluator).trim();
    //             }
    //         } else {
    //             cellValue = dataFormatter.formatCellValue(cell, evaluator).trim();
    //         }
    //     } catch (Exception e) {
    //         // Fallback to basic cell value
    //         cellValue = getCellValueAsString(cell).trim();
    //     }
    //     // DataFormatter dataFormatter = new DataFormatter();
    //     // String cellValue = dataFormatter.formatCellValue(cell, evaluator).trim();
        
    //     switch (columnIndex) {
    //         case 0: // Date (A column) - "01-Oct-2025"
    //             dto.setDate(parseDate(cellValue));
    //             break;
    //         case 1: // License Plate (B column) - "3E-2637"
    //             dto.setLicensePlate(cellValue);
    //             break;
    //         case 2: // Truck Type (C column) - "CY-Big Truck"
    //             // Consider adding this to DTO if needed for validation or logging
    //             // dto.setTruckType(cellValue);
    //             break;
    //         case 3: // Destination (D column) - "ការ៉ាស់ថ្មី - KHB - KKG3 - ការ៉ាស់ថ្មី"
    //             dto.setTotalDestination(cellValue);
    //             break;
    //         case 4: // Total KM (E column) - "km604"
    //             dto.setTotalKm(parseKm(cellValue));
    //             break;
    //         case 5: // Average (F column) - "0.36"
    //             dto.setAverage(parseDouble(cellValue));
    //             break;
    //         case 6: // Fuel Level (G column) - "កម្រិតធ្ងន់"
    //             dto.setMeasurement(mapToMeasurement(cellValue));
    //             break;
    //         case 7: // Fuel Quantity (H column) - "L217"
    //             dto.setLitreQuantity(parseLitre(cellValue));
    //             break;
    //         case 8: // Other Fuel (I column)
    //             dto.setOtherOils(parseLitreString(cellValue));
    //             break;
    //         case 9: // Total Fuel (J column) - "L217"
    //             dto.setTotalOilsChange(parseLitre(cellValue));
    //             break;
    //         case 10: // Other (K column)
    //             dto.setNote(cellValue);
    //             break;
    //         default:
    //             // Log unexpected column
    //             break;
    //     }
    // }

    private void setCellValueToDTO(CompanyTruckRequestDTO dto, int columnIndex, String cellValue) {
        DataFormatter dataFormatter = new DataFormatter();
        // String cellValue = dataFormatter.formatCellValue(cell).trim();
        
        switch (columnIndex) {
            case 0 -> 
                dto.setDate(parseDate(cellValue));
            case 1 -> 
                dto.setLicensePlate(cellValue);
            case 2 -> {
            }
            case 3 -> 
                dto.setTotalDestination(cellValue);
            case 4 -> 
                dto.setTotalKm(parseKm(cellValue));
            case 5 -> 
                dto.setAverage(parseDouble(cellValue));
            case 6 -> 
                dto.setMeasurement(mapToMeasurement(cellValue));
            case 7 -> 
                dto.setLitreQuantity(parseLitre(cellValue));
            case 8 -> 
                dto.setOtherOils(parseLitreString(cellValue)); 
            case 9 -> 
                dto.setTotalOilsChange(parseLitre(cellValue));
            case 10 -> 
                dto.setNote(cellValue);
        }
    }

    // Helper methods for parsing
    private LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        
        try {
            DateTimeFormatter javaDateFormatter =
                DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
            // Try different date formats including your format "01-Oct-2025"
            DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("dd-MMM-yyyy", new Locale("en")),
                DateTimeFormatter.ofPattern("dd-MMM-yy", new Locale("en")),   // 🔥 This fixes "21-Oct-25"
                DateTimeFormatter.ofPattern("d-MMM-yy", new Locale("en")),   // 🔥 This fixes "4-Sep-25"
                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("dd/MM/yy"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                javaDateFormatter
            };
            
            for (DateTimeFormatter formatter : formatters) {
                try {
                    if (formatter == javaDateFormatter) {
                        ZonedDateTime zdt = ZonedDateTime.parse(dateString, formatter);
                        return zdt.toLocalDate();
                    }
                    return LocalDate.parse(dateString, formatter);
                } catch (DateTimeParseException e) {
                    // Try next format
                }
            }
            
            throw new RuntimeException("Invalid date format: " + dateString);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error parsing date: " + dateString);
        }
    }

    private Double parseKm(String kmString) {
        if (kmString == null || kmString.trim().isEmpty()) {
            return null;
        }

        try {
            String cleaned = cleanNumber(kmString);

            if (cleaned.isEmpty()) return null;

            return Double.valueOf(cleaned);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid KM format: '" + kmString + "'");
        }
    }

    private Double parseLitre(String litreString) {
        if (litreString == null || litreString.trim().isEmpty()) {
            return null;
        }

        try {
            String cleaned = cleanNumber(litreString);

            if (cleaned.isEmpty()) return null;
            double value = Double.parseDouble(cleaned);
            return (double) Math.round(value);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid litre format: '" + litreString + "'");
        }
    }


    private String parseLitreString(String litreString) {
        if (litreString == null || litreString.trim().isEmpty()) {
            return null;
        }

        return litreString.replaceAll("\\s+", " ").trim();
    }


    private Double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        try {
            return Double.valueOf(value.trim());
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid number format: '" + value + "'");
        }
    }

    private boolean isValidDTO(CompanyTruckRequestDTO dto) {
        // Skip empty rows (where all fields are null/empty)
        return dto.getDate() != null && 
            dto.getLicensePlate() != null && 
            dto.getLitreQuantity() != null;
    }

    


    private String cleanNumber(String value) {
        if (value == null) return null;

        // Keep only digits, minus sign and dot
        return value
                .replaceAll("[^0-9.-]", "")  // removes km, L, commas, spaces, text
                .trim();
    }


    private Measurement mapToMeasurement(String text) {
        if (text == null) return null;

        text = text.trim().toLowerCase();

        // Khmer mapping
        if (text.contains("ធ្ងន់")) return Measurement.SEVERE;
        if (text.contains("ស្រាល")) return Measurement.MILD;
        if (text.contains("មធ្យម")) return Measurement.MODERATE;
        if (text.contains("ផ្ទេរ")) return Measurement.TRANSFER;

        // English mapping
        if (text.contains("severe")) return Measurement.SEVERE;
        if (text.contains("servere")) return Measurement.SEVERE; // common typo
        if (text.contains("moderate")) return Measurement.MODERATE;
        if (text.contains("mild")) return Measurement.MILD;
        if (text.contains("transfer")) return Measurement.TRANSFER;

        throw new IllegalArgumentException("Unknown measurement: " + text);
    }



    


    public void saveTruckFromExcel(CompanyTruckRequestDTO dto, User user) {
        // Validate required fields
        if (dto.getLicensePlate() == null || dto.getLicensePlate().trim().isEmpty()) {
            throw new RuntimeException("License plate is required");
        }
        
        if (dto.getDate() == null) {
            throw new RuntimeException("Date is required");
        }

        // Find truck
        Truck truck = truckRepository.findByLicensePlate(dto.getLicensePlate().trim())
                .orElseThrow(() -> new RuntimeException("Truck not found: " + dto.getLicensePlate()));

        // Check if record already exists for this truck and date , but commented out to allow duplicates
        // boolean exists = repository.existsByTruckAndDate(truck, dto.getDate());
        // if (exists) {
        //     throw new RuntimeException("Record already exists for this truck and date");
        // }


        String destinationCode = dto.getTotalDestination().trim();
        
        DestinationSetting destinationSetting = destinationSettingRepository.findByName(destinationCode);

        Optional<Destination> optionalDest = destinationRepository.findFirstByDateAndTruckIdAndSettingId(
            dto.getDate(),
            truck.getId(),
            destinationSetting.getId()
        );

        if (optionalDest.isPresent()) {
            Destination destination = optionalDest.get();

            // 🔥 NEW: Check if already completed → throw error
            if (destination.getStatus() == Status.COMPLETED) {
                throw new RuntimeException(
                    "Destination already COMPLETED for date: " + 
                    dto.getDate().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) +
                    ", truck: " + truck.getLicensePlate() +
                    ", destination: " + destinationCode
                );
            }

            // 🔥 Update status (only if not completed)
            destination.setStatus(Status.COMPLETED);
            destinationRepository.save(destination);

        } else {
            // 🔥 No destination found → throw error
            throw new RuntimeException(
                "No destination found for date: " + 
                dto.getDate().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) +
                ", truck: " + truck.getLicensePlate() +
                ", destination: " + destinationCode
            );
        }



        // Set additional data and save
        dto.setTruckId(truck.getId());
        dto.setCreatedBy(user.getId());

        CompanyTruck companyTruck = convertToEntity(dto);
        companyTruck.setCreatedBy(user);
        repository.save(companyTruck);
    }

}