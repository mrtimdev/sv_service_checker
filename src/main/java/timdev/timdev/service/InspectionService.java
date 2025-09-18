package timdev.timdev.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import timdev.timdev.entity.Inspection;
import timdev.timdev.repository.InspectionRepository;

@AllArgsConstructor
@Service
public class InspectionService {

    private final InspectionRepository repo;
    
    
    /** Return all inspections (no paging) */
    public List<Inspection> findAll() {
        return repo.findAll();
    }

    /** Return inspections filtered by truck license plate (no paging) */
    public List<Inspection> findByLicensePlateContaining(String licensePlate) {
        return repo.findByTruck_LicensePlateContainingIgnoreCase(licensePlate);
    }

    /** Return paged inspections filtered by license plate */
    public Page<Inspection> findByLicensePlateContainingWithPageable(String licensePlate, Pageable pageable) {
        return repo.findByTruck_LicensePlateContainingIgnoreCase(licensePlate, pageable);
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

    public void delete(Long id) {
        repo.deleteById(id);
    }

    public List<Inspection> findByTruckId(Long truckId) {
        return repo.findByTruckId(truckId);
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



}
