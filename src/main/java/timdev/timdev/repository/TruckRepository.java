package timdev.timdev.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import timdev.timdev.entity.Truck;




@Repository
public interface TruckRepository extends JpaRepository<Truck, Long> {

  
    Optional<Truck> findByLicensePlate(String licensePlate);
    boolean existsByLicensePlate(String licensePlate);


    List<Truck> findByLicensePlateContainingIgnoreCase(String licensePlate);

    Page<Truck> findByLicensePlateContainingIgnoreCase(String licensePlate, Pageable pageable);



    

}
