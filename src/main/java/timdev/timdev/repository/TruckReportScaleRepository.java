package timdev.timdev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import timdev.timdev.entity.TruckReport;
import timdev.timdev.entity.TruckReportScale;

@Repository
public interface TruckReportScaleRepository extends JpaRepository<TruckReportScale, Long> {
}