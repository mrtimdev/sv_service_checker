package timdev.timdev.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import timdev.timdev.dto.AssignedVehicleDTO;
import timdev.timdev.dto.ExternalDriverDTO;
import timdev.timdev.dto.ItemNoteDTO;
import timdev.timdev.entity.Driver;
import timdev.timdev.entity.ServiceChecker;
import timdev.timdev.entity.ServiceCheckerItem;
import timdev.timdev.entity.ServiceCheckerItemNote;
import timdev.timdev.entity.User;
import timdev.timdev.enums.ServiceCheckerStatus;
import timdev.timdev.repository.DriverRepository;
import timdev.timdev.repository.InspectionCategoryRepository;
import timdev.timdev.repository.InspectionItemRepository;
import timdev.timdev.repository.ServiceCheckerItemNoteRepository;
import timdev.timdev.repository.ServiceCheckerItemRepository;
import timdev.timdev.repository.ServiceCheckerRepository;


@RequiredArgsConstructor
@Service
public class ServiceCheckerService {
    
    private final ServiceCheckerRepository repository;
    private final DriverRepository driverRepository;
    private final AuthService userService;

    private final ServiceCheckerItemRepository itemRepo;
    private final ServiceCheckerItemNoteRepository noteRepo;

    
    private final InspectionItemRepository inspectionItemRepo;
    private final InspectionCategoryRepository inspectionCategoryRepository;

    private final DriverProxyService driverProxyService;


    public List<ServiceChecker> getByDateAndDriverFilter(Long driverId, String dateFilter, LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();

        // If explicit start and end dates are given, use them directly
        if (startDate != null && endDate != null) {
            return driverId != null
                    ? repository.findByExDriverIdAndDateBetween(driverId, startDate, endDate)
                    : repository.findByDateBetween(startDate, endDate);
        }

        // Apply dateFilter if no explicit range is provided
        LocalDate defaultStartDate;
        LocalDate defaultEndDate = today;

        switch (dateFilter != null ? dateFilter.toLowerCase() : "all") {
            case "today":
                defaultStartDate = today;
                break;
            case "yesterday":
                defaultStartDate = today.minusDays(1);
                defaultEndDate = today.minusDays(1);
                break;
            case "last7days":
                defaultStartDate = today.minusDays(7);
                break;
            case "last30days":
                defaultStartDate = today.minusDays(30);
                break;
            case "all":
            default:
                return driverId != null
                        ? repository.findByExDriverId(driverId)
                        : repository.findAll();
        }

        // Apply filter with driver if present
        return driverId != null
                ? repository.findByExDriverIdAndDateBetween(driverId, defaultStartDate, defaultEndDate)
                : repository.findByDateBetween(defaultStartDate, defaultEndDate);
    }



    public List<ServiceChecker> getByDateFilter(String dateFilter, LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();
        if (startDate != null && endDate != null) {
            return repository.findByDateBetween(startDate, endDate);
        }

        LocalDate defaultStartDate;
        LocalDate defaultEndDate = today;

        switch (dateFilter.toLowerCase()) {
            case "today":
                defaultStartDate = today;
                break;
            case "yesterday":
                defaultStartDate = today.minusDays(1);
                defaultEndDate = today.minusDays(1);
                break;
            case "last7days":
                defaultStartDate = today.minusDays(7);
                break;
            case "last30days":
                defaultStartDate = today.minusDays(30);
                break;
            case "all":
            default:
                return repository.findAll();
        }

        return repository.findByDateBetween(startDate, endDate);
    }


