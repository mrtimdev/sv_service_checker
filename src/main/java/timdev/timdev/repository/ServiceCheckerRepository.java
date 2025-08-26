package timdev.timdev.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;
import timdev.timdev.entity.Driver;
import timdev.timdev.entity.ServiceChecker;
import timdev.timdev.enums.ServiceCheckerStatus;

public interface ServiceCheckerRepository extends JpaRepository<ServiceChecker, Long> {
    
    List<ServiceChecker> findByDriverId(Long driverId);
    List<ServiceChecker> findByDateBetween(LocalDate startDate, LocalDate endDate);
    List<ServiceChecker> findByDateBetweenAndDriverId(LocalDate startDate, LocalDate endDate, Long driverId);

    @Query("SELECT sc FROM ServiceChecker sc " +
       "WHERE (:start IS NULL OR sc.date >= :start) " +
       "AND (:end IS NULL OR sc.date <= :end) " +
       "AND (:driverId IS NULL OR sc.driver.id = :driverId)")
    List<ServiceChecker> findByOptionalDatesAndDriver(
        @Param("start") LocalDate start,
        @Param("end") LocalDate end,
        @Param("driverId") Long driverId);



    List<ServiceChecker> findByIdNot(Long id);
    boolean existsByDriverAndDate(Driver driver, LocalDate date);

    boolean existsByDriverAndDateAndIdNot(Driver driver, LocalDate date, Long id);


    @Query("SELECT sc FROM ServiceChecker sc " +
           "LEFT JOIN FETCH sc.driver " +
           "LEFT JOIN FETCH sc.items i " +
           "LEFT JOIN FETCH i.category " +
           "LEFT JOIN FETCH i.notes n " +
           "LEFT JOIN FETCH n.inspectionItem " +
           "WHERE sc.id = :id")
    Optional<ServiceChecker> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT s FROM ServiceChecker s " +
           "WHERE (:driverId IS NULL OR s.driver.id = :driverId) " +
           "AND (:startDate IS NULL OR s.date >= :startDate) " +
           "AND (:endDate IS NULL OR s.date <= :endDate)")
    Page<ServiceChecker> findByFilters(Long driverId, LocalDate startDate, LocalDate endDate, Pageable pageable);



    boolean existsByDriver(Driver driver);


    @Modifying
    @Transactional
    @Query("DELETE FROM ServiceChecker s WHERE s.id IN :ids")
    void deleteAllById(@Param("ids") List<Long> ids);


    List<ServiceChecker> findByStatus(ServiceCheckerStatus status);

    @Modifying
    @Query("UPDATE ServiceChecker s SET s.status = :status WHERE s.id IN :ids")
    int updateStatusByIds(@Param("ids") List<Long> ids, @Param("status") ServiceCheckerStatus status);
}