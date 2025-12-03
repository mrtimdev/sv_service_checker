package timdev.timdev.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties.Distribution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import timdev.timdev.entity.Destination;

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

    
}
