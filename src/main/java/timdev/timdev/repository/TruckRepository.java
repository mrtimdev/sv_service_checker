package timdev.timdev.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import timdev.timdev.entity.Truck;




@Repository
public interface TruckRepository extends JpaRepository<Truck, Long> {

  
    Optional<Truck> findByLicensePlate(String licensePlate);
    boolean existsByLicensePlate(String licensePlate);


    List<Truck> findByLicensePlateContainingIgnoreCase(String licensePlate);

    Page<Truck> findByLicensePlateContainingIgnoreCase(String licensePlate, Pageable pageable);

    @Query("""
        SELECT t FROM Truck t
        WHERE t.id NOT IN (
            SELECT a.truck.id FROM Average a WHERE (:truckId IS NULL OR a.truck.id != :truckId)
        )
        """)
    List<Truck> findTrucksWithoutAverages(
            @Param("truckId") Long truckId
    );


    
    // Or if you want trucks without averages for specific measurements:
    @Query("SELECT t FROM Truck t WHERE t.id NOT IN " +
        "(SELECT a.truck.id FROM Average a WHERE a.measurement.id = :measurementId)")
    List<Truck> findTrucksWithoutAverageForMeasurement(@Param("measurementId") Long measurementId);
    

}
