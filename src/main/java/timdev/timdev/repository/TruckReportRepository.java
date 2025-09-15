package timdev.timdev.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import timdev.timdev.entity.Truck;
import timdev.timdev.entity.TruckOilsReport;

@Repository
public interface TruckReportRepository extends JpaRepository<TruckOilsReport, Long> {
    Optional<TruckOilsReport> findTopByTruckOrderByIdDesc(Truck truck);
}
