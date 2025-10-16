package timdev.timdev.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import timdev.timdev.enums.TruckSize;
import timdev.timdev.enums.TruckType;

@Entity
@Table(name = "truck_inspections")
public class TruckInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "license_plate", nullable = false, unique = true)
    private String licensePlate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "model_id", nullable = true)
    private Model model;

    @Column(name = "year", nullable = true)
    private Integer year;

    @Column(name = "expired_date", nullable = true)
    private LocalDate expiredDate = null;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TruckSize size = null;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TruckType type = null;


    @OneToMany(mappedBy = "truckInspection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Inspection> inspections = new ArrayList<>();



    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }


    @PrePersist
    public void handlePrePersistAndUpdate() {
        if (licensePlate != null && !licensePlate.isBlank()) {
            this.code = "TR-" + licensePlate.trim().toUpperCase();
        }
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        if (licensePlate != null && !licensePlate.isBlank()) {
            this.code = "TR-" + licensePlate.trim().toUpperCase();
        }
        updatedAt = LocalDateTime.now();
    }

    



    @Transient
    private Inspection lastInspection;

    public Inspection getLasInspection() {
        if (inspections != null && !inspections.isEmpty()) {
            return inspections.get(inspections.size() - 1);
        }
        return null;
    }

    @PostLoad
    private void populateRecord() {
        this.lastInspection = getLasInspection();
    }
   

    public boolean isExpired() {
        if (expiredDate == null) return false;
        return expiredDate.isBefore(LocalDate.now()) || expiredDate.isEqual(LocalDate.now());
    }


    public long expiredDurationDays() {
        if (expiredDate == null) return 0;
        return ChronoUnit.DAYS.between(LocalDate.now(), expiredDate) + 1;
    }

    public String expiredDurationText() {
        if (expiredDate == null) {
            return "No expiration date";
        }

        long days = expiredDurationDays();

        if (days > 1) {
            return days + " days left";
        } else if (days == 1) {
            return "1 day left";
        } else if (days == 0) {
            return "Expires today";
        } else if (days == -1) {
            return "Expired 1 day ago";
        } else {
            return "Expired " + Math.abs(days) + " days ago";
        }
    }


    // public String expiredColor() {
    //     long days = expiredDurationDays();
    //     if (days <= 31) return "bg-red-500 dark:bg-red-700 text-white"; // less than or equal 31 days
    //     else if (days <= 90) return "bg-yellow-500 dark:bg-yellow-600 text-white"; // 32-90 days
    //     return ""; // more than 90 days → normal
    // }

    public String expiredColor() {
        long days = expiredDurationDays();
        if (days <= 0) {
            return "bg-gray-500 dark:bg-gray-700 text-white"; // expired
        } else if (days <= 30) {
            return "bg-red-500 dark:bg-red-700 text-white"; // expires within 30 days
        } else if (days <= 90) {
            return "bg-yellow-500 dark:bg-yellow-600 text-white"; // expires within 90 days
        }
        return ""; // normal
    }

    public LocalDate getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(LocalDate expiredDate) {
        this.expiredDate = expiredDate;
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


    public TruckSize getSize() {
        return size;
    }

    public void setSize(TruckSize size) {
        this.size = size;
    }

    public List<Inspection> getInspections() {
        return inspections;
    }

    public void setInspections(List<Inspection> inspections) {
        this.inspections = inspections;
    }

    public Inspection getLastInspection() {
        return lastInspection;
    }

    public void setLastInspection(Inspection lastInspection) {
        this.lastInspection = lastInspection;
    }

    public TruckType getType() {
        return type;
    }

    public void setType(TruckType type) {
        this.type = type;
    }





}