    public ServiceChecker create(ServiceChecker serviceChecker) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        if (serviceChecker.getDriver() != null && serviceChecker.getDriver().getId() != null) {
            Driver driver = driverRepository.findById(serviceChecker.getDriver().getId())
                    .orElseThrow(() -> new RuntimeException("Driver not found"));
            serviceChecker.setDriver(driver);

            User user = userService.getByUsername(username);
            serviceChecker.setCreatedBy(user);

            // Check if already exists for that day
            if (repository.existsByDriverAndDate(driver, serviceChecker.getDate())) {
                throw new RuntimeException("A checklist already exists for this driver on " + serviceChecker.getDate());
            }
        }
        // if (serviceChecker.getItems() != null) {
        //     serviceChecker.getItems().forEach(item -> {
        //         item.setServiceChecker(serviceChecker);
        //         if (item.getNotes() != null) {
        //             item.getNotes().forEach(note -> note.setServiceCheckerItem(item));
        //         }
        //     });
        // }
        return repository.save(serviceChecker);
    }

    public List<ServiceChecker> getAll() {
        return repository.findAll();
    }

    public ServiceChecker getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("ServiceChecker not found"));
    }

    public ServiceChecker update(Long id, ServiceChecker updated) {
        ServiceChecker existing = getById(id);

        existing.setTitle(updated.getTitle());
        existing.setDate(updated.getDate());
        existing.setDriver(updated.getDriver());
        existing.setCreatedBy(updated.getCreatedBy());

        existing.getItems().clear();

        // if (updated.getItems() != null) {
        //     updated.getItems().forEach(item -> {
        //         item.setServiceChecker(existing);
        //         if (item.getNotes() != null) {
        //             item.getNotes().forEach(note -> note.setServiceCheckerItem(item));
        //         }
        //         existing.getItems().add(item);
        //     });
        // }

        return repository.save(existing);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }


    public boolean existsByDriverAndDate(Driver driver, LocalDate date) {
        return repository.existsByDriverAndDate(driver, date);
    }
    public boolean existsByDriverIdAndDate(Long driverId, LocalDate date) {
        return repository.existsByDriverIdAndDate(driverId, date);
    }


    public boolean existsByExDriverIdAndDate(Long exDriverId, LocalDate date) {
        List<ServiceChecker> list = repository.findByDate(date); // query by date only
        return list.stream()
                .map(ServiceChecker::getExDriver)
                .filter(Objects::nonNull)
                .anyMatch(d -> exDriverId.equals(d.getId()));
    }


    public List<ServiceChecker> findByOptionalDatesAndExDriver(LocalDate start, LocalDate end, Long exDriverId) {
        List<ServiceChecker> list = repository.findByOptionalDates(start, end);
        if (exDriverId != null) {
            list = list.stream()
                    .filter(sc -> sc.getExDriver() != null && exDriverId.equals(sc.getExDriver().getId()))
                    .collect(Collectors.toList());
        }
        return list;
    }

    
    public boolean existsByExDriverIdAndDateAndIdNot(Long exDriverId, LocalDate date, Long excludeId) {
        // fetch all ServiceCheckers for the given date, excluding the current ID
        List<ServiceChecker> list = repository.findByDateAndIdNot(date, excludeId);

        // filter in memory by exDriverId
        return list.stream()
                .map(ServiceChecker::getExDriver)
                .filter(Objects::nonNull)
                .anyMatch(d -> exDriverId.equals(d.getId()));
    }



    public boolean existsByDriverAndDateAndIdNot(Driver driver, LocalDate date, Long excludeId) {
        return repository.existsByDriverAndDateAndIdNot(driver, date, excludeId);
    }


    @Transactional
    public void deleteAllById(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {

            throw new IllegalArgumentException("IDs list cannot be null or empty");
        }
        for (Long id : ids) {
            repository.deleteById(id);
        }
    }



    public List<ServiceChecker> getByStatus(ServiceCheckerStatus status) {
        return repository.findByStatus(status);
    }
    
    @Transactional
    public void updateStatus(Long id, ServiceCheckerStatus status) {
        ServiceChecker checker = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("ServiceChecker not found"));
        
        // Add any business logic for status transitions
        if (checker.getStatus() == ServiceCheckerStatus.CHECKED && status == ServiceCheckerStatus.CHECKING) {
            throw new IllegalStateException("Cannot revert from CHECKED to CHECKING");
        }
        
        checker.setStatus(status);
    }
    
    @Transactional
    public void markAsChecked(Long id) {
        ServiceChecker checker = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("ServiceChecker not found"));
        checker.setStatus(ServiceCheckerStatus.CHECKED);
    }


    public boolean existsByDriver(Driver driver) {
        return repository.existsByDriver(driver);
    }



    public Optional<ServiceChecker> getServiceCheckerById(Long id) {
        return repository.findByIdWithDetails(id);
    }

     public ServiceChecker createWithInspections(ServiceChecker checker, 
                                             Map<Long, List<ItemNoteDTO>> categoryItems) {
        
        ServiceChecker saved = repository.save(checker);
        
        categoryItems.forEach((categoryId, itemNotes) -> {
            // Create ServiceCheckerItem for each category
            ServiceCheckerItem item = new ServiceCheckerItem();
            item.setServiceChecker(saved);
            item.setCategory(inspectionCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId)));
            ServiceCheckerItem savedItem = itemRepo.save(item);
            
            // Create notes for each inspection item
            itemNotes.forEach(in -> {
                ServiceCheckerItemNote note = new ServiceCheckerItemNote();
                note.setServiceCheckerItem(savedItem);
                
                // Use inspectionItemRepo instead of itemRepo here
                note.setInspectionItem(inspectionItemRepo.findById(in.getItemId())
                    .orElseThrow(() -> new EntityNotFoundException("InspectionItem not found with id: " + in.getItemId())));
                
                note.setPassed(in.isPassed());
                note.setNote(in.getNote());
                noteRepo.save(note);
            });
        });
        
        return saved;
    }



    // v2
    public ServiceChecker createV2WithExternalDriver(
        ServiceChecker checker,
        Map<Long, List<ItemNoteDTO>> categoryItems
    ) {
        
        ServiceChecker saved = repository.save(checker);
        
        categoryItems.forEach((categoryId, itemNotes) -> {
            // Create ServiceCheckerItem for each category
            ServiceCheckerItem item = new ServiceCheckerItem();
            item.setServiceChecker(saved);
            item.setCategory(inspectionCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId)));
            ServiceCheckerItem savedItem = itemRepo.save(item);
            
            // Create notes for each inspection item
            itemNotes.forEach(in -> {
                ServiceCheckerItemNote note = new ServiceCheckerItemNote();
                note.setServiceCheckerItem(savedItem);
                
                // Use inspectionItemRepo instead of itemRepo here
                note.setInspectionItem(inspectionItemRepo.findById(in.getItemId())
                    .orElseThrow(() -> new EntityNotFoundException("InspectionItem not found with id: " + in.getItemId())));
                
                note.setPassed(in.isPassed());
                note.setNote(in.getNote());
                noteRepo.save(note);
            });
        });
        
        return saved;
    }



    @Transactional
    public ServiceChecker updateWithInspections(Long id, 
                                            ServiceChecker updatedChecker,
                                            Map<Long, List<ItemNoteDTO>> categoryItems) {
        
        // 1. Find existing ServiceChecker
        ServiceChecker existingChecker = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("ServiceChecker not found with id: " + id));
        
        // 2. Update basic ServiceChecker fields
        existingChecker.setDate(updatedChecker.getDate());
        existingChecker.setDriver(updatedChecker.getDriver());
        existingChecker.setExDriver(updatedChecker.getExDriver());
        existingChecker.setUpdatedBy(updatedChecker.getUpdatedBy());
        existingChecker.setUpdatedAt(LocalDateTime.now());
        // Update other fields as needed...
        
        // 3. Process inspection updates
        categoryItems.forEach((categoryId, itemNotes) -> {
            // Find or create ServiceCheckerItem for this category
            ServiceCheckerItem categoryItem = existingChecker.getItems().stream()
                .filter(item -> item.getCategory().getId().equals(categoryId))
                .findFirst()
                .orElseGet(() -> {
                    ServiceCheckerItem newItem = new ServiceCheckerItem();
                    newItem.setServiceChecker(existingChecker);
                    newItem.setCategory(inspectionCategoryRepository.findById(categoryId)
                        .orElseThrow(() -> new EntityNotFoundException("Category not found")));
                    return itemRepo.save(newItem);
                });
            
            // Process each item note
            itemNotes.forEach(itemNote -> {
                // Find existing note or create new one
                ServiceCheckerItemNote note = categoryItem.getNotes().stream()
                    .filter(n -> n.getInspectionItem().getId().equals(itemNote.getItemId()))
                    .findFirst()
                    .orElseGet(() -> {
                        ServiceCheckerItemNote newNote = new ServiceCheckerItemNote();
                        newNote.setServiceCheckerItem(categoryItem);
                        newNote.setInspectionItem(inspectionItemRepo.findById(itemNote.getItemId())
                            .orElseThrow(() -> new EntityNotFoundException("InspectionItem not found")));
                        return newNote;
                    });
                
                // Update note values
                note.setPassed(itemNote.isPassed());
                note.setNote(itemNote.getNote());
                noteRepo.save(note);
            });
            
            // Remove notes for items not in the update
            List<Long> updatedItemIds = itemNotes.stream()
                .map(ItemNoteDTO::getItemId)
                .collect(Collectors.toList());
            
            categoryItem.getNotes().removeIf(note -> 
                !updatedItemIds.contains(note.getInspectionItem().getId()));
            
            itemRepo.save(categoryItem);
        });
        
        // 4. Remove categories not in the update
        List<Long> updatedCategoryIds = new ArrayList<>(categoryItems.keySet());
        existingChecker.getItems().removeIf(item -> 
            !updatedCategoryIds.contains(item.getCategory().getId()));
        
        // 5. Save and return
        return repository.save(existingChecker);
    }



    public Page<ServiceChecker> getServiceCheckers(int page, int limit, LocalDate startDate, LocalDate endDate, Long driverId) {
        Pageable pageable = PageRequest.of(page - 1, limit); // page is 1-based in controller
        return repository.findByFilters(driverId, startDate, endDate, pageable);
    }


    // get and set external driver dto

    public ServiceChecker refreshExDriver(ServiceChecker sc) {
        // Safely get the stored value (could be null)
        ExternalDriverDTO storedExDriver = sc.getExDriver();

        // Safely get the ID (could also be null)
        Long exDriverId = (storedExDriver != null) ? storedExDriver.getId() : null;

        // Call proxy only if we have an ID
        ExternalDriverDTO freshExDriver = null;
        if (exDriverId != null) {
            try {
                freshExDriver = driverProxyService.getDriverById(exDriverId);
            } catch (Exception e) {
                // log and ignore if external service fails
                System.out.println("Cannot fetch driver by id " + exDriverId + ": " + e.getMessage());
            }
        }

        // set the fresh driver in the entity
        sc.setExDriver(freshExDriver);

        // optionally persist the updated entity (if you want it saved immediately)
        return repository.save(sc);
    }






    public List<Map<String, Object>> convertToDataTablesFormat(List<ServiceChecker> data) {
        return data.stream().map(sc -> {
            // Safely get the stored value (could be null)
            ExternalDriverDTO storedExDriver = sc.getExDriver();

            // Safely get the ID (could also be null)
            Long exDriverId = null;
            if (storedExDriver != null) {
                exDriverId = storedExDriver.getId();
            }

            // Call proxy only if we have an ID
            ExternalDriverDTO freshExDriver = null;
            if (exDriverId != null) {
                try {
                    freshExDriver = driverProxyService.getDriverById(exDriverId);
                } catch (Exception e) {
                    // log and ignore if external service fails
                    System.out.println("Cannot fetch driver by id "+ exDriverId + ": " + e.getMessage());
                }
            }
            sc.setExDriver(freshExDriver);
            String driverName = Optional.ofNullable(sc.getExDriver())
                            .map(ExternalDriverDTO::getName)
                            .orElse("Unknown driver");

            String licensePlate = Optional.ofNullable(sc.getExDriver())
                .map(ExternalDriverDTO::getAssignedVehicle)
                .map(AssignedVehicleDTO::getLicensePlate)
                .orElse("Unknown plate");

           String truckType = Optional.ofNullable(sc.getExDriver())
                .map(ExternalDriverDTO::getAssignedVehicle)
                .map(AssignedVehicleDTO::getTruckSize)
                .orElse("Unknown type");

            Map<String, Object> row = new HashMap<>();
            row.put("id", sc.getId());
            row.put("date", sc.getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
            row.put("driverName", driverName);
            row.put("licensePlate", licensePlate);
            row.put("truckType", truckType);
            row.put("checkedCount", sc.getCheckedCount());
            row.put("notCheckedCount", sc.getNotCheckedCount());
            row.put("issuesStatus", sc.issuesStatus());
            row.put("status", sc.getStatus().toString());
            row.put("createdAt", sc.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
            row.put("updatedAt", sc.getUpdatedAt() != null ? 
                sc.getUpdatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) : "Never");
            row.put("updatedBy", sc.getUpdatedBy() != null ? sc.getUpdatedBy().fullName() : "");
            row.put("createdBy", sc.getCreatedBy() != null ? sc.getCreatedBy().fullName() : "");
            row.put("timeAgo", sc.getTimeAgo());
            row.put("hoursSinceEdit", sc.getHoursSinceEdit());
            row.put("editNote", sc.getEditNote());
            row.put("canEdit", sc.canEdit());
            return row;
        }).collect(Collectors.toList());
    }
}
