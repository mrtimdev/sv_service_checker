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

import timdev.timdev.dto.CompanySmallTruckRequestDTO;
import timdev.timdev.dto.Measurement;
import timdev.timdev.dto.Status;
import timdev.timdev.entity.CompanySmallTruck;
import timdev.timdev.entity.Destination;
import timdev.timdev.entity.DestinationSetting;
import timdev.timdev.entity.Truck;
import timdev.timdev.entity.User;
import timdev.timdev.enums.ApprovalStatus;
import timdev.timdev.repository.CompanySmallTruckRepository;
import timdev.timdev.repository.DestinationRepository;
import timdev.timdev.repository.DestinationSettingRepository;
import timdev.timdev.repository.TruckRepository;
import timdev.timdev.repository.UserRepository;


@Service
public class CompanySmallTruckService {

    @Autowired
    private CompanySmallTruckRepository repository;
    @Autowired
    private TruckRepository truckRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DestinationRepository destinationRepository;
    
    @Autowired
    private DestinationSettingRepository destinationSettingRepository;

    public List<CompanySmallTruck> getAllTrucks() {
        return repository.findAll();
    }

    public CompanySmallTruck getTruckById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public CompanySmallTruck saveTruck(CompanySmallTruck truck) {
        return repository.save(truck);
    }

