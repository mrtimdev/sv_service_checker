package timdev.timdev.dto.api;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import timdev.timdev.entity.ServiceChecker;
import timdev.timdev.enums.ServiceCheckerStatus;

public class ServiceCheckerResponseDTO {
    private Long id;
    private String licensePlate;
    private String licensePlateEstimated;
    private LocalDate date;
    private Long totalItems;
    private Long itemNoteCount;
    private Long checkedCount;
    private Long notCheckedCount;
    private String imagePath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ServiceCheckerStatus status;
    private String cancelReason;
    private LocalDateTime cancelledAt;
    
    // User information (replaced Driver)
    private UserDTO createdBy;
    private UserDTO updatedBy;
    private UserDTO cancelledBy;
    
    private List<ServiceCheckerItemDTO> items;
    
    // Constructors
    public ServiceCheckerResponseDTO() {}
    
    public ServiceCheckerResponseDTO(ServiceChecker checker) {
        this.id = checker.getId();
        this.licensePlate = checker.getLicensePlate();
        this.licensePlateEstimated = checker.getLicensePlateEstimated();
        this.date = checker.getDate();
        this.totalItems = (long) checker.getTotalItems();
        this.itemNoteCount = (long) checker.getItemNoteCount();
        this.checkedCount = (long) checker.getCheckedCount();
        this.notCheckedCount = (long) checker.getNotCheckedCount();
        this.imagePath = checker.getImagePath();
        this.createdAt = checker.getCreatedAt();
        this.updatedAt = checker.getUpdatedAt();
        this.status = checker.getStatus();
        this.cancelReason = checker.getCancelReason();
        this.cancelledAt = checker.getCancelledAt();
        
        // Set user information
        if (checker.getCreatedBy() != null) {
            this.createdBy = new UserDTO(checker.getCreatedBy());
        }
        
        if (checker.getUpdatedBy() != null) {
            this.updatedBy = new UserDTO(checker.getUpdatedBy());
        }
        
        if (checker.getCancelledBy() != null) {
            this.cancelledBy = new UserDTO(checker.getCancelledBy());
        }
        
        // Convert items if available
        if (checker.getItems() != null) {
            this.items = checker.getItems().stream()
                .map(item -> new ServiceCheckerItemDTO(item))
                .collect(Collectors.toList());
        }
    }
    
    // Getters and setters
    public Long getItemNoteCount() {
        return itemNoteCount;
    }

    public void setItemNoteCount(Long itemNoteCount) {
        this.itemNoteCount = itemNoteCount;
    }

    public Long getCheckedCount() {
        return checkedCount;
    }

    public void setCheckedCount(Long checkedCount) {
        this.checkedCount = checkedCount;
    }
    
    
    public Long getNotCheckedCount() {
        return notCheckedCount;
    }

    public void setNotCheckedCount(Long notCheckedCount) {
        this.notCheckedCount = notCheckedCount;
    }
    public Long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Long totalItems) {
        this.totalItems = totalItems;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    
    public List<ServiceCheckerItemDTO> getItems() { return items; }
    public void setItems(List<ServiceCheckerItemDTO> items) { this.items = items; }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getLicensePlateEstimated() {
        return licensePlateEstimated;
    }

    public void setLicensePlateEstimated(String licensePlateEstimated) {
        this.licensePlateEstimated = licensePlateEstimated;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public ServiceCheckerStatus getStatus() {
        return status;
    }

    public void setStatus(ServiceCheckerStatus status) {
        this.status = status;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public UserDTO getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserDTO createdBy) {
        this.createdBy = createdBy;
    }

    public UserDTO getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(UserDTO updatedBy) {
        this.updatedBy = updatedBy;
    }

    public UserDTO getCancelledBy() {
        return cancelledBy;
    }

    public void setCancelledBy(UserDTO cancelledBy) {
        this.cancelledBy = cancelledBy;
    }
}