package timdev.timdev.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;

public class TruckDistanceDto {
    
    private Long id;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    @NotNull(message = "The Date is required")
    @Column(nullable = false)  
    @DateTimeFormat(pattern = "MMM dd, yyyy")
    private LocalDate date;

    @NotNull(message = "The Truck is required")
    @Column(nullable = false)
    private Long truckId;
    private Double distance;

    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }
    public Long getTruckId() {
        return truckId;
    }
    public void setTruckId(Long truckId) {
        this.truckId = truckId;
    }
    public Double getDistance() {
        return distance;
    }
    public void setDistance(Double distance) {
        this.distance = distance;
    }
}
