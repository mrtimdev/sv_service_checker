package timdev.timdev.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import timdev.timdev.enums.OilStatus;

@Entity
@Table(name = "truck_oils_reports")
public class TruckOilsReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "truck_id", nullable = false)
    @JsonBackReference
    private Truck truck;

    @NotNull(message = "The Date is required")
    @Column(nullable = false)
    @DateTimeFormat(pattern = "MMM dd, yyyy")
    private LocalDate date;

    @Column(name = "current_km", nullable = false)
    private Double currentKm;

    @Column(name = "next_range", nullable = false)
    private Double nextRange;
    
    @Column(name = "km_for_oils_shoot", nullable = false)
    private Double kmForOilsChange = 0.0;

    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OilStatus status;

    @Column(name = "note")
    private String note;


     // --- Audit fields ---
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", updatable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Truck getTruck() {
        return truck;
    }

    public void setTruck(Truck truck) {
        this.truck = truck;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Double getCurrentKm() {
        return currentKm;
    }

    public void setCurrentKm(Double currentKm) {
        this.currentKm = currentKm;
    }

    public OilStatus getStatus() {
        return status;
    }

    public void setStatus(OilStatus status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Double getNextRange() {
        return nextRange;
    }

    public void setNextRange(Double nextRange) {
        this.nextRange = nextRange;
    }

    public Double getKmForOilsChange() {
        return kmForOilsChange;
    }

    public void setKmForOilsChange(Double kmForOilsChange) {
        this.kmForOilsChange = kmForOilsChange;
    }
    

}