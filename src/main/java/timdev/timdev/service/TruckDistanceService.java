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
import org.springframework.data.domain.Sort;
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
        User currentUser = userService.getCurrentUser();
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
                    } catch (RuntimeException e) {
                        throw new RuntimeException("Error parsing date at row " + (i) + ": " + e.getMessage());
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
                        throw new RuntimeException("Truck not found: " + truckNumber + " at row " + (i));
                    }
                }

                // Distance column (index 3)
                Cell distanceCell = row.getCell(3);
                try {
                    double distance;
                    
                    if (distanceCell == null) {
                        throw new RuntimeException("Distance cell is null at row " + (i));
                    }
                    
                    if (distanceCell.getCellType() == CellType.NUMERIC) {
                        distance = distanceCell.getNumericCellValue();
                    } else {
                        String distStr = distanceCell.getStringCellValue().trim();
                        distStr = distStr.replaceAll("[^0-9.]", "");
                        if (distStr.isEmpty()) {
                            throw new RuntimeException("Empty distance value at row " + (i));
                        } else {
                            distance = Double.parseDouble(distStr);
                        }
                    }
                    
                    if (distance <= 0) {
                        throw new RuntimeException(td.getTruck().getLicensePlate() + " Distance must be greater than 0 at row " + i + ": " + distance);
                    }
                    
                    td.setDistance(distance);
                    td.setCreatedBy(currentUser);
                    distances.add(td); 
                    
                } catch (RuntimeException e) {
                    throw new RuntimeException(td.getTruck().getLicensePlate() + " Error parsing distance at row " + (i) + ": " + e.getMessage());
                }
                
                // td.setCreatedBy(currentUser);

                // distances.add(td);
            }
        }

        return distances;
    }


    public Page<TruckDistance> getAllWithPageable(Pageable pageable, Long truckId, LocalDate from, LocalDate to) {
        return truckDistanceRepo.findFiltered(truckId, from, to, pageable);
    }
    public List<TruckDistance> getAllFiltered(Long truckId, LocalDate from, LocalDate to, Sort sort) {
        return truckDistanceRepo.findFiltered(truckId, from, to, sort);
    }
}
