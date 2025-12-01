package timdev.timdev.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

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
import timdev.timdev.dto.Measurement;
import timdev.timdev.dto.RequestStatus;
import timdev.timdev.dto.Status;
import timdev.timdev.listener.AuditListener;

@Entity
@Table(name = "company_trucks")
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

    @Enumerated(EnumType.STRING)
    private RequestStatus requestStatus = RequestStatus.NONE;

    @Column(columnDefinition = "TEXT")
    private String requestNote;

    private boolean isRequested = false;

    private LocalDateTime approvedAt;
    private LocalDateTime rejectedAt;
    private LocalDateTime requestedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by", nullable = true)
    private User approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rejected_by", nullable = true)
    private User rejectedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by", nullable = true)
    private User requestedBy;

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


    // -----------------------
    // New fields for status
    // -----------------------
    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;  // default to PENDING

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deducted_by")
    private User deductedBy;

    @Column(name = "deducted_at")
    private LocalDateTime deductedAt;

    // when user back to pending after admin approved to pending
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pending_by")
    private User pendingBy;

    @Column(name = "pending_at")
    private LocalDateTime pendingAt;

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
            return String.format("%,.2f", average);
        return "0";
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public User getDeductedBy() {
        return deductedBy;
    }

    public void setDeductedBy(User deductedBy) {
        this.deductedBy = deductedBy;
    }

    public LocalDateTime getDeductedAt() {
        return deductedAt;
    }

    public void setDeductedAt(LocalDateTime deductedAt) {
        this.deductedAt = deductedAt;
    }

    public User getPendingBy() {
        return pendingBy;
    }

    public void setPendingBy(User pendingBy) {
        this.pendingBy = pendingBy;
    }

    public LocalDateTime getPendingAt() {
        return pendingAt;
    }

    public void setPendingAt(LocalDateTime pendingAt) {
        this.pendingAt = pendingAt;
    }

    public RequestStatus getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(RequestStatus requestStatus) {
        this.requestStatus = requestStatus;
    }

    public String getRequestNote() {
        return requestNote;
    }

    public void setRequestNote(String requestNote) {
        this.requestNote = requestNote;
    }

    public boolean isRequested() {
        return isRequested;
    }

    public void setRequested(boolean isRequested) {
        this.isRequested = isRequested;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public LocalDateTime getRejectedAt() {
        return rejectedAt;
    }

    public void setRejectedAt(LocalDateTime rejectedAt) {
        this.rejectedAt = rejectedAt;
    }

    public User getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(User approvedBy) {
        this.approvedBy = approvedBy;
    }

    public User getRejectedBy() {
        return rejectedBy;
    }

    public void setRejectedBy(User rejectedBy) {
        this.rejectedBy = rejectedBy;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public User getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(User requestedBy) {
        this.requestedBy = requestedBy;
    }


    @Transient
    public String getLitreQuantityFormat() {
        return String.format("%,.2f L", litreQuantity); 
    }
    @Transient
    public String getTotalOilsChangeFormat() {
        return String.format("%,.2f L", totalOilsChange); 
    }

}