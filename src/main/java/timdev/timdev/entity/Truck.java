package timdev.timdev.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import timdev.timdev.enums.OilStatus;

@Entity
@Table(name = "trucks")
public class Truck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "license_plate", nullable = false, unique = true)
    private String licensePlate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "model_id", nullable = false)
    private Model model;

    @Column(name = "year", nullable = false)
    private Integer year;

    // គីឡូកំណត់ ទុកចន្លោះសម្រាប់បាញ់ខ្លាញ់ នឹង ប្រេង
    @Column(name = "fats_km_between", nullable = false)
    private Double kmFatsBetween = 0.0;
    @Column(name = "oils_km_between", nullable = false)
    private Double kmOilsBetween = 0.0;

    @Column(name = "km_for_oil_change", nullable = false)
    private Double kmForOilsChange = 0.0;

    // គីឡូម៉ែត្រត្រូវចូលបាញ់ខ្លាញ់
    @Column(name = "next_fats_range", nullable = false)
    private Double nextFatsRange = 0.0;

    // គីឡូម៉ែត្រត្រូវចូលបាញ់ប្រេង

    @Column(name = "next_oils_range", nullable = false)
    private Double nextOilsRange = 0.0;

    @Column(name = "km_for_fats_shoot", nullable = false)
    private Double kmForFatsShoot = 0.0;

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
    private List<TruckDistance> distances = new ArrayList<>();
    

    public List<TruckDistance> getDistances() {
        return distances;
    }

    public void setDistances(List<TruckDistance> distances) {
        this.distances = distances;
    }

    @PrePersist
    @PreUpdate
    public void handlePrePersistAndUpdate() {
        if (licensePlate != null && !licensePlate.isBlank()) {
            this.code = "TR-" + licensePlate.trim().toUpperCase();
        }
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
        }
        return balance;
    }

    public Double getKmOilsBalance()
    {
        Double balance = 0.00;
        if(this.lastOilsReport != null) {
            balance = this.lastOilsReport.getNextRange() - currentKm;
        }
        return balance;
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




}