package timdev.timdev.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import timdev.timdev.dto.Status;
import timdev.timdev.entity.CompanySmallTruck;
import timdev.timdev.entity.Truck;

@Repository
public interface CompanySmallTruckRepository extends JpaRepository<CompanySmallTruck, Long> {

        List<CompanySmallTruck> findByTruck_LicensePlateContaining(String licensePlate, Sort sort);

        // Non-paginated search by Truck License Plate
        List<CompanySmallTruck> findByTruck_LicensePlateContainingIgnoreCase(String licensePlate);

        // Paginated search by Truck License Plate
        Page<CompanySmallTruck> findByTruck_LicensePlateContainingIgnoreCase(String licensePlate, Pageable pageable);

        @Query("SELECT c FROM CompanySmallTruck c JOIN FETCH c.truck")
        List<CompanySmallTruck> findAllWithTruck();


        @Query("SELECT c FROM CompanySmallTruck c JOIN FETCH c.truck WHERE LOWER(c.truck.licensePlate) LIKE LOWER(CONCAT('%', :licensePlate, '%'))")
        Page<CompanySmallTruck> findByTruckLicensePlateWithTruck(@Param("licensePlate") String licensePlate, Pageable pageable);

        boolean existsByTruckAndDate(Truck truck, LocalDate date);

        Optional<CompanySmallTruck> findByTruckAndDate(Truck truck, LocalDate date);


        List<CompanySmallTruck> findByStatus(Status status);

        

        Page<CompanySmallTruck> findByStatus(Status status, Pageable pageable);

        @Query("""
                SELECT c 
                FROM CompanySmallTruck c 
                JOIN FETCH c.truck t 
                WHERE LOWER(t.licensePlate) LIKE LOWER(CONCAT('%', :licensePlate, '%'))
                AND c.status = :status
        """)
        List<CompanySmallTruck> findByLicensePlateContainingAndStatus(
                @Param("licensePlate") String licensePlate,
                @Param("status") Status status);

        @Query("""
                SELECT c 
                FROM CompanySmallTruck c 
                JOIN c.truck t 
                WHERE LOWER(t.licensePlate) LIKE LOWER(CONCAT('%', :licensePlate, '%'))
                AND c.status = :status
        """)
        Page<CompanySmallTruck> findByLicensePlateContainingAndStatus(
                @Param("licensePlate") String licensePlate,
                @Param("status") Status status,
                Pageable pageable);


        @Query("""
                SELECT c 
                FROM CompanySmallTruck c 
                JOIN c.truck t 
                WHERE LOWER(t.licensePlate) LIKE LOWER(CONCAT('%', :licensePlate, '%'))
                AND c.status IN :statuses
        """)
        Page<CompanySmallTruck> findByLicensePlateContainingAndStatusIn(
                @Param("licensePlate") String licensePlate,
                @Param("statuses") List<Status> statuses,
                Pageable pageable);


        @Query("""
                SELECT c
                FROM CompanySmallTruck c
                WHERE (:startDate IS NULL OR c.date >= :startDate)
                AND (:endDate IS NULL OR c.date <= :endDate)
                AND (
                        :query IS NULL
                        OR LOWER(c.truck.licensePlate) LIKE LOWER(CONCAT('%', :query, '%'))
                        OR LOWER(c.totalDestination) LIKE LOWER(CONCAT('%', :query, '%'))
                )
        """)
        Page<CompanySmallTruck> findByFilterQueriesPage(
                @Param("startDate") LocalDate startDate,
                @Param("endDate") LocalDate endDate,
                @Param("query") String query,
                Pageable pageable
        );

        @Query("""
                SELECT c
                FROM CompanySmallTruck c
                WHERE (:startDate IS NULL OR c.date >= :startDate)
                AND (:endDate IS NULL OR c.date <= :endDate)
                AND (
                        :query IS NULL
                        OR LOWER(c.truck.licensePlate) LIKE LOWER(CONCAT('%', :query, '%'))
                        OR LOWER(c.totalDestination) LIKE LOWER(CONCAT('%', :query, '%'))
                )
                AND (:status IS NULL OR c.status = :status)
        """)
        Page<CompanySmallTruck> findByFilterQueriesPageAndStatus(
                @Param("startDate") LocalDate startDate,
                @Param("endDate") LocalDate endDate,
                @Param("query") String query,
                Pageable pageable,
                @Param("status") Status status
        );



        @Query("""
                SELECT c
                FROM CompanySmallTruck c
                WHERE (:startDate IS NULL OR c.date >= :startDate)
                AND (:endDate IS NULL OR c.date <= :endDate)
                AND (
                        :query IS NULL
                        OR LOWER(c.truck.licensePlate) LIKE LOWER(CONCAT('%', :query, '%'))
                        OR LOWER(c.totalDestination) LIKE LOWER(CONCAT('%', :query, '%'))
                )
        """)
        List<CompanySmallTruck> findByFilterQueriesList(
                @Param("startDate") LocalDate startDate,
                @Param("endDate") LocalDate endDate,
                @Param("query") String query,
                Pageable pageable
        );

        @Query("""
                SELECT c
                FROM CompanySmallTruck c
                WHERE (:startDate IS NULL OR c.date >= :startDate)
                AND (:endDate IS NULL OR c.date <= :endDate)
                AND (
                        :query IS NULL
                        OR LOWER(c.truck.licensePlate) LIKE LOWER(CONCAT('%', :query, '%'))
                        OR LOWER(c.totalDestination) LIKE LOWER(CONCAT('%', :query, '%'))
                )
        """)
        List<CompanySmallTruck> findByFilterQueriesListAndSort(
                @Param("startDate") LocalDate startDate,
                @Param("endDate") LocalDate endDate,
                @Param("query") String query,
                Sort sort
        );

        @Query("""
                SELECT c
                FROM CompanySmallTruck c
                WHERE (:startDate IS NULL OR c.date >= :startDate)
                AND (:endDate IS NULL OR c.date <= :endDate)
                AND (
                        :query IS NULL
                        OR LOWER(c.truck.licensePlate) LIKE LOWER(CONCAT('%', :query, '%'))
                        OR LOWER(c.totalDestination) LIKE LOWER(CONCAT('%', :query, '%'))
                )
                AND (:status IS NULL OR c.status = :status)
        """)
        List<CompanySmallTruck> findByFilterQueriesListAndSortAndStatus(
                @Param("startDate") LocalDate startDate,
                @Param("endDate") LocalDate endDate,
                @Param("query") String query,
                Sort sort,
                @Param("status") Status status
        );


        // Optional (Update case – ignore same record)
        boolean existsByDateAndTruck_IdAndTotalDestinationAndIdNot(
                LocalDate date,
                Long truckId,
                String totalDestination,
                Long id
        );





}