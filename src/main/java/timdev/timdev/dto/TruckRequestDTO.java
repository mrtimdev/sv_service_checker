package timdev.timdev.dto;

import io.micrometer.common.lang.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import timdev.timdev.enums.TruckSize;

public class TruckRequestDTO {
    
    private Long id;

    @NotBlank(message = "License plate is required")
    private String licensePlate;

    @NotBlank(message = "Group is required")
    private String group;


    @Nullable
    private TruckSize size;

    @Nullable
    private String sizeOfTruck;


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


    public String getGroup() {
        return group;
    }


    public void setGroup(String group) {
        this.group = group;
    }


    public TruckSize getSize() {
        return size;
    }


    public void setSize(TruckSize size) {
        this.size = size;
    }


    public String getSizeOfTruck() {
        return sizeOfTruck;
    }


    public void setSizeOfTruck(String sizeOfTruck) {
        this.sizeOfTruck = sizeOfTruck;
    }   
    
}