    public CompanySmallTruck updateTruck(Long id, CompanySmallTruck updatedTruck) {
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


    public CompanySmallTruck createTruck(CompanySmallTruckRequestDTO dto) {
        CompanySmallTruck truck = convertToEntity(dto);
        truck.setCreatedAt(LocalDateTime.now());
        truck.setOtherOils(dto.getOtherOils());
        truck.setTotalOilsChange(dto.getTotalOilsChange());
        return repository.save(truck);
    }

    public CompanySmallTruck updateTruck(Long id, CompanySmallTruckRequestDTO dto) {
        CompanySmallTruck existing = getTruckById(id);
        User userUpdate = userRepository.findById(dto.getUpdatedBy()).orElse(null);
        existing.setDate(dto.getDate());
        existing.setTruck(truckRepository.findById(dto.getTruckId()).orElseThrow());
        existing.setTotalDestination(dto.getTotalDestination());
        existing.setTotalKm(dto.getTotalKm());
        existing.setAverage(dto.getAverage());
        existing.setMeasurement(dto.getMeasurement());
        existing.setLitreQuantity(dto.getLitreQuantity());
        existing.setNote(dto.getNote());
        existing.setOtherOils(dto.getOtherOils());
        existing.setTotalOilsChange(dto.getTotalOilsChange());
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(userUpdate != null ? userUpdate : null);
        return repository.save(existing);
    }

    public CompanySmallTruck convertToEntity(CompanySmallTruckRequestDTO dto) {
        CompanySmallTruck truck = new CompanySmallTruck();

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

    public CompanySmallTruckRequestDTO convertToDto(CompanySmallTruck entity) {
        CompanySmallTruckRequestDTO dto = new CompanySmallTruckRequestDTO();
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
    public List<CompanySmallTruck> getAll() {
        return repository.findAll();
    }

    public List<CompanySmallTruck> getAllWIthSort(Sort sort) {
        return repository.findAll(sort);
    }


    public List<CompanySmallTruck> findByLicensePlateContaining(String licensePlate, Sort sort) {
        return repository.findByTruck_LicensePlateContaining(licensePlate, sort);
    }

    public Page<CompanySmallTruck> findByFilterQueriesPage(LocalDate startDate, LocalDate endDate, String query, Pageable pageable) {

        return repository.findByFilterQueriesPage(startDate, endDate, query, pageable);
    }

     public Page<CompanySmallTruck> findByFilterQueriesPageAndStatus(LocalDate startDate, LocalDate endDate, String query, Pageable pageable, Status status) {

        return repository.findByFilterQueriesPageAndStatus(startDate, endDate, query, pageable, status);
    }

    public List<CompanySmallTruck> findByFilterQueriesList(LocalDate startDate, LocalDate endDate, String query, Pageable pageable) {

        return repository.findByFilterQueriesList(startDate, endDate, query, pageable);
    }

    public List<CompanySmallTruck> findByFilterQueriesListAndSort(LocalDate startDate, LocalDate endDate, String query, Sort sort) {

        return repository.findByFilterQueriesListAndSort(startDate, endDate, query, sort);
    }

    public List<CompanySmallTruck> findByFilterQueriesListAndSortAndStatus(LocalDate startDate, LocalDate endDate, String query, Sort sort, Status status) {

        return repository.findByFilterQueriesListAndSortAndStatus(startDate, endDate, query, sort, status);
    }


    // Fetch all with pagination
    public Page<CompanySmallTruck> getAllWithPageable(Pageable pageable) {
        return repository.findAll(pageable);
    }
    

    // Filter by license plate (non-paginated)
    public List<CompanySmallTruck> findByLicensePlateContaining(String licensePlate) {
        return repository.findByTruck_LicensePlateContainingIgnoreCase(licensePlate);
    }

    
    public List<CompanySmallTruck> findByStatus(Status status) {
        return repository.findByStatus(status);
    }

    
    public List<CompanySmallTruck> findByLicensePlateAndStatus(String licensePlate, Status status) {
        return repository
                .findByLicensePlateContainingAndStatus(licensePlate, status);
    }

    
    public Page<CompanySmallTruck> findByStatusWithPageable(Status status, Pageable pageable) {
        return repository.findByStatus(status, pageable);
    }

    
    public Page<CompanySmallTruck> findByLicensePlateAndStatusWithPageable(
            String licensePlate, Status status, Pageable pageable) {

        return repository
                .findByLicensePlateContainingAndStatus(licensePlate, status, pageable);
    }

    public Page<CompanySmallTruck> findByLicensePlateAndStatusInWithPageable(
            String licensePlate, List<Status> statuses, Pageable pageable) {

        return repository.findByLicensePlateContainingAndStatusIn(
                licensePlate, statuses, pageable
        );
    }


    



    // Filter by license plate (paginated)
    public Page<CompanySmallTruck> findByLicensePlateContainingWithPageable(String licensePlate, Pageable pageable) {
        return repository.findByTruck_LicensePlateContainingIgnoreCase(licensePlate, pageable);
    }

    public Page<CompanySmallTruck> findByTruckLicensePlateWithTruckWithPageable(String licensePlate, Pageable pageable) {
        return repository.findByTruckLicensePlateWithTruck(licensePlate, pageable);
    }

    public boolean isExistsByTruckAndDate(Truck companySmallTruck, LocalDate date) {
        boolean exists = repository.existsByTruckAndDate(companySmallTruck, date);
        if (exists) {
            return true;
        }
        return false;
    }

    public CompanySmallTruck findByTruckAndDate(Truck truck, LocalDate date) {
        return repository.findByTruckAndDate(truck, date).orElse(null);
    }


    public Optional<CompanySmallTruck> findById(Long id) {
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





    public List<CompanySmallTruckRequestDTO> readExcel(MultipartFile file) {
        List<CompanySmallTruckRequestDTO> dataList = new ArrayList<>();
        
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int rowNum = 3; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null) continue; 
                
                CompanySmallTruckRequestDTO dto = new CompanySmallTruckRequestDTO();
                
                try {
                    for (int colNum = 0; colNum <= 7; colNum++) {
                        Cell cell = row.getCell(colNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        setCellValueToDTO(dto, colNum, cell);
                    }
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


    private void setCellValueToDTO(CompanySmallTruckRequestDTO dto, int columnIndex, Cell cell) {
        DataFormatter dataFormatter = new DataFormatter();
        String cellValue = dataFormatter.formatCellValue(cell).trim();
        
        switch (columnIndex) {
            case 0 -> 
                dto.setDate(parseDate(cellValue));
            case 1 -> 
                dto.setLicensePlate(cellValue);
            case 2 -> {
                dto.setSizeOfTruck(cellValue);
            }
            case 3 -> 
                dto.setTotalDestination(cellValue);
            case 4 -> 
                dto.setTotalKm(parseKm(cellValue));
            case 5 -> 
                dto.setMeasurement(cellValue);
            case 6 ->
                dto.setLitreQuantity(parseLitre(cellValue));
            case 7 -> 
                dto.setTotalOilsChange(parseLitre(cellValue));
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

    private boolean isValidDTO(CompanySmallTruckRequestDTO dto) {
        return dto.getDate() != null && 
            dto.getLicensePlate() != null && 
            dto.getTotalOilsChange()!= null && dto.getTotalDestination() != null;
    }

    


    private String cleanNumber(String value) {
        if (value == null) return null;

        // Keep only digits, minus sign and dot
        return value
                .replaceAll("[^0-9.-]", "")  // removes km, L, commas, spaces, text
                .trim();
    }

    public void saveTruckFromExcel(CompanySmallTruckRequestDTO dto, User user) {
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
        String destinationCode = dto.getTotalDestination().trim();

        boolean isDuplicate = isDuplicate(
            dto.getDate(),
            truck.getId(),
            destinationCode,
            null
        );

        if (isDuplicate) {
            throw new RuntimeException(
                "Duplicate entry for date: " + 
                dto.getDate().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) +
                ", truck: " + truck.getLicensePlate() +
                ", destination: " + destinationCode
            );
        }

        
        // Set additional data and save
        dto.setTruckId(truck.getId());
        dto.setCreatedBy(user.getId());

        CompanySmallTruck companySmallTruck = convertToEntity(dto);
        companySmallTruck.setCreatedBy(user);
        repository.save(companySmallTruck);
    }

    public boolean isDuplicate(
            LocalDate date,
            Long truckId,
            String totalDestination,
            Long excludeId
    ) {
        return repository.existsByDateAndTruck_IdAndTotalDestinationAndIdNot(date, truckId, totalDestination, excludeId);
    }

    public List<CompanySmallTruck> searchPending(String query, Long destinationId) {
        return repository.searchPending(Status.PENDING, query, destinationId);
    }

}