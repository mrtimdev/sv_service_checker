package timdev.timdev.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import io.micrometer.common.lang.Nullable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;

public class CompanySmallTruckRequestDTO {
    
    @Nullable
    private Long id;

    @NotNull(message = "Date is required")
    @DateTimeFormat(pattern = "MMM dd, yyyy")
    private LocalDate date;

    @NotNull(message = "Truck ID is required")
    private Long truckId; // from Truck entity

    @NotNull(message = "Total Destination is required")
    private String totalDestination;

    @Nullable
    private String sizeOfTruck;

    @Nullable
    @DecimalMin(value = "0.0", inclusive = false, message = "Total KM must be greater than 0")
    private Double totalKm;

    @NotNull(message = "Average is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Average must be greater than 0")
    private Double average;

    @Nullable
    private String measurement;

    @NotNull(message = "Litre Quantity is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Litre Quantity must be greater than 0")
    private Double litreQuantity;

    private String licensePlate; 

    @Size(max = 500, message = "Note cannot exceed 500 characters")
    private String note;
    @Nullable
    private Double totalOilsChange;
    @Nullable
    private String otherOils;
    @Nullable
    private Long createdBy;
    @Nullable
    private Long updatedBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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

    public String getTotalDestination() {
        return totalDestination;
    }

    public void setTotalDestination(String totalDestination) {
        this.totalDestination = totalDestination;
    }

    public Double getTotalKm() {
        return totalKm;
    }

    public void setTotalKm(Double totalKm) {
        this.totalKm = totalKm;
    }

    public Double getAverage() {
        return average;
    }

    public void setAverage(Double average) {
        this.average = average;
    }

    public String getMeasurement() {
        return measurement;
    }

    public void setMeasurement(String measurement) {
        this.measurement = measurement;
    }

    public Double getLitreQuantity() {
        return litreQuantity;
    }

    public void setLitreQuantity(Double litreQuantity) {
        this.litreQuantity = litreQuantity;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getTotalOilsChange() {
        return totalOilsChange;
    }

    public void setTotalOilsChange(Double totalOilsChange) {
        this.totalOilsChange = totalOilsChange;
    }

    public String getOtherOils() {
        return otherOils;
    }

    public void setOtherOils(String otherOils) {
        this.otherOils = otherOils;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getSizeOfTruck() {
        return sizeOfTruck;
    }

    public void setSizeOfTruck(String sizeOfTruck) {
        this.sizeOfTruck = sizeOfTruck;
    }
}
