package timdev.timdev.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import timdev.timdev.enums.TruckSize;

public class TruckRequestDTO {
    
    private Long id;

    @NotBlank(message = "License plate is required")
    private String licensePlate;

    @NotNull(message = "Truck size is required")
    private TruckSize size;   // ✅ ADD THIS FIELD

    // @NotNull(message = "Model is required")
    private Long modelId; 

    @NotNull(message = "Year is required")
    @Min(value = 1900, message = "Year must be valid")
    private Integer year;

    @NotNull(message = "KM For Change Fats is required")
    @Min(value = 0, message = "KM must be positive")
    private Double kmForFatsShoot;

    @NotNull(message = "KM For Oils is required")
    @Min(value = 0, message = "KM must be positive")
    private Double kmForOilsChange;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
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

    public TruckSize getSize() {
        return size;
    }

    public void setSize(TruckSize size) {
        this.size = size;
    }
}
