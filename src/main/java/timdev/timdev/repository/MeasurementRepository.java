package timdev.timdev.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import timdev.timdev.entity.Measurement;



@Repository
public interface MeasurementRepository extends JpaRepository<Measurement, Long> {
    Optional<Measurement> findByName(String name);

    @Query("SELECT m FROM Measurement m WHERE m.id NOT IN " +
       "(SELECT a.measurement.id FROM Average a WHERE a.truck.id = :truckId)")
    List<Measurement> findMeasurementsNotInTruck(@Param("truckId") Long truckId);

    @Query("SELECT m FROM Measurement m ORDER BY m.id DESC")
    List<Measurement> getAllMeasurements();
}