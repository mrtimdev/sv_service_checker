package timdev.timdev.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import timdev.timdev.entity.Inspection;
import timdev.timdev.entity.TruckInspection;
import timdev.timdev.repository.InspectionRepository;
import timdev.timdev.repository.TruckInspectionRepository;

@AllArgsConstructor
@Service
public class InspectionService {

    private final InspectionRepository repo;
    private final TruckInspectionRepository truckRepo;
    
    
    /** Return all inspections (no paging) */
    public List<Inspection> findAll() {
        return repo.findAll();
    }

    /** Return inspections filtered by truck license plate (no paging) */
    public List<Inspection> findByLicensePlateContaining(String licensePlate) {
        return repo.findByTruckInspection_LicensePlateContainingIgnoreCase(licensePlate);
    }

    /** Return paged inspections filtered by license plate */
    public Page<Inspection> findByLicensePlateContainingWithPageable(String licensePlate, Pageable pageable) {
        return repo.findByTruckInspection_LicensePlateContainingIgnoreCase(licensePlate, pageable);
    }

    /** Return paged inspections (no filter) */
    public Page<Inspection> getAllWithPageable(Pageable pageable) {
        return repo.findAll(pageable);
    }

    public Optional<Inspection> findById(Long id) {
        return repo.findById(id);
    }

    public Inspection save(Inspection ins) {
        return repo.save(ins);
    }

    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }

    @Transactional
    public void deleteInspection(Long inspectionId) {
        Inspection inspection = repo.findById(inspectionId)
                .orElseThrow(() -> new RuntimeException("Inspection not found"));

        TruckInspection truck = inspection.getTruckInspection();

        if (truck != null && truck.getLastInspection() != null
                && truck.getLastInspection().getId().equals(inspectionId)) {
            truck.setLastInspection(null);
            truck.setExpiredDate(null);
            truckRepo.save(truck); // flush before deleting inspection
        }

        repo.delete(inspection);
    }


    public List<Inspection> findByTruckInspectionId(Long truckInspectionId) {
        return repo.findByTruckInspectionId(truckInspectionId);
    }



    // --- Without pagination ---
    public List<Inspection> findAllFiltered(
            String licensePlate,
            LocalDate fromDate,
            LocalDate toDate,
            LocalDate expiredFromDate,
            LocalDate expiredToDate
    ) {
        return repo.findAllFiltered(licensePlate, fromDate, toDate, expiredFromDate, expiredToDate);
    }

    // --- With pagination ---
    public Page<Inspection> findAllFilteredWithPageable(
            String licensePlate,
            LocalDate fromDate,
            LocalDate toDate,
            LocalDate expiredFromDate,
            LocalDate expiredToDate,
            Pageable pageable
    ) {
        return repo.findAllFilteredWithPageable(licensePlate, fromDate, toDate, expiredFromDate, expiredToDate, pageable);
    }



    public boolean isDuplicateInspection(Long truckInspectionId, LocalDate inspectionDate) {
        // Find the latest inspection for this truck
        Optional<Inspection> latestInspection = repo
            .findTopByTruckInspectionIdOrderByDateDesc(truckInspectionId);
        
        if (latestInspection.isEmpty()) {
            return false; // No existing inspections, so not duplicate
        }
        
        Inspection lastInspection = latestInspection.get();
        
        // Check if the last inspection is still valid (not expired)
        if (lastInspection.getExpiredDate() != null && 
            !lastInspection.isExpired()) {
            return true; // Duplicate - truck still has valid inspection
        }
        
        return false; // Last inspection is expired, allow new one
    }

    public boolean hasActiveInspection(TruckInspection truck) {
        Optional<Inspection> existing = repo.findActiveInspectionByTruck(truck, LocalDate.now());
        return existing.isPresent();
    }

    public boolean hasActiveWithAllowMoreNewInspection(TruckInspection truck) {
        Optional<Inspection> existing = repo.findActiveInspectionByTruck(truck, LocalDate.now());
        Inspection inspection = existing.get();
        if (inspection.getExpiredDate() == null) {
            return false; // still active without expired date
        }

        long days = inspection.expiredDurationDays();
        return days > 31; // allow new inspection if the current active one has more than 30 days left
    }

    // ✅ Service wrapper for repository method
    public List<Inspection> findOverlappingInspections(Long truckInspectionId, LocalDate startDate, LocalDate endDate) {
        return repo.findOverlappingInspections(truckInspectionId, startDate, endDate);
    }



}
