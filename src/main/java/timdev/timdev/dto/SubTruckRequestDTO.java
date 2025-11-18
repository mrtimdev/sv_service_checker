package timdev.timdev.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


public class SubTruckRequestDTO {

    private Long id;

    @NotNull(message = "Date is required")
    @DateTimeFormat(pattern = "MMM dd, yyyy")
    private LocalDate date;

    @NotNull(message = "Truck is required")
    private Long truckId;

    @NotBlank(message = "Truck owner is required")
    private String truckOwner;

    @NotNull(message = "Oils quantity is required")
    @Positive(message = "Oils quantity must be greater than 0")
    private Double oilsQuantity;
    private String note;
    private String approvedNote;
    private String rejectedNote;
    private String approvedBy;
    private ApproveStatus status;

    // Constructors
    public SubTruckRequestDTO() {}

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

    public Long getTruckId() {
        return truckId;
    }

    public void setTruckId(Long truckId) {
        this.truckId = truckId;
    }

    public String getTruckOwner() {
        return truckOwner;
    }

    public void setTruckOwner(String truckOwner) {
        this.truckOwner = truckOwner;
    }

    public Double getOilsQuantity() {
        return oilsQuantity;
    }

    public void setOilsQuantity(Double oilsQuantity) {
        this.oilsQuantity = oilsQuantity;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getApprovedNote() {
        return approvedNote;
    }

    public void setApprovedNote(String approvedNote) {
        this.approvedNote = approvedNote;
    }

    public String getRejectedNote() {
        return rejectedNote;
    }

    public void setRejectedNote(String rejectedNote) {
        this.rejectedNote = rejectedNote;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public ApproveStatus getStatus() {
        return status;
    }

    public void setStatus(ApproveStatus status) {
        this.status = status;
    }



}