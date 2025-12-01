package timdev.timdev.entity;

import org.hibernate.envers.Audited;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Audited
@EntityListeners(AuditingEntityListener.class)
@Table(name = "destinations")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Destination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==========================
    //   BASIC FIELDS
    // ==========================

    @NotNull(message = "The Destination Code is required")
    @Column(name = "destination_code", nullable = false, unique = false, length = 50)
    private String code;

    @NotNull(message = "The Destination Name is required")
    @Column(name = "destination_name", nullable = false, unique = false, columnDefinition = "TEXT")
    private String name;

    @Column(name = "distance", nullable = false)
    private double distance;

    // Truck Relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "truck_id")
    @JsonBackReference
    private Truck truck;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setting_id")  
    private DestinationSetting setting;


    // ==========================
    //   AUDIT FIELDS
    // ==========================

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    // @LastModifiedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    // ==========================
    //   GETTERS/SETTERS
    // ==========================

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public Truck getTruck() {
        return truck;
    }

    public void setTruck(Truck truck) {
        this.truck = truck;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Transient
    public String getDistanceFormat() {
        return String.format("%,.2f km", distance); 
    }

    public DestinationSetting getSetting() {
        return setting;
    }

    public void setSetting(DestinationSetting setting) {
        this.setting = setting;
    }


}
