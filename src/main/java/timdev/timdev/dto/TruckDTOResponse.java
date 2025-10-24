package timdev.timdev.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import timdev.timdev.entity.Driver;
import timdev.timdev.entity.FatsOilsSetting;
import timdev.timdev.entity.Truck;
import timdev.timdev.enums.OilStatus;
import timdev.timdev.enums.TruckSize;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TruckDTOResponse {
    private Long id;
    private String code;
    private String licensePlate;
    private Double literQuantityOfFats = 0.0;
    private Double literQuantityOfOils = 0.0;
    private Boolean requiredFatOil = true;
    private Boolean requiredInspection = true;
    private Integer year;
    private Double kmFatsBetween = 0.0;
    private Double kmOilsBetween = 0.0;
    private Double kmForOilsChange = 0.0;
    private Double nextFatsRange = 0.0;
    private Double nextOilsRange = 0.0;
    private Double kmForFatsShoot = 0.0;
    private LocalDate expiredDate;
    private TruckSize size;
    private OilStatus status = OilStatus.PENDING;
    private Double currentKm = 0.0;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private FatsOilsSetting setting;
    private Driver driver;

    // --- Related DTO lists ---
    private List<TruckDistanceDTOResponse> distances = new ArrayList<>();
    private List<TruckFatsReportDTO> truckFatsReports = new ArrayList<>();
    private List<TruckOilsReportDTO> truckOilsReports = new ArrayList<>();

    // --- Last reports ---
    private TruckFatsReportDTO lastFatsReport;
    private TruckOilsReportDTO lastOilsReport;

    // --- Calculated fields ---
    private Double kmFatsBalance;
    private Double kmOilsBalance;
    private Boolean expired;
    private Long expiredDurationDays;
    private String expiredDurationText;
    private String expiredColor;
    private String currentKmFormat;

    // --- Constructors ---
    public TruckDTOResponse() {}

    // --- Static Mapper ---
    public static TruckDTOResponse fromEntity(Truck truck) {
        TruckDTOResponse dto = new TruckDTOResponse();

        dto.setId(truck.getId());
        dto.setCode(truck.getCode());
        dto.setLicensePlate(truck.getLicensePlate());
        dto.setLiterQuantityOfFats(truck.getLiterQuantityOfFats());
        dto.setLiterQuantityOfOils(truck.getLiterQuantityOfOils());
        dto.setRequiredFatOil(truck.getRequiredFatOil());
        dto.setRequiredInspection(truck.getRequiredInspection());
        dto.setYear(truck.getYear());
        dto.setKmFatsBetween(truck.getKmFatsBetween());
        dto.setKmOilsBetween(truck.getKmOilsBetween());
        dto.setKmForOilsChange(truck.getKmForOilsChange());
        dto.setNextFatsRange(truck.getNextFatsRange());
        dto.setNextOilsRange(truck.getNextOilsRange());
        dto.setKmForFatsShoot(truck.getKmForFatsShoot());
        dto.setExpiredDate(truck.getExpiredDate());
        dto.setSize(truck.getSize());
        dto.setStatus(truck.getStatus());
        dto.setCurrentKm(truck.getCurrentKm());
        dto.setCreatedAt(truck.getCreatedAt());
        dto.setUpdatedAt(truck.getUpdatedAt());
        dto.setSetting(truck.getSetting());
        dto.setDriver(truck.getDriver());

        // --- Map Distances ---
        if (truck.getDistances() != null) {
            dto.setDistances(truck.getDistances().stream()
                .map(distance -> {
                    TruckDistanceDTOResponse d = new TruckDistanceDTOResponse();
                    d.setId(distance.getId());
                    d.setDate(distance.getDate());
                    d.setTruckId(distance.getTruck().getId());
                    d.setTruckName(distance.getTruck().getLicensePlate());
                    d.setDistance(distance.getDistance());
                    d.setDistanceFormat(distance.getDistanceFormat());
                    d.setNote(distance.getNote());
                    d.setCreatedAt(distance.getCreatedAt());
                    d.setUpdatedAt(distance.getUpdatedAt());
                    return d;
                }).toList());
        }

        // --- Map Fats Reports ---
        if (truck.getTruckFatsReports() != null) {
            dto.setTruckFatsReports(truck.getTruckFatsReports().stream()
                .map(report -> {
                    TruckFatsReportDTO r = new TruckFatsReportDTO();
                    r.setId(report.getId());
                    r.setTruckId(report.getTruck().getId());
                    r.setTruckName(report.getTruck().getLicensePlate());
                    r.setLiterQuantityOfFats(report.getLiterQuantityOfFats());
                    r.setDate(report.getDate());
                    r.setCurrentKm(report.getCurrentKm());
                    r.setDistanceKm(report.getDistanceKm());
                    r.setNextRange(report.getNextRange());
                    r.setKmForFatsShoot(report.getKmForFatsShoot());
                    r.setStatus(report.getStatus());
                    r.setNote(report.getNote());
                    r.setCreatedAt(report.getCreatedAt());
                    r.setUpdatedAt(report.getUpdatedAt());
                    return r;
                }).toList());
        }

        // --- Map Oils Reports ---
        if (truck.getTruckOilsReports() != null) {
            dto.setTruckOilsReports(truck.getTruckOilsReports().stream()
                .map(report -> {
                    TruckOilsReportDTO r = new TruckOilsReportDTO();
                    r.setId(report.getId());
                    r.setTruckId(report.getTruck().getId());
                    r.setTruckName(report.getTruck().getLicensePlate());
                    r.setLiterQuantityOfOils(report.getLiterQuantityOfOils());
                    r.setDate(report.getDate());
                    r.setCurrentKm(report.getCurrentKm());
                    r.setDistanceKm(report.getDistanceKm());
                    r.setNextRange(report.getNextRange());
                    r.setKmForOilsChange(report.getKmForOilsChange());
                    r.setStatus(report.getStatus());
                    r.setNote(report.getNote());
                    r.setCreatedAt(report.getCreatedAt());
                    r.setUpdatedAt(report.getUpdatedAt());
                    return r;
                }).toList());
        }

        // --- Last Reports ---
        if (truck.getLastFatsReport() != null) {
            TruckFatsReportDTO lastFats = new TruckFatsReportDTO();
            lastFats.setId(truck.getLastFatsReport().getId());
            lastFats.setDate(truck.getLastFatsReport().getDate());
            lastFats.setCurrentKm(truck.getLastFatsReport().getCurrentKm());
            lastFats.setNextRange(truck.getLastFatsReport().getNextRange());
            dto.setLastFatsReport(lastFats);
        }

        if (truck.getLastOilsReport() != null) {
            TruckOilsReportDTO lastOils = new TruckOilsReportDTO();
            lastOils.setId(truck.getLastOilsReport().getId());
            lastOils.setDate(truck.getLastOilsReport().getDate());
            lastOils.setCurrentKm(truck.getLastOilsReport().getCurrentKm());
            lastOils.setNextRange(truck.getLastOilsReport().getNextRange());
            dto.setLastOilsReport(lastOils);
        }

        // --- Compute expired status ---
        if (truck.getExpiredDate() != null) {
            long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), truck.getExpiredDate());
            dto.setExpired(daysLeft < 0);
            dto.setExpiredDurationDays(Math.abs(daysLeft));
            dto.setExpiredDurationText(daysLeft < 0 ? "Expired " + Math.abs(daysLeft) + " days ago" : "In " + daysLeft + " days");
            dto.setExpiredColor(daysLeft < 0 ? "red" : "green");
        }

        // --- Format current KM ---
        dto.setCurrentKmFormat(String.format("%,.2f km", dto.getCurrentKm()));

        dto.setKmFatsBalance(truck.getKmFatsBalance());
        dto.setKmOilsBalance(truck.getKmOilsBalance());

        return dto;
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

    public Boolean getRequiredFatOil() {
        return requiredFatOil;
    }

    public void setRequiredFatOil(Boolean requiredFatOil) {
        this.requiredFatOil = requiredFatOil;
    }

    public Boolean getRequiredInspection() {
        return requiredInspection;
    }

    public void setRequiredInspection(Boolean requiredInspection) {
        this.requiredInspection = requiredInspection;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
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

    public Double getKmForOilsChange() {
        return kmForOilsChange;
    }

    public void setKmForOilsChange(Double kmForOilsChange) {
        this.kmForOilsChange = kmForOilsChange;
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

    public Double getKmForFatsShoot() {
        return kmForFatsShoot;
    }

    public void setKmForFatsShoot(Double kmForFatsShoot) {
        this.kmForFatsShoot = kmForFatsShoot;
    }

    public LocalDate getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(LocalDate expiredDate) {
        this.expiredDate = expiredDate;
    }

    public TruckSize getSize() {
        return size;
    }

    public void setSize(TruckSize size) {
        this.size = size;
    }

    public OilStatus getStatus() {
        return status;
    }

    public void setStatus(OilStatus status) {
        this.status = status;
    }

    public Double getCurrentKm() {
        return currentKm;
    }

    public void setCurrentKm(Double currentKm) {
        this.currentKm = currentKm;
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

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public List<TruckDistanceDTOResponse> getDistances() {
        return distances;
    }

    public void setDistances(List<TruckDistanceDTOResponse> distances) {
        this.distances = distances;
    }

    public List<TruckFatsReportDTO> getTruckFatsReports() {
        return truckFatsReports;
    }

    public void setTruckFatsReports(List<TruckFatsReportDTO> truckFatsReports) {
        this.truckFatsReports = truckFatsReports;
    }

    public List<TruckOilsReportDTO> getTruckOilsReports() {
        return truckOilsReports;
    }

    public void setTruckOilsReports(List<TruckOilsReportDTO> truckOilsReports) {
        this.truckOilsReports = truckOilsReports;
    }

    public TruckFatsReportDTO getLastFatsReport() {
        return lastFatsReport;
    }

    public void setLastFatsReport(TruckFatsReportDTO lastFatsReport) {
        this.lastFatsReport = lastFatsReport;
    }

    public TruckOilsReportDTO getLastOilsReport() {
        return lastOilsReport;
    }

    public void setLastOilsReport(TruckOilsReportDTO lastOilsReport) {
        this.lastOilsReport = lastOilsReport;
    }

    public Double getKmFatsBalance() {
        return kmFatsBalance;
    }

    public void setKmFatsBalance(Double kmFatsBalance) {
        this.kmFatsBalance = kmFatsBalance;
    }

    public Double getKmOilsBalance() {
        return kmOilsBalance;
    }

    public void setKmOilsBalance(Double kmOilsBalance) {
        this.kmOilsBalance = kmOilsBalance;
    }

    public Boolean getExpired() {
        return expired;
    }

    public void setExpired(Boolean expired) {
        this.expired = expired;
    }

    public Long getExpiredDurationDays() {
        return expiredDurationDays;
    }

    public void setExpiredDurationDays(Long expiredDurationDays) {
        this.expiredDurationDays = expiredDurationDays;
    }

    public String getExpiredDurationText() {
        return expiredDurationText;
    }

    public void setExpiredDurationText(String expiredDurationText) {
        this.expiredDurationText = expiredDurationText;
    }

    public String getExpiredColor() {
        return expiredColor;
    }

    public void setExpiredColor(String expiredColor) {
        this.expiredColor = expiredColor;
    }

    public String getCurrentKmFormat() {
        return currentKmFormat;
    }

    public void setCurrentKmFormat(String currentKmFormat) {
        this.currentKmFormat = currentKmFormat;
    }

    // --- Getters & Setters ---
    // (keep all your existing getters/setters here)
}
