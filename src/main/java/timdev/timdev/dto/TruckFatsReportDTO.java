package timdev.timdev.dto;

import java.time.LocalDate;

import timdev.timdev.enums.OilStatus;


public class TruckFatsReportDTO {

    private Long id;
    private Long truckId;
    private LocalDate date;
    private Double currentKm;
    private OilStatus status;
    private String note;

    public TruckFatsReportDTO() {}

    public TruckFatsReportDTO(Long id, Long truckId, LocalDate date, Double currentKm, OilStatus status, String note) {
        this.id = id;
        this.truckId = truckId;
        this.date = date;
        this.currentKm = currentKm;
        this.status = status;
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTruckId() {
        return truckId;
    }

    public void setTruckId(Long truckId) {
        this.truckId = truckId;
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
}
