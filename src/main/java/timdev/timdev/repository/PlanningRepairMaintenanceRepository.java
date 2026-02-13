package timdev.timdev.repository;

import java.util.List;
import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import timdev.timdev.entity.PlanningRepairMaintenance;
import timdev.timdev.enums.PlanningStatus;

@Repository
public interface PlanningRepairMaintenanceRepository extends JpaRepository<PlanningRepairMaintenance, Long> {

    List<PlanningRepairMaintenance> findByTruck_IdAndStatus(Long truckId, PlanningStatus status);

    List<PlanningRepairMaintenance> findByStatusIn(List<PlanningStatus> statuses);

    boolean existsByTruck_IdAndStatusIn(Long truckId, List<PlanningStatus> statuses);

    List<PlanningRepairMaintenance> findByStatusInOrderByPlannedStartDateAsc(List<PlanningStatus> statuses);

    List<PlanningRepairMaintenance> findByStatusInOrderByUpdatedAtDesc(List<PlanningStatus> statuses);

    @Query("""
        select p from PlanningRepairMaintenance p
        where p.status in :statuses
          and (:q is null or :q = '' or
            lower(p.planTitle) like lower(concat('%', :q, '%'))
            or lower(p.truck.licensePlate) like lower(concat('%', :q, '%'))
          )
          and (:from is null or p.plannedStartDate >= :from)
          and (:to is null or p.plannedStartDate <= :to)
        order by p.plannedStartDate asc
        """)
    List<PlanningRepairMaintenance> searchActive(
        @Param("statuses") List<PlanningStatus> statuses,
        @Param("q") String q,
        @Param("from") LocalDate from,
        @Param("to") LocalDate to
    );

    List<PlanningRepairMaintenance> findByStatusInAndPlannedStartDateBetweenOrderByPlannedStartDateAsc(
        List<PlanningStatus> statuses,
        LocalDate start,
        LocalDate end
    );

    @Query("""
        select p from PlanningRepairMaintenance p
        where p.status in :statuses
          and (:q is null or :q = '' or
            lower(p.planTitle) like lower(concat('%', :q, '%'))
            or lower(p.truck.licensePlate) like lower(concat('%', :q, '%'))
          )
          and (:from is null or p.plannedStartDate >= :from)
          and (:to is null or p.plannedStartDate <= :to)
        order by p.updatedAt desc
        """)
    List<PlanningRepairMaintenance> searchHistory(
        @Param("statuses") List<PlanningStatus> statuses,
        @Param("q") String q,
        @Param("from") LocalDate from,
        @Param("to") LocalDate to
    );

    @Query("""
        select p from PlanningRepairMaintenance p
        where p.status in :statuses
          and (:q is null or :q = '' or
            lower(p.planTitle) like lower(concat('%', :q, '%'))
            or lower(p.truck.licensePlate) like lower(concat('%', :q, '%'))
          )
          and (:from is null or p.plannedStartDate >= :from)
          and (:to is null or p.plannedStartDate <= :to)
        """)
    Page<PlanningRepairMaintenance> searchHistoryPage(
        @Param("statuses") List<PlanningStatus> statuses,
        @Param("q") String q,
        @Param("from") LocalDate from,
        @Param("to") LocalDate to,
        Pageable pageable
    );

    List<PlanningRepairMaintenance> findByStatusInAndPlannedStartDateBeforeOrderByPlannedStartDateAsc(
        List<PlanningStatus> statuses,
        LocalDate date
    );
}
