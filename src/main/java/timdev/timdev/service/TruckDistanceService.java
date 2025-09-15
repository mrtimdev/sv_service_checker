package timdev.timdev.service;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import timdev.timdev.entity.Truck;
import timdev.timdev.entity.TruckDistance;
import timdev.timdev.entity.User;
import timdev.timdev.repository.TruckDistanceRepository;

@AllArgsConstructor
@Service
public class TruckDistanceService {


    
    private final TruckDistanceRepository truckDistanceRepo;
    private final UserService userService;
    // ✅ Get all
    public List<TruckDistance> getAll() {
        return truckDistanceRepo.findAll();
    }

    public Page<TruckDistance> getAllPaged(Pageable pageable) {
        return truckDistanceRepo.findAll(pageable);
    }

    // ✅ Find by ID
    public Optional<TruckDistance> findById(Long id) {
        return truckDistanceRepo.findById(id);
    }

    // ✅ Find by Truck
    public List<TruckDistance> findByTruck(Truck truck) {
        return truckDistanceRepo.findByTruck(truck);
    }

    // ✅ Find by Date
    public List<TruckDistance> findByDate(LocalDate date) {
        return truckDistanceRepo.findByDate(date);
    }

    // ✅ Find by Date Range
    public List<TruckDistance> findByDateRange(LocalDate start, LocalDate end) {
        return truckDistanceRepo.findByDateBetween(start, end);
    }

    public List<TruckDistance> findAllById(List<Long> ids) {
        return truckDistanceRepo.findAllById(ids);
    }


    // ✅ Save (create or update)
    @Transactional
    public TruckDistance save(TruckDistance truckDistance) {
        User currentUser = userService.getCurrentUser();

        if (truckDistance.getId() == null) {
            // New record
            truckDistance.setCreatedBy(currentUser);
            truckDistance.setUpdatedBy(currentUser);
        } else {
            // Existing record being updated
            truckDistance.setUpdatedBy(currentUser);
        }

        return truckDistanceRepo.save(truckDistance);
    }

    @Transactional
    public void delete(Long id) {
        truckDistanceRepo.deleteById(id);
    }


    @Transactional
    public List<TruckDistance> saveAll(List<TruckDistance> truckDistances) {
        return truckDistanceRepo.saveAll(truckDistances);
    }




    public List<TruckDistance> importFromExcel(MultipartFile file, TruckService truckService) throws Exception {
        List<TruckDistance> distances = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);

            // Skip header row (row 0)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                TruckDistance td = new TruckDistance();

                // Date column (index 1)
                Cell dateCell = row.getCell(1);
                if (dateCell != null) {
                    try {
                        LocalDate importDate;
                        
                        if (dateCell.getCellType() == CellType.NUMERIC) {
                            // Excel stores dates as numeric values
                            importDate = dateCell.getLocalDateTimeCellValue().toLocalDate();
                        } else {
                            String dateStr = dateCell.getStringCellValue().trim();
                            // Try multiple date formats
                            DateTimeFormatter[] formatters = {
                                DateTimeFormatter.ofPattern("dd-MMM-yy"),
                                DateTimeFormatter.ofPattern("dd/MM/yy"),
                                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                                DateTimeFormatter.ofPattern("MM/dd/yyyy")
                            };
                            
                            for (DateTimeFormatter formatter : formatters) {
                                try {
                                    importDate = LocalDate.parse(dateStr, formatter);
                                    break;
                                } catch (DateTimeParseException e) {
                                    // Try next format
                                }
                            }
                            throw new RuntimeException("Unsupported date format: " + dateStr);
                        }
                        
                        td.setDate(importDate);
                    } catch (Exception e) {
                        throw new RuntimeException("Error parsing date at row " + (i + 1) + ": " + e.getMessage());
                    }
                }

                                // Truck Number column (index 2)
                Cell truckCell = row.getCell(2);
                if (truckCell != null) {
                    String truckNumber;
                    if (truckCell.getCellType() == CellType.NUMERIC) {
                        truckNumber = String.valueOf((int) truckCell.getNumericCellValue());
                    } else {
                        truckNumber = truckCell.getStringCellValue().trim();
                    }
                    Truck truck = truckService.getByLicensePlate(truckNumber).orElse(null);
                    if (truck != null) {
                        td.setTruck(truck);
                    } else {
                        throw new RuntimeException("Truck not found: " + truckNumber + " at row " + (i + 1));
                    }
                }

                // Distance column (index 3)
                Cell distanceCell = row.getCell(3);
                if (distanceCell != null) {
                    try {
                        double distance;
                        if (distanceCell.getCellType() == CellType.NUMERIC) {
                            distance = distanceCell.getNumericCellValue();
                        } else {
                            String distStr = distanceCell.getStringCellValue().trim();
                            // Extract numbers from string like "km33", " km16 ", etc.
                            distStr = distStr.replaceAll("[^0-9.]", ""); // Remove non-numeric characters except decimal point
                            if (distStr.isEmpty()) {
                                distance = 0.0;
                            } else {
                                distance = Double.parseDouble(distStr);
                            }
                        }
                        td.setDistance(distance);
                    } catch (Exception e) {
                        throw new RuntimeException("Error parsing distance at row " + (i + 1) + ": " + e.getMessage());
                    }
                }

                distances.add(td);
            }
        }

        return distances;
    }
}
