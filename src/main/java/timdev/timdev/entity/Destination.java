package timdev.timdev.entity;

import org.hibernate.envers.Audited;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import timdev.timdev.dto.Status;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Audited
@EntityListeners(AuditingEntityListener.class)
@Table(name = "destinations")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Destination extends AuditableUser{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @DateTimeFormat(pattern = "MMM dd, yyyy")
    private LocalDate date;

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

    @Column(columnDefinition = "TEXT")
    private String note;

    // Truck Relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "truck_id")
    @JsonBackReference
    private Truck truck;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setting_id")  
    @JsonManagedReference
    private DestinationSetting setting;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;


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

    public void setId(Long id) {
        this.id = id;
    }


    @Transient
    public String getDistanceFormat() {
        return String.format("%,.0f km", distance); 
    }

    public DestinationSetting getSetting() {
        return setting;
    }

    public void setSetting(DestinationSetting setting) {
        this.setting = setting;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }


}
