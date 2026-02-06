package timdev.timdev.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import timdev.timdev.entity.TruckInspection;
import timdev.timdev.repository.TruckInspectionRepository;


@Service
public class TruckInspectionService {
    
    @Autowired
    private TruckInspectionRepository truckRepo;

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
    @Transactional
    public void deleteById(Long id) {
        truckRepo.deleteById(id); 
    }

}
