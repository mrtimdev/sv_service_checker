package timdev.timdev.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import timdev.timdev.entity.TruckReport;
import timdev.timdev.entity.Truck;
import timdev.timdev.entity.ScaleStation;

public interface TruckReportRepository extends JpaRepository<TruckReport, Long>, JpaSpecificationExecutor<TruckReport> {

    // Find by Truck
    List<TruckReport> findByTruck(Truck truck);

    // Find by Scale Station
    List<TruckReport> findByScaleStation(ScaleStation scaleStation);

    // Find by Date
    List<TruckReport> findByReportDate(LocalDate reportDate);

    // Find by Date Range
    List<TruckReport> findByReportDateBetween(LocalDate startDate, LocalDate endDate);

    // Find by Truck and Date
    List<TruckReport> findByTruckAndReportDate(Truck truck, LocalDate reportDate);

    // Find by Truck and Date Range
    List<TruckReport> findByTruckAndReportDateBetween(
            Truck truck,
            LocalDate startDate,
            LocalDate endDate);

}