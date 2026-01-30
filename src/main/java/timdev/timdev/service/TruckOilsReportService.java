package timdev.timdev.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import timdev.timdev.entity.TruckOilsReport;
import timdev.timdev.repository.TruckOilsReportRepository;

@AllArgsConstructor
@Service
public class TruckOilsReportService {

    private TruckOilsReportRepository reportRepo;

    public void deleteById(Long id) {
        reportRepo.deleteById(id);
    }

    public TruckOilsReport save(TruckOilsReport report) {
        return reportRepo.save(report);
    }

    public Optional<TruckOilsReport> findById(Long id) {
        return reportRepo.findById(id);
    }

    public List<TruckOilsReport> getAll() {
        return reportRepo.findAll();
    }

    public Page<TruckOilsReport> getAllWithPageable(Pageable pageable) {
        return reportRepo.findAll(pageable);
    }



    public Page<TruckOilsReport> getAllWithPageable(Pageable pageable, Long truckId, LocalDate from, LocalDate to) {
        return reportRepo.findFiltered(truckId, from, to, pageable);
    }

    public List<TruckOilsReport> getAllFiltered(Long truckId, LocalDate from, LocalDate to) {
        return reportRepo.findFiltered(truckId, from, to);
    }



    public List<TruckOilsReport> findTop10ByOrderByDateDesc() {
        return reportRepo.findTop10ByOrderByDateDesc();
    }
}