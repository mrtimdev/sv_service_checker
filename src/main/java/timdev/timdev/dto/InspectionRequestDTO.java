package timdev.timdev.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class InspectionRequestDTO {
    

    private Long id;

    @NotNull(message = "Date is required")
    @DateTimeFormat(pattern = "MMM dd, yyyy")
    private LocalDate date;

    @NotNull(message = "Truck is required")
    private Long truckInspectionId;

    @NotNull(message = "Expired date is required")
    @DateTimeFormat(pattern = "MMM dd, yyyy")
    private LocalDate expiredDate;

    @Min(value = 1, message = "Quantity must be greater than or equal to 1")
    private Integer quantity;


    @Size(max = 500, message = "Note must be at most 500 characters")
    private String note;

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

    public Long getTruckInspectionId() {
        return truckInspectionId;
    }

    public void setTruckInspectionId(Long truckInspectionId) {
        this.truckInspectionId = truckInspectionId;
    }

    public LocalDate getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(LocalDate expiredDate) {
        this.expiredDate = expiredDate;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
