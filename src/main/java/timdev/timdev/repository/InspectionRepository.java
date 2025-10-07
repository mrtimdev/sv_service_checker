package timdev.timdev.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import timdev.timdev.entity.Inspection;
import timdev.timdev.entity.TruckInspection;

@Repository
public interface InspectionRepository extends JpaRepository<Inspection, Long> {
    List<Inspection> findByTruckInspectionId(Long truckInspectionId);

    List<Inspection> findByTruckInspection_LicensePlateContainingIgnoreCase(String licensePlate);
    Page<Inspection> findByTruckInspection_LicensePlateContainingIgnoreCase(String licensePlate, Pageable pageable);



    @Query("SELECT i FROM Inspection i " +
           "WHERE (:licensePlate IS NULL OR i.truckInspection.licensePlate LIKE %:licensePlate%) " +
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
           "WHERE (:licensePlate IS NULL OR i.truckInspection.licensePlate LIKE %:licensePlate%) " +
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

        Optional<Inspection> findTopByTruckInspectionIdOrderByDateDesc(Long truckInspectionId);

        @Query("SELECT i FROM Inspection i WHERE i.truckInspection = :truckInspection AND (i.expiredDate IS NULL OR i.expiredDate >= :today)")
        Optional<Inspection> findActiveInspectionByTruck(@Param("truckInspection") TruckInspection truckInspection, @Param("today") LocalDate today);


        @Query("SELECT i FROM Inspection i " +
           "WHERE i.truckInspection.id = :truckInspectionId " +
           "AND (:startDate <= i.expiredDate AND :endDate >= i.date)")
        List<Inspection> findOverlappingInspections(
                @Param("truckInspectionId") Long truckInspectionId,
                @Param("startDate") LocalDate startDate,
                @Param("endDate") LocalDate endDate
        );
}
