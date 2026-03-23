package timdev.timdev.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import timdev.timdev.entity.ScaleStation;
import timdev.timdev.entity.Truck;
import timdev.timdev.entity.TruckReport;
import timdev.timdev.repository.TruckReportRepository;

@Service
public class TruckReportService {

    private final TruckReportRepository truckReportRepository;

    public TruckReportService(TruckReportRepository truckReportRepository) {
        this.truckReportRepository = truckReportRepository;
    }

    public TruckReport findById(Long id) {
        return truckReportRepository.findById(id).orElse(null);
    }

    public List<TruckReport> findByTruck(Truck truck) {
        return truckReportRepository.findByTruck(truck);
    }

    public List<TruckReport> findByScaleStation(ScaleStation scaleStation) {
        return truckReportRepository.findByScaleStation(scaleStation);
    }

    public List<TruckReport> findByDate(LocalDate date) {
        return truckReportRepository.findByReportDate(date);
    }

    public List<TruckReport> findByDateRange(LocalDate start, LocalDate end) {
        return truckReportRepository.findByReportDateBetween(start, end);
    }

}