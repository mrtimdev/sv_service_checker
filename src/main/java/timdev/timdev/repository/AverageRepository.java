package timdev.timdev.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import timdev.timdev.entity.Average;
import timdev.timdev.entity.Measurement;

@Repository
public interface AverageRepository extends JpaRepository<Average, Long> {
    Optional<Measurement> findByValue(double value);


    Average findByMeasurementId(Long measurementId);
    Average findByValueAndMeasurementId(double value, Long measurementId);

    boolean existsByTruckIdAndMeasurementId(Long truckId, Long measurementId);

    boolean existsByTruckIdAndMeasurementIdAndIdNot(Long truckId, Long measurementId, Long id);


    // Find all averages for a truck
    List<Average> findByTruckId(Long truckId);

    // Find with paging
    Page<Average> findByTruckId(Long truckId, Pageable pageable);

    // Optional: find by truck + measurement
    Optional<Average> findByTruckIdAndMeasurementId(Long truckId, Long measurementId);


    @Query("SELECT a.measurement FROM Average a WHERE a.truck.id = :truckId")
    List<Measurement> findMeasurementsByTruckId(Long truckId);


    @Query("SELECT a FROM Average a WHERE (:truckId IS NULL OR a.truck.id = :truckId) " +
           "ORDER BY a.createdAt DESC")
    Page<Average> findByTruckWithFilter(@Param("truckId") Long truckId, Pageable pageable);
    
    List<Average> findByTruckIdOrderByCreatedAtDesc(Long truckId);
    
    List<Average> findAllByOrderByCreatedAtDesc();

    @Query("SELECT a FROM Average a WHERE a.truck.id = :truckId AND a.measurement.id = :measurementId")
    Optional<Average> findAverageByTruckAndMeasurement(@Param("truckId") Long truckId,
                                                       @Param("measurementId") Long measurementId);

}
