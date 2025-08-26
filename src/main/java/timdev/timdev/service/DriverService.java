package timdev.timdev.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import timdev.timdev.entity.Driver;
import timdev.timdev.exception.DuplicateResourceException;
import timdev.timdev.exception.ResourceNotFoundException;
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
        // Check for duplicate phone
        if (driverRepository.existsByPhone(driver.getPhone())) {
            throw new DuplicateResourceException(
                "Phone number already exists", "phone");
        }

        // Check for duplicate plate number
        if (driverRepository.existsByPlateNumber(driver.getPlateNumber())) {
            throw new DuplicateResourceException(
                "Plate number already exists", "plateNumber");
        }

        // Additional validation if needed
        if (driver.getFirstName() == null || driver.getFirstName().isBlank()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (driver.getLastName() == null || driver.getLastName().isBlank()) {
            throw new IllegalArgumentException("Last name is required");
        }

        // Set any additional default values if needed
        driver.setId(null); // Ensure we're creating new record

        return driverRepository.save(driver);
    }

    public Driver updateDriver(Long id, Driver driverDetails) {
        return driverRepository.findById(id)
                .map(existingDriver -> {
                    // Check for duplicate phone (if changed)
                    if (!existingDriver.getPhone().equals(driverDetails.getPhone()) &&
                        driverRepository.existsByPhone(driverDetails.getPhone())) {
                        throw new DuplicateResourceException(
                            "Phone number already exists", "phone");
                    }

                    // Check for duplicate plate number (if changed)
                    if (!existingDriver.getPlateNumber().equals(driverDetails.getPlateNumber()) &&
                        driverRepository.existsByPlateNumber(driverDetails.getPlateNumber())) {
                        throw new DuplicateResourceException(
                            "Plate number already exists", "plateNumber");
                    }

                    existingDriver.setFirstName(driverDetails.getFirstName());
                    existingDriver.setLastName(driverDetails.getLastName());
                    existingDriver.setPhone(driverDetails.getPhone());
                    existingDriver.setPlateNumber(driverDetails.getPlateNumber());

                    return driverRepository.save(existingDriver);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + id));
    }

    public void deleteDriver(Long id) {
        driverRepository.deleteById(id);
    }

    public boolean existsByPhone(String phone) {
        return driverRepository.existsByPhone(phone);
    }
    public boolean existsByPlateNumber(String phone) {
        return driverRepository.existsByPlateNumber(phone);
    }
}
