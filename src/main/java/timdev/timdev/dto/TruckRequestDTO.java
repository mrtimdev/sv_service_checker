package timdev.timdev.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import timdev.timdev.enums.TruckSize;

public class TruckRequestDTO {
    
    private Long id;

    @NotBlank(message = "License plate is required")
    private String licensePlate;

    @NotBlank(message = "Group is required")
    private String group;


    @NotNull(message = "Truck size is required")
    private TruckSize size;


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
    
}
