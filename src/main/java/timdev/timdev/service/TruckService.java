package timdev.timdev.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import timdev.timdev.entity.Truck;
import timdev.timdev.repository.TruckRepository;


@RequiredArgsConstructor
@Service
public class TruckService {
    
    private final TruckRepository truckRepo;

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


    public List<Truck> getTrucksRequiringFatAndOil() {
        return truckRepo.findByRequiredFatOilTrue();
    }
    
    public List<Truck> getTrucksNotRequiringFatAndOil() {
        return truckRepo.findByRequiredFatOilFalse();
    }
    
    public List<Truck> getAllTrucksByFatAndOilRequirement(Boolean required) {
        if (required == null) {
            return truckRepo.findAll();
        }
        return truckRepo.findByRequiredFatOil(required);
    }



}
