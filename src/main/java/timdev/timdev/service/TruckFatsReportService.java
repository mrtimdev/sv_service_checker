package timdev.timdev.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import timdev.timdev.entity.Truck;
import timdev.timdev.entity.TruckFatsReport;
import timdev.timdev.enums.OilStatus;
import timdev.timdev.repository.TruckFatsReportRepository;

@AllArgsConstructor
@Service
public class TruckFatsReportService {

    @Autowired
    private TruckFatsReportRepository reportRepo;

    @Autowired
    private TruckService truckService;

    public TruckFatsReport save(TruckFatsReport report) {
        return reportRepo.save(report);
    }

    public Optional<TruckFatsReport> findById(Long id) {
        return reportRepo.findById(id);
    }

    public List<TruckFatsReport> getAll() {
        return reportRepo.findAll();
    }

    public Page<TruckFatsReport> getAllWithPageable(Pageable pageable) {
        return reportRepo.findAll(pageable);
    }



    public Page<TruckFatsReport> getAllWithPageable(Pageable pageable, Long truckId, LocalDate from, LocalDate to) {
        return reportRepo.findFiltered(truckId, from, to, pageable);
    }

    public List<TruckFatsReport> getAllFiltered(Long truckId, LocalDate from, LocalDate to) {
        return reportRepo.findFiltered(truckId, from, to);
    }

    public List<TruckFatsReport> findTop10ByOrderByDateDesc() {
        return reportRepo.findTop10ByOrderByDateDesc();
    }

    @Transactional
    public void delete(TruckFatsReport report) {
        reportRepo.delete(report);
    }

    @Transactional
    public void deleteById(Long id) {
        reportRepo.deleteById(id);
    }

    @Transactional
    public void deleteReport(Long reportId) {
        TruckFatsReport existing = reportRepo.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Fats report not found"));
        
        Truck truck = existing.getTruck();
        
        // Update truck's next range
        Double newNextRange = truck.getNextFatsRange() - existing.getDistanceKm();
        if (newNextRange < 0) {
            newNextRange = 0.0;
        }
        truck.setNextFatsRange(newNextRange);
        truck.setStatus(OilStatus.PENDING);
        
        // Save truck first
        truckService.save(truck);
        
        // Then delete the report
        reportRepo.delete(existing);
    }

}