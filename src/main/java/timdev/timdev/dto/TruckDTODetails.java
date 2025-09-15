package timdev.timdev.dto;

import java.time.LocalDate;
import java.util.List;

import timdev.timdev.entity.Truck;
import timdev.timdev.entity.TruckOilsReport;
import timdev.timdev.enums.OilStatus;

public class TruckDTODetails {

    private Long id;
    private String code;
    private String licensePlate;
    private String modelName;
    private Integer year;

    private Double kmForFatsShoot;
    private Double kmForOilsChange;
    private Double currentKm;

    private Double nextFatsRange;
    private Double nextOilsRange;

    private OilStatus status;

    // Latest OilReport
    private Long latestOilReportId;
    private Double latestOilReportKm;
    private Double kmToNextOilChange;
    private LocalDate oilChangeDate;
    private OilStatus oilReportStatus;
    private String oilReportNote;

    // ------------------ Mapping method ------------------
    public static TruckDTODetails mapToDTO(Truck truck, List<TruckOilsReport> oilReports) {
        TruckDTODetails dto = new TruckDTODetails();
        dto.setId(truck.getId());
        dto.setCode(truck.getCode());
        dto.setLicensePlate(truck.getLicensePlate());
        dto.setModelName(truck.getModel().getName());
        dto.setYear(truck.getYear());
        dto.setKmForFatsShoot(truck.getKmForFatsShoot());
        dto.setKmForOilsChange(truck.getKmForOilsChange());
        dto.setCurrentKm(truck.getCurrentKm());
        dto.setNextFatsRange(truck.getNextFatsRange());
        dto.setNextOilsRange(truck.getNextOilsRange());
        dto.setStatus(truck.getStatus());

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

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

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

    public Long getLatestOilReportId() {
        return latestOilReportId;
    }

    public void setLatestOilReportId(Long latestOilReportId) {
        this.latestOilReportId = latestOilReportId;
    }

    public Double getLatestOilReportKm() {
        return latestOilReportKm;
    }

    public void setLatestOilReportKm(Double latestOilReportKm) {
        this.latestOilReportKm = latestOilReportKm;
    }

    public Double getKmToNextOilChange() {
        return kmToNextOilChange;
    }

    public void setKmToNextOilChange(Double kmToNextOilChange) {
        this.kmToNextOilChange = kmToNextOilChange;
    }

    public LocalDate getOilChangeDate() {
        return oilChangeDate;
    }

    public void setOilChangeDate(LocalDate oilChangeDate) {
        this.oilChangeDate = oilChangeDate;
    }

    public OilStatus getOilReportStatus() {
        return oilReportStatus;
    }

    public void setOilReportStatus(OilStatus oilReportStatus) {
        this.oilReportStatus = oilReportStatus;
    }

    public String getOilReportNote() {
        return oilReportNote;
    }

    public void setOilReportNote(String oilReportNote) {
        this.oilReportNote = oilReportNote;
    }

    public Double getNextFatsRange() {
        return nextFatsRange;
    }

    public void setNextFatsRange(Double nexFatsRange) {
        this.nextFatsRange = nexFatsRange;
    }

    public Double getNextOilsRange() {
        return nextOilsRange;
    }

    public void setNextOilsRange(Double nextOilsRange) {
        this.nextOilsRange = nextOilsRange;
    }

    
}
