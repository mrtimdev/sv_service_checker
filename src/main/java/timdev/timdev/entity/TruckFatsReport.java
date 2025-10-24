package timdev.timdev.entity;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import timdev.timdev.enums.OilStatus;

@Entity
@Table(name = "truck_fats_reports")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TruckFatsReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "truck_id", nullable = false)
    @JsonBackReference
    private Truck truck;

    @Column(nullable = false)
    private Double literQuantityOfFats = 0.0;

    @NotNull(message = "The Date is required")
    @Column(nullable = false)
    @DateTimeFormat(pattern = "MMM dd, yyyy")
    private LocalDate date;

    @Column(name = "current_km", nullable = false)
    private Double currentKm;

    @Column(name = "distance_km", nullable = false)
    private Double distanceKm;

    @Column(name = "next_range", nullable = false)
    private Double nextRange;
    
    @Column(name = "km_for_fats_shoot", nullable = false)
    private Double kmForFatsShoot = 0.0;

    
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

    public Double getKmForFatsShoot() {
        return kmForFatsShoot;
    }

    public void setKmForFatsShoot(Double kmForFatsShoot) {
        this.kmForFatsShoot = kmForFatsShoot;
    }


    public String getCreatedAgo() {
        if (createdAt == null) return "Unknown";

        LocalDateTime now = LocalDateTime.now();

        // If createdAt is in the future
        if (createdAt.isAfter(now)) {
            return "in the future";
        }

        Duration duration = Duration.between(createdAt, now);
        long seconds = duration.getSeconds();

        if (seconds < 60) {
            return "just now";
        }

        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        }

        long hours = minutes / 60;
        if (hours < 24) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        }

        long days = hours / 24;
        if (days < 7) {
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        }

        long weeks = days / 7;
        if (weeks < 4) {
            return weeks + " week" + (weeks > 1 ? "s" : "") + " ago";
        }

        long months = days / 30;
        if (months < 12) {
            return months + " month" + (months > 1 ? "s" : "") + " ago";
        }

        long years = days / 365;
        return years + " year" + (years > 1 ? "s" : "") + " ago";
    }

    public Double getLiterQuantityOfFats() {
        return literQuantityOfFats;
    }

    public void setLiterQuantityOfFats(Double literQuantityOfFats) {
        this.literQuantityOfFats = literQuantityOfFats;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }


    @Transient
    public String getCurrentKmFormat() {
        return String.format("%,.2f km", currentKm); 
    }

    @Transient
    public String getDistanceFormat() {
        return String.format("%,.2f km", distanceKm); 
    }

    @Transient
    public String getNextRangeFormat() {
        return String.format("%,.2f km", nextRange); 
    }
    

}