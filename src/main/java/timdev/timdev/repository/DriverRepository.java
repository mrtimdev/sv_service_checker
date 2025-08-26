package timdev.timdev.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import timdev.timdev.entity.Driver;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    
    boolean existsByPhone(String phone);
    boolean existsByPlateNumber(String plateNumber);
}
