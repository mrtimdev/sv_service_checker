package timdev.timdev.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties.Distribution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import timdev.timdev.dto.Status;
import timdev.timdev.entity.Destination;
import timdev.timdev.entity.DestinationSetting;

@Repository
public interface DestinationRepository extends JpaRepository<Destination, Long>, JpaSpecificationExecutor<Distribution> {

    Destination findByCode(String code);
    Destination findByName(String name);

    boolean existsByCode(String code);
    boolean existsByName(String name);


    // Pagination + Search
    @Query("""
        SELECT d FROM Destination d
        WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(d.code) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(d.truck.licensePlate) LIKE LOWER(CONCAT('%', :search, '%'))
    """)
    Page<Destination> searchWithPage(String search, Pageable pageable);

    // Show all + Sort
    @Query("""
        SELECT d FROM Destination d
        WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(d.code) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(d.truck.licensePlate) LIKE LOWER(CONCAT('%', :search, '%'))
    """)
    List<Destination> searchAll(String search, Sort sort);


    @Query("""
        SELECT d FROM Destination d
        WHERE (:startDate IS NULL OR d.date >= :startDate)
        AND (:endDate IS NULL OR d.date <= :endDate)
        AND (
                :query IS NULL
                OR LOWER(d.name) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(d.code) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(d.truck.licensePlate) LIKE LOWER(CONCAT('%', :query, '%'))
            )
    """)
    Page<Destination> findByFilterQueriesWithPage(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("query") String query,
            Pageable pageable);


    @Query("""
        SELECT d FROM Destination d
        WHERE (:startDate IS NULL OR d.date >= :startDate)
        AND (:endDate IS NULL OR d.date <= :endDate)
        AND (
                :query IS NULL
                OR LOWER(d.name) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(d.code) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(d.truck.licensePlate) LIKE LOWER(CONCAT('%', :query, '%'))
            )
    """)
    List<Destination> findByFilterQueriesAndSort(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("query") String query,
            Sort sort);

    @Query("""
        SELECT d FROM Destination d
        WHERE (:startDate IS NULL OR d.date >= :startDate)
          AND (:endDate IS NULL OR d.date <= :endDate)
          AND (:query IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :query, '%')))
          AND d.status IN :statuses
    """)
    List<Destination> findByFilterQueriesAndSortWithStatus(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("query") String query,
            @Param("statuses") List<Status> statuses,
            Sort sort
    );


    boolean existsByDateAndTruckIdAndSettingId(LocalDate date, Long truckId, Long settingId);


    @Query("""
        SELECT CASE WHEN COUNT(d) > 0 THEN TRUE ELSE FALSE END
        FROM Destination d
        WHERE d.date = :destinationDate
        AND d.truck.id = :truckId
        AND d.setting.id = :settingId
        AND (:destinationId IS NULL OR d.id <> :destinationId)
        """)
        boolean existsByDateAndTruckAndSetting(
                @Param("destinationDate") LocalDate destinationDate,
                @Param("truckId") Long truckId,
                @Param("settingId") Long settingId,
                @Param("destinationId") Long destinationId
        );

    
    // Optional<Destination> findByDateAndTruckIdAndSettingId(LocalDate date, Long truckId, Long settingId);


    // Multiple results
    List<Destination> findByDateAndTruckIdAndSettingId(LocalDate date, Long truckId, Long settingId);

    // Single result (optional)
    Optional<Destination> findFirstByDateAndTruckIdAndSettingId(LocalDate date, Long truckId, Long settingId);
    
    // Optional: Find by date and truck only
    List<Destination> findByDateAndTruckId(LocalDate date, Long truckId);
    
    // Optional: Find by status
    List<Destination> findByStatus(Status status);




    @Query("""
        SELECT d FROM Destination d
        WHERE (:startDate IS NULL OR d.date >= :startDate)
          AND (:endDate IS NULL OR d.date <= :endDate)
          AND (:query IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :query, '%')))
          AND d.status IN :statuses
    """)
    List<Destination> findByFilterQueriesAndStatus(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("query") String query,
            @Param("statuses") List<Status> statuses,
            Sort sort
    );

    @Query("""
        SELECT d FROM Destination d
        WHERE (:startDate IS NULL OR d.date >= :startDate)
          AND (:endDate IS NULL OR d.date <= :endDate)
          AND (:query IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :query, '%')))
          AND d.status IN :statuses
    """)
    Page<Destination> findByFilterQueriesWithStatus(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("query") String query,
            @Param("statuses") List<Status> statuses,
            Pageable pageable
    );

        @Query("""
                SELECT d FROM Destination d
                WHERE
                        (:query = '' OR
                        LOWER(d.name) LIKE LOWER(CONCAT('%', :query, '%')) OR
                        LOWER(d.code) LIKE LOWER(CONCAT('%', :query, '%'))
                        )
                AND (:startDate IS NULL OR d.date >= :startDate)
                AND (:endDate IS NULL OR d.date <= :endDate)
                AND d.status IN :statuses
        """)
        Page<Destination> findByFilters(
                @Param("query") String query,
                @Param("startDate") LocalDate startDate,
                @Param("endDate") LocalDate endDate,
                @Param("statuses") List<Status> statuses,
                Pageable pageable);

        // @Query("""
        //         SELECT d FROM Destination d
        //         LEFT JOIN d.setting s
        //         LEFT JOIN d.truck t
        //         WHERE d.status = :status
        //         AND (
        //         LOWER(s.code) LIKE LOWER(CONCAT('%', :q, '%'))
        //         OR LOWER(s.name) LIKE LOWER(CONCAT('%', :q, '%'))
        //         OR LOWER(t.licensePlate) LIKE LOWER(CONCAT('%', :q, '%'))
        //         )
        //         AND d.id NOT IN (
        //                 SELECT ct.destination.id FROM CompanyTruck ct WHERE (:destinationId IS NULL OR ct.destination.id != :destinationId)
        //         )
        //         ORDER BY d.id DESC
        // """)
        // List<Destination> searchPending(
        //         @Param("status") Status status,
        //         @Param("q") String query,
        //         @Param("destination") Long destinationId
        // );

        @Query("""
                SELECT d FROM Destination d
                LEFT JOIN d.setting s
                LEFT JOIN d.truck t
                WHERE d.status = :status
                AND (
                        LOWER(s.code) LIKE LOWER(CONCAT('%', :q, '%'))
                        OR LOWER(s.name) LIKE LOWER(CONCAT('%', :q, '%'))
                        OR LOWER(t.licensePlate) LIKE LOWER(CONCAT('%', :q, '%'))
                )
                AND NOT EXISTS (
                        SELECT 1
                        FROM CompanyTruck ct
                        WHERE ct.destination = d
                        AND (:destinationId IS NULL OR ct.destination.id <> :destinationId)
                )
                ORDER BY d.id DESC
                """)
                List<Destination> searchPending(
                        @Param("status") Status status,
                        @Param("q") String q,
                        @Param("destinationId") Long destinationId
                );


        




    
}
