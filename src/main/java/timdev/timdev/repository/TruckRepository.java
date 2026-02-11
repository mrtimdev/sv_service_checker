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

    // Check by both together
    boolean existsByLicensePlateAndCode(String licensePlate, String code);
    // (Optional) Check by either one
    boolean existsByLicensePlateOrCode(String licensePlate, String code);

    Optional<Truck> findByLicensePlate(String licensePlate);
    boolean existsByLicensePlate(String licensePlate);
    boolean existsByCode(String code);


    List<Truck> findByLicensePlateContainingIgnoreCase(String licensePlate);

    @Query("""
        SELECT t FROM Truck t
        WHERE (:query IS NULL OR LOWER(t.licensePlate) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(t.modelName) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(t.groupName) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(t.yearOfManufacture) LIKE LOWER(CONCAT('%', :query, '%')))
    """)
    List<Truck> advancedFilter(@Param("query") String query);

    Page<Truck> findByLicensePlateContainingIgnoreCase(String licensePlate, Pageable pageable);

    // Find all trucks that require fat and oil
    List<Truck> findByRequiredFatOilTrue();
    
    List<Truck> findByRequiredFatOilFalse();
    
    List<Truck> findByRequiredFatOil(Boolean requiredFatOil);


    // Inspection methods
    List<Truck> findByRequiredInspectionTrue();
    List<Truck> findByRequiredInspectionFalse();
    List<Truck> findByRequiredInspection(Boolean requiredInspection);

}
