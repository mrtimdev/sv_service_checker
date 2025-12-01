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
import timdev.timdev.entity.CompanyTruck;
import timdev.timdev.entity.Truck;

@Repository
public interface CompanyTruckRepository extends JpaRepository<CompanyTruck, Long> {

    List<CompanyTruck> findByTruck_LicensePlateContaining(String licensePlate, Sort sort);

    // Non-paginated search by Truck License Plate
    List<CompanyTruck> findByTruck_LicensePlateContainingIgnoreCase(String licensePlate);

    // Paginated search by Truck License Plate
    Page<CompanyTruck> findByTruck_LicensePlateContainingIgnoreCase(String licensePlate, Pageable pageable);

    @Query("SELECT c FROM CompanyTruck c JOIN FETCH c.truck")
    List<CompanyTruck> findAllWithTruck();


    @Query("SELECT c FROM CompanyTruck c JOIN FETCH c.truck WHERE LOWER(c.truck.licensePlate) LIKE LOWER(CONCAT('%', :licensePlate, '%'))")
    Page<CompanyTruck> findByTruckLicensePlateWithTruck(@Param("licensePlate") String licensePlate, Pageable pageable);

    boolean existsByTruckAndDate(Truck truck, LocalDate date);

    Optional<CompanyTruck> findByTruckAndDate(Truck truck, LocalDate date);


    List<CompanyTruck> findByStatus(Status status);

   

    Page<CompanyTruck> findByStatus(Status status, Pageable pageable);

    @Query("""
        SELECT c 
        FROM CompanyTruck c 
        JOIN FETCH c.truck t 
        WHERE LOWER(t.licensePlate) LIKE LOWER(CONCAT('%', :licensePlate, '%'))
        AND c.status = :status
    """)
    List<CompanyTruck> findByLicensePlateContainingAndStatus(
            @Param("licensePlate") String licensePlate,
            @Param("status") Status status);

    @Query("""
        SELECT c 
        FROM CompanyTruck c 
        JOIN c.truck t 
        WHERE LOWER(t.licensePlate) LIKE LOWER(CONCAT('%', :licensePlate, '%'))
        AND c.status = :status
    """)
    Page<CompanyTruck> findByLicensePlateContainingAndStatus(
            @Param("licensePlate") String licensePlate,
            @Param("status") Status status,
            Pageable pageable);


    @Query("""
        SELECT c 
        FROM CompanyTruck c 
        JOIN c.truck t 
        WHERE LOWER(t.licensePlate) LIKE LOWER(CONCAT('%', :licensePlate, '%'))
        AND c.status IN :statuses
    """)
    Page<CompanyTruck> findByLicensePlateContainingAndStatusIn(
            @Param("licensePlate") String licensePlate,
            @Param("statuses") List<Status> statuses,
            Pageable pageable);


}