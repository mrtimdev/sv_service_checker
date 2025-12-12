package timdev.timdev.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
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
        existing.setMeasurement(dto.getMeasurement());
        existing.setLitreQuantity(dto.getLitreQuantity());
        existing.setNote(dto.getNote());
        existing.setOtherOils(dto.getOtherOils());
        existing.setTotalOilsChange(dto.getTotalOilsChange());
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(userUpdate != null ? userUpdate : null);
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


    public List<CompanyTruckRequestDTO> readExcel(MultipartFile file) {
        List<CompanyTruckRequestDTO> dataList = new ArrayList<>();
        
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // Start from row 3 (0-based index, so row 5 = Excel row 6)
            for (int rowNum = 3; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null) continue; // Skip empty rows
                
                CompanyTruckRequestDTO dto = new CompanyTruckRequestDTO();
                
                try {
                    // Read cells from column A to K (0 to 10)
                    for (int colNum = 0; colNum <= 10; colNum++) {
                        Cell cell = row.getCell(colNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        setCellValueToDTO(dto, colNum, cell);
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

    private void setCellValueToDTO(CompanyTruckRequestDTO dto, int columnIndex, Cell cell) {
        DataFormatter dataFormatter = new DataFormatter();
        String cellValue = dataFormatter.formatCellValue(cell).trim();
        
        switch (columnIndex) {
            case 0: // Date (A column) - "01-Oct-2025"
                dto.setDate(parseDate(cellValue));
                break;
            case 1: // License Plate (B column) - "3E-2637" - Store for later truck lookup
                dto.setLicensePlate(cellValue);
                break;
            case 2: // Truck Type (C column) - "CY-Big Truck" - Can be used for validation
                // This field might not be needed in DTO since you have truckId
                break;
            case 3: // Destination (D column) - "ការ៉ាស់ថ្មី - KHB - KKG3 - ការ៉ាស់ថ្មី"
                dto.setTotalDestination(cellValue);
                break;
            case 4: // Total KM (E column) - "km604"
                dto.setTotalKm(parseKm(cellValue));
                break;
            case 5: // Average (F column) - "0.36"
                dto.setAverage(parseDouble(cellValue));
                break;
            case 6: // Fuel Level (G column) - "កម្រិតធ្ងន់" - Map to Measurement enum
                dto.setMeasurement(mapToMeasurement(cellValue));
                break;
            case 7: // Fuel Quantity (H column) - "L217" - This is litreQuantity
                dto.setLitreQuantity(parseLitre(cellValue));
                break;
            case 8: // Other Fuel (I column) - Could be otherOils
                dto.setOtherOils(parseLitreString(cellValue)); // Store as string with L
                break;
            case 9: // Total Fuel (J column) - "L217" - Could be totalOilsChange
                dto.setTotalOilsChange(parseLitre(cellValue));
                break;
            case 10: // Other (K column) - This could be note
                dto.setNote(cellValue);
                break;
        }
    }

    // Helper methods for parsing
    private LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Try different date formats including your format "01-Oct-2025"
            DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("dd-MMM-yyyy", new Locale("en")),
                DateTimeFormatter.ofPattern("dd-MMM-yy", new Locale("en")),   // 🔥 This fixes "21-Oct-25"
                DateTimeFormatter.ofPattern("d-MMM-yy", new Locale("en")),   // 🔥 This fixes "4-Sep-25"
                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("dd/MM/yy"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy")
            };
            
            for (DateTimeFormatter formatter : formatters) {
                try {
                    return LocalDate.parse(dateString, formatter);
                } catch (DateTimeParseException e) {
                    // Try next format
                }
            }
            
            throw new RuntimeException("Invalid date format: " + dateString);
        } catch (Exception e) {
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

            return Double.parseDouble(cleaned);
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

            return Double.parseDouble(cleaned);
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