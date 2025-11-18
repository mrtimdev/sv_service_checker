package timdev.timdev.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import timdev.timdev.dto.Measurement;
import timdev.timdev.listener.AuditListener;

@Entity
@Table(name = "company_trucks", 
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"date", "truck_id"})
    }
)
@Audited
@EntityListeners(AuditListener.class)
public class CompanyTruck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "truck_id", nullable = false)
    @JsonBackReference
    private Truck truck; 

    @Column(name = "total_destination")
    private String totalDestination;

    @Column(name = "total_km")
    private Double totalKm;

    private Double average;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Measurement measurement;

    @Column(name = "litre_quantity")
    private Double litreQuantity;

    @Column(name = "total_oils_change")
    private Double totalOilsChange;

    @Column(columnDefinition = "TEXT", name = "other_oils")
    private String otherOils;

    @Column(columnDefinition = "TEXT")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy; 

    @Column(name = "created_at", updatable = false, nullable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Constructors
    public CompanyTruck() {}

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Truck getTruck() {
        return truck;
    }

    public void setTruck(Truck truck) {
        this.truck = truck;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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


    @Transient
    public String getTotalKmFormat() {
        if (totalKm != null)
            return String.format("%,.2f km", totalKm);
        return "0 Km";
    }

    @Transient
    public String getAverageFormat() {
        if (average != null)
            return String.format("%,.2f km", average);
        return "0 Km";
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

}