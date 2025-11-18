package timdev.timdev.service;


import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import timdev.timdev.entity.TruckGroup;
import timdev.timdev.repository.TruckGroupRepository;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class TruckGroupService {

    private TruckGroupRepository repository;

    public List<TruckGroup> getAll() {
        return repository.findAll();
    }

    public Optional<TruckGroup> getById(Long id) {
        return repository.findById(id);
    }

    public TruckGroup save(TruckGroup truckGroup) {
        if (repository.existsByName(truckGroup.getName())) {
            throw new RuntimeException("Truck group already exists!");
        }
        return repository.save(truckGroup);
    }

    public TruckGroup update(TruckGroup truckGroup) {
        truckGroup.setUpdatedAt(LocalDateTime.now());
        return repository.save(truckGroup);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}