package timdev.timdev.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import timdev.timdev.entity.Driver;
import timdev.timdev.entity.Truck;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    
    boolean existsByPhone(String phone);
    boolean existsByTruck(Truck truck);

    Driver findByPhone(String phone);
}
