package timdev.timdev.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import timdev.timdev.entity.TruckInspection;
import timdev.timdev.repository.TruckInspectionRepository;


@RequiredArgsConstructor
@Service
public class TruckInspectionService {
    
    private final TruckInspectionRepository truckRepo;

    public List<TruckInspection> getAll() {
        return truckRepo.findAll();
    }

    public Page<TruckInspection> getAllWithPageable(Pageable pageable) {
        return truckRepo.findAll(pageable);
    }

    public List<TruckInspection> findByLicensePlateContaining(String licensePlate) {
        return truckRepo.findByLicensePlateContainingIgnoreCase(licensePlate);
    }

    public Page<TruckInspection> findByLicensePlateContainingWithPageable(String licensePlate, Pageable pageable) {
        return truckRepo.findByLicensePlateContainingIgnoreCase(licensePlate, pageable);
    }


    public Optional<TruckInspection> findById(Long id) {
        return truckRepo.findById(id);
    }

    public Optional<TruckInspection> findByLicensePlate(String lp) {
        return truckRepo.findByLicensePlate(lp);
    }

    

    public TruckInspection save(TruckInspection truck) {
        return truckRepo.save(truck);
    }

    public List<TruckInspection> saveAll(List<TruckInspection> trucks) {
        return truckRepo.saveAll(trucks);
    }


    public Optional<TruckInspection> getByLicensePlate(String lp) {
        return truckRepo.findByLicensePlate(lp);
    }

    public void deleteById(Long id) {
        truckRepo.deleteById(id); 
    }

}
