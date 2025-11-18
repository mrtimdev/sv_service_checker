package timdev.timdev.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CompanyTruckViewDTO {
    
    private Long id;
    private String licensePlate;
    private LocalDate date;
    private String totalDestination;
    private Double totalKm;
    private Double average;
    private Measurement measurement;
    private Double litreQuantity;
    private String note;
    private Double totalOilsChange;

    private String otherOils;

    private Long createdBy;
    private Long updatedBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getLicensePlate() {
        return licensePlate;
    }
    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }
    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
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
    public Measurement getMeasurement() {
        return measurement;
    }
    public void setMeasurement(Measurement measurement) {
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
}
