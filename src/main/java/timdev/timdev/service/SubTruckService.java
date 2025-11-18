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
import timdev.timdev.dto.SubTruckRequestDTO;
import timdev.timdev.entity.CompanyTruck;
import timdev.timdev.entity.SubTruck;
import timdev.timdev.entity.Truck;
import timdev.timdev.entity.User;
import timdev.timdev.repository.SubTruckRepository;
import timdev.timdev.repository.TruckRepository;

@AllArgsConstructor
@Service
public class SubTruckService {

    private SubTruckRepository repository;
    private TruckRepository truckRepository;
    private  UserService userService;

    public List<SubTruck> findAll() {
        return repository.findAll();
    }

    public SubTruck findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public SubTruck save(SubTruck subTruck) {
        return repository.save(subTruck);
    }
    public SubTruck update(SubTruck subTruck) {
        return repository.save(subTruck);
    }

    public SubTruck saveFromDto(SubTruckRequestDTO dto) {
        SubTruck subTruck = new SubTruck();
        ApproveStatus status = ApproveStatus.PENDING;
        User currentUser = userService.getCurrentUser();
        if (dto.getId() != null) {
            subTruck = repository.findById(dto.getId()).orElse(new SubTruck());
            status = subTruck.getStatus();
           subTruck.setUpdatedBy(currentUser);
        } else {
            subTruck.setCreatedBy(currentUser);
        }

        subTruck.setDate(dto.getDate());
        subTruck.setTruckOwner(dto.getTruckOwner());
        subTruck.setOilsQuantity(dto.getOilsQuantity());
        subTruck.setNote(dto.getNote());
        subTruck.setApprovedNote(dto.getApprovedNote());
        subTruck.setRejectedNote(dto.getRejectedNote());
        subTruck.setStatus(status);

        if (dto.getTruckId() != null) {
            Truck truck = truckRepository.findById(dto.getTruckId()).orElse(null);
            subTruck.setTruck(truck);
        }

        return repository.save(subTruck);
    }

    public SubTruckRequestDTO convertToDto(SubTruck subTruck) {
        SubTruckRequestDTO dto = new SubTruckRequestDTO();
        dto.setId(subTruck.getId());
        dto.setTruckId(subTruck.getTruck().getId());
        dto.setTruckOwner(subTruck.getTruckOwner());
        dto.setOilsQuantity(subTruck.getOilsQuantity());
        dto.setNote(subTruck.getNote());
        dto.setStatus(subTruck.getStatus());
        dto.setDate(subTruck.getDate());
        return dto;
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }


    public Page<SubTruck> findFiltered(int page, int size, String licensePlate, String truckOwner, Long truckId, 
                                      ApproveStatus status, LocalDate startDate, LocalDate endDate) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        
        // If all filters are null, return all records
        if (licensePlate == null && truckOwner == null && truckId == null && status == null && startDate == null && endDate == null) {
            return repository.findAll(pageable);
        }
        
        return repository.findFiltered(licensePlate, truckOwner, truckId, status, startDate, endDate, pageable);
    }

    public boolean isExistsByTruckAndDate(Truck companyTruck, LocalDate date) {
        boolean exists = repository.existsByTruckAndDate(companyTruck, date);
        if (exists) {
            return true;
        }
        return false;
    }

    public SubTruck findByTruckAndDate(Truck truck, LocalDate date) {
        return repository.findByTruckAndDate(truck, date).orElse(null);
    }
}