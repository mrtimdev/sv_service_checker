package timdev.timdev.service;


import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import timdev.timdev.dto.ApproveStatus;
import timdev.timdev.dto.FuelRequestRequestDTO;
import timdev.timdev.entity.CompanyTruck;
import timdev.timdev.entity.FuelRequest;
import timdev.timdev.entity.Truck;
import timdev.timdev.entity.User;
import timdev.timdev.repository.FuelRequestRepository;
import timdev.timdev.repository.TruckRepository;

@AllArgsConstructor
@Service
public class FuelRequestService {

    private FuelRequestRepository repository;
    private TruckRepository truckRepository;
    private  UserService userService;

    public List<FuelRequest> findAll() {
        return repository.findAll();
    }

    public FuelRequest findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public FuelRequest save(FuelRequest fuelRequest) {
        return repository.save(fuelRequest);
    }
    public FuelRequest update(FuelRequest fuelRequest) {
        return repository.save(fuelRequest);
    }

    public FuelRequest saveFromDto(FuelRequestRequestDTO dto) {
        FuelRequest fuelRequest = new FuelRequest();
        ApproveStatus status = ApproveStatus.PENDING;
        User currentUser = userService.getCurrentUser();
        if (dto.getId() != null) {
            fuelRequest = repository.findById(dto.getId()).orElse(new FuelRequest());
            status = fuelRequest.getStatus();
            fuelRequest.setUpdatedBy(currentUser);
        } else {
            fuelRequest.setCreatedBy(currentUser);
        }

        fuelRequest.setDate(dto.getDate());
        fuelRequest.setOilsQuantity(dto.getOilsQuantity());
        fuelRequest.setNote(dto.getNote());
        fuelRequest.setRequester(dto.getRequester());
        fuelRequest.setPurpose(dto.getPurpose());
        fuelRequest.setPosition(dto.getPosition());
        fuelRequest.setStatus(status);

        if (dto.getTruckId() != null) {
            Truck truck = truckRepository.findById(dto.getTruckId()).orElse(null);
            fuelRequest.setTruck(truck);
        }

        return repository.save(fuelRequest);
    }

    public FuelRequestRequestDTO convertToDto(FuelRequest fuelRequest) {
        FuelRequestRequestDTO dto = new FuelRequestRequestDTO();
        dto.setId(fuelRequest.getId());
        dto.setTruckId(fuelRequest.getTruck() != null ? fuelRequest.getTruck().getId() : null);
        dto.setOilsQuantity(fuelRequest.getOilsQuantity());
        dto.setNote(fuelRequest.getNote());
        dto.setRequester(fuelRequest.getRequester());
        dto.setPurpose(fuelRequest.getPurpose());
        dto.setPosition(fuelRequest.getPosition());
        dto.setStatus(fuelRequest.getStatus());
        dto.setDate(fuelRequest.getDate());
        return dto;
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }


    public Page<FuelRequest> findFiltered(int page, int size, String requester, String position, String purpose, Long truckId, 
                                      ApproveStatus status, LocalDate startDate, LocalDate endDate, Long createdById, Boolean isFilled) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        
        // If all filters are null, return all records
        // if (requester == null && position == null && truckId == null && status == null && startDate == null && endDate == null) {
        //     return repository.findAll(pageable);
        // }
        
        return repository.findFiltered(requester, position, purpose, truckId, status, startDate, endDate, pageable, createdById, isFilled);
    }

    


    public boolean isExistsByTruckAndDate(Truck companyTruck, LocalDate date) {
        boolean exists = repository.existsByTruckAndDate(companyTruck, date);
        if (exists) {
            return true;
        }
        return false;
    }

    public FuelRequest findByTruckAndDate(Truck truck, LocalDate date) {
        return repository.findByTruckAndDate(truck, date).orElse(null);
    }
}