package timdev.timdev.dto.api;

import java.time.LocalDate;
import java.util.List;

public class ServiceCheckerRequest {
    private LocalDate date;
    private Long driverId;
    private List<CategoryItemRequest> categories;
    
    // Constructors
    public ServiceCheckerRequest() {}
    
    // Getters and setters
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }
    
    public List<CategoryItemRequest> getCategories() { return categories; }
    public void setCategories(List<CategoryItemRequest> categories) { this.categories = categories; }
}
