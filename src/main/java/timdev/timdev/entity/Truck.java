package timdev.timdev.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import timdev.timdev.enums.OilStatus;
import timdev.timdev.enums.TruckSize;
import timdev.timdev.listener.AuditListener;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
@Entity
@Table(name = "trucks")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Audited
@EntityListeners(AuditListener.class)
public class Truck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "license_plate", nullable = false, unique = true)
    private String licensePlate;

    @Column(name = "model_name", nullable = true)
    private String modelName;

    @Column(name = "group_name", nullable = true)
    private String groupName;

    @Column(name = "year_of_manufacture", nullable = true)
    private String yearOfManufacture;

    

    @Column(nullable = false)
    private Double literQuantityOfFats = 0.0;

    @Column(nullable = false)
    private Double literQuantityOfOils = 0.0;

    @Column(name = "required_fat_oil", nullable = true)
    private Boolean requiredFatOil = true;
    // for inspection
    @Column(name = "required_inspection", nullable = true)
    private Boolean requiredInspection = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "model_id", nullable = true)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Model model;

    @Column(name = "year", nullable = false)
    private Integer year;

    // គីឡូកំណត់ ទុកចន្លោះសម្រាប់បាញ់ខ្លាញ់ នឹង ប្រេង
    @Column(name = "fats_km_between", nullable = false)
    private Double kmFatsBetween = 0.0;
    @Column(name = "oils_km_between", nullable = false)
    private Double kmOilsBetween = 0.0;

    @Column(name = "km_for_oils_change", nullable = false)
    private Double kmForOilsChange = 0.0;

    // គីឡូម៉ែត្រត្រូវចូលបាញ់ខ្លាញ់
    @Column(name = "next_fats_range", nullable = false)
    private Double nextFatsRange = 0.0;

    // គីឡូម៉ែត្រត្រូវចូលបាញ់ប្រេង

    @Column(name = "next_oils_range", nullable = false)
    private Double nextOilsRange = 0.0;

    @Column(name = "km_for_fats_shoot", nullable = false)
    private Double kmForFatsShoot = 0.0;

    @Column(name = "expired_date", nullable = true)
    private LocalDate expiredDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TruckSize size;

    public Double getKmForFatsShoot() {
        return kmForFatsShoot;
    }

    public void setKmForFatsShoot(Double kmForFatsShoot) {
        this.kmForFatsShoot = kmForFatsShoot;
    }

    public Double getKmForOilsChange() {
        return kmForOilsChange;
    }

    public void setKmForOilsChange(Double kmForOilsChange) {
        this.kmForOilsChange = kmForOilsChange;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OilStatus status = OilStatus.PENDING;

    public OilStatus getStatus() {
        return status;
    }

    public void setStatus(OilStatus status) {
        this.status = status;
    }

    @Column(name = "current_km", nullable = false)
    private Double currentKm = 0.0;

    public Double getCurrentKm() {
        return currentKm;
    }

    public void setCurrentKm(Double currentKm) {
        this.currentKm = currentKm;
    }

    @OneToOne(mappedBy = "truck", fetch = FetchType.LAZY)
    private Driver driver;


    // ✅ Get latest inspection
    // public Optional<Inspection> getLatestInspection() {
    //     return inspections.stream()
    //             .sorted(Comparator.comparing(Inspection::getExpiredDate,
    //                     Comparator.nullsLast(Comparator.reverseOrder())))
    //             .findFirst();
    // }


    @Column(name = "created_at", updatable = false, nullable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Optionally store the selected setting directly
    @ManyToOne
    @JoinColumn(name = "fats_oils_setting_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private FatsOilsSetting setting;

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

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

    @OneToMany(mappedBy = "truck", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @JsonIgnoreProperties("truck")
    private List<TruckDistance> distances = new ArrayList<>();
    

    public List<TruckDistance> getDistances() {
        return distances;
    }

    public void setDistances(List<TruckDistance> distances) {
        this.distances = distances;
    }

    @PrePersist
    public void handlePrePersistAndUpdate() {
        if (licensePlate != null && !licensePlate.isBlank()) {
            this.code = "TR-" + licensePlate.trim().toUpperCase();
        }
        createdAt = LocalDateTime.now();
        calculateCurrentKm();
    }

    @PreUpdate
    public void onUpdate() {
        if (licensePlate != null && !licensePlate.isBlank()) {
            this.code = "TR-" + licensePlate.trim().toUpperCase();
        }
        updatedAt = LocalDateTime.now();
        calculateCurrentKm();
    }

    private void calculateCurrentKm() {
        if (this.distances != null && !this.distances.isEmpty()) {
            this.currentKm = this.distances.stream()
                .mapToDouble(TruckDistance::getDistance)
                .sum();
        } else {
            this.currentKm = 0.0;
        }
    }
    
    // // Alternatively, you can use a method to update when distances change
    public void addDistance(TruckDistance distance) {
        if (this.distances == null) {
            this.distances = new ArrayList<>();
        }
        distance.setTruck(this);
        this.distances.add(distance);
        calculateCurrentKm(); // Recalculate after adding
    }
    
    public void removeDistance(TruckDistance distance) {
        if (this.distances != null) {
            this.distances.remove(distance);
            distance.setTruck(null);
            calculateCurrentKm(); // Recalculate after removing
        }
    }


    @OneToMany(mappedBy = "truck", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference
    private List<TruckFatsReport> truckFatsReports = new ArrayList<>();

    @OneToMany(mappedBy = "truck", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference
    private List<TruckOilsReport> truckOilsReports = new ArrayList<>();

    public List<TruckOilsReport> getTruckOilsReports() {
        return truckOilsReports;
    }

    public void setTruckOilsReports(List<TruckOilsReport> truckOilsReports) {
        this.truckOilsReports = truckOilsReports;
    }



    @Transient
    private TruckOilsReport lastOilsReport;

    public TruckOilsReport getLastOilsReport() {
        if (truckOilsReports != null && !truckOilsReports.isEmpty()) {
            return truckOilsReports.get(truckOilsReports.size() - 1);
        }
        return null;
    }

    public TruckOilsReport getTruckOilsReport() {
        return lastOilsReport;
    }

    
    

    @Transient
    private TruckFatsReport lastFatsReport;

    public TruckFatsReport getLastFatsReport() {
        if (truckFatsReports != null && !truckFatsReports.isEmpty()) {
            return truckFatsReports.get(truckFatsReports.size() - 1);
        }
        return null;
    }

    public TruckFatsReport getTruckFatsReport() {
        return lastFatsReport;
    }


    @PostLoad
    private void populateLastFatsAndOilsReport() {
        this.lastFatsReport = getLastFatsReport();
        this.lastOilsReport = getLastOilsReport();
    }
    // @PostLoad
    // private void populateLastOilsReport() {
    //     this.lastOilsReport = getLastOilsReport();
    // }


    public List<TruckFatsReport> getTruckFatsReports() {
        return truckFatsReports;
    }

    public void setTruckFatsReports(List<TruckFatsReport> truckFatsReports) {
        this.truckFatsReports = truckFatsReports;
    }

    public Double getNextFatsRange() {
        return nextFatsRange;
    }

    public void setNextFatsRange(Double nextFatsRange) {
        this.nextFatsRange = nextFatsRange;
    }

    public Double getNextOilsRange() {
        return nextOilsRange;
    }

    public void setNextOilsRange(Double nextOilsRange) {
        this.nextOilsRange = nextOilsRange;
    }




    public Double getKmFatsBalance()
    {
        Double balance = 0.00;
        if(this.lastFatsReport != null) {
            balance = this.lastFatsReport.getNextRange() - currentKm;
        } else {
            balance = this.kmForFatsShoot - currentKm;
        }
        BigDecimal bd = BigDecimal.valueOf(balance);
        bd = bd.setScale(2, RoundingMode.HALF_UP);

        return bd.doubleValue();
    }

    public Double getKmOilsBalance()
    {
        Double balance = 0.00;
        if(this.lastOilsReport != null) {
            balance = this.lastOilsReport.getNextRange() - currentKm;
        } else {
            balance = this.kmForOilsChange - currentKm;
        }
        BigDecimal bd = BigDecimal.valueOf(balance);
        bd = bd.setScale(2, RoundingMode.HALF_UP);

        return bd.doubleValue();
    }

    public Double getKmFatsBetween() {
        return kmFatsBetween;
    }

    public void setKmFatsBetween(Double kmFatsBetween) {
        this.kmFatsBetween = kmFatsBetween;
    }

    public Double getKmOilsBetween() {
        return kmOilsBetween;
    }

    public void setKmOilsBetween(Double kmOilsBetween) {
        this.kmOilsBetween = kmOilsBetween;
    }

    public void setLastFatsReport(TruckFatsReport lastFatsReport) {
        this.lastFatsReport = lastFatsReport;
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
        if (expiredDate == null) return "No expiration date";
        long days = expiredDurationDays();
        if (days > 0) return days + " days left";
        else if (days < 0) return Math.abs(days) + " days expired";
        else return "Expires today";
    }

    public String expiredColor() {
        long days = expiredDurationDays();
        if (days <= 31) return "bg-red-500 dark:bg-red-700 text-white"; // less than or equal 31 days
        else if (days <= 90) return "bg-yellow-500 dark:bg-yellow-600 text-white"; // 32-90 days
        return ""; // more than 90 days → normal
    }

    public LocalDate getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(LocalDate expiredDate) {
        this.expiredDate = expiredDate;
    }

    public void setLastOilsReport(TruckOilsReport lastOilsReport) {
        this.lastOilsReport = lastOilsReport;
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

    public FatsOilsSetting getSetting() {
        return setting;
    }

    public void setSetting(FatsOilsSetting setting) {
        this.setting = setting;
    }

    public TruckSize getSize() {
        return size;
    }

    public void setSize(TruckSize size) {
        this.size = size;
    }

    public Boolean getRequiredFatOil() {
        return requiredFatOil;
    }

    public void setRequiredFatOil(Boolean requiredFatOil) {
        this.requiredFatOil = requiredFatOil;
    }

    public boolean isRequiredFatOil() {
        return Boolean.TRUE.equals(requiredFatOil);
    }

    public Boolean getRequiredInspection() {
        return requiredInspection;
    }

    public void setRequiredInspection(Boolean requiredInspection) {
        this.requiredInspection = requiredInspection;
    }

    public boolean isRequiredInspection() {
        return Boolean.TRUE.equals(requiredInspection);
    }

    public Double getLiterQuantityOfFats() {
        return literQuantityOfFats;
    }

    public void setLiterQuantityOfFats(Double literQuantityOfFats) {
        this.literQuantityOfFats = literQuantityOfFats;
    }

    public Double getLiterQuantityOfOils() {
        return literQuantityOfOils;
    }

    public void setLiterQuantityOfOils(Double literQuantityOfOils) {
        this.literQuantityOfOils = literQuantityOfOils;
    }


    @Transient
    public String getCurrentKmFormat() {
        return String.format("%,.2f km", currentKm); 
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getYearOfManufacture() {
        return yearOfManufacture;
    }

    public void setYearOfManufacture(String yearOfManufacture) {
        this.yearOfManufacture = yearOfManufacture;
    }




}