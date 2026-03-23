package timdev.timdev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import timdev.timdev.entity.TruckReportPort;

@Repository
public interface TruckReportPortRepository extends JpaRepository<TruckReportPort, Long> {
}