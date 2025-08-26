package timdev.timdev.dto.api;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import timdev.timdev.entity.ServiceChecker;

public class ServiceCheckerResponseDTO {
    private Long id;
    private LocalDate date;
    private Long totalItems;
    private Long itemNoteCount;
    private Long checkedCount;
    private Long notCheckedCount;
    
    private DriverDTO driver;
    private List<ServiceCheckerItemDTO> items;
    
    // Constructors
    public ServiceCheckerResponseDTO() {}
    
    public ServiceCheckerResponseDTO(ServiceChecker serviceChecker) {
        this.id = serviceChecker.getId();
        this.date = serviceChecker.getDate();
        this.totalItems = serviceChecker.getTotalItems();
        this.itemNoteCount = serviceChecker.getItemNoteCount();
        this.checkedCount = serviceChecker.getCheckedCount();
        this.notCheckedCount = serviceChecker.getNotCheckedCount();
        this.driver = new DriverDTO(serviceChecker.getDriver());
        this.items = serviceChecker.getItems().stream()
                .map(ServiceCheckerItemDTO::new)
                .collect(Collectors.toList());
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
    
    public DriverDTO getDriver() { return driver; }
    public void setDriver(DriverDTO driver) { this.driver = driver; }
    
    public List<ServiceCheckerItemDTO> getItems() { return items; }
    public void setItems(List<ServiceCheckerItemDTO> items) { this.items = items; }
}