package timdev.timdev.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import timdev.timdev.entity.Driver;
import timdev.timdev.entity.Truck;
import timdev.timdev.repository.DriverRepository;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;

    public List<Driver> getAllDrivers() {
        return driverRepository.findAll();
    }

    public Optional<Driver> findById(Long id) {
        return driverRepository.findById(id);
    }

    public Optional<Driver> getDriverById(Long id) {
        return driverRepository.findById(id);
    }

    public Driver createDriver(Driver driver) {
        return driverRepository.save(driver);
    }

    public Driver updateDriver(Driver existingDriver, Driver driverDetails) {
        existingDriver.setFirstName(driverDetails.getFirstName());
        existingDriver.setLastName(driverDetails.getLastName());
        existingDriver.setPhone(driverDetails.getPhone());
        existingDriver.setNativeName(driverDetails.getNativeName());
        existingDriver.setTruck(driverDetails.getTruck());
        return driverRepository.save(existingDriver);
    }

    public void deleteDriver(Long id) {
        driverRepository.deleteById(id);
    }

    public boolean existsByPhone(String phone) {
        return driverRepository.existsByPhone(phone);
    }
    public boolean existsByTruck(Truck truck) {
        return driverRepository.existsByTruck(truck);
    }

    public Driver findByPhone(String phone) {
        return driverRepository.findByPhone(phone);
    }
}
