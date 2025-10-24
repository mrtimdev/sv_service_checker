package timdev.timdev.dto;


import java.time.LocalDate;
import java.time.LocalDateTime;
import timdev.timdev.enums.OilStatus;

public class TruckOilsReportDTO {
    private Long id;
    private Long truckId;
    private String truckName; // optional
    private Double literQuantityOfOils;
    private LocalDate date;
    private Double currentKm;
    private Double distanceKm;
    private Double nextRange;
    private Double kmForOilsChange;
    private OilStatus status;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTruckId() { return truckId; }
    public void setTruckId(Long truckId) { this.truckId = truckId; }

    public String getTruckName() { return truckName; }
    public void setTruckName(String truckName) { this.truckName = truckName; }

    public Double getLiterQuantityOfOils() { return literQuantityOfOils; }
    public void setLiterQuantityOfOils(Double literQuantityOfOils) { this.literQuantityOfOils = literQuantityOfOils; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Double getCurrentKm() { return currentKm; }
    public void setCurrentKm(Double currentKm) { this.currentKm = currentKm; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public Double getNextRange() { return nextRange; }
    public void setNextRange(Double nextRange) { this.nextRange = nextRange; }

    public Double getKmForOilsChange() { return kmForOilsChange; }
    public void setKmForOilsChange(Double kmForOilsChange) { this.kmForOilsChange = kmForOilsChange; }

    public OilStatus getStatus() { return status; }
    public void setStatus(OilStatus status) { this.status = status; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
