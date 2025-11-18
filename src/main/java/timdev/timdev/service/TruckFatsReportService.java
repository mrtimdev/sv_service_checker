package timdev.timdev.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import timdev.timdev.entity.TruckFatsReport;
import timdev.timdev.entity.TruckOilsReport;
import timdev.timdev.repository.TruckFatsReportRepository;

@AllArgsConstructor
@Service
public class TruckFatsReportService {

    private TruckFatsReportRepository reportRepo;

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
}