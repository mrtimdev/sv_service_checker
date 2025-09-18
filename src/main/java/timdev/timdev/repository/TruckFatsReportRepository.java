package timdev.timdev.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import timdev.timdev.entity.TruckFatsReport;

public interface TruckFatsReportRepository extends JpaRepository<TruckFatsReport, Long> {



    @Query("""
        SELECT r FROM TruckFatsReport r
        WHERE (:truckId IS NULL OR r.truck.id = :truckId)
          AND (:fromDate IS NULL OR r.date >= :fromDate)
          AND (:toDate IS NULL OR r.date <= :toDate)
        """)
    Page<TruckFatsReport> findFiltered(
        @Param("truckId") Long truckId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        Pageable pageable
    );

    @Query("""
        SELECT r FROM TruckFatsReport r
        WHERE (:truckId IS NULL OR r.truck.id = :truckId)
          AND (:fromDate IS NULL OR r.date >= :fromDate)
          AND (:toDate IS NULL OR r.date <= :toDate)
        """)
    List<TruckFatsReport> findFiltered(
        @Param("truckId") Long truckId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    List<TruckFatsReport> findTop10ByOrderByDateDesc();

    
}