package timdev.timdev.entity;


import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import timdev.timdev.enums.TruckSize;
import timdev.timdev.listener.AuditListener;


@Entity
@Table(name = "trucks", uniqueConstraints = {
    @UniqueConstraint(columnNames = "license_plate")
})
@Audited
@EntityListeners(AuditListener.class)
public class Truck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "license_plate", nullable = false, unique = true)
    private String licensePlate;

    @Column(name = "truck_group", nullable = true)
    private String group;

    
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private TruckSize size = null;


    @Column(name = "size_of_truck", length=50)
    private String sizeOfTruck; // 3T, 5T, 10T, etc.


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