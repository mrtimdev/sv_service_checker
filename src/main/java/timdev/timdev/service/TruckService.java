package timdev.timdev.service;

import java.util.List;
import java.util.Optional;

import org.hibernate.envers.Audited;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import timdev.timdev.entity.Truck;
import timdev.timdev.repository.TruckRepository;

@AllArgsConstructor
@Service
public class TruckService {

    private TruckRepository truckRepo;

    public List<Truck> getAll() {
        return truckRepo.findAll();
    }

    public Page<Truck> getAllWithPageable(Pageable pageable) {
        return truckRepo.findAll(pageable);
    }

    public List<Truck> findByLicensePlateContaining(String licensePlate) {
        return truckRepo.findByLicensePlateContainingIgnoreCase(licensePlate);
    }

    public Page<Truck> findByLicensePlateContainingWithPageable(String licensePlate, Pageable pageable) {
        return truckRepo.findByLicensePlateContainingIgnoreCase(licensePlate, pageable);
    }

    public Optional<Truck> findById(Long id) {
        return truckRepo.findById(id);
    }

    public Optional<Truck> findByLicensePlate(String lp) {
        return truckRepo.findByLicensePlate(lp);
    }

    public Truck save(Truck truck) {
        return truckRepo.save(truck);
    }

    public List<Truck> saveAll(List<Truck> trucks) {
        return truckRepo.saveAll(trucks);
    }

    public Optional<Truck> getByLicensePlate(String lp) {
        return truckRepo.findByLicensePlate(lp);
    }

    public void deleteById(Long id) {
        truckRepo.deleteById(id);
    }

    public List<Truck> findTrucksWithoutAverages(Long truckId) {
        return truckRepo.findTrucksWithoutAverages(truckId);
    }

    public List<Truck> findTrucksWithoutAverageForMeasurement(Long measurementId) {
        return truckRepo.findTrucksWithoutAverageForMeasurement(measurementId);
    }

    public String findRouteNumberByLicensePlate(String licensePlate) {
        Optional<String> routeNumber = truckRepo.findRouteNumberByLicensePlate(licensePlate);
        return routeNumber.orElse(null);
    }

    public boolean existsByLicensePlate(String licensePlate) {
        return truckRepo.existsByLicensePlate(licensePlate);
    }

}
