package timdev.timdev.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import timdev.timdev.entity.Inspection;

@Repository
public interface InspectionRepository extends JpaRepository<Inspection, Long> {
    List<Inspection> findByTruckId(Long truckId);

    List<Inspection> findByTruck_LicensePlateContainingIgnoreCase(String licensePlate);
    Page<Inspection> findByTruck_LicensePlateContainingIgnoreCase(String licensePlate, Pageable pageable);



    @Query("SELECT i FROM Inspection i " +
           "WHERE (:licensePlate IS NULL OR i.truck.licensePlate LIKE %:licensePlate%) " +
           "AND (:fromDate IS NULL OR i.date >= :fromDate) " +
           "AND (:toDate IS NULL OR i.date <= :toDate) " +
           "AND (:expiredFromDate IS NULL OR i.expiredDate >= :expiredFromDate) " +
           "AND (:expiredToDate IS NULL OR i.expiredDate <= :expiredToDate) " +
           "ORDER BY i.expiredDate ASC")
    List<Inspection> findAllFiltered(
            @Param("licensePlate") String licensePlate,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("expiredFromDate") LocalDate expiredFromDate,
            @Param("expiredToDate") LocalDate expiredToDate
    );

    // --- List with pagination ---
    @Query("SELECT i FROM Inspection i " +
           "WHERE (:licensePlate IS NULL OR i.truck.licensePlate LIKE %:licensePlate%) " +
           "AND (:fromDate IS NULL OR i.date >= :fromDate) " +
           "AND (:toDate IS NULL OR i.date <= :toDate) " +
           "AND (:expiredFromDate IS NULL OR i.expiredDate >= :expiredFromDate) " +
           "AND (:expiredToDate IS NULL OR i.expiredDate <= :expiredToDate) " +
           "ORDER BY i.expiredDate ASC")
    Page<Inspection> findAllFilteredWithPageable(
            @Param("licensePlate") String licensePlate,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("expiredFromDate") LocalDate expiredFromDate,
            @Param("expiredToDate") LocalDate expiredToDate,
            Pageable pageable
    );
}
