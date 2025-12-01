package timdev.timdev.repository;

import java.util.List;

import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties.Distribution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
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
    
}
