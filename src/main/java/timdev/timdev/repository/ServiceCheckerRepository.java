package timdev.timdev.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

      @Query(value = "SELECT * FROM service_checkers sc WHERE sc.ex_driver_id = :exDriverId", nativeQuery = true)
      List<ServiceChecker> findByExDriverId(@Param("exDriverId") Long exDriverId);

      List<ServiceChecker> findByDriverId(Long driverId);

      List<ServiceChecker> findByDate(LocalDate startDate);

      @Query("SELECT sc FROM ServiceChecker sc WHERE sc.date BETWEEN :startDate AND :endDate AND sc.status != :excludedStatus")
      List<ServiceChecker> findByDateBetween(
                  @Param("startDate") LocalDate startDate,
                  @Param("endDate") LocalDate endDate,
                  @Param("excludedStatus") ServiceCheckerStatus excludedStatus);

      @Query(value = "SELECT * FROM service_checkers sc " +
                  "WHERE sc.ex_driver_id = :exDriverId " +
                  "AND sc.date BETWEEN :start AND :end", nativeQuery = true)
      List<ServiceChecker> findByExDriverIdAndDateBetween(@Param("exDriverId") Long exDriverId,
                  @Param("start") LocalDate start,
                  @Param("end") LocalDate end);

      List<ServiceChecker> findByDateBetweenAndDriverId(LocalDate startDate, LocalDate endDate, Long driverId);

      @Query("SELECT sc FROM ServiceChecker sc " +
                  "WHERE (:start IS NULL OR sc.date >= :start) " +
                  "AND (:end IS NULL OR sc.date <= :end) " +
                  "AND (:driverId IS NULL OR sc.driver.id = :driverId)")
      List<ServiceChecker> findByOptionalDatesAndDriver(
                  @Param("start") LocalDate start,
                  @Param("end") LocalDate end,
                  @Param("driverId") Long driverId);

      @Query("SELECT sc FROM ServiceChecker sc " +
                  "WHERE (:start IS NULL OR sc.date >= :start) " +
                  "AND (:end IS NULL OR sc.date <= :end)")
      List<ServiceChecker> findByOptionalDates(@Param("start") LocalDate start,
                  @Param("end") LocalDate end);

      List<ServiceChecker> findByIdNot(Long id);

      boolean existsByDriverAndDate(Driver driver, LocalDate date);

      boolean existsByDriverIdAndDate(Long driverId, LocalDate date);

      @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END " +
                  "FROM service_checkers sc " +
                  "WHERE sc.ex_driver_id = :exDriverId " +
                  "AND sc.date = :date", nativeQuery = true)
      boolean existsByExDriverIdAndDate(@Param("exDriverId") Long exDriverId,
                  @Param("date") LocalDate date);

      boolean existsByDriverAndDateAndIdNot(Driver driver, LocalDate date, Long id);

      @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END " +
                  "FROM service_checkers sc " +
                  "WHERE sc.ex_driver_id = :exDriverId " +
                  "AND sc.date = :date " +
                  "AND sc.id <> :id", nativeQuery = true)
      boolean existsByExDriverAndDateAndIdNot(@Param("exDriverId") Long exDriverId,
                  @Param("date") LocalDate date,
                  @Param("id") Long id);

      @Query("SELECT sc FROM ServiceChecker sc " +
                  "WHERE sc.date = :date AND sc.id <> :excludeId")
      List<ServiceChecker> findByDateAndIdNot(@Param("date") LocalDate date,
                  @Param("excludeId") Long excludeId);

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

      @Query("SELECT s FROM ServiceChecker s " +
                  "WHERE (:deviceId IS NULL OR s.deviceId = :deviceId) " +
                  "AND (:startDate IS NULL OR s.date >= :startDate) " +
                  "AND (:endDate IS NULL OR s.date <= :endDate)" +
                  "AND s.status != 'CANCELLED'")
      Page<ServiceChecker> findByDeviceIdFilters(
                  String deviceId,
                  LocalDate startDate,
                  LocalDate endDate,
                  Pageable pageable);

      boolean existsByLicensePlateAndDate(String licensePlate, LocalDate date);

      boolean existsByDriver(Driver driver);

      @Modifying
      @Transactional
      @Query("DELETE FROM ServiceChecker s WHERE s.id IN :ids")
      void deleteAllById(@Param("ids") List<Long> ids);

      List<ServiceChecker> findByStatus(ServiceCheckerStatus status);

      @Modifying
      @Query("UPDATE ServiceChecker s SET s.status = :status WHERE s.id IN :ids")
      int updateStatusByIds(@Param("ids") List<Long> ids, @Param("status") ServiceCheckerStatus status);

      // Convenience method that defaults to excluding CANCELLED
      default List<ServiceChecker> findByDateBetweenExcludingCancelled(LocalDate startDate, LocalDate endDate) {
            return findByDateBetween(startDate, endDate, ServiceCheckerStatus.CANCELLED);
      }

      // Find all excluding CANCELLED
      @Query("SELECT sc FROM ServiceChecker sc WHERE sc.status != :excludedStatus")
      List<ServiceChecker> findAllExcludingCancelled(@Param("excludedStatus") ServiceCheckerStatus excludedStatus);

      Page<ServiceChecker> findAll(Pageable pageable);

      @Query("SELECT s FROM ServiceChecker s WHERE s.status != 'CANCELLED'")
      Page<ServiceChecker> findAllNonCancelled(Pageable pageable);

      Page<ServiceChecker> findAllByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

      @Query("SELECT s FROM ServiceChecker s " +
                  "WHERE (:startDate IS NULL OR s.createdAt >= :startDate) " +
                  "AND (:endDate IS NULL OR s.createdAt <= :endDate) " +
                  "ORDER BY s.createdAt DESC")
      Page<ServiceChecker> findAllByDateRange(
                  @Param("startDate") LocalDateTime startDate,
                  @Param("endDate") LocalDateTime endDate,
                  Pageable pageable);

      // Get non-cancelled checklists by date range
      @Query("SELECT s FROM ServiceChecker s " +
                  "WHERE s.status != 'CANCELLED' " +
                  "AND (:startDate IS NULL OR s.createdAt >= :startDate) " +
                  "AND (:endDate IS NULL OR s.createdAt <= :endDate) " +
                  "ORDER BY s.createdAt DESC")
      Page<ServiceChecker> findAllNonCancelledByDateRange(
                  @Param("startDate") LocalDateTime startDate,
                  @Param("endDate") LocalDateTime endDate,
                  Pageable pageable);

      // Alternative: Combined method with date range
      @Query("SELECT s FROM ServiceChecker s " +
                  "WHERE s.status != 'CANCELLED' " +
                  "AND (:startDate IS NULL OR s.createdAt >= :startDate) " +
                  "AND (:endDate IS NULL OR s.createdAt <= :endDate)")
      Page<ServiceChecker> findAllNonCancelledWithFilters(
                  @Param("startDate") LocalDateTime startDate,
                  @Param("endDate") LocalDateTime endDate,
                  Pageable pageable);

}