package timdev.timdev.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import timdev.timdev.entity.Truck;
import timdev.timdev.entity.TruckDistance;



@Repository
public interface TruckDistanceRepository extends JpaRepository<TruckDistance, Long> {

    List<TruckDistance> findByTruck(Truck truck);

    List<TruckDistance> findByDate(LocalDate date);

    List<TruckDistance> findByDateBetween(LocalDate start, LocalDate end);

    @Query("""
        SELECT r FROM TruckDistance r
        WHERE (:truckId IS NULL OR r.truck.id = :truckId)
          AND (:fromDate IS NULL OR r.date >= :fromDate)
          AND (:toDate IS NULL OR r.date <= :toDate)
    """)
    Page<TruckDistance> findFiltered(
        @Param("truckId") Long truckId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        Pageable pageable
    );

    @Query("""
        SELECT r FROM TruckDistance r
        WHERE (:truckId IS NULL OR r.truck.id = :truckId)
          AND (:fromDate IS NULL OR r.date >= :fromDate)
          AND (:toDate IS NULL OR r.date <= :toDate)
        """)
    List<TruckDistance> findFiltered(
        @Param("truckId") Long truckId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        Sort sort
    );

    @Query("SELECT COALESCE(SUM(td.distance), 0) FROM TruckDistance td WHERE td.truck.id = :truckId AND td.date <= :selectedDate")
    Double getTotalDistanceFromDate(@Param("truckId") Long truckId, @Param("selectedDate") LocalDate selectedDate);

}
